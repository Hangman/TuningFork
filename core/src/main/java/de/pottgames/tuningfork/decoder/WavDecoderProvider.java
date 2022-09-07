package de.pottgames.tuningfork.decoder;

@FunctionalInterface
public interface WavDecoderProvider {

    WavDecoder getDecoder(int inputBitsPerSample, int audioFormat);

}
