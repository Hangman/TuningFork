package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.math.Vector3;

/**
 * A sound source that is backed by a single buffer.
 *
 * @author Matthias
 *
 */
public class BufferedSoundSource extends SoundSource {
    private SoundBuffer buffer;
    boolean             obtained = false;


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


    @Override
    public float getDuration() {
        return this.buffer != null ? this.buffer.getDuration() : -1f;
    }


    void reset(float attenuationFactor, float attenuationMinDistance, float attenuationMaxDistance) {
        this.obtained = true;
        AL10.alSourceRewind(this.sourceId);
        this.setBuffer(null);
        this.setLooping(false);
        this.setPitch(1f);
        this.setVolume(1f);
        this.setRelative(false);
        this.setPosition(0f, 0f, 0f);
        this.setSpeed(0f, 0f, 0f);
        this.setAttenuationFactor(attenuationFactor);
        this.setAttenuationMaxDistance(attenuationMaxDistance);
        this.setAttenuationMinDistance(attenuationMinDistance);
        this.detachAllEffects();
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
