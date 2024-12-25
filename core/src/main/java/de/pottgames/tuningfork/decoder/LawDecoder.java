/**
 * Copyright 2023 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.badlogic.gdx.math.MathUtils;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class LawDecoder implements WavDecoder, AiffDecoder {
    private long           bytesRemaining;
    private final Encoding encoding;
    private final int      channels;
    private final int      sampleRate;
    private long           totalOutputSamplesPerChannel;
    private final boolean  bigEndian;

    private AudioInputStream inputStream;
    private AudioInputStream outputStream;


    public LawDecoder(int channels, int sampleRate, Encoding encoding, boolean bigEndian) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.encoding = encoding;
        this.bigEndian = bigEndian;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        bytesRemaining = streamLength;
        totalOutputSamplesPerChannel = streamLength / channels;

        // INPUT STREAM
        final AudioFormat.Encoding inputEncoding = encoding == Encoding.U_LAW ? AudioFormat.Encoding.ULAW : AudioFormat.Encoding.ALAW;
        final AudioFormat inputFormat = new AudioFormat(inputEncoding, sampleRate, 8, channels, channels, sampleRate, bigEndian);
        inputStream = new AudioInputStream(stream, inputFormat, totalOutputSamplesPerChannel);

        // OUTPUT STREAM
        final AudioFormat outputFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, channels, channels * 2, sampleRate, false);
        outputStream = AudioSystem.getAudioInputStream(outputFormat, inputStream);
    }


    @Override
    public int read(byte[] output) throws IOException {
        final int read = outputStream.read(output);
        if (read <= 0) {
            bytesRemaining = -1;
        } else {
            bytesRemaining = MathUtils.clamp(bytesRemaining - read, -1, Long.MAX_VALUE);
        }
        return read;
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
        return bytesRemaining * 2;
    }


    @Override
    public void close() throws IOException {
        inputStream.close();
    }


    public enum Encoding {
        U_LAW, A_LAW;
    }

}
