package de.pottgames.tuningfork;

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
        try {
            this.decoder = new FlacDecoder(file.file());
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
        if (numChannels < 0 || numChannels > 2) {
            throw new TuningForkRuntimeException("Unsupported number of channels in flac file. Must be 1 or 2 but is: " + numChannels);
        }
        final int bitsPerSample = this.decoder.streamInfo.sampleDepth;
        if (bitsPerSample != 8 && bitsPerSample != 16) {
            throw new TuningForkRuntimeException("Unsupported bits per sample in flac file, only 8 and 16 Bit is supported.");
        }
        if (this.decoder.streamInfo.maxBlockSize > StreamedSoundSource.BUFFER_SIZE) {
            throw new TuningForkRuntimeException("Flac file exceeds maximum supported block size by TuningFork which is: " + StreamedSoundSource.BUFFER_SIZE);
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
                    final int sample = this.sampleBuffer[channelIndex][i];
                    for (int j = 0; j < this.bytesPerSample; j++) {
                        bytes[bytesIndex] = (byte) (sample >>> (j << 3));
                        bytesIndex++;
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
