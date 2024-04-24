package de.pottgames.tuningfork.decoder;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

import java.io.IOException;
import java.io.InputStream;

public class Int24To16PcmDecoder implements WavDecoder {
    private static final int         END_OF_STREAM            = Integer.MAX_VALUE;
    protected            InputStream stream;
    protected            long        bytesRemaining;
    private final        int         channels;
    private final        int         sampleRate;
    private              int         outputSample;
    private              long        totalOutputSamplesPerChannel;
    private              int         outputSampleFetchedBytes = 2;


    public Int24To16PcmDecoder(int channels, int sampleRate) {
        this.channels = channels;
        this.sampleRate = sampleRate;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
        this.totalOutputSamplesPerChannel = this.bytesRemaining / 3L / this.channels;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance

        for (int i = 0; i < output.length; i++) {
            final int outputByte = this.fetchNextOutputByte();
            if (outputByte == Int24To16PcmDecoder.END_OF_STREAM) {
                return i == 0 ? -1 : i;
            }
            output[i] = (byte) outputByte;
        }

        return output.length;
    }


    private int fetchNextOutputByte() throws IOException {
        if (this.outputSampleFetchedBytes >= 2) {
            this.outputSample = this.fetchNextOutputSample();
            if (this.outputSample == Int24To16PcmDecoder.END_OF_STREAM) {
                return Int24To16PcmDecoder.END_OF_STREAM;
            }
            this.outputSampleFetchedBytes = 0;
        }

        final int outputByte = this.outputSample >>> this.outputSampleFetchedBytes * 8 & 0xff;
        this.outputSampleFetchedBytes++;

        return outputByte;
    }


    private int fetchNextOutputSample() throws IOException {
        final int inputSample = this.fetchNextInputSample();
        if (inputSample == Int24To16PcmDecoder.END_OF_STREAM) {
            return Int24To16PcmDecoder.END_OF_STREAM;
        }

        return (short) (inputSample >>> 8);
    }


    private int fetchNextInputSample() throws IOException {
        if (this.bytesRemaining < 3) {
            return Int24To16PcmDecoder.END_OF_STREAM;
        }

        final int byte1 = this.stream.read();
        final int byte2 = this.stream.read();
        final int byte3 = this.stream.read();
        if (byte3 < 0) {
            this.bytesRemaining = 0;
            return Int24To16PcmDecoder.END_OF_STREAM;
        }
        this.bytesRemaining -= 3;
        if (this.bytesRemaining < 0) {
            this.bytesRemaining = 0;
        }

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
    public int outputChannels() {
        return this.channels;
    }


    @Override
    public int outputSampleRate() {
        return this.sampleRate;
    }


    @Override
    public long outputTotalSamplesPerChannel() {
        return this.totalOutputSamplesPerChannel;
    }


    @Override
    public PcmDataType outputPcmDataType() {
        return PcmDataType.INTEGER;
    }


    @Override
    public long bytesRemaining() {
        return (long) (this.bytesRemaining / 3f * 2f);
    }


    @Override
    public void close() throws IOException, NullPointerException {
        this.stream.close();
    }

}
