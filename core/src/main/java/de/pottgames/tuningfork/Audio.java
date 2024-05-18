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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import de.pottgames.tuningfork.AudioConfig.Spatialization;
import de.pottgames.tuningfork.AudioConfig.Virtualization;
import de.pottgames.tuningfork.decoder.WavDecoderProvider;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * The main management and entry point of TuningFork. This class initializes the sound device and gives access to SoundSource's for advanced manual playback
 * control.
 *
 * @author Matthias
 */
public class Audio implements Disposable {
    private static Audio instance;

    private final boolean            nativeDecoderAvailable;
    final StreamManager              streamManager;
    final Filter                     publicFilter;
    private final WavDecoderProvider wavDecoderProvider;
    private final SoundListener      listener;
    private final SoundSourcePool    sourcePool;
    private final Array<SoundSource> managedSources  = new Array<>();
    private final TuningForkLogger   logger;
    private final AudioDevice        device;
    private final AudioSettings      defaultSettings = new AudioSettings();


    /**
     * Initializes an Audio instance with the default {@link AudioConfig}. Errors are logged but exceptions are silently ignored. Call
     * {@link Audio#initSafe(AudioConfig)} instead, if you want to handle exceptions.
     *
     * @return the initialized Audio instance or null on failure
     */
    public static Audio init() {
        return Audio.init(new AudioConfig());
    }


    /**
     * Initializes an Audio instance with the default {@link AudioConfig}. If you don't want to take care of exceptions, call {@link Audio#init(AudioConfig)}
     * instead.
     *
     * @return the initialized Audio instance
     *
     * @throws OpenDeviceException is thrown when a device couldn't be opened
     * @throws UnsupportedAudioDeviceException is thrown when the desired device couldn't be found or isn't a valid device
     */
    public static Audio initSafe() throws OpenDeviceException, UnsupportedAudioDeviceException {
        return Audio.initSafe(new AudioConfig());
    }


    /**
     * Initializes an Audio instance with the given {@link AudioConfig}. Errors are logged but exceptions are silently ignored. Call
     * {@link Audio#initSafe(AudioConfig)} instead, if you want to handle exceptions.
     *
     * @param config the audio config
     *
     * @return the initialized Audio instance or null on failure
     */
    public static Audio init(AudioConfig config) {
        AudioDevice device;
        Audio audio = null;
        try {
            device = new AudioDevice(config.getDeviceConfig(), config.getLogger());
            audio = new Audio(device, config);
        } catch (final Exception e) {
            config.getLogger().error(Audio.class, "Failed to init Audio. Details: " + e.getMessage());
        }
        return audio;
    }


    /**
     * Initializes an Audio instance with the given {@link AudioConfig}. If you don't want to take care of exceptions, call {@link Audio#init(AudioConfig)}
     * instead.
     *
     * @param config the audio config
     *
     * @return the initialized Audio instance
     *
     * @throws OpenDeviceException is thrown when a device couldn't be opened
     * @throws UnsupportedAudioDeviceException is thrown when the desired device couldn't be found or isn't a valid device
     */
    public static Audio initSafe(AudioConfig config) throws OpenDeviceException, UnsupportedAudioDeviceException {
        final AudioDevice device = new AudioDevice(config.getDeviceConfig(), config.getLogger());
        return new Audio(device, config);
    }


    private Audio(AudioDevice device, AudioConfig config) {
        this.logger = config.getLogger();

        // LOAD NATIVE LIBRARIES
        final SharedLibraryLoader loader = new SharedLibraryLoader();
        boolean nativesLoaded = false;
        if (config.useNativeDecoders()) {
            try {
                loader.load("decoders_rs");
                nativesLoaded = true;
            } catch (final Exception e) {
                this.logger.warn(this.getClass(), e.getMessage());
                this.logger.warn(this.getClass(), "Native decoders aren't available on this platform");
            }
        }
        this.nativeDecoderAvailable = nativesLoaded;

        // INIT
        this.device = device;
        this.wavDecoderProvider = config.getResamplerProvider();
        Audio.instance = this;
        this.publicFilter = new Filter(1f, 1f);
        this.streamManager = new StreamManager(config, this.logger);

        // SET DEFAULTS
        this.setDistanceAttenuationModel(config.getDistanceAttenuationModel());
        this.defaultSettings.setVirtualization(config.getVirtualization());
        this.defaultSettings.setSpatialization(config.getSpatialization());

        // CREATE LISTENER
        this.listener = new SoundListener();

        // CREATE SOURCES
        this.sourcePool = new SoundSourcePool(config.getSimultaneousSources());

    }


