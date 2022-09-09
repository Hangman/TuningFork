/**
 * Copyright 2022 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.SOFTDirectChannelsRemix;

import java.util.Objects;

import de.pottgames.tuningfork.decoder.DefaultWavDecoderProvider;
import de.pottgames.tuningfork.decoder.WavDecoderProvider;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.logger.GdxLogger;
import de.pottgames.tuningfork.logger.MockLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

public class AudioConfig {
    private AudioDeviceConfig        deviceConfig;
    private DistanceAttenuationModel distanceAttenuationModel;
    private int                      simultaneousSources;
    private int                      idleTasks;
    private Virtualization           virtualization;
    private TuningForkLogger         logger;
    private WavDecoderProvider       wavDecoderProvider;


    /**
     * Creates an AudioConfig with default settings.
     */
    public AudioConfig() {
        this.setDeviceConfig(new AudioDeviceConfig());
        this.setDistanceAttenuationModel(DistanceAttenuationModel.INVERSE_DISTANCE_CLAMPED);
        this.setLogger(new GdxLogger());
        this.setSimultaneousSources(20);
        this.setIdleTasks(10);
        this.setVirtualization(Virtualization.ON);
        this.setWavDecoderProvider(new DefaultWavDecoderProvider());
    }


    /**
     * Creates an AudioConfig with the given settings.
     *
     * @param deviceConfig
     * @param distanceAttenuationModel
     * @param simultaneousSources defines how many {@link BufferedSoundSource}s are allowed to play simultaneously
     * @param idleTasks the initial task pool capacity, 10 is the default, only go higher if you plan to make heavy use of {@link StreamedSoundSource}s
     *            simultaneously
     * @param logger the logger to be used by TuningFork. You can implement the {@link TuningForkLogger} interface to write your own or choose one of the
     *            available logger implementations that are shipped with TuningFork.
     */
    public AudioConfig(AudioDeviceConfig deviceConfig, DistanceAttenuationModel distanceAttenuationModel, int simultaneousSources, int idleTasks,
            TuningForkLogger logger) {
        this.setDeviceConfig(deviceConfig);
        this.setDistanceAttenuationModel(distanceAttenuationModel);
        this.setSimultaneousSources(simultaneousSources);
        this.setIdleTasks(idleTasks);
        this.setLogger(logger);
        this.setVirtualization(Virtualization.ON);
        this.setWavDecoderProvider(new DefaultWavDecoderProvider());
    }


    /**
     * Creates an AudioConfig with the given settings.
     *
     * @param deviceConfig
     * @param distanceAttenuationModel
     * @param simultaneousSources defines how many {@link BufferedSoundSource}s are allowed to play simultaneously
     * @param idleTasks the initial task pool capacity, 10 is the default, only go higher if you plan to make heavy use of {@link StreamedSoundSource}s
     *            simultaneously
     * @param virtualization see {@link #setVirtualization(Virtualization)} for info
     * @param logger the logger to be used by TuningFork. You can implement the {@link TuningForkLogger} interface to write your own or choose one of the
     *            available logger implementations that are shipped with TuningFork.
     */
    public AudioConfig(AudioDeviceConfig deviceConfig, DistanceAttenuationModel distanceAttenuationModel, int simultaneousSources, int idleTasks,
            Virtualization virtualization, TuningForkLogger logger) {
        this.setDeviceConfig(deviceConfig);
        this.setDistanceAttenuationModel(distanceAttenuationModel);
        this.setSimultaneousSources(simultaneousSources);
        this.setIdleTasks(idleTasks);
        this.setLogger(logger);
        this.setVirtualization(virtualization);
        this.setWavDecoderProvider(new DefaultWavDecoderProvider());
    }


    public AudioDeviceConfig getDeviceConfig() {
        return this.deviceConfig;
    }


    /**
     * Sets the audio device config.
     *
     * @param deviceConfig
     *
     * @return this
     */
    public AudioConfig setDeviceConfig(AudioDeviceConfig deviceConfig) {
        this.deviceConfig = deviceConfig;
        if (deviceConfig == null) {
            this.deviceConfig = new AudioDeviceConfig();
        }
        return this;
    }


    public DistanceAttenuationModel getDistanceAttenuationModel() {
        return this.distanceAttenuationModel;
    }


    /**
     * Sets the distance attenuation model.
     *
     * @param distanceAttenuationModel
     *
     * @return this
     */
    public AudioConfig setDistanceAttenuationModel(DistanceAttenuationModel distanceAttenuationModel) {
        this.distanceAttenuationModel = distanceAttenuationModel;
        if (distanceAttenuationModel == null) {
            this.distanceAttenuationModel = DistanceAttenuationModel.NONE;
        }
        return this;
    }


    public int getSimultaneousSources() {
        return this.simultaneousSources;
    }


    /**
     * Defines how many {@link BufferedSoundSource}s are allowed to play simultaneously.
     *
     * @param simultaneousSources
     *
     * @return this
     */
    public AudioConfig setSimultaneousSources(int simultaneousSources) {
        this.simultaneousSources = simultaneousSources;
        if (simultaneousSources < 2) {
            this.simultaneousSources = 2;
        }
        return this;
    }


    public int getIdleTasks() {
        return this.idleTasks;
    }


    /**
     * The initial task pool capacity, 10 is the default, only go higher if you plan to make heavy use of {@link StreamedSoundSource}s simultaneously.
     *
     * @param idleTasks
     *
     * @return this
     */
    public AudioConfig setIdleTasks(int idleTasks) {
        this.idleTasks = idleTasks;
        if (idleTasks < 2) {
            this.idleTasks = 2;
        }
        return this;
    }


    public Virtualization getVirtualization() {
        return this.virtualization;
    }


    /**
     * Sets the default virtualization enabled state all sound sources are initialized with. You can change it on a per source basis later.<br>
     * OpenAL requires buffer channels to be down-mixed to the output channel configuration, possibly using HRTF or other virtualization techniques to give a
     * sense of speakers that may not be physically present. This leads to sometimes unexpected and unwanted audio output, so you can disable it.<br>
     * <br>
     * Check {@link Virtualization} for the different methods available.<br>
     *
     * @param virtualization
     */
    public void setVirtualization(Virtualization virtualization) {
        this.virtualization = virtualization;
    }


    public TuningForkLogger getLogger() {
        return this.logger;
    }


    /**
     * Sets the logger to be used by TuningFork. You can implement the {@link TuningForkLogger} interface to write your own or choose one of the available
     * logger implementations that are shipped with TuningFork.
     *
     * @param logger may be null to turn off logging
     *
     * @return this
     */
    public AudioConfig setLogger(TuningForkLogger logger) {
        this.logger = logger;
        if (logger == null) {
            this.logger = new MockLogger();
        }
        return this;
    }


    /**
     * Holds different options for enabling/disabling the virtualization.
     *
     * @author Matthias
     *
     */
    public enum Virtualization {
        /**
         * Virtualization is on. Input channels will not be mapped to the corresponding output channels, instead OpenAL decides where to play each channel based
         * on the spatialization algorithm.
         */
        ON(AL10.AL_FALSE),

        /**
         * Virtualizations is off. Input channels are routed to output channels as-is, input channels that don't match an output channel will be dropped.<br>
         * An example: You try to play a 4-channel sound file on a stereo system. With this setting, only channel 1 and 2 are played, channel 3 and 4 will be
         * ignored.
         */
        OFF_DROP_CHANNELS(SOFTDirectChannelsRemix.AL_DROP_UNMATCHED_SOFT),

        /**
         * Virtualization is off. Input channels are routed to output channels as-is, input channels that do not match an output channel are mixed into the
         * closest available output channel.<br>
         * An example: You try to play a 4-channel sound file on a stereo system. With this setting, channel 1 and 3 are mapped to the left speaker, channel 2
         * and 4 to the right speaker.
         */
        OFF_REMIX_CHANNELS(SOFTDirectChannelsRemix.AL_REMIX_UNMATCHED_SOFT);


        private static final Virtualization[] MAP = Virtualization.values();
        private final int                     alId;


        Virtualization(int alId) {
            this.alId = alId;
        }


        int getAlId() {
            return this.alId;
        }


        static Virtualization getByAlId(int id) {
            for (final Virtualization virtualization : Virtualization.MAP) {
                if (id == virtualization.alId) {
                    return virtualization;
                }
            }
            return null;
        }
    }

    public WavDecoderProvider getResamplerProvider() {
        return this.wavDecoderProvider;
    }


    /**
     * Sets the decoder provider that is used by {@link WavInputStream}.
     *
     * @param decoderProvider must not be null
     */
    public void setWavDecoderProvider(WavDecoderProvider decoderProvider) {
        Objects.requireNonNull(decoderProvider);
        this.wavDecoderProvider = decoderProvider;
    }

}
