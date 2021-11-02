package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.math.Vector3;

/**
 * A sound source that is backed by a single buffer.
 *
 * @author Matthias
 *
 */
public class BufferedSoundSource implements SoundSource {
    final int             sourceId;
    private SoundBuffer   buffer;
    boolean               obtained = false;
    private final Vector3 position = new Vector3(0f, 0f, 0f);


    BufferedSoundSource() {
        this.sourceId = AL10.alGenSources();
    }


    @Override
    public void setVolume(float volume) {
        if (this.obtained) {
            AL10.alSourcef(this.sourceId, AL10.AL_GAIN, volume);
        }
    }


    @Override
    public void setPitch(float pitch) {
        if (this.obtained) {
            AL10.alSourcef(this.sourceId, AL10.AL_PITCH, pitch);
        }
    }


    @Override
    public void play() {
        if (this.obtained) {
            AL10.alSourcePlay(this.sourceId);
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
            AL10.alSourcei(this.sourceId, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
        }
    }


    @Override
    public void setPosition(Vector3 position) {
        this.setPosition(position.x, position.y, position.z);
    }


    @Override
    public Vector3 getPosition(Vector3 saveTo) {
        return saveTo.set(this.position);
    }


    @Override
    public void setPosition(float x, float y, float z) {
        if (this.obtained) {
            AL10.alSource3f(this.sourceId, AL10.AL_POSITION, x, y, z);
            this.position.set(x, y, z);
        }
    }


    @Override
    public void setDistanceFactor(float rolloff) {
        if (this.obtained) {
            AL10.alSourcef(this.sourceId, AL10.AL_ROLLOFF_FACTOR, rolloff);
        }
    }


    @Override
    public void setSpeed(Vector3 speed) {
        this.setSpeed(speed.x, speed.y, speed.z);
    }


    @Override
    public void setSpeed(float x, float y, float z) {
        if (this.obtained) {
            AL10.alSource3f(this.sourceId, AL10.AL_VELOCITY, x, y, z);
        }
    }


    @Override
    public void setLooping(boolean looping) {
        if (this.obtained) {
            AL10.alSourcei(this.sourceId, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
        }
    }


    @Override
    public boolean isPlaying() {
        return AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }


    @Override
    public void pause() {
        if (this.obtained) {
            AL10.alSourcePause(this.sourceId);
        }
    }


    @Override
    public void stop() {
        if (this.obtained) {
            AL10.alSourceRewind(this.sourceId);
        }
    }


    @Override
    public float getDuration() {
        return this.buffer != null ? this.buffer.getDuration() : -1f;
    }


    void reset() {
        this.obtained = true;
        AL10.alSourceRewind(this.sourceId);
        this.setBuffer(null);
        this.setLooping(false);
        this.setPitch(1f);
        this.setVolume(1f);
        this.setRelative(false);
        this.setPosition(0f, 0f, 0f);
        this.setSpeed(0f, 0f, 0f);
        this.setDistanceFactor(1f);
        this.obtained = false;
    }


    @Override
    public void free() {
        this.stop();
        this.obtained = false;
    }


    void dispose() {
        AL10.alDeleteSources(this.sourceId);
    }

}
