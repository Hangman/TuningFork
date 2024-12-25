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

package de.pottgames.tuningfork.decoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import io.nayuki.flac.decode.FlacDecoder;

/**
 * An {@link AudioStream} implementation to read flac files.
 *
 * @author Matthias
 */
public class FlacInputStream implements AudioStream {
    private final FlacDecoder decoder;
    private boolean           closed = false;
    private int[][]           sampleBuffer;
    private int               sampleBufferBlockSize;
    private int               bytesPerSample;
    private float             duration;
    private final FileHandle  fileHandle;


    /**
     * Initializes a {@link FlacInputStream} from a {@link File}.
     *
     * @param file the file
     */
    public FlacInputStream(File file) {
        this(new FileHandle(file));
    }


    /**
     * Initializes a {@link FlacInputStream} from an {@link InputStream}. This stream does not support the reset function. Use
     * {@link #FlacInputStream(FileHandle)} instead to get the full functionality.
     *
     * @param stream the input stream
     */
    public FlacInputStream(InputStream stream) {
        fileHandle = null;
        try {
            decoder = new FlacDecoder(stream);
            while (decoder.readAndHandleMetadataBlock() != null) {
                // read all meta data blocks
            }
        } catch (final IOException e) {
            StreamUtils.closeQuietly(this);
            throw new TuningForkRuntimeException(e);
        }

        initialize();
    }


    /**
     * Initializes a {@link FlacInputStream} from a {@link FileHandle}.
     *
     * @param file the file handle
     */
    public FlacInputStream(FileHandle file) {
        fileHandle = file;
        try {
            decoder = new FlacDecoder(file.read());
            while (decoder.readAndHandleMetadataBlock() != null) {
                // read all meta data blocks
            }
        } catch (final IOException e) {
            StreamUtils.closeQuietly(this);
            throw new TuningForkRuntimeException(e);
        }

        initialize();
    }


    private void initialize() {
        if (decoder.streamInfo == null) {
            throw new TuningForkRuntimeException("Missing StreamInfo in flac file.");
        }
        final int numChannels = decoder.streamInfo.numChannels;
        if (!PcmFormat.isSupportedChannelCount(numChannels)) {
            throw new TuningForkRuntimeException("Unsupported number of channels in flac file. Must be 1, 2, 4, 6, 7 or 8 but is: " + numChannels);
        }
        final int bitsPerSample = decoder.streamInfo.sampleDepth;
        if (bitsPerSample != 8 && bitsPerSample != 16) {
            throw new TuningForkRuntimeException("Unsupported bits per sample in flac file, only 8 and 16 Bit is supported.");
        }
        if (decoder.streamInfo.maxBlockSize > StreamedSoundSource.BUFFER_SIZE_PER_CHANNEL * numChannels) {
            throw new TuningForkRuntimeException(
                    "Flac file exceeds maximum supported block size by TuningFork which is: " + StreamedSoundSource.BUFFER_SIZE_PER_CHANNEL + " per channel");
        }

        sampleBuffer = new int[decoder.streamInfo.numChannels][65536];
        bytesPerSample = decoder.streamInfo.sampleDepth / 8;

        readBlock();

        duration = (float) totalSamples() / getSampleRate();
    }


    @Override
    public float getDuration() {
        return duration;
    }


    @Override
    public AudioStream reset() {
        if (fileHandle == null) {
            throw new TuningForkRuntimeException("This AudioStream doesn't support resetting.");
        }
        StreamUtils.closeQuietly(this);
        return new FlacInputStream(fileHandle);
    }


    @Override
    public int read(byte[] bytes) {
        if (sampleBufferBlockSize == 0) {
            return -1;
        }

        int availableBytes = bytes.length;
        int bytesIndex = 0;

        while (availableBytes >= sampleBufferBlockSize * decoder.streamInfo.numChannels * bytesPerSample) {
            for (int i = 0; i < sampleBufferBlockSize; i++) {
                for (int channelIndex = 0; channelIndex < decoder.streamInfo.numChannels; channelIndex++) {
                    int sample = sampleBuffer[channelIndex][i];
                    if (bytesPerSample == 1) {
                        sample += 128; // because OpenAL expects an unsigned byte
                    }
                    for (int j = 0; j < bytesPerSample; j++, bytesIndex++) {
                        bytes[bytesIndex] = (byte) (sample >>> (j << 3));
                    }
                }
            }

            availableBytes -= sampleBufferBlockSize * decoder.streamInfo.numChannels * bytesPerSample;
            readBlock();
            if (sampleBufferBlockSize == 0) {
                break;
            }
        }

        return bytes.length - availableBytes;
    }


    private void readBlock() {
        try {
            sampleBufferBlockSize = decoder.readAudioBlock(sampleBuffer, 0);
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    public long totalSamples() {
        return decoder.streamInfo.numSamples;
    }


    @Override
    public int getChannels() {
        return decoder.streamInfo.numChannels;
    }


    @Override
    public int getSampleRate() {
        return decoder.streamInfo.sampleRate;
    }


    @Override
    public int getBitsPerSample() {
        return decoder.streamInfo.sampleDepth;
    }


    public int getBytesPerSample() {
        return bytesPerSample;
    }


    @Override
    public PcmDataType getPcmDataType() {
        return PcmDataType.INTEGER;
    }


    @Override
    public boolean isClosed() {
        return closed;
    }


    @Override
    public void close() throws IOException {
        try {
            decoder.close();
        } finally {
            closed = true;
        }
    }

}
