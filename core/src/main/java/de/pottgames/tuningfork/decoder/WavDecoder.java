package de.pottgames.tuningfork.decoder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public interface WavDecoder extends Closeable {

    void setup(InputStream stream, long streamLength);


    int inputBitsPerSample();


    int outputBitsPerSample();


    PcmDataType getPcmDataType();


    int read(byte[] output) throws IOException;

}
