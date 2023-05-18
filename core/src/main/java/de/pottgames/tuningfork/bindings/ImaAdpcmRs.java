package de.pottgames.tuningfork.bindings;

public class ImaAdpcmRs {

    public native byte[] decode(byte[] data, int blockSize, boolean stereo);


    public native ImaAdpcmData decodeFile(String path);

}
