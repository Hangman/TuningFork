package de.pottgames.tuningfork.decoder;

@FunctionalInterface
public interface WavDecoderProvider {

    WavDecoder getDecoder(WavFmtChunk fmtChunk);

}
