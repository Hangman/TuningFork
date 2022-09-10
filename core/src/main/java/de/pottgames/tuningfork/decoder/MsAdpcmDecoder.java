package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.math.MathUtils;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class MsAdpcmDecoder implements WavDecoder {
    private static final int END_OF_STREAM = Integer.MAX_VALUE;
    private final int        blockSize;
    private InputStream      stream;
    private final boolean    stereo;
    private long             bytesRemaining;
    private final byte       outputSamples[];
    private int              outputSamplePosition;
    private int              outputSampleSize;
    private final Prediction prediction    = new Prediction();


    public MsAdpcmDecoder(int blockSize, int channels) {
        assert channels == 1 || channels == 2;
        this.stereo = channels == 2;
        this.blockSize = blockSize;
        this.outputSamples = new byte[blockSize * 4 - 4 * channels];
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        this.bytesRemaining = streamLength;
    }


    @Override
    public int read(byte[] output) throws IOException {
        for (int i = 0; i < output.length; i++) {
            // DECODE NEXT BLOCK IF NECESSARY
            if (this.outputSamplePosition >= this.outputSampleSize) {
                if (this.decodeNextBlock() == MsAdpcmDecoder.END_OF_STREAM) {
                    return i > 0 ? i : -1;
                }
            }

            // COPY OUTPUT BYTE
            output[i] = this.outputSamples[this.outputSamplePosition++];
        }

        return output.length;
    }


    private int decodeNextBlock() throws IOException {
        // READ PREAMBLE
        final int leftCoeff1;
        int rightCoeff1 = 0;
        final int leftCoeff2;
        int rightCoeff2 = 0;
        int leftDelta;
        int rightDelta = 0;
        if (this.stereo) {
            final int leftBlockPredictor = this.readByte();
            if (leftBlockPredictor < 0 || this.bytesRemaining <= 0) {
                return MsAdpcmDecoder.END_OF_STREAM;
            }
            final int rightBlockPredictor = this.readByte();
            if (rightBlockPredictor < 0 || this.bytesRemaining <= 0) {
                return MsAdpcmDecoder.END_OF_STREAM;
            }
            leftCoeff1 = Prediction.COEFF1_TABLE[leftBlockPredictor];
            rightCoeff1 = Prediction.COEFF1_TABLE[rightBlockPredictor];
            leftCoeff2 = Prediction.COEFF2_TABLE[leftBlockPredictor];
            rightCoeff2 = Prediction.COEFF2_TABLE[rightBlockPredictor];
            leftDelta = this.readByte() | this.readByte() << 8;
            rightDelta = this.readByte() | this.readByte() << 8;
            this.prediction.leftSample1 = this.readByte() | this.readByte() << 8;
            this.prediction.rightSample1 = this.readByte() | this.readByte() << 8;
            this.prediction.leftSample2 = this.readByte() | this.readByte() << 8;
            this.prediction.rightSample2 = this.readByte() | this.readByte() << 8;
            this.outputSamples[0] = (byte) this.prediction.leftSample2;
            this.outputSamples[1] = (byte) (this.prediction.leftSample2 >>> 8);
            this.outputSamples[2] = (byte) this.prediction.rightSample2;
            this.outputSamples[3] = (byte) (this.prediction.rightSample2 >>> 8);
            this.outputSamples[4] = (byte) this.prediction.leftSample1;
            this.outputSamples[5] = (byte) (this.prediction.leftSample1 >>> 8);
            this.outputSamples[6] = (byte) this.prediction.rightSample1;
            this.outputSamples[7] = (byte) (this.prediction.rightSample1 >>> 8);
            this.outputSamplePosition = 8;
        } else {
            final int leftBlockPredictor = this.readByte();
            if (leftBlockPredictor < 0 || this.bytesRemaining <= 0) {
                return MsAdpcmDecoder.END_OF_STREAM;
            }
            leftCoeff1 = Prediction.COEFF1_TABLE[leftBlockPredictor];
            leftCoeff2 = Prediction.COEFF2_TABLE[leftBlockPredictor];
            leftDelta = this.readByte() | this.readByte() << 8;
            this.prediction.leftSample1 = this.readByte() | this.readByte() << 8;
            this.prediction.leftSample2 = this.readByte() | this.readByte() << 8;
            this.outputSamples[0] = (byte) this.prediction.leftSample2;
            this.outputSamples[1] = (byte) (this.prediction.leftSample2 >>> 8);
            this.outputSamples[2] = (byte) this.prediction.leftSample1;
            this.outputSamples[3] = (byte) (this.prediction.leftSample1 >>> 8);
            this.outputSamplePosition = 4;
        }

        // DECODE BLOCK
        for (int i = this.outputSamplePosition; i < this.blockSize; i++) {

            // READ NIBBLES
            final int nibblePair = this.readByte();
            if (nibblePair < 0 || this.bytesRemaining < 0) {
                this.outputSampleSize = this.outputSamplePosition - 1;
                this.outputSamplePosition = 0;
                return this.outputSampleSize;
            }
            final int nibble1 = nibblePair >>> 4;
            final int nibble2 = nibblePair & 0xF;

            // DECODE NIBBLE 1
            int predictor = this.prediction.leftSample1 * leftCoeff1 + this.prediction.leftSample2 * leftCoeff2;
            predictor = predictor >>> 8;
            predictor += (nibble1 & 0b111) * leftDelta;
            predictor *= (nibble1 & 0b1000) != 0 ? -1 : 1;
            int sample = predictor;
            if (sample > Short.MAX_VALUE) {
                sample = Short.MAX_VALUE;
            } else if (sample < Short.MIN_VALUE) {
                sample = Short.MIN_VALUE;
            }
            this.outputSamples[this.outputSamplePosition++] = (byte) sample;
            this.outputSamples[this.outputSamplePosition++] = (byte) (sample >>> 8);
            this.prediction.leftSample2 = this.prediction.leftSample1;
            this.prediction.leftSample1 = sample;
            leftDelta = MathUtils.clamp(Prediction.ADAPTION_TABLE[nibble1] * leftDelta >>> 8, 16, Integer.MAX_VALUE);

            // DECODE NIBBLE 2
            if (this.stereo) {
                int rightPredictor = this.prediction.rightSample1 * rightCoeff1 + this.prediction.rightSample2 * rightCoeff2;
                rightPredictor = rightPredictor >>> 8;
                rightPredictor += (nibble2 & 0b111) * rightDelta;
                rightPredictor *= (nibble2 & 0b1000) != 0 ? -1 : 1;
                int rightSample = rightPredictor;
                if (rightSample > Short.MAX_VALUE) {
                    rightSample = Short.MAX_VALUE;
                } else if (rightSample < Short.MIN_VALUE) {
                    rightSample = Short.MIN_VALUE;
                }
                this.outputSamples[this.outputSamplePosition++] = (byte) rightSample;
                this.outputSamples[this.outputSamplePosition++] = (byte) (rightSample >>> 8);
                this.prediction.rightSample2 = this.prediction.rightSample1;
                this.prediction.rightSample1 = rightSample;
                rightDelta = MathUtils.clamp(Prediction.ADAPTION_TABLE[nibble2] * rightDelta >>> 8, 16, Integer.MAX_VALUE);
            } else {
                predictor = this.prediction.leftSample1 * leftCoeff1 + this.prediction.leftSample2 * leftCoeff2;
                predictor = predictor >>> 8;
                predictor += (nibble1 & 0b111) * leftDelta;
                predictor *= (nibble1 & 0b1000) != 0 ? -1 : 1;
                sample = predictor;
                if (sample > Short.MAX_VALUE) {
                    sample = Short.MAX_VALUE;
                } else if (sample < Short.MIN_VALUE) {
                    sample = Short.MIN_VALUE;
                }
                this.outputSamples[this.outputSamplePosition++] = (byte) sample;
                this.outputSamples[this.outputSamplePosition++] = (byte) (sample >>> 8);
                this.prediction.leftSample2 = this.prediction.leftSample1;
                this.prediction.leftSample1 = sample;
                leftDelta = MathUtils.clamp(Prediction.ADAPTION_TABLE[nibble1] * leftDelta >>> 8, 16, Integer.MAX_VALUE);
            }
        }

        this.outputSampleSize = this.outputSamplePosition - 1;
        this.outputSamplePosition = 0;
        return this.outputSampleSize;
    }


    private int readByte() throws IOException {
        this.bytesRemaining -= 1;
        return this.stream.read();
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
    public PcmDataType getPcmDataType() {
        return PcmDataType.INTEGER;
    }


    @Override
    public void close() throws IOException {
        this.stream.close();
    }


    private static class Prediction {
        private static final int[] ADAPTION_TABLE = { 230, 230, 230, 230, 307, 409, 512, 614, 768, 614, 512, 409, 307, 230, 230, 230 };
        private static final int[] COEFF1_TABLE   = { 256, 512, 0, 192, 240, 460, 392 };
        private static final int[] COEFF2_TABLE   = { 0, -256, 0, 64, 0, -208, -232 };
        private int                leftSample1;
        private int                leftSample2;
        private int                rightSample1;
        private int                rightSample2;
    }

}
