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
import java.io.FileInputStream;
import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

public abstract class WaveLoader {

    public static SoundBuffer load(FileHandle file) {
        SoundBuffer result = null;

        WavInputStream input = null;
        try {
            input = new WavInputStream(file);
            result = new SoundBuffer(StreamUtils.copyStreamToByteArray(input, input.dataRemaining), input.channels, input.sampleRate, input.getBitsPerSample());
        } catch (final IOException ex) {
            throw new TuningForkRuntimeException("Error reading WAV file: " + file, ex);
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }


    public static SoundBuffer load(File file) {
        SoundBuffer result = null;

        WavInputStream input = null;
        try {
            input = new WavInputStream(new FileInputStream(file), file.getPath());
            result = new SoundBuffer(StreamUtils.copyStreamToByteArray(input, input.dataRemaining), input.channels, input.sampleRate, input.getBitsPerSample());
        } catch (final IOException ex) {
            throw new TuningForkRuntimeException("Error reading WAV file: " + file, ex);
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
