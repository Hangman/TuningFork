package de.pottgames.tuningfork.decoder;

import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class DefaultResamplerProvider implements ResamplerProvider {

    @Override
    public Resampler getResampler(InputStream stream, long streamLength, int inputBitsPerSample, PcmDataType inputDataType) {
        if (inputBitsPerSample <= 0 || inputDataType == null) {
            return null;
        }

        switch (inputBitsPerSample) {
            case 8:
                if (inputDataType == PcmDataType.INTEGER) {
                    return new ForwardResampler(stream, streamLength, inputBitsPerSample);
                }
                break;
            case 16:
                if (inputDataType == PcmDataType.INTEGER) {
                    return new ForwardResampler(stream, streamLength, inputBitsPerSample);
                }
                break;
            case 24:
                if (inputDataType == PcmDataType.INTEGER) {
                    return new Int24To16Resampler(stream, streamLength);
                }
                break;
            case 32:
                if (inputDataType == PcmDataType.FLOAT) {
                    return new ForwardResampler(stream, streamLength, inputBitsPerSample);
                }
                if (inputDataType == PcmDataType.INTEGER) {
                    // TODO: planned
                    return null;
                }
                break;
            case 64:
                if (inputDataType == PcmDataType.FLOAT) {
                    return new ForwardResampler(stream, streamLength, inputBitsPerSample);
                }
                break;
        }

        return null;
    }

}
