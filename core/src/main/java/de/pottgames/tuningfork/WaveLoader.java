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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.decoder.WavInputStream;

public abstract class WaveLoader {

    /**
     * Loads a wav file into a {@link SoundBuffer}.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return WaveLoader.load(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads a wav file into a {@link SoundBuffer}.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        final WavInputStream input = new WavInputStream(file);
        return WaveLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from an {@link InputStream} and closes it afterwards.
     *
     * @param stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        final WavInputStream input = new WavInputStream(stream);
        return WaveLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link WavInputStream}.
     *
     * @param input
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(WavInputStream input) {
        SoundBuffer result = null;
        try {
            final byte[] buffer = new byte[(int) input.bytesRemaining()];
            input.read(buffer);
            result = new SoundBuffer(buffer, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType(),
                    input.getBlockAlign());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
