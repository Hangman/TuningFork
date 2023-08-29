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
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.openal.EXTSourceRadius;
import org.lwjgl.openal.SOFTDirectChannels;
import org.lwjgl.openal.SOFTSourceResampler;
import org.lwjgl.openal.SOFTSourceSpatialize;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.pottgames.tuningfork.AudioConfig.Spatialization;
import de.pottgames.tuningfork.AudioConfig.Virtualization;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A sound source is used to represent the position, speed and other attributes of a sound in the virtual audio world. It enables you to play, pause, stop,
 * position sounds and let's you set different effects on it.
 *
 * @author Matthias
 *
 */
public abstract class SoundSource {
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    protected final int            sourceId;
    private final SoundEffect[]    effects;
    private int                    nextSoundEffectSendId = 0;
    private float                  attenuationFactor     = 1f;
    private final Vector3          position              = new Vector3(0f, 0f, 0f);
    private boolean                directional           = false;
    private volatile int           resamplerIndex        = -1;
    private boolean                directFilter          = false;


    protected SoundSource() {
        final Audio audio = Audio.get();
        this.logger = audio.getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);
        this.effects = new SoundEffect[audio.getDevice().getNumberOfEffectSlots()];

        this.sourceId = AL10.alGenSources();
        AL10.alSourcef(this.sourceId, EXTEfx.AL_AIR_ABSORPTION_FACTOR, 1f);

        if (!this.errorLogger.checkLogError("Failed to create the SoundSource")) {
            this.logger.trace(this.getClass(), "SoundSource successfully created");
        }

