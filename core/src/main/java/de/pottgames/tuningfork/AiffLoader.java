/**
 * Copyright 2023 Matthias Finke
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

import de.pottgames.tuningfork.decoder.AiffInputStream;
import de.pottgames.tuningfork.misc.PcmUtil;

public abstract class AiffLoader {

    /**
     * Loads an aiff file into a {@link SoundBuffer}.
     *
     * @param file the file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return AiffLoader.load(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads an aiff file into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        final AiffInputStream input = new AiffInputStream(file);
        return AiffLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from an {@link InputStream} and closes it afterwards.
     *
     * @param stream the input stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        final AiffInputStream input = new AiffInputStream(stream);
        return AiffLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link AiffInputStream}.
     *
     * @param input the AiffInputStream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(AiffInputStream input) {
        SoundBuffer result = null;
        try {
            final byte[] buffer = new byte[(int) input.totalSamplesPerChannel() * (input.getBitsPerSample() / 8) * input.getChannels()];
            input.read(buffer);
            result = new SoundBuffer(buffer, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }


    /**
     * Loads an aiff file in reverse into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadReverse(FileHandle file) {
        final AiffInputStream input = new AiffInputStream(file);
        SoundBuffer result = null;
        try {
            if (input.getBitsPerSample() % 8 != 0) {
                throw new TuningForkRuntimeException("Reverse loading isn't supported for sample sizes that aren't divisible by 8.");
            }

            final byte[] buffer = new byte[(int) input.totalSamplesPerChannel() * (input.getBitsPerSample() / 8) * input.getChannels()];
            input.read(buffer);
            final byte[] reversedPcm = PcmUtil.reverseAudio(buffer, input.getBitsPerSample() / 8);
            result = new SoundBuffer(reversedPcm, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
