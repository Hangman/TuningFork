package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

public class ForwardResampler implements Resampler {
    protected final InputStream stream;
    protected long              bytesRemaining;
    private final int           bitsPerSample;


    public ForwardResampler(InputStream stream, long streamLength, int bitsPerSample) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
        this.bitsPerSample = bitsPerSample;
    }


    @Override
    public int read(byte[] output) throws IOException {
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
    public void close() throws IOException {
        this.stream.close();
    }

}
