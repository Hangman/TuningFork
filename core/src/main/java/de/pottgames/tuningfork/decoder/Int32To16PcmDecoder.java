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
        bytesRemaining = streamLength;
        totalOutputSamplesPerChannel = bytesRemaining / 4L / channels;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance

        for (int i = 0; i < output.length; i++) {
            final long outputByte = fetchNextOutputByte();
            if (outputByte == Int32To16PcmDecoder.END_OF_STREAM) {
                return i == 0 ? -1 : i;
            }
            output[i] = (byte) outputByte;
        }

        return output.length;
    }


    private long fetchNextOutputByte() throws IOException {
        if (outputSampleFetchedBytes >= 2) {
            outputSample = fetchNextOutputSample();
            if (outputSample == Int32To16PcmDecoder.END_OF_STREAM) {
                return Int32To16PcmDecoder.END_OF_STREAM;
            }
            outputSampleFetchedBytes = 0;
        }

        final long outputByte = outputSample >>> outputSampleFetchedBytes * 8 & 0xffL;
        outputSampleFetchedBytes++;

        return outputByte;
    }


    private long fetchNextOutputSample() throws IOException {
        final long inputSample = fetchNextInputSample();
        if (inputSample == Int32To16PcmDecoder.END_OF_STREAM) {
            return Int32To16PcmDecoder.END_OF_STREAM;
        }

        return inputSample >>> 16;
    }


    private long fetchNextInputSample() throws IOException {
        if (bytesRemaining < 3) {
            return Int32To16PcmDecoder.END_OF_STREAM;
        }

        final int byte1 = stream.read();
        final int byte2 = stream.read();
        final int byte3 = stream.read();
        final int byte4 = stream.read();
        if (byte4 < 0) {
            bytesRemaining = 0;
            return Int32To16PcmDecoder.END_OF_STREAM;
        }
        bytesRemaining -= 4;
        if (bytesRemaining < 0) {
            bytesRemaining = 0;
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
        return channels;
    }


    @Override
    public int outputSampleRate() {
        return sampleRate;
    }


    @Override
    public long outputTotalSamplesPerChannel() {
        return totalOutputSamplesPerChannel;
    }


    @Override
    public PcmDataType outputPcmDataType() {
        return PcmDataType.INTEGER;
    }


    @Override
    public long bytesRemaining() {
        return bytesRemaining / 2L;
    }


    @Override
    public void close() throws IOException {
        stream.close();
    }

}