    /**
     * Returns the currently active instance of Audio.
     *
     * @return the instance
     */
    public static Audio get() {
        return Audio.instance;
    }


    /**
     * Returns the currently used AudioDevice.
     *
     * @return the device in charge
     */
    public AudioDevice getDevice() {
        return this.device;
    }


    /**
     * As the listener moves away from a sound source, the volume of the sound source decreases for the listener. The Attenuation Model is responsible for this
     * calculation.<br>
     * <br>
     * <b>Note:</b> Setting a DistanceAttenuationModel overwrites the default min and max attenuation distance as well as the attenuation factor that is used
     * for new sound sources. It won't affect existing ones.<br>
     * <br>
     * There are several {@link de.pottgames.tuningfork.DistanceAttenuationModel models} to choose from, including a
     * {@link de.pottgames.tuningfork.DistanceAttenuationModel#LINEAR_DISTANCE_CLAMPED linear model} and a
     * {@link de.pottgames.tuningfork.DistanceAttenuationModel#INVERSE_DISTANCE_CLAMPED semi-realistic model} based on the real world. By default the
     * {@link de.pottgames.tuningfork.DistanceAttenuationModel#INVERSE_DISTANCE_CLAMPED INVERSE_DISTANCE_CLAMPED} model is used.
     *
     * @param model the model
     */
    public void setDistanceAttenuationModel(DistanceAttenuationModel model) {
        AL10.alDistanceModel(model.getAlId());
        this.setDefaultAttenuationFactor(model.getAttenuationFactor());
        this.setDefaultAttenuationMinDistance(model.getAttenuationMinDistance());
        this.setDefaultAttenuationMaxDistance(model.getAttenuationMaxDistance());
    }


    /**
     * Sets the distance the listener must be from the sound source at which the attenuation should begin. The attenuation itself is controlled by the
     * attenuation model and the attenuation factor of the source. This value is used for all sources that are created/obtained afterwards, it doesn't affect
     * existing or already obtained sources. If you want to set this per source, you can do so: {@link SoundSource#setAttenuationMinDistance(float)}.
     *
     * @param distance (default depends on the attenuation model)
     */
    public void setDefaultAttenuationMinDistance(float distance) {
        this.defaultSettings.setMinAttenuationDistance(distance);
    }


    /**
     * Sets the distance the listener must be from the sound source at which the attenuation should stop. The attenuation itself is controlled by the
     * attenuation model and the attenuation factor of the source. This value is used for all sources that are created/obtained afterwards, it doesn't affect
     * existing or already obtained sources. If you want to set this per source, you can do so: {@link SoundSource#setAttenuationMaxDistance(float)}.
     *
     * @param distance (default depends on the attenuation model)
     */
    public void setDefaultAttenuationMaxDistance(float distance) {
        this.defaultSettings.setMaxAttenuationDistance(distance);
    }


    /**
     * This factor determines how slowly or how quickly the sound source loses volume as the listener moves away from the source. A factor of 0.5 reduces the
     * volume loss by half. With a factor of 2, the source loses volume twice as fast. This factor is used for all sources that are created/obtained afterwards,
     * it doesn't affect existing or already obtained sources. If you want to set this per source, you can do so:
     * {@link SoundSource#setAttenuationFactor(float)}.
     *
     * @param rolloff (default depends on the attenuation model)
     */
    public void setDefaultAttenuationFactor(float rolloff) {
        this.defaultSettings.setAttenuationFactor(rolloff);
    }


    /**
     * Returns the default attenuation minimum distance that is used to calculate the attenuation by the current default attenuation model.
     *
     * @return the default attenuation min distance
     */
    public float getDefaultAttenuationMinDistance() {
        return this.defaultSettings.getMinAttenuationDistance();
    }


    /**
     * Returns the default attenuation maximum distance that is used to calculate the attenuation by the current default attenuation model.
     *
     * @return the default attenuation max distance
     */
    public float getDefaultAttenuationMaxDistance() {
        return this.defaultSettings.getMaxAttenuationDistance();
    }


