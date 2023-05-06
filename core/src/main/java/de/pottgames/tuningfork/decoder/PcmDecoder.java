package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class PcmDecoder implements WavDecoder {
    protected InputStream       stream;
    protected final PcmDataType pcmDataType;
    protected long              bytesRemaining;
    private final int           bitsPerSample;


    public PcmDecoder(int bitsPerSample, PcmDataType pcmDataType) {
        this.bitsPerSample = bitsPerSample;
        this.pcmDataType = pcmDataType;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
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
        return this.bitsPerSample;
    }


    @Override
    public int outputBitsPerSample() {
        return this.bitsPerSample;
    }


    @Override
    public PcmDataType getPcmDataType() {
        return this.pcmDataType;
    }


    @Override
    public int blockAlign() {
        return -1;
    }


    @Override
    public int blockSize() {
        return -1;
    }


    @Override
    public void close() throws IOException, NullPointerException {
        this.stream.close();
    }

}
