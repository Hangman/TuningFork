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

package de.pottgames.tuningfork.decoder;

import java.io.Closeable;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.StreamedSoundSource;

/**
 * An audio stream interface that can be implemented to feed a {@link StreamedSoundSource}.
 *
 * @author Matthias
 *
 */
public interface AudioStream extends Closeable {

    /**
     * Returns the duration in seconds or -1 if this information is not available.
     *
     * @return duration in seconds or -1 if the information is not available
     */
    float getDuration();


    /**
     * Resets the audio stream as if it was re-opened. Implementations are free to close themselves and provide a new AudioStream. The AudioStream returned by
     * this function will be used, regardless of whether it is a new instance or the old one.
     *
     * @return an AudioStream
     */
    AudioStream reset();


    /**
     * Returns the number of audio channels.
     *
     * @return number of channels
     */
    int getChannels();


    /**
     * Returns the sample rate.
     *
     * @return the sample rate
     */
    int getSampleRate();


    /**
     * Returns the number of bits per sample, also known as the sample depth.
     *
     * @return the number of bits per sample
     */
    int getBitsPerSample();


    /**
     * Reads bytes from the stream until the given array is full or the stream ends. Returns the number of bytes that were actually read.
     *
     * @param bytes the byte array to store the bytes in
     *
     * @return number of bytes read or -1 if there are no bytes left
     */
    int read(byte[] bytes);


    /**
     * Returns the output data format of this AudioStream.
     *
     * @return the pcm data type
     */
    PcmDataType getPcmDataType();


    /**
     * Returns true if the AudioStream is closed.
     *
     * @return true if closed, false if open
     */
    boolean isClosed();

}
