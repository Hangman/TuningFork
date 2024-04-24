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

package de.pottgames.tuningfork.decoder;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkRuntimeException;

import java.io.IOException;
import java.io.InputStream;

public class MsAdpcmDecoder implements WavDecoder {
    private       InputStream stream;
    private       long        bytesRemaining;
    private final int         blockAlign;
    private final int         blockSize;
    private final int         channels;
    private final int         sampleRate;
    private       int         totalSamplesPerChannel;


    public MsAdpcmDecoder(int blockSize, int channels, int sampleRate) {
        if (channels < 1 || channels > 2) {
            throw new TuningForkRuntimeException("Unsupported number of channels: " + channels);
        }
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.blockSize = blockSize;
        this.blockAlign = (blockSize / channels - 7) * 2 + 2;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
        final int numberOfBlocks = (int) (streamLength / this.blockSize);
        this.totalSamplesPerChannel = numberOfBlocks * this.blockAlign;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance

        if (this.bytesRemaining <= 0) {
            return -1;
        }

        int bytesToRead = output.length;
        int offset = 0;

        while (bytesToRead > 0 && this.bytesRemaining > 0) {
            final int bytesRead = this.stream.read(output, offset, (int) Math.min(bytesToRead, this.bytesRemaining));
            if (bytesRead == -1) {
                if (offset > 0) {
                    return offset;
                }
                this.bytesRemaining = 0;
                return -1;
            }
            this.bytesRemaining -= bytesRead;
            bytesToRead -= bytesRead;
            offset += bytesRead;
        }

        return offset;
    }


    @Override
    public int inputBitsPerSample() {
        return 4;
    }


    @Override
    public int outputBitsPerSample() {
        return 4;
    }


    @Override
    public int blockAlign() {
        return this.blockAlign;
    }


    @Override
    public int blockSize() {
        return this.blockSize;
    }


    @Override
    public int outputChannels() {
        return this.channels;
    }


    @Override
    public int outputSampleRate() {
        return this.sampleRate;
    }


    @Override
    public long outputTotalSamplesPerChannel() {
        return this.totalSamplesPerChannel;
    }


    @Override
    public PcmDataType outputPcmDataType() {
        return PcmDataType.MS_ADPCM;
    }


    @Override
    public long bytesRemaining() {
        return this.bytesRemaining;
    }


    @Override
    public void close() throws IOException {
        this.stream.close();
    }

}
