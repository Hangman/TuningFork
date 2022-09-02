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
import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import io.nayuki.flac.decode.FlacDecoder;

public class FlacInputStream implements AudioStream {
    private FlacDecoder decoder;
    private boolean     closed = false;
    private int[][]     sampleBuffer;
    private int         sampleBufferBlockSize;
    private final int   bytesPerSample;


    public FlacInputStream(FileHandle file) {
        this(file.file());
    }


    public FlacInputStream(File file) {
        try {
            this.decoder = new FlacDecoder(file);
            while (this.decoder.readAndHandleMetadataBlock() != null) {
                // read all meta data blocks
            }
        } catch (final IOException e) {
            StreamUtils.closeQuietly(this);
            throw new TuningForkRuntimeException(e);
        }

        // CHECK IF THE FLAC FILE IS SUPPORTED
        if (this.decoder == null) {
            throw new TuningForkRuntimeException("FlacInputStream couldn't be opened.");
        }
        if (this.decoder.streamInfo == null) {
            throw new TuningForkRuntimeException("Missing StreamInfo in flac file.");
        }
        final int numChannels = this.decoder.streamInfo.numChannels;
        if (!PcmFormat.isSupportedChannelCount(numChannels)) {
            throw new TuningForkRuntimeException("Unsupported number of channels in flac file. Must be 1, 2, 4, 6, 7 or 8 but is: " + numChannels);
        }
        final int bitsPerSample = this.decoder.streamInfo.sampleDepth;
        if (bitsPerSample != 8 && bitsPerSample != 16) {
            throw new TuningForkRuntimeException("Unsupported bits per sample in flac file, only 8 and 16 Bit is supported.");
        }
        if (this.decoder.streamInfo.maxBlockSize > StreamedSoundSource.BUFFER_SIZE_PER_CHANNEL * numChannels) {
            throw new TuningForkRuntimeException(
                    "Flac file exceeds maximum supported block size by TuningFork which is: " + StreamedSoundSource.BUFFER_SIZE_PER_CHANNEL + " per channel");
        }

        this.sampleBuffer = new int[this.decoder.streamInfo.numChannels][65536];
        this.bytesPerSample = this.decoder.streamInfo.sampleDepth / 8;

        this.readBlock();
    }


    @Override
    public int read(byte[] bytes) {
        if (this.sampleBufferBlockSize == 0) {
            return -1;
        }

        int availableBytes = bytes.length;
        int bytesIndex = 0;

        while (availableBytes >= this.sampleBufferBlockSize * this.decoder.streamInfo.numChannels * this.bytesPerSample) {
            for (int i = 0; i < this.sampleBufferBlockSize; i++) {
                for (int channelIndex = 0; channelIndex < this.decoder.streamInfo.numChannels; channelIndex++) {
                    int sample = this.sampleBuffer[channelIndex][i];
                    if (this.bytesPerSample == 1) {
                        sample += 128; // because OpenAL expects an unsigned byte
                    }
                    for (int j = 0; j < this.bytesPerSample; j++, bytesIndex++) {
                        bytes[bytesIndex] = (byte) (sample >>> (j << 3));
                    }
                }
            }

            availableBytes -= this.sampleBufferBlockSize * this.decoder.streamInfo.numChannels * this.bytesPerSample;
            this.readBlock();
            if (this.sampleBufferBlockSize == 0) {
                break;
            }
        }

        return bytes.length - availableBytes;
    }


    private void readBlock() {
        try {
            this.sampleBufferBlockSize = this.decoder.readAudioBlock(this.sampleBuffer, 0);
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    public long totalSamples() {
        return this.decoder.streamInfo.numSamples;
    }


    @Override
    public int getChannels() {
        return this.decoder.streamInfo.numChannels;
    }


    @Override
    public int getSampleRate() {
        return this.decoder.streamInfo.sampleRate;
    }


    @Override
    public int getBitsPerSample() {
        return this.decoder.streamInfo.sampleDepth;
    }


    public int getBytesPerSample() {
        return this.bytesPerSample;
    }


    @Override
    public boolean isClosed() {
        return this.closed;
    }


    @Override
    public void close() throws IOException {
        try {
            this.decoder.close();
        } finally {
            this.closed = true;
        }
    }

}
