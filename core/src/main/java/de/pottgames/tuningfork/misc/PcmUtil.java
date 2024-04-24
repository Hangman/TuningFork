/**
 * Copyright 2023 Matthias Finke
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

public abstract class PcmUtil {

    /**
     * Reverses the audio data.
     *
     * @param pcmData the source array of pcm data
     * @param sampleSizeInBytes the bytes per sample
     *
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

}
