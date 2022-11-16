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
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
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
    public static SoundBuffer load(File file) {
        return OggLoader.load(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads sound data from a {@link FileHandle} into a {@link SoundBuffer} using the ogg decoder and closes the stream afterwards.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        final OggInputStream input = new OggInputStream(file, null);
        return OggLoader.load(input);
    }


    /**
     * Loads an ogg input stream into a {@link SoundBuffer} and closes it afterwards.
     *
     * @param stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        final OggInputStream input = new OggInputStream(stream);
        return OggLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link OggInputStream}.
     *
     * @param input
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(OggInputStream input) {
        SoundBuffer result = null;
        try {
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
