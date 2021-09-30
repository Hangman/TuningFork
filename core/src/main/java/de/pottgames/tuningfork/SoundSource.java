package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

public class SoundSource {
    final int             sourceId;
    boolean               obtained   = false;
    private final Vector3 zeroVector = new Vector3(0f, 0f, 0f);

    // OPEN AL STATES
    private float         volume   = 1f;
    private float         pitch    = 1f;
    private final Vector3 position = new Vector3(this.zeroVector);
    private final Vector3 speed    = new Vector3(this.zeroVector);
    private int           bufferId = Integer.MIN_VALUE;
    private boolean       relative = false;
    private boolean       looping  = false;


    SoundSource() {
        this.sourceId = AL10.alGenSources();
    }


    public void setVolume(float volume) {
        if (this.obtained) {
            if (NumberUtils.floatToIntBits(volume) != NumberUtils.floatToIntBits(this.volume)) {
                AL10.alSourcef(this.sourceId, AL10.AL_GAIN, volume);
                this.volume = volume;
            }
        }
    }


    public void setPitch(float pitch) {
        if (this.obtained) {
            if (NumberUtils.floatToIntBits(pitch) != NumberUtils.floatToIntBits(this.pitch)) {
                AL10.alSourcef(this.sourceId, AL10.AL_PITCH, pitch);
                this.pitch = pitch;
            }
        }
    }


    public void play() {
        if (this.obtained) {
            AL10.alSourcePlay(this.sourceId);
        }
    }


    public void setBuffer(int bufferId) {
        if (this.obtained) {
            if (bufferId != this.bufferId) {
                AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, bufferId);
                this.bufferId = bufferId;
            }
        }
    }


    public void setRelative(boolean relative) {
        if (this.obtained) {
            if (relative != this.relative) {
                AL10.alSourcei(this.sourceId, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
                this.relative = relative;
            }
        }
    }


    public void setPosition(Vector3 position) {
        if (this.obtained) {
            if (!this.position.equals(position)) {
                AL10.alSource3f(this.sourceId, AL10.AL_POSITION, position.x, position.y, position.z);
                this.position.set(position);
            }
        }
    }


    public void setSpeed(Vector3 speed) {
        if (this.obtained) {
            if (!this.speed.equals(speed)) {
                AL10.alSource3f(this.sourceId, AL10.AL_VELOCITY, speed.x, speed.y, speed.z);
                this.speed.set(speed);
            }
        }
    }


    public void setLooping(boolean looping) {
        if (this.obtained) {
            if (this.looping != looping) {
                AL10.alSourcei(this.sourceId, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
                this.looping = looping;
            }
        }
    }


    public boolean isPlaying() {
        return AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }


    public void pause() {
        if (this.obtained) {
            AL10.alSourcePause(this.sourceId);
        }
    }


    public void stop() {
        if (this.obtained) {
            AL10.alSourceStop(this.sourceId);
        }
    }


    void reset() {
        this.obtained = true;
        AL10.alSourceRewind(this.sourceId);
        this.setLooping(false);
        this.setPitch(1f);
        this.setVolume(1f);
        this.setRelative(false);
        this.setPosition(this.zeroVector);
        this.setSpeed(this.zeroVector);
        this.obtained = false;
    }


    public void free() {
        this.stop();
        this.obtained = false;
    }


    void dispose() {
        this.stop();
        AL10.alDeleteSources(this.sourceId);
    }

}