    /**
     * Returns the default attenuation factor that is used to calculate the attenuation by the current default attenuation model.
     *
     * @return the default attenuation factor
     */
    public float getDefaultAttenuationFactor() {
        return this.defaultSettings.getAttenuationFactor();
    }


    /**
     * Returns whether virtualization is enabled or disabled by default for all sound sources. See {@link AudioConfig#setVirtualization(Virtualization)} for
     * more info.
     *
     * @return the virtualization method
     */
    public Virtualization getDefaultVirtualization() {
        return this.defaultSettings.getVirtualization();
    }


    /**
     * Returns the default {@link AudioSettings}.
     *
     * @return the default audio settings
     */
    AudioSettings getDefaultAudioSettings() {
        return this.defaultSettings;
    }


    /**
     * Immediately sets the virtualization method for all sound sources, regardless of their state (playing, paused, obtained, etc.).<br>
     * Sources that are created afterward are also initialized with the new default virtualization method.<br>
     * Unless you have a specific reason to change this at runtime, it's recommended to set the default via {@link AudioConfig} on
     * {@link Audio#init(AudioConfig)}.<br>
     * <br>
     * See {@link Virtualization} for the different options available.
     *
     * @param virtualization the virtualization
     */
    public void setDefaultVirtualization(Virtualization virtualization) {
        this.defaultSettings.setVirtualization(virtualization);
        this.sourcePool.setVirtualization(virtualization);
        this.streamManager.setDefaultVirtualization(virtualization);

        for (int i = 0; i < this.managedSources.size; i++) {
            final SoundSource source = this.managedSources.get(i);
            if (source != null) {
                source.setVirtualization(virtualization);
            }
        }
    }


    public Spatialization getDefaultSpatialization() {
        return this.defaultSettings.getSpatialization();
    }


    public void setDefaultSpatialization(Spatialization spatialization) {
        this.defaultSettings.setSpatialization(spatialization);
        this.sourcePool.setSpatialization(spatialization);
        this.streamManager.setDefaultSpatialization(spatialization);

        for (int i = 0; i < this.managedSources.size; i++) {
            final SoundSource source = this.managedSources.get(i);
            if (source != null) {
                source.setSpatialization(spatialization);
            }
        }
    }


    /**
     * Returns the global volume value that is applied to all sources.
     *
     * @return the master volume in the range: 0 - 1
     */
    public float getMasterVolume() {
        return this.listener.getMasterVolume();
    }


    /**
     * Sets the global volume that is applied to all sources. Values above 1 or below 0 will be clamped.
     *
     * @param volume range: 0 - 1
     */
    public void setMasterVolume(float volume) {
        this.listener.setMasterVolume(MathUtils.clamp(volume, 0f, 1f));
    }


    /**
     * Changing the doppler factor exaggerates or de-emphasizes the doppler effect. Physically accurate doppler calculation might not give the desired result,
     * so changing this to your needs is fine. The default doppler factor is 1. Values &lt; 0 are ignored, 0 turns the doppler effect off, values &gt; 1 will
     * increase the strength of the doppler effect.
     *
     * @param dopplerFactor (default 1)
     */
    public void setDopplerFactor(float dopplerFactor) {
        if (dopplerFactor > 0f) {
            AL10.alDopplerFactor(dopplerFactor);
        }
    }


    /**
     * Returns a {@link BufferedSoundSource} for permanent use. Call {@link BufferedSoundSource#free() free()} on it to return it to the pool of available
     * sources.
     *
     * @param buffer the sound buffer
     *
     * @return the {@link BufferedSoundSource}
     */
    public BufferedSoundSource obtainSource(SoundBuffer buffer) {
        // FIND FREE SOUND SOURCE
        final BufferedSoundSource source = this.sourcePool.findFreeSource(this.defaultSettings);

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer);
        source.setRelative(false);

