package de.pottgames.tuningfork;

import java.io.Closeable;

interface AudioStream extends Closeable {

    int getChannels();


    int getSampleRate();


    int read(byte[] bytes);


    boolean isClosed();

}
