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

import java.io.File;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.decoder.QoaInputStream;
import de.pottgames.tuningfork.misc.PcmUtil;

public class QoaLoader {
    /**
     * Loads a qoa file into a {@link SoundBuffer}.
     *
     * @param file the file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return QoaLoader.load(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads a qoa file into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        final QoaInputStream input = new QoaInputStream(file, false);
        return QoaLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from an {@link InputStream} and closes it afterwards.
     *
     * @param stream the input stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        final QoaInputStream input = new QoaInputStream(stream, false);
        return QoaLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link QoaInputStream}.
     *
     * @param input the QoaInputStream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(QoaInputStream input) {
        SoundBuffer result = null;
        try {
            final byte[] buffer = new byte[(int) input.totalSamplesPerChannel() * input.getChannels() * (input.getBitsPerSample() / 8)];
            input.read(buffer);
            result = new SoundBuffer(buffer, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType(),
                    input.getBlockAlign());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }


    /**
     * Loads a qoa file in reverse into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadReverse(FileHandle file) {
        final QoaInputStream input = new QoaInputStream(file, false);
        SoundBuffer result = null;
        try {
            if (input.getBitsPerSample() % 8 != 0) {
                throw new TuningForkRuntimeException("Reverse loading isn't supported for sample sizes that aren't divisible by 8.");
            }

            final byte[] buffer = new byte[(int) input.totalSamplesPerChannel() * input.getChannels() * (input.getBitsPerSample() / 8)];
            input.read(buffer);
            final byte[] reversedPcm = PcmUtil.reverseAudio(buffer, input.getBitsPerSample() / 8);
            result = new SoundBuffer(reversedPcm, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType(),
                    input.getBlockAlign());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
