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

public abstract class FlacLoader {

    public static SoundBuffer load(FileHandle file) {
        return FlacLoader.load(file.file());
    }


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
