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
import org.lwjgl.openal.EXTDouble;
import org.lwjgl.openal.EXTFloat32;
import org.lwjgl.openal.EXTMCFormats;
import org.lwjgl.openal.SOFTMSADPCM;

public enum PcmFormat {
    SURROUND_7DOT1_16_BIT(EXTMCFormats.AL_FORMAT_71CHN16, 8, 16, PcmDataType.INTEGER),
    SURROUND_7DOT1_8_BIT(EXTMCFormats.AL_FORMAT_71CHN8, 8, 8, PcmDataType.INTEGER),
    SURROUND_6DOT1_16_BIT(EXTMCFormats.AL_FORMAT_61CHN16, 7, 16, PcmDataType.INTEGER),
    SURROUND_6DOT1_8_BIT(EXTMCFormats.AL_FORMAT_61CHN8, 7, 8, PcmDataType.INTEGER),
    SURROUND_5DOT1_16_BIT(EXTMCFormats.AL_FORMAT_51CHN16, 6, 16, PcmDataType.INTEGER),
    SURROUND_5DOT1_8_BIT(EXTMCFormats.AL_FORMAT_51CHN8, 6, 8, PcmDataType.INTEGER),
    QUAD_16_BIT(EXTMCFormats.AL_FORMAT_QUAD16, 4, 16, PcmDataType.INTEGER),
    QUAD_8_BIT(EXTMCFormats.AL_FORMAT_QUAD8, 4, 8, PcmDataType.INTEGER),
    FLOAT_STEREO_64_BIT(EXTDouble.AL_FORMAT_STEREO_DOUBLE_EXT, 2, 64, PcmDataType.FLOAT),
    FLOAT_STEREO_32_BIT(EXTFloat32.AL_FORMAT_STEREO_FLOAT32, 2, 32, PcmDataType.FLOAT),
    STEREO_16_BIT(AL10.AL_FORMAT_STEREO16, 2, 16, PcmDataType.INTEGER),
    STEREO_8_BIT(AL10.AL_FORMAT_STEREO8, 2, 8, PcmDataType.INTEGER),
    FLOAT_MONO_64_BIT(EXTDouble.AL_FORMAT_MONO_DOUBLE_EXT, 1, 64, PcmDataType.FLOAT),
    FLOAT_MONO_32_BIT(EXTFloat32.AL_FORMAT_MONO_FLOAT32, 1, 32, PcmDataType.FLOAT),
    MONO_16_BIT(AL10.AL_FORMAT_MONO16, 1, 16, PcmDataType.INTEGER),
    MONO_8_BIT(AL10.AL_FORMAT_MONO8, 1, 8, PcmDataType.INTEGER),
    MS_ADPCM_MONO(SOFTMSADPCM.AL_FORMAT_MONO_MSADPCM_SOFT, 1, 4, PcmDataType.MS_ADPCM),
    MS_ADPCM_STEREO(SOFTMSADPCM.AL_FORMAT_STEREO_MSADPCM_SOFT, 2, 4, PcmDataType.MS_ADPCM);


    private final int         alId;
    private final int         channels;
    private final int         bitsPerSample;
    private final PcmDataType dataType;


    PcmFormat(int alId, int channels, int bitsPerSample, PcmDataType dataType) {
        this.alId = alId;
        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
        this.dataType = dataType;
    }


    public int getAlId() {
        return alId;
    }


    public int getChannels() {
        return channels;
    }


    public int getBitsPerSample() {
        return bitsPerSample;
    }


    public PcmDataType getDataType() {
        return dataType;
    }


    public static boolean isSupportedChannelCount(int channels) {
        return channels > 0 && channels != 3 && channels != 5 && channels <= 8;
    }


    public static boolean isSupportedBitRate(int bits) {
        return bits == 8 || bits == 16 || bits == 32 || bits == 64;
    }


    public static PcmFormat determineFormat(int channels, int bitsPerSample, PcmDataType pcmDataType) {
        if (pcmDataType == PcmDataType.INTEGER) {
            switch (channels) {
                case 1:
                    switch (bitsPerSample) {
                        case 8:
                            return PcmFormat.MONO_8_BIT;
                        case 16:
                            return PcmFormat.MONO_16_BIT;
                    }
                    break;
                case 2:
                    switch (bitsPerSample) {
                        case 8:
                            return PcmFormat.STEREO_8_BIT;
                        case 16:
                            return PcmFormat.STEREO_16_BIT;
                    }
                    break;
                case 4:
                    switch (bitsPerSample) {
                        case 8:
                            return PcmFormat.QUAD_8_BIT;
                        case 16:
                            return PcmFormat.QUAD_16_BIT;
                    }
                    break;
                case 6:
                    switch (bitsPerSample) {
                        case 8:
                            return PcmFormat.SURROUND_5DOT1_8_BIT;
                        case 16:
                            return PcmFormat.SURROUND_5DOT1_16_BIT;
                    }
                    break;
                case 7:
                    switch (bitsPerSample) {
                        case 8:
                            return PcmFormat.SURROUND_6DOT1_8_BIT;
                        case 16:
                            return PcmFormat.SURROUND_6DOT1_16_BIT;
                    }
                    break;
                case 8:
                    switch (bitsPerSample) {
                        case 8:
                            return PcmFormat.SURROUND_7DOT1_8_BIT;
                        case 16:
                            return PcmFormat.SURROUND_7DOT1_16_BIT;
                    }
                    break;
            }
        } else if (pcmDataType == PcmDataType.FLOAT) {
            switch (channels) {
                case 1:
                    switch (bitsPerSample) {
                        case 32:
                            return PcmFormat.FLOAT_MONO_32_BIT;
                        case 64:
                            return PcmFormat.FLOAT_MONO_64_BIT;
                    }
                    break;
                case 2:
                    switch (bitsPerSample) {
                        case 32:
                            return PcmFormat.FLOAT_STEREO_32_BIT;
                        case 64:
                            return PcmFormat.FLOAT_STEREO_64_BIT;
                    }
                    break;
            }
        } else if (pcmDataType == PcmDataType.MS_ADPCM) {
            switch (channels) {
                case 1:
                    return PcmFormat.MS_ADPCM_MONO;
                case 2:
                    return PcmFormat.MS_ADPCM_STEREO;
            }
        }

        return null;
    }


    public enum PcmDataType {
        INTEGER, FLOAT, MS_ADPCM;
    }

}
