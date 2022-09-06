package de.pottgames.tuningfork.decoder;

import java.io.Closeable;
import java.io.IOException;

public interface Resampler extends Closeable {

    int inputBitsPerSample();


    int outputBitsPerSample();


    int read(byte[] output) throws IOException;

}
