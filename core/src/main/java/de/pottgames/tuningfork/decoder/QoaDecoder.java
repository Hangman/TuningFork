/**
 * Copyright 2024 Matthias Finke
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

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkException;
import de.pottgames.tuningfork.decoder.util.Util;

public class QoaDecoder {
    private static final int DEQUANT_LUT[][] = { { 1, -1, 3, -3, 5, -5, 7, -7 }, { 5, -5, 18, -18, 32, -32, 49, -49 }, { 16, -16, 53, -53, 95, -95, 147, -147 },
            { 34, -34, 113, -113, 203, -203, 315, -315 }, { 63, -63, 210, -210, 378, -378, 588, -588 }, { 104, -104, 345, -345, 621, -621, 966, -966 },
            { 158, -158, 528, -528, 950, -950, 1477, -1477 }, { 228, -228, 760, -760, 1368, -1368, 2128, -2128 },
            { 316, -316, 1053, -1053, 1895, -1895, 2947, -2947 }, { 422, -422, 1405, -1405, 2529, -2529, 3934, -3934 },
            { 548, -548, 1828, -1828, 3290, -3290, 5117, -5117 }, { 696, -696, 2320, -2320, 4176, -4176, 6496, -6496 },
            { 868, -868, 2893, -2893, 5207, -5207, 8099, -8099 }, { 1064, -1064, 3548, -3548, 6386, -6386, 9933, -9933 },
            { 1286, -1286, 4288, -4288, 7718, -7718, 12005, -12005 }, { 1536, -1536, 5120, -5120, 9216, -9216, 14336, -14336 }, };

    protected InputStream    stream;
    private final int        channels;
    private long             totalOutputSamplesPerChannel;
    private final int        sampleRate;
    private FrameHeader      currentFrameHeader = new FrameHeader();
    private byte[]           rawFrameData       = null;
    private final LmsState[] lmsStates          = { new LmsState(), new LmsState(), new LmsState(), new LmsState(), new LmsState(), new LmsState(),
            new LmsState(), new LmsState() };
    private short[]          buffer;
    private int              bufferCursor       = 0;
    private int              bufferLength       = 0;


    public QoaDecoder(InputStream stream, long samplesPerChannel) throws IOException, TuningForkException {
        this.stream = stream;
        this.totalOutputSamplesPerChannel = samplesPerChannel;

        // decode first frame to know channels & sample rate
        this.decodeFrame(true);
        this.channels = this.currentFrameHeader.channels();
        this.sampleRate = this.currentFrameHeader.sampleRate();
    }


    public int read(byte[] output) throws IOException, TuningForkException {
        boolean nextSample = false;
        int writtenBytes = 0;
        for (int i = 0; i < output.length; i++) {
            if (this.bufferCursor >= this.bufferLength) {
                this.decodeFrame(false);
                if (this.bufferLength <= 0) {
                    return writtenBytes;
                }
            }

            if (nextSample) {
                output[i] = (byte) (this.buffer[this.bufferCursor++] >>> 8 & 0xFF);
            } else {
                output[i] = (byte) (this.buffer[this.bufferCursor] & 0xFF);
            }
            nextSample = !nextSample;
            writtenBytes++;
        }

        return writtenBytes;
    }


    private void decodeFrame(boolean firstFrame) throws IOException, TuningForkException {
        this.bufferCursor = 0;
        Util.readAll(this.stream, this.currentFrameHeader.data, 8);
        final int channels = this.currentFrameHeader.channels();
        final int samples = this.currentFrameHeader.samplesPerChannel();
        final int frameSize = this.currentFrameHeader.frameSize();

        // Here it would be nice to check whether this frame still has the same sample rate and number of channels,
        // but we are optimizing for performance, not data validity.

        final int sliceDataSize = frameSize - 8 - 4 * 4 * channels;
        final int numSlices = sliceDataSize / 8;
        final int totalSamples = numSlices * 20;

        final int dataSize = this.currentFrameHeader.frameSize() - 8;
        if (firstFrame) {
            // 16 bytes lms * 8 channels max + 8 bytes per slice * 256 slices max * 8 channels max
            this.rawFrameData = new byte[16 * channels + 8 * 256 * channels];
            this.buffer = new short[256 * 20 * channels];
        }

        Util.readAll(this.stream, this.rawFrameData, dataSize);

        int cursor = 0;
        for (int channel = 0; channel < channels; channel++) {
            this.lmsStates[channel].history[0] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
            this.lmsStates[channel].history[1] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
            this.lmsStates[channel].history[2] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
            this.lmsStates[channel].history[3] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
            this.lmsStates[channel].weights[0] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
            this.lmsStates[channel].weights[1] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
            this.lmsStates[channel].weights[2] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
            this.lmsStates[channel].weights[3] = (short) ((this.rawFrameData[cursor++] & 0xFF) << 8 | this.rawFrameData[cursor++] & 0xFF);
        }

        for (int sampleIndex = 0; sampleIndex < samples; sampleIndex += 20) {
            final int sliceStartBase = sampleIndex * channels;
            for (int channel = 0; channel < channels; channel++) {
                long slice = Util.longOfBigEndianBytes(this.rawFrameData, cursor);
                cursor += 8;

                final int scaleFactor = (int) (slice >> 60 & 0xF);
                final int sliceStart = sliceStartBase + channel;
                final int sliceEnd = Util.limit(sampleIndex + 20, samples) * channels + channel;

                for (int si = sliceStart; si < sliceEnd; si += channels) {
                    final int predicted = this.lmsStates[channel].predict();
                    final int quantizedResidual = (int) (slice >> 57 & 0x7);
                    final int residual = QoaDecoder.DEQUANT_LUT[scaleFactor][quantizedResidual];
                    final int unclamped_sample = predicted + residual;

                    // A faster clamp:
                    // The first if statement here checks for both branches (v < -32768 and v > 32767) simultaneously and is very rarely taken. This helps the
                    // CPU's branch predictor to correctly predict and skip the branch in the vast majority of cases.
                    int clampedSample = unclamped_sample;
                    if (unclamped_sample + 32768 > 65535) {
                        if (unclamped_sample < -32768) {
                            clampedSample = -32768;
                        }
                        if (unclamped_sample > 32767) {
                            clampedSample = 32767;
                        }
                    }

                    final short decodedSample = (short) clampedSample;
                    this.lmsStates[channel].update(decodedSample, residual);
                    this.buffer[si] = decodedSample;
                    slice <<= 3;
                }
            }
        }

        this.bufferLength = totalSamples;
    }


    public int outputBitsPerSample() {
        return 16;
    }


    public int outputChannels() {
        return this.channels;
    }


    public int outputSampleRate() {
        return this.sampleRate;
    }


    public long outputTotalSamplesPerChannel() {
        return this.totalOutputSamplesPerChannel;
    }


    public PcmDataType outputPcmDataType() {
        return PcmDataType.INTEGER;
    }


    public void close() throws IOException, NullPointerException {
        this.stream.close();
    }


    private static class LmsState {
        private final short[] history = new short[4];
        private final short[] weights = new short[4];


        private int predict() {
            int prediction = 0;
            prediction += this.weights[0] * this.history[0];
            prediction += this.weights[1] * this.history[1];
            prediction += this.weights[2] * this.history[2];
            prediction += this.weights[3] * this.history[3];
            return prediction >> 13;
        }


        private void update(short sample, int residual) {
            final int delta = residual >> 4;
            this.weights[0] += this.history[0] < 0 ? -delta : delta;
            this.weights[1] += this.history[1] < 0 ? -delta : delta;
            this.weights[2] += this.history[2] < 0 ? -delta : delta;
            this.weights[3] += this.history[3] < 0 ? -delta : delta;

            this.history[0] = this.history[1];
            this.history[1] = this.history[2];
            this.history[2] = this.history[3];
            this.history[3] = sample;
        }
    }


    private static class FrameHeader {
        private final byte[] data = new byte[8];


        private int channels() {
            return this.data[0] & 0xFF;
        }


        private int sampleRate() {
            return (this.data[1] & 0xFF) << 16 | (this.data[2] & 0xFF) << 8 | this.data[3] & 0xFF;
        }


        private int samplesPerChannel() {
            return (this.data[4] & 0xFF) << 8 | this.data[5] & 0xFF;
        }


        private int frameSize() {
            return (this.data[6] & 0xFF) << 8 | this.data[7] & 0xFF;
        }
    }

}
