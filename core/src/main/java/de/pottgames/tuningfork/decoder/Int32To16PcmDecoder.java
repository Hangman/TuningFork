package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class Int32To16PcmDecoder implements WavDecoder {
    private static final long END_OF_STREAM            = Long.MAX_VALUE;
    protected InputStream     stream;
    protected long            bytesRemaining;
    private final int         channels;
    private final int         sampleRate;
    private long              outputSample;
    private long              totalOutputSamplesPerChannel;
    private int               outputSampleFetchedBytes = 2;


    public Int32To16PcmDecoder(int channels, int sampleRate) {
        this.channels = channels;
        this.sampleRate = sampleRate;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
        this.totalOutputSamplesPerChannel = this.bytesRemaining / 4L / this.channels;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance

        for (int i = 0; i < output.length; i++) {
            final long outputByte = this.fetchNextOutputByte();
            if (outputByte == Int32To16PcmDecoder.END_OF_STREAM) {
                return i == 0 ? -1 : i;
            }
            output[i] = (byte) outputByte;
        }

        return output.length;
    }


    private long fetchNextOutputByte() throws IOException {
        if (this.outputSampleFetchedBytes >= 2) {
            this.outputSample = this.fetchNextOutputSample();
            if (this.outputSample == Int32To16PcmDecoder.END_OF_STREAM) {
                return Int32To16PcmDecoder.END_OF_STREAM;
            }
            this.outputSampleFetchedBytes = 0;
        }

        final long outputByte = this.outputSample >>> this.outputSampleFetchedBytes * 8 & 0xffL;
        this.outputSampleFetchedBytes++;

        return outputByte;
    }


    private long fetchNextOutputSample() throws IOException {
        final long inputSample = this.fetchNextInputSample();
        if (inputSample == Int32To16PcmDecoder.END_OF_STREAM) {
            return Int32To16PcmDecoder.END_OF_STREAM;
        }

        return inputSample >>> 16;
    }


    private long fetchNextInputSample() throws IOException {
        if (this.bytesRemaining < 3) {
            return Int32To16PcmDecoder.END_OF_STREAM;
        }

        final int byte1 = this.stream.read();
        final int byte2 = this.stream.read();
        final int byte3 = this.stream.read();
        final int byte4 = this.stream.read();
        if (byte4 < 0) {
            this.bytesRemaining = 0;
            return Int32To16PcmDecoder.END_OF_STREAM;
        }
        this.bytesRemaining -= 4;
        if (this.bytesRemaining < 0) {
            this.bytesRemaining = 0;
        }

        return byte1 | (long) byte2 << 8 | (long) byte3 << 16 | (long) byte4 << 24;
    }


    @Override
    public int inputBitsPerSample() {
        return 32;
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
        return this.bytesRemaining / 2L;
    }


    @Override
    public void close() throws IOException {
        this.stream.close();
    }

}
