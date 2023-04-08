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

public enum SoundFileType {
    OGG, WAV, FLAC, MP3, AIFF;


    public static SoundFileType getByFileEnding(String fileExtension) {
        if ("ogg".equalsIgnoreCase(fileExtension) || "oga".equalsIgnoreCase(fileExtension) || "ogx".equalsIgnoreCase(fileExtension)
                || "opus".equalsIgnoreCase(fileExtension)) {
            return OGG;
        }
        if ("wav".equalsIgnoreCase(fileExtension) || "wave".equalsIgnoreCase(fileExtension)) {
            return WAV;
        }
        if ("flac".equalsIgnoreCase(fileExtension)) {
            return FLAC;
        }
        if ("mp3".equalsIgnoreCase(fileExtension)) {
            return MP3;
        }
        if ("aiff".equalsIgnoreCase(fileExtension) || "aif".equalsIgnoreCase(fileExtension)) {
            return AIFF;
        }

        return null;
    }

}
