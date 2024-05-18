package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.bindings.ImaAdpcmRs;
import de.pottgames.tuningfork.decoder.util.Util;

public class ImaAdpcmRsDecoder implements WavDecoder {
    private InputStream stream;
    private final int   blockSize;
    private final int   channels;
    private final int   sampleRate;
    private long        totalOutputSamplesPerChannel;
    private long        bytesRemaining = -1L;
    private long        streamLength;
    private byte[]      audioData;
    private int         readIndex;


    public ImaAdpcmRsDecoder(int blockSize, int channels, int sampleRate) {
        assert channels == 1 || channels == 2;
        this.channels = channels;
        this.blockSize = blockSize;
        this.sampleRate = sampleRate;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.streamLength = streamLength;
        this.stream = stream;
        long numberOfBlocks = streamLength / this.blockSize;
        if (streamLength % this.blockSize > 0) {
            numberOfBlocks++;
        }
        final long blockBytes = numberOfBlocks * 4L * this.channels;
        this.totalOutputSamplesPerChannel = (streamLength * 2L - blockBytes * 2L) / this.channels;

        try {
            this.decode();
        } catch (final IOException e) {
            throw new TuningForkRuntimeException("Error decoding IMA ADPCM data", e);
        }
    }


    private void decode() throws IOException {
        final ImaAdpcmRs nativeDecoder = new ImaAdpcmRs();
        final byte[] data = new byte[(int) this.streamLength];
        Util.readAll(this.stream, data, data.length);
        this.audioData = nativeDecoder.decode(data, this.blockSize, this.channels == 2);
        this.bytesRemaining = this.audioData.length;
        this.readIndex = 0;
    }


    @Override
    public int read(byte[] output) throws IOException {
        final int copiedBytes = (int) Math.min(this.bytesRemaining, output.length);
        if (copiedBytes <= 0) {
            return -1;
        }
        System.arraycopy(this.audioData, this.readIndex, output, 0, copiedBytes);
        this.bytesRemaining -= copiedBytes;
        return copiedBytes;
    }


    @Override
    public int inputBitsPerSample() {
        return 4;
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
        return this.bytesRemaining;
    }


    @Override
    public void close() throws IOException {
        this.stream.close();
    }

}