        return source;
    }


    private BufferedSoundSource obtainRelativeSource(SoundBuffer buffer, boolean looping) {
        // FIND FREE SOUND SOURCE
        final BufferedSoundSource source = this.sourcePool.findFreeSource(this.defaultSettings);

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer);
        source.setRelative(true);

        return source;
    }


    /**
     * Plays the sound.
     *
     * @param buffer the sound buffer
     */
    protected void play(SoundBuffer buffer) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound at the specified time. Negative values for time will result in an error log entry but do nothing else. Positive values that point to the
     * past will make the source play immediately. The source will be in playing-state while waiting for the start time to be reached.
     *
     * @param buffer the sound buffer
     * @param time the time in nanoseconds, use {@link AudioDevice#getClockTime()} to get the current time
     */
    protected void playAtTime(SoundBuffer buffer, long time) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.playAtTime(time);
        source.obtained = false;
    }


    /**
     * Plays a sound with an effect.
     *
     * @param buffer the sound buffer
     * @param effect the sound effect
     */
    protected void play(SoundBuffer buffer, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     */
    protected void play(SoundBuffer buffer, float volume) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume and effect.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param effect the sound effect
     */
    protected void play(SoundBuffer buffer, float volume, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume and pitch.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     */
    protected void play(SoundBuffer buffer, float volume, float pitch) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch and filter.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param lowFreqVolume the volume of low frequencies
     * @param highFreqVolume the volume of high frequencies
     */
    protected void play(SoundBuffer buffer, float volume, float pitch, float lowFreqVolume, float highFreqVolume) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setFilter(lowFreqVolume, highFreqVolume);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch and effect.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param effect the sound effect
     */
    protected void play(SoundBuffer buffer, float volume, float pitch, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch and pan.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param pan in the range of -1.0 (full left) to 1.0 (full right). (default center 0.0)
     */
    protected void play(SoundBuffer buffer, float volume, float pitch, float pan) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setAttenuationFactor(0f);
        source.setPosition(pan, 0, (float) -Math.sqrt(1d - pan * pan));
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch, pan and effect.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param pan in the range of -1.0 (full left) to 1.0 (full right). (default center 0.0)
     * @param effect the sound effect
     */
    protected void play(SoundBuffer buffer, float volume, float pitch, float pan, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setAttenuationFactor(0f);
        source.setPosition(pan, 0, (float) -Math.sqrt(1d - pan * pan));
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound at the given position.
     *
     * @param buffer the sound buffer
     * @param position the position in 3D space
     */
    protected void play3D(SoundBuffer buffer, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given filter at the given position.
     *
     * @param buffer the sound buffer
     * @param position the position in 3D space
     * @param lowFreqVolume the volume of low frequencies
     * @param highFreqVolume the volume of high frequencies
     */
    protected void play3D(SoundBuffer buffer, Vector3 position, float lowFreqVolume, float highFreqVolume) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.setFilter(lowFreqVolume, highFreqVolume);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound at the given position with an effect.
     *
     * @param buffer the sound buffer
     * @param position the position in 3D space
     * @param effect the sound effect
     */
    protected void play3D(SoundBuffer buffer, Vector3 position, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound at the given position with the given effect and filter.
     *
     * @param buffer the sound buffer
     * @param position the position in 3D space
     * @param lowFreqVolume the volume of low frequencies
     * @param highFreqVolume the volume of high frequencies
     * @param effect the sound effect
     */
    protected void play3D(SoundBuffer buffer, Vector3 position, float lowFreqVolume, float highFreqVolume, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.attachEffect(effect);
        source.setFilter(lowFreqVolume, highFreqVolume);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume at the given position.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position the position in 3D space
     */
    protected void play3D(SoundBuffer buffer, float volume, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume and filter at the given position.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position the position in 3D space
     * @param lowFreqVolume the volume of low frequencies
     * @param highFreqVolume the volume of high frequencies
     */
    protected void play3D(SoundBuffer buffer, float volume, Vector3 position, float lowFreqVolume, float highFreqVolume) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPosition(position);
        source.setFilter(lowFreqVolume, highFreqVolume);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume and effect at the given position.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position the position in 3D space
     * @param effect the sound effect
     */
    protected void play3D(SoundBuffer buffer, float volume, Vector3 position, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPosition(position);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume and pitch at the given position.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param position the position in 3D space
     */
    protected void play3D(SoundBuffer buffer, float volume, float pitch, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume, pitch and effect at the given position.
     *
     * @param buffer the sound buffer
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param position the position in 3D space
     * @param effect the sound effect
     */
    protected void play3D(SoundBuffer buffer, float volume, float pitch, Vector3 position, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setPosition(position);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Immediately sets the resampler for all sound sources, regardless of their state (playing, paused, obtained, etc.).<br>
     * Sources that are created afterward are also initialized with the new default resampler.<br>
     * <br>
     * Check {@link AudioDevice#getAvailableResamplers()} for a list of available resamplers.
     *
     * @param resampler the resampler
     *
     * @return true if successful, false if the desired resampler isn't available
     */
    public boolean setDefaultResampler(String resampler) {
        if (resampler != null) {
            final int resamplerIndex = this.device.getResamplerIndexByName(resampler);
            if (resamplerIndex >= 0) {
                this.sourcePool.setResamplerByIndex(resamplerIndex);

                this.streamManager.setDefaultResampler(resamplerIndex);

                for (int i = 0; i < this.managedSources.size; i++) {
                    final SoundSource source = this.managedSources.get(i);
                    if (source != null) {
                        source.setResamplerByIndex(resamplerIndex);
                    }
                }
            }
            return true;
        }

        return false;
    }


    /**
     * Resumes to play all {@link BufferedSoundSource}s and {@link StreamedSoundSource}s that are paused.
     */
    public void resumeAll() {
        this.resumeAllBufferedSources();
        this.resumeAllStreamedSources();
    }


    /**
     * Resumes to play all {@link StreamedSoundSource}s that are paused at the moment.
     */
    public void resumeAllStreamedSources() {
        this.streamManager.resumeAll();
    }


    /**
     * Resumes to play all {@link BufferedSoundSource}s that are paused at the moment.
     */
    public void resumeAllBufferedSources() {
        this.sourcePool.resumeAll();
    }


    /**
     * Pauses all {@link BufferedSoundSource}s and {@link StreamedSoundSource}s.
     */
    public void pauseAll() {
        this.pauseAllBufferedSources();
        this.pauseAllStreamedSources();
    }


    /**
     * Pauses all {@link StreamedSoundSource}.
     */
    public void pauseAllStreamedSources() {
        this.streamManager.pauseAll();
    }


    /**
     * Pauses all {@link BufferedSoundSource}s.
     */
    public void pauseAllBufferedSources() {
        this.sourcePool.pauseAll();
    }


    /**
     * Stops all {@link BufferedSoundSource}s and {@link StreamedSoundSource}s.
     */
    public void stopAll() {
        this.stopAllBufferedSources();
        this.stopAllStreamedSources();
    }


    /**
     * Stops all {@link StreamedSoundSource}s.
     */
    public void stopAllStreamedSources() {
        this.streamManager.stopAll();
    }


    /**
     * Stops all {@link BufferedSoundSource}s.
     */
    public void stopAllBufferedSources() {
        this.sourcePool.stopAll();
    }


    int getDefaultResamplerIndex() {
        return this.defaultSettings.getResamplerIndex();
    }


    void registerManagedSource(SoundSource source) {
        this.managedSources.add(source);
    }


    void removeManagedSource(SoundSource source) {
        this.managedSources.removeValue(source, true);
    }


    /**
     * Returns the {@link SoundListener}.
     *
     * @return the {@link SoundListener}
     */
    public SoundListener getListener() {
        return this.listener;
    }


    /**
     * Returns the wav decoder provider that is used by {@link WavInputStream}.
     *
     * @return the resampler provider
     */
    public WavDecoderProvider getWavDecoderProvider() {
        return this.wavDecoderProvider;
    }


    void onBufferDisposal(SoundBuffer buffer) {
        this.sourcePool.onBufferDisposal(buffer);
    }


    public TuningForkLogger getLogger() {
        return this.logger;
    }


    /**
     * Returns true if the native decoders are available. Java decoders will be used as a fallback.
     *
     * @return native decoders available
     */
    public boolean isNativeDecodersAvailable() {
        return this.nativeDecoderAvailable;
    }


    /**
     * Shuts down TuningFork.
     */
    @Override
    public void dispose() {
        this.publicFilter.dispose();
        this.streamManager.dispose();
        this.stopAllBufferedSources();
        this.sourcePool.dispose();

        // DISPOSE DEVICE LAST
        this.device.dispose(true);

        Audio.instance = null;
    }

}
