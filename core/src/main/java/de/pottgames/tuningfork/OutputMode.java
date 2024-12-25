package de.pottgames.tuningfork;

import org.lwjgl.openal.SOFTOutputMode;

public enum OutputMode {
    /**
     * Any (default). Autodetect from the system when possible.
     */
    ANY(SOFTOutputMode.ALC_ANY_SOFT),

    /**
     * Monaural.
     */
    MONO(SOFTOutputMode.ALC_MONO_SOFT),

    /**
     * 2-channel stereophonic. An umbrella mode covering the other stereo modes.
     */
    STEREO(SOFTOutputMode.ALC_STEREO_SOFT),

    /**
     * Basic 2-channel mixing (e.g. pan-pot).
     */
    STEREO_BASIC(SOFTOutputMode.ALC_STEREO_BASIC_SOFT),

    /**
     * Stereo-compatible 2-channel UHJ surround encoding.
     */
    STEREO_UHJ(SOFTOutputMode.ALC_STEREO_UHJ_SOFT),

    /**
     * 2-channel HRTF mixing.
     */
    STEREO_HRTF(SOFTOutputMode.ALC_STEREO_HRTF_SOFT),

    /**
     * Quadraphonic.
     */
    QUAD(SOFTOutputMode.ALC_QUAD_SOFT),

    /**
     * 5.1 Surround.
     */
    SURROUND_5_1(SOFTOutputMode.ALC_SURROUND_5_1_SOFT),

    /**
     * 6.1 Surround.
     */
    SURROUND_6_1(SOFTOutputMode.ALC_SURROUND_6_1_SOFT),

    /**
     * 7.1 Surround.
     */
    SURROUND_7_1(SOFTOutputMode.ALC_SURROUND_7_1_SOFT);


    private static final OutputMode[] MAP = OutputMode.values();
    private final int                 alId;


    OutputMode(int alId) {
        this.alId = alId;
    }


    int getAlId() {
        return alId;
    }


    public static OutputMode getByAlId(int id) {
        for (final OutputMode mode : OutputMode.MAP) {
            if (id == mode.alId) {
                return mode;
            }
        }
        return ANY;
    }
}
