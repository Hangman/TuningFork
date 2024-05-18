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
        this.bytesRemaining = streamLength;
        this.totalOutputSamplesPerChannel = streamLength / this.channels;

        // INPUT STREAM
        final AudioFormat.Encoding inputEncoding = this.encoding == Encoding.U_LAW ? AudioFormat.Encoding.ULAW : AudioFormat.Encoding.ALAW;
        final AudioFormat inputFormat = new AudioFormat(inputEncoding, this.sampleRate, 8, this.channels, this.channels, this.sampleRate, this.bigEndian);
        this.inputStream = new AudioInputStream(stream, inputFormat, this.totalOutputSamplesPerChannel);

        // OUTPUT STREAM
        final AudioFormat outputFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, this.sampleRate, 16, this.channels, this.channels * 2,
                this.sampleRate, false);
        this.outputStream = AudioSystem.getAudioInputStream(outputFormat, this.inputStream);
    }


    @Override
    public int read(byte[] output) throws IOException {
        final int read = this.outputStream.read(output);
        if (read <= 0) {
            this.bytesRemaining = -1;
        } else {
            this.bytesRemaining = MathUtils.clamp(this.bytesRemaining - read, -1, Long.MAX_VALUE);
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
        return this.bytesRemaining * 2;
    }


    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }


    public enum Encoding {
        U_LAW, A_LAW;
    }

}
