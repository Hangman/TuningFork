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

import org.lwjgl.openal.AL10;

public enum PcmFormat {
    STEREO_16_BIT(AL10.AL_FORMAT_STEREO16), STEREO_8_BIT(AL10.AL_FORMAT_STEREO8), MONO_16_BIT(AL10.AL_FORMAT_MONO16), MONO_8_BIT(AL10.AL_FORMAT_MONO8);


    private final int alId;


    PcmFormat(int alId) {
        this.alId = alId;
    }


    public int getAlId() {
        return this.alId;
    }


    public static PcmFormat getBySampleDepthAndChannels(int channels, int bitsPerSample) {
        switch (channels) {
            case 1:
                switch (bitsPerSample) {
                    case 8:
                        return PcmFormat.MONO_8_BIT;
                    case 16:
                        return PcmFormat.MONO_16_BIT;
                }
            case 2:
                switch (bitsPerSample) {
                    case 8:
                        return PcmFormat.STEREO_8_BIT;
                    case 16:
                        return PcmFormat.STEREO_16_BIT;
                }
        }

        return null;
    }

}
