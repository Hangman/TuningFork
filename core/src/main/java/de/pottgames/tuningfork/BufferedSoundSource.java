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

import com.badlogic.gdx.math.Vector3;

import de.pottgames.tuningfork.AudioConfig.Virtualization;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A sound source that is backed by a single buffer.
 *
 * @author Matthias
 *
 */
public class BufferedSoundSource extends SoundSource {
    private SoundBuffer            buffer;
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    boolean                        obtained = false;


    BufferedSoundSource() {
        final Audio audio = Audio.get();
        this.logger = audio.logger;
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);
    }


    @Override
    public void setVolume(float volume) {
        if (this.obtained) {
            super.setVolume(volume);
        }
    }


    @Override
    public void setPitch(float pitch) {
        if (this.obtained) {
            super.setPitch(pitch);
        }
    }


    @Override
    public void play() {
        if (this.obtained) {
            super.play();
        }
    }


    void setBuffer(SoundBuffer buffer) {
        if (this.obtained) {
            this.buffer = buffer;
            AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, buffer != null ? buffer.getBufferId() : 0);
        }
    }


    @Override
    public void setRelative(boolean relative) {
        if (this.obtained) {
            super.setRelative(relative);
        }
    }


    @Override
    public void setPosition(float x, float y, float z) {
        if (this.obtained) {
            super.setPosition(x, y, z);
        }
    }


    @Override
    public void enableAttenuation() {
        if (this.obtained) {
            super.enableAttenuation();
        }
    }


    @Override
    public void disableAttenuation() {
        if (this.obtained) {
            super.disableAttenuation();
        }
    }


    @Override
    public void setAttenuationFactor(float rolloff) {
        if (this.obtained) {
            super.setAttenuationFactor(rolloff);
        }
    }


    @Override
    public void setAttenuationMinDistance(float minDistance) {
        if (this.obtained) {
            super.setAttenuationMinDistance(minDistance);
        }
    }


    @Override
    public void setAttenuationMaxDistance(float maxDistance) {
        if (this.obtained) {
            super.setAttenuationMaxDistance(maxDistance);
        }
    }


    @Override
    public void makeDirectional(Vector3 direction, float coneInnerAngle, float coneOuterAngle, float outOfConeVolume) {
        if (this.obtained) {
            super.makeDirectional(direction, coneInnerAngle, coneOuterAngle, outOfConeVolume);
        }
    }


    @Override
    public void setDirection(Vector3 direction) {
        if (this.obtained) {
            super.setDirection(direction);
        }
    }


    @Override
    public void makeOmniDirectional() {
        if (this.obtained) {
            super.makeOmniDirectional();
        }
    }


    @Override
    public void setSpeed(float x, float y, float z) {
        if (this.obtained) {
            super.setSpeed(x, y, z);
        }
    }


    @Override
    public void setLooping(boolean looping) {
        if (this.obtained) {
            super.setLooping(looping);
        }
    }


    @Override
    public void pause() {
        if (this.obtained) {
            super.pause();
        }
    }


    @Override
    public void stop() {
        if (this.obtained) {
            super.stop();
        }
    }


    /**
     * Returns the duration of the attached sound in seconds.
     *
     * @return the duration of the attached sound.<br>
     *         Returns -1f if no buffer is attached or the duration couldn't be measured.
     */
    public float getDuration() {
        return this.buffer != null ? this.buffer.getDuration() : -1f;
    }


    /**
     * Sets the playback position of this sound source. Invalid values are ignored but an error is logged.
     *
     * @param seconds
     */
    public void setPlaybackPosition(float seconds) {
        AL10.alSourcef(this.sourceId, AL11.AL_SEC_OFFSET, seconds);
        this.errorLogger.checkLogError("Failed to set playback position");
    }


    /**
     * Returns the current playback position in seconds.
     *
     * @return the playback position
     */
    public float getPlaybackPosition() {
        return AL10.alGetSourcef(this.sourceId, AL11.AL_SEC_OFFSET);
    }


    @Override
    public SoundEffect attachEffect(SoundEffect effect) {
        if (this.obtained) {
            return super.attachEffect(effect);
        }

        return null;
    }


    @Override
    public boolean detachEffect(SoundEffect effect) {
        if (this.obtained) {
            return super.detachEffect(effect);
        }

        return false;
    }


    @Override
    public void detachAllEffects() {
        if (this.obtained) {
            super.detachAllEffects();
        }
    }


    SoundBuffer getBuffer() {
        return this.buffer;
    }


    void reset(float attenuationFactor, float attenuationMinDistance, float attenuationMaxDistance, Virtualization virtualization, int resamplerIndex) {
        this.obtained = true;
        AL10.alSourceRewind(this.sourceId);
        this.setBuffer(null);
        this.setFilter(null);
        this.setLooping(false);
        this.setPitch(1f);
        this.setVolume(1f);
        this.setRelative(false);
        this.setPosition(0f, 0f, 0f);
        this.setSpeed(0f, 0f, 0f);
        this.setVirtualization(virtualization);
        this.setAttenuationFactor(attenuationFactor);
        this.setAttenuationMaxDistance(attenuationMaxDistance);
        this.setAttenuationMinDistance(attenuationMinDistance);
        this.detachAllEffects();
        this.setResamplerByIndex(resamplerIndex);
        this.obtained = false;
    }


    /**
     * Releases this sound source which makes it available again. Always call this after you're done using it.
     */
    public void free() {
        if (!this.obtained) {
            throw new TuningForkRuntimeException("Invalid call to BufferedSoundSource.free(), you are not the owner of this sound source.");
        }
        this.stop();
        this.obtained = false;
    }

}
