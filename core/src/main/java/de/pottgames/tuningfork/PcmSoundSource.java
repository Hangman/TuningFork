package de.pottgames.tuningfork;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import com.badlogic.gdx.utils.IntArray;

import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A low level sound source class that can be fed with raw pcm data.
 *
 * @author Matthias
 *
 */
public class PcmSoundSource extends SoundSource {
    private static final int BUFFER_SIZE          = 4096 * 10;
    private static final int INITIAL_BUFFER_COUNT = 10;

    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final IntArray         freeBufferIds = new IntArray();
    private final ByteBuffer       tempBuffer;
    private final PcmFormat        format;
    private final int              sampleRate;


    public PcmSoundSource(int sampleRate, PcmFormat pcmFormat) {
        this.logger = Audio.get().logger;
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        this.sampleRate = sampleRate;
        this.format = pcmFormat;
        this.tempBuffer = BufferUtils.createByteBuffer(PcmSoundSource.BUFFER_SIZE);

        for (int i = 0; i < PcmSoundSource.INITIAL_BUFFER_COUNT; i++) {
            this.freeBufferIds.add(AL10.alGenBuffers());
        }
    }


    /**
     * Adds pcm data to the queue of this sound source.<br>
     * <br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero.<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * <br>
     * <b>Note:</b> An underflow of pcm data will cause the source to stop playing. If you want it to keep playing, call {@link SoundSource#play() play()} after
     * queueing samples.
     *
     * @param pcm
     * @param offset
     * @param length
     */
    public void queueSamples(byte[] pcm, int offset, int length) {
        // UNQUEUE PROCESSED BUFFERS
        final int processedBuffers = AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_PROCESSED);
        for (int i = 0; i < processedBuffers; i++) {
            this.freeBufferIds.add(AL10.alSourceUnqueueBuffers(this.sourceId));
        }

        // QUEUE PCM
        while (length > 0) {
            // FIND FREE BUFFER
            int freeBufferId;
            if (this.freeBufferIds.isEmpty()) {
                freeBufferId = AL10.alGenBuffers();
            } else {
                freeBufferId = this.freeBufferIds.pop();
            }

            // WRITE PCM
            final int writtenLength = Math.min(PcmSoundSource.BUFFER_SIZE, length);
            this.tempBuffer.clear();
            this.tempBuffer.put(pcm, offset, writtenLength).flip();
            AL10.alBufferData(freeBufferId, this.format.getAlId(), this.tempBuffer, this.sampleRate);
            AL10.alSourceQueueBuffers(this.sourceId, freeBufferId);
            length -= writtenLength;
            offset += writtenLength;
        }
    }


    @Override
    public void dispose() {
        this.stop();

        final int processedBuffers = AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_PROCESSED);
        for (int i = 0; i < processedBuffers; i++) {
            this.freeBufferIds.add(AL10.alSourceUnqueueBuffers(this.sourceId));
        }

        for (int i = 0; i < this.freeBufferIds.size; i++) {
            AL10.alDeleteBuffers(this.freeBufferIds.get(i));
        }

        this.errorLogger.checkLogError("Failed to dispose the SoundSource");
        super.dispose();
    }

}
