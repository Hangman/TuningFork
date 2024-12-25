package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.decoder.util.Util;

public class PcmDecoder implements WavDecoder {
    protected InputStream       stream;
    protected final PcmDataType pcmDataType;
    protected long              bytesRemaining;
    private final int           channels;
    private long                totalOutputSamplesPerChannel;
    private final int           sampleRate;
    private final int           bitsPerSample;


    public PcmDecoder(int bitsPerSample, int channels, int sampleRate, PcmDataType pcmDataType) {
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.pcmDataType = pcmDataType;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        bytesRemaining = streamLength;
        totalOutputSamplesPerChannel = bytesRemaining / (bitsPerSample / 8L) / channels;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance
        if (bytesRemaining <= 0) {
            return -1;
        }
        final int bytesRead = Util.readAll(stream, output, (int) Math.min(bytesRemaining, output.length));
        bytesRemaining -= bytesRead;
        return bytesRead;
    }


    @Override
    public int inputBitsPerSample() {
        return bitsPerSample;
    }


    @Override
    public int outputBitsPerSample() {
        return bitsPerSample;
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
        return pcmDataType;
    }


    @Override
    public long bytesRemaining() {
        return bytesRemaining;
    }


    @Override
    public void close() throws IOException, NullPointerException {
        stream.close();
    }

}
