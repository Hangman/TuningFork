/**
 * Copyright 2024 Matthias Finke
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

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

/**
 * A {@link SoundBuffer} that keeps a copy of the audio data that can be read.
 *
 * @author Matthias
 */
public class ReadableSoundBuffer extends SoundBuffer {
    private final byte[] bufferCopy;


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * <br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero .<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * The interleaved format also applies to surround sound.
     *
     * @param pcm the pcm data
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType the pcm data type
     */
    public ReadableSoundBuffer(byte[] pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType) {
        super(pcm, channels, sampleRate, bitsPerSample, pcmDataType);
        bufferCopy = pcm;
    }


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero .<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * The interleaved format also applies to surround sound.
     *
     * @param pcm the pcm data buffer
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType the pcm data type
     * @param blockAlign the block alignment (currently only used for MS ADPCM data)
     */
    public ReadableSoundBuffer(ShortBuffer pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType, int blockAlign) {
        super(pcm, channels, sampleRate, bitsPerSample, pcmDataType, blockAlign);
        pcm.rewind();
        final ByteBuffer byteBuffer = ByteBuffer.allocate(pcm.remaining() * 2);
        while (pcm.hasRemaining()) {
            byteBuffer.putShort(pcm.get());
        }
        bufferCopy = byteBuffer.array();
    }


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * Consider using {@link #ReadableSoundBuffer(byte[], int, int, int, PcmDataType)} instead if you're not providing MS_ADPCM data.<br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero .<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * The interleaved format also applies to surround sound.
     *
     * @param pcm the pcm data
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType the pcm data type
     * @param blockAlign the block alignment (currently only used for MS ADPCM data)
     */
    public ReadableSoundBuffer(byte[] pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType, int blockAlign) {
        super(pcm, channels, sampleRate, bitsPerSample, pcmDataType, blockAlign);
        bufferCopy = pcm;
    }


    /**
     * Returns a copy of the audio data (no allocation involved here) used in this SoundBuffer. Writing to it has no effect to the actual data used.<br>
     * Use {@link #getPcmFormat()} to get info about the data layout.
     *
     * @return a copy of the audio data
     */
    public byte[] getAudioData() {
        return bufferCopy;
    }

}
