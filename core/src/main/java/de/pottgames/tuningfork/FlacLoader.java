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

import java.io.File;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.decoder.FlacInputStream;
import de.pottgames.tuningfork.misc.PcmUtil;

public abstract class FlacLoader {

    /**
     * Loads a flac file into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        return FlacLoader.load(new FlacInputStream(file));
    }


    /**
     * Loads a flac file into a {@link SoundBuffer}.
     *
     * @param file the file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return FlacLoader.load(new FlacInputStream(file));
    }


    /**
     * Loads a flac stream into a {@link SoundBuffer} and closes it afterward.
     *
     * @param stream the input stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        return FlacLoader.load(new FlacInputStream(stream));
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link FlacInputStream}.
     *
     * @param flacStream the stream
     *
     * @return the SoundBuffer
     */
    private static SoundBuffer load(FlacInputStream flacStream) {
        SoundBuffer result;

        try {
            final byte[] buffer = new byte[(int) flacStream.totalSamples() * flacStream.getBytesPerSample() * flacStream.getChannels()];
            flacStream.read(buffer);
            result = new SoundBuffer(buffer, flacStream.getChannels(), flacStream.getSampleRate(), flacStream.getBitsPerSample(), flacStream.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(flacStream);
        }

        return result;
    }


    /**
     * Loads a flac file in reverse into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadReverse(FileHandle file) {
        final FlacInputStream stream = new FlacInputStream(file);
        SoundBuffer result;

        try {
            if (stream.getBitsPerSample() % 8 != 0) {
                throw new TuningForkRuntimeException("Reverse loading isn't supported for sample sizes that aren't divisible by 8.");
            }

            final byte[] buffer = new byte[(int) stream.totalSamples() * stream.getBytesPerSample() * stream.getChannels()];
            stream.read(buffer);
            final byte[] reversedPcm = PcmUtil.reverseAudio(buffer, stream.getBitsPerSample() / 8);
            result = new SoundBuffer(reversedPcm, stream.getChannels(), stream.getSampleRate(), stream.getBitsPerSample(), stream.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(stream);
        }

        return result;
    }

}
