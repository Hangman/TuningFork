package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.math.MathUtils;

public class Int24To16Resampler implements Resampler {
    private static final int    END_OF_STREAM            = Integer.MAX_VALUE;
    protected final InputStream stream;
    protected long              bytesRemaining;
    private int                 outputSample;
    private int                 outputSampleFetchedBytes = 2;


    public Int24To16Resampler(InputStream stream, long streamLength) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
    }


    @Override
    public int read(byte[] output) throws IOException {
        for (int i = 0; i < output.length; i++) {
            final int outputByte = this.fetchNextOutputByte();
            if (outputByte == Int24To16Resampler.END_OF_STREAM) {
                return i == 0 ? -1 : i;
            }
            output[i] = (byte) outputByte;
        }

        return output.length;
    }


    private int fetchNextOutputByte() throws IOException {
        if (this.outputSampleFetchedBytes >= 2) {
            this.outputSample = this.fetchNextOutputSample();
            if (this.outputSample == Int24To16Resampler.END_OF_STREAM) {
                return Int24To16Resampler.END_OF_STREAM;
            }
            this.outputSampleFetchedBytes = 0;
        }

        final int outputByte = this.outputSample >>> this.outputSampleFetchedBytes * 8 & 0xff;
        this.outputSampleFetchedBytes++;

        return outputByte;
    }


    private int fetchNextOutputSample() throws IOException {
        final int inputSample = this.fetchNextInputSample();
        if (inputSample == Int24To16Resampler.END_OF_STREAM) {
            return Int24To16Resampler.END_OF_STREAM;
        }

        final short outputSample = (short) (inputSample >>> 8);
        return outputSample;
    }


    private int fetchNextInputSample() throws IOException {
        if (this.bytesRemaining < 3) {
            return Int24To16Resampler.END_OF_STREAM;
        }

        final int byte1 = this.stream.read();
        final int byte2 = this.stream.read();
        final int byte3 = this.stream.read();
        if (byte1 < 0 || byte2 < 0 || byte3 < 0) {
            this.bytesRemaining = 0;
            return Int24To16Resampler.END_OF_STREAM;
        }
        this.bytesRemaining = MathUtils.clamp(this.bytesRemaining - 3, 0, Integer.MAX_VALUE);

        return byte1 | byte2 << 8 | byte3 << 16;
    }


    @Override
    public int inputBitsPerSample() {
        return 24;
    }


    @Override
    public int outputBitsPerSample() {
        return 16;
    }


    @Override
    public void close() throws IOException {
        this.stream.close();
    }

}