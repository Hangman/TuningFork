package de.pottgames.tuningfork.decoder;

import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

@FunctionalInterface
public interface ResamplerProvider {

    Resampler getResampler(InputStream stream, long streamLength, int inputBitsPerSample, PcmDataType inputDataType);

}
