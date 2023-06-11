package de.pottgames.tuningfork.bindings;

import de.pottgames.tuningfork.misc.ExperimentalFeature;

@ExperimentalFeature
public class FlacRs {

    @ExperimentalFeature
    public static native ImaAdpcmData decodeFlac(byte[] data);

}
