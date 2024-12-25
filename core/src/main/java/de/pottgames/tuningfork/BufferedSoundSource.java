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
import org.lwjgl.openal.SOFTSourceStartDelay;

import com.badlogic.gdx.math.Vector3;

import de.pottgames.tuningfork.jukebox.song.SongSource;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A sound source that is backed by a single buffer.
 *
 * @author Matthias
 *
 */
public class BufferedSoundSource extends SongSource {
    private SoundBuffer            buffer;
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    boolean                        obtained = false;


    BufferedSoundSource() {
        final Audio audio = Audio.get();
        logger = audio.getLogger();
        errorLogger = new ErrorLogger(this.getClass(), logger);
    }


    @Override
    public void setVolume(float volume) {
        if (obtained) {
            super.setVolume(volume);
        }
    }


    @Override
    public void setPitch(float pitch) {
        if (obtained) {
            super.setPitch(pitch);
        }
    }


    @Override
    public void play() {
        if (obtained) {
            super.play();
        }
    }


    /**
     * Plays the sound at the specified time. Negative values for time will result in an error log entry but do nothing else. Positive values that point to the
     * past will make the source play immediately. The source will be in playing-state while waiting for the start time to be reached. A call to {@link #play()}
     * will not play the sound immediately anymore. In order to delete the play-at-time, call {@link #stop()}.
     *
     * @param time the time in nanoseconds, use {@link AudioDevice#getClockTime()} to get the current time
     */
    public void playAtTime(long time) {
        if (time < 0) {
            logger.error(this.getClass(), "Invalid time parameter: " + time);
            return;
        }

        if (obtained) {
            SOFTSourceStartDelay.alSourcePlayAtTimeSOFT(sourceId, time);
        }
    }


    void setBuffer(SoundBuffer buffer) {
        if (obtained) {
            this.buffer = buffer;
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, buffer != null ? buffer.getBufferId() : 0);
        }
    }


    @Override
    public void setRelative(boolean relative) {
        if (obtained) {
            super.setRelative(relative);
        }
    }


    @Override
    public void setPosition(float x, float y, float z) {
        if (obtained) {
            super.setPosition(x, y, z);
        }
    }


    @Override
    public void enableAttenuation() {
        if (obtained) {
            super.enableAttenuation();
        }
    }


    @Override
    public void disableAttenuation() {
        if (obtained) {
            super.disableAttenuation();
        }
    }


    @Override
    public void setAttenuationFactor(float rolloff) {
        if (obtained) {
            super.setAttenuationFactor(rolloff);
        }
    }


    @Override
    public void setAttenuationMinDistance(float minDistance) {
        if (obtained) {
            super.setAttenuationMinDistance(minDistance);
        }
    }


    @Override
    public void setAttenuationMaxDistance(float maxDistance) {
        if (obtained) {
            super.setAttenuationMaxDistance(maxDistance);
        }
    }


    @Override
    public void makeDirectional(Vector3 direction, float coneInnerAngle, float coneOuterAngle, float outOfConeVolume) {
        if (obtained) {
            super.makeDirectional(direction, coneInnerAngle, coneOuterAngle, outOfConeVolume);
        }
    }


    @Override
    public void setDirection(Vector3 direction) {
        if (obtained) {
            super.setDirection(direction);
        }
    }


    @Override
    public void makeOmniDirectional() {
        if (obtained) {
            super.makeOmniDirectional();
        }
    }


    @Override
    public void setSpeed(float x, float y, float z) {
        if (obtained) {
            super.setSpeed(x, y, z);
        }
    }


    @Override
    public void setLooping(boolean looping) {
        if (obtained) {
            super.setLooping(looping);
        }
    }


    @Override
    public void pause() {
        if (obtained) {
            super.pause();
        }
    }


    @Override
    public void stop() {
        if (obtained) {
            super.stop();
        }
    }


    /**
     * Returns the duration of the attached sound in seconds.
     *
     * @return the duration of the attached sound.<br>
     *         Returns -1f if no buffer is attached or the duration couldn't be measured.
     */
    @Override
    public float getDuration() {
        return buffer != null ? buffer.getDuration() : -1f;
    }


    /**
     * Sets the playback position of this sound source. Invalid values are ignored but an error is logged.
     *
     * @param seconds the position in seconds
     */
    public void setPlaybackPosition(float seconds) {
        AL10.alSourcef(sourceId, AL11.AL_SEC_OFFSET, seconds);
        errorLogger.checkLogError("Failed to set playback position");
    }


    /**
     * Returns the current playback position in seconds.
     *
     * @return the playback position
     */
    @Override
    public float getPlaybackPosition() {
        return AL10.alGetSourcef(sourceId, AL11.AL_SEC_OFFSET);
    }


    @Override
    public SoundEffect attachEffect(SoundEffect effect) {
        if (obtained) {
            return super.attachEffect(effect);
        }

        return null;
    }


    @Override
    public boolean detachEffect(SoundEffect effect) {
        if (obtained) {
            return super.detachEffect(effect);
        }

        return false;
    }


    @Override
    public void detachAllEffects() {
        if (obtained) {
            super.detachAllEffects();
        }
    }


    /**
     * Returns the SoundBuffer that this source is currently using.
     *
     * @return the buffer
     */
    public SoundBuffer getBuffer() {
        return buffer;
    }


    void reset(AudioSettings defaultSettings) {
        obtained = true;
        AL10.alSourceRewind(sourceId);
        setBuffer(null);
        setFilter(1f, 1f);
        setLooping(false);
        setPitch(1f);
        setVolume(1f);
        setRelative(false);
        this.setPosition(0f, 0f, 0f);
        this.setSpeed(0f, 0f, 0f);
        setVirtualization(defaultSettings.getVirtualization());
        setSpatialization(defaultSettings.getSpatialization());
        setAttenuationFactor(defaultSettings.getAttenuationFactor());
        setAttenuationMaxDistance(defaultSettings.getMaxAttenuationDistance());
        setAttenuationMinDistance(defaultSettings.getMinAttenuationDistance());
        detachAllEffects();
        setResamplerByIndex(defaultSettings.getResamplerIndex());
        setRadius(0f);
        obtained = false;
    }


    /**
     * Releases this sound source which makes it available again. Always call this after you're done using it.
     */
    public void free() {
        if (!obtained) {
            throw new TuningForkRuntimeException("Invalid call to BufferedSoundSource.free(), you are not the owner of this sound source.");
        }
        stop();
        setBuffer(null);
        detachAllEffects();
        obtained = false;
    }

}
