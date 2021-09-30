package de.pottgames.tuningfork;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public class Audio implements Disposable {
    private long            device;
    private long            context;
    private SoundListener   listener;
    private SoundSourcePool sourcePool;


    public Audio(int simultaneousSources) {
        // OPEN THE DEFAULT SOUND DEVICE
        this.device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (this.device == 0L) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        // CREATE A CONTEXT AND SET IT ACTIVE
        final ALCCapabilities deviceCapabilities = ALC.createCapabilities(this.device);
        this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);
        if (this.context == 0L) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        ALC10.alcMakeContextCurrent(this.context);
        AL.createCapabilities(deviceCapabilities);

        // SET DISTANCE ATTENUATION MODEL
        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);

        // CREATE LISTENER
        this.listener = new SoundListener();

        // CREATE SOURCES
        this.sourcePool = new SoundSourcePool(simultaneousSources);
    }


    public void updateListener(Camera camera) {
        this.listener.setSpeed(camera);
        this.listener.setPosition(camera);
        this.listener.setOrientation(camera);
    }


    public SoundSource obtainSource(SoundBuffer buffer) {
        return this.obtainSource(buffer, false);
    }


    public SoundSource obtainSource(SoundBuffer buffer, boolean allowNull) {
        // FIND FREE SOUND SOURCE
        final SoundSource source = this.sourcePool.findFreeSource();

        // THROW EXCEPTION IF ALL SOUND SOURCES ARE BUSY/OBTAINED
        if (!allowNull && source == null) {
            throw new TuningForkRuntimeException(
                    "All SoundSources are busy. Make sure to call free on obtained SoundSources when the sound finished playing. Otherwise consider increasing the simultaneousSources.");
        }

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer.getBufferId());
        source.setRelative(false);

        return source;
    }


    private SoundSource obtainRelativeSource(SoundBuffer buffer, boolean looping) {
        // FIND FREE SOUND SOURCE
        final SoundSource source = this.sourcePool.findFreeSource();

        // THROW EXCEPTION IF ALL SOUND SOURCES ARE BUSY/OBTAINED
        if (source == null) {
            throw new TuningForkRuntimeException(
                    "All SoundSources are busy. Make sure to call free on obtained SoundSources when the sound finished playing. Otherwise consider increasing the simultaneousSources.");
        }

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer.getBufferId());
        source.setRelative(true);

        return source;
    }


    public void play(SoundBuffer buffer) {
        final SoundSource source = this.obtainRelativeSource(buffer, false);
        source.play();
        source.obtained = false;
    }


    public void play(SoundBuffer buffer, float volume) {
        final SoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.play();
        source.obtained = false;
    }


    public void play(SoundBuffer buffer, float volume, float pitch) {
        final SoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.play();
        source.obtained = false;
    }


    public void play(SoundBuffer buffer, float volume, float pitch, float pan) {
        final SoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        AL10.alSource3f(source.sourceId, AL10.AL_POSITION, MathUtils.cos((pan - 1f) * MathUtils.PI / 2f), 0f, MathUtils.sin((pan + 1f) * MathUtils.PI / 2f));
        source.play();
        source.obtained = false;
    }


    public void play3D(SoundBuffer buffer, Vector3 position) {
        final SoundSource source = this.obtainSource(buffer);
        source.play();
        source.play();
        source.obtained = false;
    }


    public void play3D(SoundBuffer buffer, float volume, Vector3 position) {
        final SoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.play();
        source.play();
        source.obtained = false;
    }


    public void play3D(SoundBuffer buffer, float volume, float pitch, Vector3 position) {
        final SoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.play();
        source.play();
        source.obtained = false;
    }


    @Override
    public void dispose() {
        this.sourcePool.dispose();
        if (this.context != 0L) {
            ALC10.alcDestroyContext(this.context);
        }
        if (this.device != 0L) {
            ALC10.alcCloseDevice(this.device);
        }
    }

}
