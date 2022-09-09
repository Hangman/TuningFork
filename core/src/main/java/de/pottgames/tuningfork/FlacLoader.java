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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.decoder.FlacInputStream;

public abstract class FlacLoader {

    /**
     * Loads a flac file into a {@link SoundBuffer}.<br>
     * <br>
     * <b>Important:</b> Flac files cannot be read from a jar file. The libGDX setup tool as well as liftoff both pack all assets into the jar in the
     * desktop:dist gradle task by default. So you should take care to exclude flac files and deliver them separately.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        return FlacLoader.load(file.file());
    }


    /**
     * Loads a flac file into a {@link SoundBuffer}.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        SoundBuffer result = null;
        FlacInputStream input = null;

        try {
            input = new FlacInputStream(file);
            final byte[] buffer = new byte[(int) input.totalSamples() * input.getBytesPerSample() * input.getChannels()];
            input.read(buffer);
            result = new SoundBuffer(buffer, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
