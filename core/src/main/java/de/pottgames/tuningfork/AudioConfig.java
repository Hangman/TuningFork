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
    private boolean                  virtualizationEnabled;
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
        this.setVirtualizationEnabled(true);
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
        this.setVirtualizationEnabled(true);
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
     * @param virtualizationEnabled see {@link #setVirtualizationEnabled(boolean)} for info
     * @param logger the logger to be used by TuningFork. You can implement the {@link TuningForkLogger} interface to write your own or choose one of the
     *            available logger implementations that are shipped with TuningFork.
     */
    public AudioConfig(AudioDeviceConfig deviceConfig, DistanceAttenuationModel distanceAttenuationModel, int simultaneousSources, int idleTasks,
            boolean virtualizationEnabled, TuningForkLogger logger) {
        this.setDeviceConfig(deviceConfig);
        this.setDistanceAttenuationModel(distanceAttenuationModel);
        this.setSimultaneousSources(simultaneousSources);
        this.setIdleTasks(idleTasks);
        this.setLogger(logger);
        this.setVirtualizationEnabled(virtualizationEnabled);
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


    public boolean isVirtualizationEnabled() {
        return this.virtualizationEnabled;
    }


    /**
     * Sets the default virtualization enabled state for any sound source. OpenAL requires buffer channels to be down-mixed to the output channel configuration,
     * possibly using HRTF or other virtualization techniques to give a sense of speakers that may not be physically present. This leads to sometimes unexpected
     * and unwanted audio output, so you can disable it. Note that existing input channels may be dropped if they don't exist on the output configuration when
     * virtualization is disabled.<br>
     * An example: You try to play a 4-channel sound file on a stereo system. With virtualization enabled, channel 1 and 3 are routed to the left speaker,
     * channel 2 and 4 to the right speaker. With virtualization disabled, only channel 1 and 2 are played, channel 3 and 4 will be ignored.<br>
     * By default virtualization is enabled and it's the recommended setting.
     *
     * @param enabled
     */
    public void setVirtualizationEnabled(boolean enabled) {
        this.virtualizationEnabled = enabled;
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
