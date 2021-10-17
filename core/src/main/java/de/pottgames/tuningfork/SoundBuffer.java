package de.pottgames.tuningfork;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.utils.Disposable;

/**
 * Stores sound data in an OpenAL buffer that can be used by sound sources. Needs to be disposed when no longer needed.
 *
 * @author Matthias
 *
 */
public class SoundBuffer implements Disposable {
    private final int   bufferId;
    private final float duration;


    SoundBuffer(byte[] pcm, int channels, int sampleRate) {
        final int bytes = pcm.length - pcm.length % (channels > 1 ? 4 : 2);
        final int samples = bytes / (2 * channels);
        this.duration = samples / (float) sampleRate;

        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(pcm, 0, bytes);
        buffer.flip();

        this.bufferId = AL10.alGenBuffers();
        AL10.alBufferData(this.bufferId, channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, buffer.asShortBuffer(), sampleRate);
    }


    int getBufferId() {
        return this.bufferId;
    }


    /**
     * Returns the duration in seconds.
     *
     * @return the playback duration in seconds.
     */
    public float getDuration() {
        return this.duration;
    }


    @Override
    public void dispose() {
        AL10.alDeleteBuffers(this.bufferId);
    }

}
