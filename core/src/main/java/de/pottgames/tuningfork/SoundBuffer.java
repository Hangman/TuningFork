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
import java.nio.ByteOrder;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * Stores sound data in an OpenAL buffer that can be used by sound sources. Needs to be disposed when no longer needed.
 *
 * @author Matthias
 *
 */
public class SoundBuffer implements Disposable {
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final int              bufferId;
    private final float            duration;


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * <br>
     * The pcm data must be 8 or 16-bit.<br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero.<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.
     *
     * @param pcm
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType
     */
    public SoundBuffer(byte[] pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType) {
        this.logger = Audio.get().getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        // DETERMINE PCM FORMAT AND DURATION
        final int samplesPerChannel = pcm.length / (bitsPerSample / 8 * channels);
        this.duration = samplesPerChannel / (float) sampleRate;
        final PcmFormat pcmFormat = PcmFormat.determineFormat(channels, bitsPerSample, pcmDataType);
        if (pcmFormat == null) {
            throw new TuningForkRuntimeException("Unsupported pcm format - channels: " + channels + ", sample depth: " + bitsPerSample);
        }

        // PCM ARRAY TO TEMP BUFFER
        final ByteBuffer buffer = ByteBuffer.allocateDirect(pcm.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(pcm);
        buffer.flip();

        // GEN BUFFER AND UPLOAD PCM DATA
        this.bufferId = AL10.alGenBuffers(); 
        AL10.alBufferData(this.bufferId, pcmFormat.getAlId(), buffer.asShortBuffer(), sampleRate);

        // CHECK FOR ERRORS
        if (!this.errorLogger.checkLogError("Failed to create the SoundBuffer")) {
            this.logger.debug(this.getClass(), "SoundBuffer successfully created");
        }
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
        Audio.get().onBufferDisposal(this);
        AL10.alDeleteBuffers(this.bufferId);
        if (!this.errorLogger.checkLogError("Failed to dispose the SoundBuffer")) {
            this.logger.debug(this.getClass(), "SoundBuffer successfully disposed");
        }
    }

}
