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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.decoder.OggInputStream;

public abstract class OggLoader {

    /**
     * Loads an ogg into a {@link SoundBuffer}.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        return OggLoader.load(file.read());
    }


    /**
     * Loads an ogg into a {@link SoundBuffer}.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        try {
            return OggLoader.load(new FileInputStream(file));
        } catch (final FileNotFoundException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    /**
     * Loads sound data from an {@link InputStream} into a {@link SoundBuffer} using the ogg decoder and closes the stream afterwards.
     *
     * @param stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        SoundBuffer result = null;
        OggInputStream input = null;
        try {
            input = new OggInputStream(stream);
            final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
            final byte[] buffer = new byte[2048];
            while (!input.atEnd()) {
                final int length = input.read(buffer);
                if (length == -1) {
                    break;
                }
                output.write(buffer, 0, length);
            }
            result = new SoundBuffer(output.toByteArray(), input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
