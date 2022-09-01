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

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;

import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A low level sound source class that can be fed with raw pcm data in a real-time fashion.
 *
 * @author Matthias
 *
 */
public class PcmSoundSource extends SoundSource implements Disposable {
    private static final int BUFFER_SIZE          = 4096 * 10;
    private static final int INITIAL_BUFFER_COUNT = 10;

    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final IntArray         freeBufferIds = new IntArray();
    private final ByteBuffer       tempBuffer;
    private final PcmFormat        format;
    private final int              sampleRate;


    /**
     * Creates a new {@link PcmSoundSource} with the given specs.
     *
     * @param sampleRate
     * @param pcmFormat
     */
    public PcmSoundSource(int sampleRate, PcmFormat pcmFormat) {
        final Audio audio = Audio.get();
        this.logger = audio.logger;
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        this.sampleRate = sampleRate;
        this.format = pcmFormat;
        this.tempBuffer = BufferUtils.createByteBuffer(PcmSoundSource.BUFFER_SIZE);

        for (int i = 0; i < PcmSoundSource.INITIAL_BUFFER_COUNT; i++) {
            this.freeBufferIds.add(AL10.alGenBuffers());
        }

        // SET DEFAULTS
        this.setAttenuationFactor(audio.getDefaultAttenuationFactor());
        this.setAttenuationMinDistance(audio.getDefaultAttenuationMinDistance());
        this.setAttenuationMaxDistance(audio.getDefaultAttenuationMaxDistance());

        // REGISTER
        audio.registerManagedSource(this);
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
     * @param offset the start index where to begin reading pcm data in the pcm byte array
     * @param length the length of the pcm data that should be read
     */
    public void queueSamples(byte[] pcm, int offset, int length) {
        this.unqueueProcessedBuffers();

        while (length > 0) {
            final int alBufferId = this.getFreeBufferId();
            final int writtenLength = Math.min(PcmSoundSource.BUFFER_SIZE, length);
            this.tempBuffer.clear();
            this.tempBuffer.put(pcm, offset, writtenLength).flip();
            AL10.alBufferData(alBufferId, this.format.getAlId(), this.tempBuffer, this.sampleRate);
            AL10.alSourceQueueBuffers(this.sourceId, alBufferId);
            length -= writtenLength;
            offset += writtenLength;
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
     * @param pcm in native order
     */
    public void queueSamples(ByteBuffer pcm) {
        this.unqueueProcessedBuffers();
        final int alBufferId = this.getFreeBufferId();
        AL10.alBufferData(alBufferId, this.format.getAlId(), pcm, this.sampleRate);
        AL10.alSourceQueueBuffers(this.sourceId, alBufferId);
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
     * @param pcm in native order
     */
    public void queueSamples(ShortBuffer pcm) {
        this.unqueueProcessedBuffers();
        final int alBufferId = this.getFreeBufferId();
        AL10.alBufferData(alBufferId, this.format.getAlId(), pcm, this.sampleRate);
        AL10.alSourceQueueBuffers(this.sourceId, alBufferId);
    }


    private int getFreeBufferId() {
        if (this.freeBufferIds.isEmpty()) {
            return AL10.alGenBuffers();
        }
        return this.freeBufferIds.pop();
    }


    /**
     * Unqueues processed buffers. This is called automatically on each call to any of the queueSamples methods, so you never <b>have</b> to call it manually.
     */
    public void unqueueProcessedBuffers() {
        final int processedBuffers = AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_PROCESSED);
        for (int i = 0; i < processedBuffers; i++) {
            this.freeBufferIds.add(AL10.alSourceUnqueueBuffers(this.sourceId));
        }
    }


    /**
     * Returns the number of queued buffers. This number is automatically decreased once a buffer is processed (finished playing). Each call to any of the
     * queueSamples methods queues a buffer.
     *
     * @return the number of buffers queued
     */
    public int queuedBuffers() {
        return AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_QUEUED);
    }


    /**
     * Disposes the sound sources native resources. You should never use this sound source after disposing it.
     */
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

        final Audio audio = Audio.get();
        audio.removeManagedSource(this);

        this.errorLogger.checkLogError("Failed to dispose the SoundSource");
        super.dispose();
    }

}
