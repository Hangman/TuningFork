package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;

public enum PcmFormat {
    STEREO_16_BIT(AL10.AL_FORMAT_STEREO16), STEREO_8_BIT(AL10.AL_FORMAT_STEREO8), MONO_16_BIT(AL10.AL_FORMAT_MONO16), MONO_8_BIT(AL10.AL_FORMAT_MONO8);


    private final int alId;


    PcmFormat(int alId) {
        this.alId = alId;
    }


    public int getAlId() {
        return this.alId;
    }

}