        // SET DEFAULTS
        final AudioSettings defaultSettings = audio.getDefaultAudioSettings();
        this.setAttenuationFactor(defaultSettings.getAttenuationFactor());
        this.setAttenuationMinDistance(defaultSettings.getMinAttenuationDistance());
        this.setAttenuationMaxDistance(defaultSettings.getMaxAttenuationDistance());
        this.setVirtualization(defaultSettings.getVirtualization());
        this.setSpatialization(defaultSettings.getSpatialization());
        this.setResamplerByIndex(defaultSettings.getResamplerIndex());
    }


    /**
     * Sets the base volume of this sound source. The final output volume might differ depending on the source's position, listener position etc.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     */
    public void setVolume(float volume) {
        AL10.alSourcef(this.sourceId, AL10.AL_GAIN, MathUtils.clamp(volume, 0f, 1f));
    }


    /**
     * Returns the base volume of this sound source.
     *
     * @return volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume
     */
    public float getVolume() {
        return AL10.alGetSourcef(this.sourceId, AL10.AL_GAIN);
    }


    /**
     * Sets the pitch of this sound source.
     *
     * @param pitch The pitch value, which must be >= 0. Values less than 1 result in slower playback and a deeper tone, while values greater than 1 make it
     *            faster and produce a higher tone.
     */
    public void setPitch(float pitch) {
        if (pitch < 0f) {
            pitch = 0f;
        }
        AL10.alSourcef(this.sourceId, AL10.AL_PITCH, pitch);
    }


    /**
     * Returns the pitch of this sound source.
     *
     * @return pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster
     */
    public float getPitch() {
        return AL10.alGetSourcef(this.sourceId, AL10.AL_PITCH);
    }


    /**
     * Starts the playback of this sound source.
     */
    public void play() {
        if (!this.isPlaying()) {
            AL10.alSourcePlay(this.sourceId);
        }
    }


    /**
     * Sets whether the position attribute of this sound source should be handled as relative or absolute values to the listener's position.<br>
     * If set to false, the position is the absolute position in the 3D world.<br>
     * If set to true, the position is relative to the listener's position, meaning a position of x=0,y=0,z=0 is always identical to the listener's position.
     *
     * @param relative true = relative, false = absolute
     */
    public void setRelative(boolean relative) {
        AL10.alSourcei(this.sourceId, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
    }


    /**
     * Returns whether this sound source handles the position attribute as relative to the listeners position.
     *
     * @return relative
     */
    public boolean isRelative() {
        return AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_RELATIVE) == AL10.AL_TRUE;
    }


    /**
     * Sets the positions of this sound source in the virtual world.
     *
     * @param position
     */
    public void setPosition(Vector3 position) {
        this.setPosition(position.x, position.y, position.z);
    }


    /**
     * Sets the positions of this sound source in the virtual world.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPosition(float x, float y, float z) {
        this.position.set(this.position);
        AL10.alSource3f(this.sourceId, AL10.AL_POSITION, x, y, z);
    }


    /**
     * Retrieves the position of this sound source.
     *
     * @param saveTo the vector the result should be saved to
     *
     * @return returns the saveTo parameter vector that contains the result
     */
    public Vector3 getPosition(Vector3 saveTo) {
        return saveTo.set(this.position);
    }


    /**
     * Changes the source to a "large" source with a radius. The source has a raised cosine shape. A radius of 0 is the default (point-source).<br>
     * The OpenAL documentation about this is very thin. I don't know nor have the resources to test how this really works with attenuation and directionality.
     * If you happen to know, please PR a fix for this javadoc.
     *
     * @param radius
     */
    public void setRadius(float radius) {
        radius = MathUtils.clamp(radius, 0f, Float.MAX_VALUE);
        AL10.alSourcef(this.sourceId, EXTSourceRadius.AL_SOURCE_RADIUS, radius);
    }


    /**
     * Returns the radius of the source.
     *
     * @return the radius
     */
    public float getRadius() {
        return AL10.alGetSourcef(this.sourceId, EXTSourceRadius.AL_SOURCE_RADIUS);
    }


    /**
     * Sets the speed of this sound source. The speed is <b>not</b> automatically determined by changes to the position, you need to call setSpeed manually.<br>
     * The speed is only used for calculating a Doppler effect. Note that you need to call set speed on the sound listener as well in order to get a proper
     * Doppler effect.
     *
     * @param speed
     */
    public void setSpeed(Vector3 speed) {
        this.setSpeed(speed.x, speed.y, speed.z);
    }


    /**
     * Sets the speed of this sound source. The speed is <b>not</b> automatically determined by changes to the position, you need to call setSpeed manually.<br>
     * The speed is only used for calculating a Doppler effect. Note that you need to call set speed on the sound listener as well in order to get a proper
     * Doppler effect.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setSpeed(float x, float y, float z) {
        AL10.alSource3f(this.sourceId, AL10.AL_VELOCITY, x, y, z);
    }


    /**
     * Makes this sound source emit sound in a cone shape facing a direction. Inside the inner cone angle, the listener hears the sound at full volume. Outside
     * the outer cone angle the sound is even on the level specified by outOfConeVolume. The volume is faded in between both angles (inside the cone). Call
     * {@link #makeOmniDirectional()} to make the source non-directional again.
     *
     * @param direction the direction the source is facing
     * @param coneInnerAngle the inner cone angle
     * @param coneOuterAngle the outer cone angle
     * @param outOfConeVolume the volume of the sound source when outside of the cone
     */
    public void makeDirectional(Vector3 direction, float coneInnerAngle, float coneOuterAngle, float outOfConeVolume) {
        this.directional = true;
        AL10.alSourcef(this.sourceId, AL10.AL_CONE_INNER_ANGLE, coneInnerAngle);
        AL10.alSourcef(this.sourceId, AL10.AL_CONE_OUTER_ANGLE, coneOuterAngle);
        AL10.alSourcef(this.sourceId, AL10.AL_CONE_OUTER_GAIN, outOfConeVolume);
        this.setDirection(direction);
        this.logger.trace(this.getClass(), "SoundSource successfully set to directional");
    }


    /**
     * Sets the direction of this sound source. You need to call {@link #makeDirectional(Vector3, float, float, float)} first, otherwise the direction will be
     * ignored.
     *
     * @param direction
     */
    public void setDirection(Vector3 direction) {
        if (this.directional) {
            AL10.alSource3f(this.sourceId, AL10.AL_DIRECTION, direction.x, direction.y, direction.z);
        }
    }


    /**
     * Makes this sound source omni-directional. This is the default, so you only need to call it if you have made the source directional earlier.
     */
    public void makeOmniDirectional() {
        this.directional = false;
        AL10.alSource3f(this.sourceId, AL10.AL_DIRECTION, 0f, 0f, 0f);
        this.logger.trace(this.getClass(), "SoundSource successfully set to omnidirectional");
    }


    /**
     * Returns true if this sound source is directional.
     *
     * @return directional = true, omni-directional = false
     */
    public boolean isDirectional() {
        return this.directional;
    }


    /**
     * Enables the distance attenuation of this sound source.
     */
    public void enableAttenuation() {
        AL10.alSourcef(this.sourceId, AL10.AL_ROLLOFF_FACTOR, this.attenuationFactor);
    }


    /**
     * Disables the distance attenuation of this sound source.
     */
    public void disableAttenuation() {
        AL10.alSourcef(this.sourceId, AL10.AL_ROLLOFF_FACTOR, 0f);
    }


    /**
     * This factor determines how slowly or how quickly the sound source loses volume as the listener moves away from the source. A factor of 0.5 reduces the
     * volume loss by half. With a factor of 2, the source loses volume twice as fast.
     *
     * @param rolloff (default depends on the attenuation model)
     */
    public void setAttenuationFactor(float rolloff) {
        this.attenuationFactor = rolloff;
        AL10.alSourcef(this.sourceId, AL10.AL_ROLLOFF_FACTOR, rolloff);
    }


    /**
     * Sets the distance the listener must be from the sound source at which the attenuation should begin. The attenuation itself is controlled by the
     * attenuation model and the attenuation factor of the source.
     *
     * @param minDistance (default depends on the attenuation model)
     */
    public void setAttenuationMinDistance(float minDistance) {
        AL10.alSourcef(this.sourceId, AL10.AL_REFERENCE_DISTANCE, minDistance);
    }


    /**
     * Sets the distance the listener must be from the sound source at which the attenuation should stop. The attenuation itself is controlled by the
     * attenuation model and the attenuation factor of the source.
     *
     * @param maxDistance (default depends on the attenuation model)
     */
    public void setAttenuationMaxDistance(float maxDistance) {
        AL10.alSourcef(this.sourceId, AL10.AL_MAX_DISTANCE, maxDistance);
    }


    /**
     * Returns the attenuation factor of this source. See {@link #setAttenuationFactor(float)} for more information.
     *
     * @return the attenuation factor (default depends on the attenuation model)
     */
    public float getAttenuationFactor() {
        return AL10.alGetSourcef(this.sourceId, AL10.AL_ROLLOFF_FACTOR);
    }


    /**
     * Returns the minimum distance for the attenuation to start. See {@link #setAttenuationMinDistance(float)} for more information.
     *
     * @return the attenuation min distance (default depends on the attenuation model)
     */
    public float getAttenuationMinDistance() {
        return AL10.alGetSourcef(this.sourceId, AL10.AL_REFERENCE_DISTANCE);
    }


    /**
     * Returns the distance at which the attenuation will stop. See {@link #setAttenuationMaxDistance(float)} for more information.
     *
     * @return the attenuation max distance (default depends on the attenuation model)
     */
    public float getAttenuationMaxDistance() {
        return AL10.alGetSourcef(this.sourceId, AL10.AL_MAX_DISTANCE);
    }


    /**
     * Sets the virtualization enabled state for this sound source.<br>
     * OpenAL requires input channels to be down-mixed to the output channel configuration, possibly using HRTF or other virtualization techniques to give a
     * sense of speakers that may not be physically present. This leads to sometimes unexpected and unwanted audio output, so you can disable it.<br>
     * <br>
     * Check {@link Virtualization} for the different methods available.<br>
     * <br>
     * Recommended settings:<br>
     * <ul>
     * <li>{@link Virtualization#ON} for 3D sound</li>
     * <li>{@link Virtualization#OFF_REMIX_CHANNELS} for music and other non-3D sounds</li>
     * </ul>
     *
     * @param virtualization null is a legal NOP
     */
    public void setVirtualization(Virtualization virtualization) {
        if (virtualization != null) {
            AL10.alSourcei(this.sourceId, SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, virtualization.getAlId());
        }
    }


    /**
     * Retrieves the virtualization mode.
     *
     * @return the virtualization mode
     */
    public Virtualization getVirtualization() {
        final int alId = AL10.alGetSourcei(this.sourceId, SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT);
        return Virtualization.getByAlId(alId);
    }


    /**
     * Sets the spatialization mode for this sound source.
     *
     * @param spatialization null is a legal NOP
     */
    public void setSpatialization(Spatialization spatialization) {
        if (spatialization != null) {
            AL10.alSourcei(this.sourceId, SOFTSourceSpatialize.AL_SOURCE_SPATIALIZE_SOFT, spatialization.getAlId());
        }
    }


    /**
     * Retrieves the spatialization mode.
     *
     * @return the spatialization mode
     */
    public Spatialization getSpatialization() {
        final int alId = AL10.alGetSourcei(this.sourceId, SOFTSourceSpatialize.AL_SOURCE_SPATIALIZE_SOFT);
        return Spatialization.getByAlId(alId);
    }


    /**
     * Sets wether this sound source should loop. When looping is enabled, the source will immediately play the sound again when it's finished playing.
     *
     * @param looping
     */
    public void setLooping(boolean looping) {
        AL10.alSourcei(this.sourceId, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
    }


    /**
     * Returns whether this sound source is currently playing.
     *
     * @return true when this sound source is playing, false otherwise.
     */
    public boolean isPlaying() {
        return AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }


    /**
     * Returns whether this sound source is paused.
     *
     * @return true when this sound source is paused, false otherwise.
     */
    public boolean isPaused() {
        return AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED;
    }


    /**
     * Pauses the sound playback.
     */
    public void pause() {
        AL10.alSourcePause(this.sourceId);
    }


    /**
     * Stops the sound playback and rewinds it.
     */
    public void stop() {
        AL10.alSourceRewind(this.sourceId);
    }


    /**
     * Enables the given filter as direct filter (dry signal) on this sound source.
     *
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     *
     */
    public void setFilter(float lowFreqVolume, float highFreqVolume) {
        this.directFilter = lowFreqVolume != 1f || highFreqVolume != 1f;
        Filter filter = null;
        if (this.directFilter) {
            filter = Audio.get().publicFilter;
            filter.setLowFrequencyVolume(lowFreqVolume);
            filter.setHighFrequencyVolume(highFreqVolume);
        }
        AL10.alSourcei(this.sourceId, EXTEfx.AL_DIRECT_FILTER, filter != null ? filter.getId() : EXTEfx.AL_FILTER_NULL);
    }


    /**
     * Returns true if a direct filter is active on this sound source.
     *
     * @return result
     */
    public boolean hasFilter() {
        return this.directFilter;
    }


    /**
     * Attaches a sound effect to this sound source. If you attach more effects than effect slots are available, the oldest attached effect will be kicked out.
     * Attaching an effect that is already attached to this source is a legal NOP.<br>
     * <br>
     * {@link AudioDeviceConfig#setEffectSlots(int)} to change the number of available effect slots.<br>
     * Call {@link AudioDevice#getNumberOfEffectSlots()} to retrieve the number of available effect slots.
     *
     * @param effect
     *
     * @return the effect that was kicked out or null otherwise
     */
    public SoundEffect attachEffect(SoundEffect effect) {
        return this.attachEffect(effect, 1f, 1f);
    }


    /**
     * Attaches a sound effect to this sound source. If you attach more effects than effect slots are available, the oldest attached effect will be kicked out.
     * Attaching an effect that is already attached to this source is a legal NOP. Optionally you can set a filter that is only used for this effect, or null if
     * you don't want to apply a filter. <br>
     * <br>
     * {@link AudioDeviceConfig#setEffectSlots(int)} to change the number of available effect slots.<br>
     * Call {@link AudioDevice#getNumberOfEffectSlots()} to retrieve the number of available effect slots.
     *
     * @param effect
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     *
     * @return the effect that was kicked out or null otherwise
     */
    public SoundEffect attachEffect(SoundEffect effect, float lowFreqVolume, float highFreqVolume) {
        if (this.effects.length == 0) {
            this.logger.error(this.getClass(), "Attaching an effect failed: no effect slots available, check your AudioDeviceConfig.");
            return null;
        }

        SoundEffect result = null;

        // CANCEL IF THE EFFECT IS ALREADY ATTACHED TO THIS SOURCE
        for (final SoundEffect effect2 : this.effects) {
            if (effect2 == effect) {
                return null;
            }
        }

        // REMOVE OLD EFFECT IF ANY
        if (this.effects[this.nextSoundEffectSendId] != null) {
            this.effects[this.nextSoundEffectSendId].removeSource(this);
            result = this.effects[this.nextSoundEffectSendId];
            this.effects[this.nextSoundEffectSendId] = null;
        }

        // ADD EFFECT
        Filter filter = null;
        if (lowFreqVolume != 1f || highFreqVolume != 1f) {
            filter = Audio.get().publicFilter;
            filter.setLowFrequencyVolume(lowFreqVolume);
            filter.setHighFrequencyVolume(highFreqVolume);
        }
        final int filterHandle = filter != null ? filter.getId() : EXTEfx.AL_FILTER_NULL;
        AL11.alSource3i(this.sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, effect.getAuxSlotId(), this.nextSoundEffectSendId, filterHandle);
        this.effects[this.nextSoundEffectSendId] = effect;
        effect.addSource(this);

        // SET NEXT SOUND EFFECT SEND ID
        this.nextSoundEffectSendId++;
        if (this.nextSoundEffectSendId >= this.effects.length) {
            this.nextSoundEffectSendId = 0;
        }

        return result;
    }


    /**
     * Detaches the given SoundEffect from this sound source.
     *
     * @param effect the effect
     *
     * @return true if the effect was successfully detached, false if the effect isn't attached to this source (anymore)
     */
    public boolean detachEffect(SoundEffect effect) {
        for (int i = 0; i < this.effects.length; i++) {
            if (this.effects[i] == effect) {
                AL11.alSource3i(this.sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, EXTEfx.AL_EFFECTSLOT_NULL, i, EXTEfx.AL_FILTER_NULL);
                this.effects[i].removeSource(this);
                this.effects[i] = null;
                return true;
            }
        }

        return false;
    }


    /**
     * Detaches all currently attached sound effects from this sound source.
     */
    public void detachAllEffects() {
        for (int i = 0; i < this.effects.length; i++) {
            if (this.effects[i] != null) {
                AL11.alSource3i(this.sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, EXTEfx.AL_EFFECTSLOT_NULL, i, EXTEfx.AL_FILTER_NULL);
                this.effects[i].removeSource(this);
                this.effects[i] = null;
            }
        }
        this.nextSoundEffectSendId = 0;
    }


    protected void setResamplerByIndex(int index) {
        if (index >= 0 && index != this.resamplerIndex) {
            this.resamplerIndex = index;
            AL10.alSourcei(this.sourceId, SOFTSourceResampler.AL_SOURCE_RESAMPLER_SOFT, index);
        }
    }


    /**
     * Sets the resampler for this sound source.<br>
     * <br>
     * Check {@link AudioDevice#getAvailableResamplers()} for a list of available resamplers.
     *
     * @param resampler
     *
     * @return true if successful, false if the desired resampler is not available
     */
    public boolean setResampler(String resampler) {
        final AudioDevice device = Audio.get().getDevice();
        final int resamplerIndex = device.getResamplerIndexByName(resampler);
        if (resamplerIndex >= 0) {
            this.setResamplerByIndex(resamplerIndex);
            return true;
        }

        return false;
    }


    /**
     * Returns the name of the resampler currently in use.
     *
     * @return name of the resampler
     */
    public String getResampler() {
        final AudioDevice device = Audio.get().getDevice();
        final int resamplerIndex = AL10.alGetSourcei(this.sourceId, SOFTSourceResampler.AL_SOURCE_RESAMPLER_SOFT);
        return device.getResamplerNameByIndex(resamplerIndex);
    }


    void onEffectDisposal(SoundEffect effect) {
        for (int i = 0; i < this.effects.length; i++) {
            if (this.effects[i] == effect) {
                AL11.alSource3i(this.sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, EXTEfx.AL_EFFECTSLOT_NULL, i, EXTEfx.AL_FILTER_NULL);
                this.effects[i] = null;
                this.nextSoundEffectSendId = i;
                break;
            }
        }
    }


    protected void dispose() {
        this.detachAllEffects();
        AL10.alDeleteSources(this.sourceId);
        if (!this.errorLogger.checkLogError("Failed to dispose the SoundSource")) {
            this.logger.debug(this.getClass(), "SoundSource successfully disposed");
        }
    }

}
