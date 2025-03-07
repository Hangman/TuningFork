package de.pottgames.tuningfork.decoder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.math.MathUtils;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class ImaAdpcmDecoder implements WavDecoder {
    private static final int   END_OF_STREAM = Integer.MAX_VALUE;
    private final int          blockSize;
    private InputStream        stream;
    private final int          channels;
    private final boolean      stereo;
    private long               bytesRemaining;
    private final byte[]       outputSamples;
    private int                outputSamplePosition;
    private int                outputSampleSize;
    private final int          sampleRate;
    private long               totalOutputSamplesPerChannel;
    private long               outputBytesRemaining;
    private final Prediction[] prediction    = new Prediction[2];


    public ImaAdpcmDecoder(int blockSize, int channels, int sampleRate) {
        assert channels == 1 || channels == 2;
        this.channels = channels;
        stereo = channels == 2;
        this.blockSize = blockSize;
        this.sampleRate = sampleRate;
        outputSamples = new byte[blockSize * 4 - 4 * channels];
        prediction[0] = new Prediction();
        prediction[1] = new Prediction();
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        bytesRemaining = streamLength;
        this.stream = new BufferedInputStream(stream, blockSize * 2);

        // CALC TOTAL SAMPLES
        long numberOfBlocks = streamLength / blockSize;
        if (streamLength % blockSize > 0) {
            numberOfBlocks++;
        }
        final long blockBytes = numberOfBlocks * 4L * channels;
        totalOutputSamplesPerChannel = (streamLength * 2L - blockBytes * 2L) / channels;
        outputBytesRemaining = totalOutputSamplesPerChannel * channels * 2;
    }


    @Override
    public int read(byte[] output) throws IOException {
        for (int i = 0; i < output.length; i++) {
            // DECODE NEXT BLOCK IF NECESSARY
            if (outputSamplePosition >= outputSampleSize) {
                if (decodeNextBlock() == ImaAdpcmDecoder.END_OF_STREAM) {
                    return i > 0 ? i : -1;
                }
            }

            // COPY OUTPUT BYTE
            output[i] = outputSamples[outputSamplePosition++];
        }

        outputBytesRemaining -= output.length;

        return output.length;
    }


    private int decodeNextBlock() throws IOException {
        int preambleBytes = 0;

        // READ LEFT CHANNEL PREAMBLE
        prediction[0].predictor = (short) (readByte() | readByte() << 8);
        prediction[0].stepIndex = MathUtils.clamp(readByte(), 0, 88);
        prediction[0].step = Prediction.STEP_TABLE[prediction[0].stepIndex];
        final int skipValue = readByte();
        if (skipValue < 0) {
            return ImaAdpcmDecoder.END_OF_STREAM;
        }
        preambleBytes += 4;

        // READ RIGHT CHANNEL PREAMBLE
        if (stereo) {
            prediction[1].predictor = (short) (readByte() | readByte() << 8);
            prediction[1].stepIndex = MathUtils.clamp(readByte(), 0, 88);
            prediction[1].step = Prediction.STEP_TABLE[prediction[1].stepIndex];
            final int skipValue2 = readByte();
            if (skipValue2 < 0 || bytesRemaining < 0) {
                return ImaAdpcmDecoder.END_OF_STREAM;
            }
            preambleBytes += 4;
        }

        // DECODE BLOCK
        outputSamplePosition = 0;
        int outputSamples = 0;
        int byteChannelCounter = 0;
        int predictionIndex = 0;
        int outputSamplesIndexLeft = 0;
        int outputSamplesIndexRight = 2;
        final int outputSampleStep = stereo ? 3 : 1;
        for (int i = 0; i < blockSize - preambleBytes; i++) {

            // READ INPUT BYTE
            final int inputByte = readByte();
            if (inputByte < 0 || bytesRemaining < 0) {
                outputSampleSize = outputSamples * 2;
                return ImaAdpcmDecoder.END_OF_STREAM;
            }

            // DECODE
            final int nibble1 = inputByte & 0b1111;
            final int nibble2 = inputByte >>> 4;
            final int sample1 = decodeNibble(nibble1, prediction[predictionIndex]);
            final int sample2 = decodeNibble(nibble2, prediction[predictionIndex]);
            final byte outputSample1Byte1 = (byte) sample1;
            final byte outputSample1Byte2 = (byte) (sample1 >>> 8);
            final byte outputSample2Byte1 = (byte) sample2;
            final byte outputSample2Byte2 = (byte) (sample2 >>> 8);

            // WRITE TO OUTPUT SAMPLE CACHE
            if (predictionIndex == 0) {
                this.outputSamples[outputSamplesIndexLeft] = outputSample1Byte1;
                outputSamplesIndexLeft++;
                this.outputSamples[outputSamplesIndexLeft] = outputSample1Byte2;
                outputSamplesIndexLeft += outputSampleStep;
                this.outputSamples[outputSamplesIndexLeft] = outputSample2Byte1;
                outputSamplesIndexLeft++;
                this.outputSamples[outputSamplesIndexLeft] = outputSample2Byte2;
                outputSamplesIndexLeft += outputSampleStep;
            } else {
                this.outputSamples[outputSamplesIndexRight] = outputSample1Byte1;
                outputSamplesIndexRight++;
                this.outputSamples[outputSamplesIndexRight] = outputSample1Byte2;
                outputSamplesIndexRight += outputSampleStep;
                this.outputSamples[outputSamplesIndexRight] = outputSample2Byte1;
                outputSamplesIndexRight++;
                this.outputSamples[outputSamplesIndexRight] = outputSample2Byte2;
                outputSamplesIndexRight += outputSampleStep;
            }
            outputSamples += 2;

            // SETUP PREDICTION FOR NEXT BYTE
            if (stereo) {
                byteChannelCounter += 1;
                if (byteChannelCounter >= 4) {
                    byteChannelCounter = 0;
                    predictionIndex = 1 - predictionIndex;
                }
            }

        }

        outputSampleSize = outputSamples * 2;
        return outputSampleSize;
    }


    private int decodeNibble(int nibble, Prediction prediction) {
        final int sign = nibble & 0b1000;
        final int delta = nibble & 0b111;

        prediction.stepIndex += Prediction.INDEX_TABLE[nibble];
        prediction.stepIndex = MathUtils.clamp(prediction.stepIndex, 0, 88);

        int diff = prediction.step >>> 3;
        if ((delta & 4) != 0) {
            diff += prediction.step;
        }
        if ((delta & 2) != 0) {
            diff += prediction.step >>> 1;
        }
        if ((delta & 1) != 0) {
            diff += prediction.step >>> 2;
        }
        if (sign != 0) {
            prediction.predictor -= (short) diff;
        } else {
            prediction.predictor += (short) diff;
        }
        prediction.predictor = MathUtils.clamp(prediction.predictor, Short.MIN_VALUE, Short.MAX_VALUE);

        prediction.step = Prediction.STEP_TABLE[prediction.stepIndex];

        return prediction.predictor & 0xFFFF;
    }


    private int readByte() throws IOException {
        bytesRemaining -= 1;
        return stream.read();
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
        return outputBytesRemaining;
    }


    @Override
    public void close() throws IOException {
        stream.close();
    }


    private static class Prediction {
        private static final int[] INDEX_TABLE = { -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8 };
        private static final int[] STEP_TABLE  = { 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97,
                107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411,
                1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635,
                13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767 };
        private short              predictor;
        private int                stepIndex;
        private int                step;
    }

}
