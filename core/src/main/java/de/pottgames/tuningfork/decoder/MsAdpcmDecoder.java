package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkRuntimeException;

public class MsAdpcmDecoder implements WavDecoder {
    private InputStream stream;
    private long        bytesRemaining;
    private final int   blockAlign;
    private final int   blockSize;


    public MsAdpcmDecoder(int blockSize, int channels) {
        if (channels < 1 || channels > 2) {
            throw new TuningForkRuntimeException("Unsupported number of channels: " + channels);
        }
        this.blockSize = blockSize;
        this.blockAlign = (blockSize / channels - 7) * 2 + 2;
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
        return 4;
    }


    @Override
    public int outputBitsPerSample() {
        return 4;
    }


    @Override
    public PcmDataType getPcmDataType() {
        return PcmDataType.MS_ADPCM;
    }


    @Override
    public int blockAlign() {
        return this.blockAlign;
    }


    public int blockSize() {
        return this.blockSize;
    }


    @Override
    public void close() throws IOException {
        this.stream.close();
    }
}
