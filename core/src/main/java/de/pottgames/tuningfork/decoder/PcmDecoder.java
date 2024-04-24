package de.pottgames.tuningfork.decoder;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.decoder.util.Util;

import java.io.IOException;
import java.io.InputStream;

public class PcmDecoder implements WavDecoder {
    protected       InputStream stream;
    protected final PcmDataType pcmDataType;
    protected       long        bytesRemaining;
    private final   int         channels;
    private         long        totalOutputSamplesPerChannel;
    private final   int         sampleRate;
    private final   int         bitsPerSample;


    public PcmDecoder(int bitsPerSample, int channels, int sampleRate, PcmDataType pcmDataType) {
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.pcmDataType = pcmDataType;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
        this.totalOutputSamplesPerChannel = this.bytesRemaining / (this.bitsPerSample / 8L) / this.channels;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance
        if (this.bytesRemaining <= 0) {
            return -1;
        }
        final int bytesRead = Util.readAll(this.stream, output, (int) Math.min(this.bytesRemaining, output.length));
        this.bytesRemaining -= bytesRead;
        return bytesRead;
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
        return this.pcmDataType;
    }


    @Override
    public long bytesRemaining() {
        return this.bytesRemaining;
    }


    @Override
    public void close() throws IOException, NullPointerException {
        this.stream.close();
    }

}
