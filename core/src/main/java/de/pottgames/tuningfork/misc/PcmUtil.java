/**
 * Copyright 2024 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.misc;

import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.decoder.util.Util;

public abstract class PcmUtil {

    /**
     * Reverses the audio data.
     *
     * @param pcmData           the source array of pcm data
     * @param sampleSizeInBytes the bytes per sample
     * @return the reversed audio data
     */
    public static byte[] reverseAudio(byte[] pcmData, int sampleSizeInBytes) {
        final int numSamples = pcmData.length / sampleSizeInBytes;
        final byte[] reversedData = new byte[pcmData.length];

        for (int i = 0; i < numSamples; i++) {
            final int srcStart = i * sampleSizeInBytes;
            final int destStart = (numSamples - i - 1) * sampleSizeInBytes;
            System.arraycopy(pcmData, srcStart, reversedData, destStart, sampleSizeInBytes);
        }

        return reversedData;
    }


    /**
     * Calculates the average amplitude of a slice of samples. The result is always in the range 0 - 1. With 0 equals
     * silence and 1 equals full amplitude.
     *
     * @param input       the input byte array containing the samples
     * @param format      the data format of the input byte array
     * @param startSample the start index of the desired slice
     * @param endSample   the end index of the desired slice
     * @param channel     the channel number samples will be read from
     * @return the average amplitude of all samples in the slice in the range 0 - 1
     */
    @ExperimentalFeature(description = "Needs more testing and accuracy in code and javadoc.")
    public static float averageSample(byte[] input, PcmFormat format, int startSample, int endSample, int channel) {
        if (channel > format.getChannels()) {
            throw new TuningForkRuntimeException("The specified channel number " + channel + "doesn't exist.");
        }

        switch (format) {
            case MS_ADPCM_STEREO:
            case MS_ADPCM_MONO:
                throw new TuningForkRuntimeException("Currently not supported for this format.");
            case FLOAT_MONO_64_BIT:
            case FLOAT_STEREO_64_BIT:
                return averageSampleDouble(input, format.getChannels(), startSample, endSample, channel);
            case FLOAT_MONO_32_BIT:
            case FLOAT_STEREO_32_BIT:
                return averageSampleFloat(input, format.getChannels(), startSample, endSample, channel);
            case MONO_16_BIT:
            case STEREO_16_BIT:
            case QUAD_16_BIT:
            case SURROUND_7DOT1_16_BIT:
            case SURROUND_6DOT1_16_BIT:
            case SURROUND_5DOT1_16_BIT:
                return averageSample16Bit(input, format.getChannels(), startSample, endSample, channel);
            case MONO_8_BIT:
            case STEREO_8_BIT:
            case QUAD_8_BIT:
            case SURROUND_5DOT1_8_BIT:
            case SURROUND_6DOT1_8_BIT:
            case SURROUND_7DOT1_8_BIT:
                return averageSample8Bit(input, format.getChannels(), startSample, endSample, channel);
            default:
                throw new TuningForkRuntimeException("Unknown format.");
        }
    }


    private static float averageSample8Bit(byte[] input, int channels, int startSample, int endSample, int channel) {
        float sampleSum = 0f;
        for (int sampleIndex = startSample; sampleIndex <= endSample; sampleIndex += channels) {
            sampleSum += Math.abs((Byte.toUnsignedInt(input[sampleIndex]) - 128)) / 128f;
        }
        return sampleSum / (endSample - startSample);
    }


    private static float averageSample16Bit(byte[] input, int channels, int startSample, int endSample, int channel) {
        final int totalSamples = endSample - startSample;
        final int startIndex = startSample * channels * 2 + (channel - 1) * 2;
        final int endIndex = startIndex + totalSamples * channels * 2 + (channel - 1) * 2;
        final int indexStepSize = channels * 2;
        float sampleSum = 0f;
        for (int sampleIndex = startIndex; sampleIndex < endIndex - 1; sampleIndex += indexStepSize) {
            byte byte1 = input[sampleIndex];
            byte byte2 = input[sampleIndex + 1];
            short sample = (short) (byte1 | byte2 << 8);
            sampleSum += Math.abs(sample) / 32768f;
        }
        return sampleSum / totalSamples;
    }


    private static float averageSampleFloat(byte[] input, int channels, int startSample, int endSample, int channel) {
        final int totalSamples = endSample - startSample;
        final int startIndex = startSample * channels * 4 + (channel - 1) * 4;
        final int endIndex = startIndex + totalSamples * channels * 4 + (channel - 1) * 4;
        final int indexStepSize = channels * 4;
        float sampleSum = 0f;
        for (int sampleIndex = startIndex; sampleIndex < endIndex - 3; sampleIndex += indexStepSize) {
            float sample = Float.intBitsToFloat(Util.intOfLittleEndianBytes(input, sampleIndex));
            sampleSum += sample;
        }
        return sampleSum / totalSamples;
    }


    private static float averageSampleDouble(byte[] input, int channels, int startSample, int endSample, int channel) {
        final int totalSamples = endSample - startSample;
        final int startIndex = startSample * channels * 8 + (channel - 1) * 8;
        final int endIndex = startIndex + totalSamples * channels * 8 + (channel - 1) * 8;
        final int indexStepSize = channels * 8;
        float sampleSum = 0f;
        for (int sampleIndex = startIndex; sampleIndex < endIndex - 7; sampleIndex += indexStepSize) {
            float sample = (float) Double.longBitsToDouble(Util.longOfLittleEndianBytes(input, sampleIndex));
            sampleSum += sample;
        }
        return sampleSum / totalSamples;
    }

}
