package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class LawDecoder implements WavDecoder {
    private final Encoding encoding;
    private final int      channels;
    private final int      sampleRate;
    private long           totalOutputSamplesPerChannel;

    private AudioInputStream inputStream;
    private AudioInputStream outputStream;


    public LawDecoder(int channels, int sampleRate, Encoding encoding) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.encoding = encoding;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.totalOutputSamplesPerChannel = streamLength / this.channels;

        // INPUT STREAM
        final AudioFormat.Encoding inputEncoding = this.encoding == Encoding.U_LAW ? AudioFormat.Encoding.ULAW : AudioFormat.Encoding.ALAW;
        final AudioFormat inputFormat = new AudioFormat(inputEncoding, this.sampleRate, 8, this.channels, this.channels, this.sampleRate, false);
        this.inputStream = new AudioInputStream(stream, inputFormat, this.totalOutputSamplesPerChannel);

        // OUTPUT STREAM
        final AudioFormat outputFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, this.sampleRate, 16, this.channels, this.channels * 2,
                this.sampleRate, false);
        this.outputStream = AudioSystem.getAudioInputStream(outputFormat, this.inputStream);
    }


    @Override
    public int read(byte[] output) throws IOException {
        return this.outputStream.read(output);
    }


    @Override
    public int inputBitsPerSample() {
        return 8;
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
    public void close() throws IOException {
        this.inputStream.close();
    }


    public enum Encoding {
        U_LAW, A_LAW;
    }

}
