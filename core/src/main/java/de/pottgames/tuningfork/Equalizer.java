package de.pottgames.tuningfork;

import org.lwjgl.openal.EXTEfx;

/**
 * The OpenAL Effects Extension EQ is very flexible, providing tonal control over four different adjustable frequency ranges. The lowest frequency range is
 * called “low.” The middle ranges are called “mid1” and “mid2.” The high range is called “high.”
 *
 * @author Matthias
 *
 */
public class Equalizer extends SoundEffectData {
    /**
     * Range: 0.126 - 7.943, Default: 1.0<br>
     * This property controls amount of cut or boost on the low frequency range.
     */
    public float lowGain = 1f;

    /**
     * Range: 50.0 - 800.0, Default: 200.0<br>
     * This property controls the low frequency below which signal will be cut off.
     */
    public float lowCutoff = 200f;

    /**
     * Range: 0.126 - 7.943, Default: 1.0<br>
     * This property allows you to cut / boost signal on the “mid1” range.
     */
    public float mid1Gain = 1f;

    /**
     * Range: 200.0 - 3000.0, Default: 500.0<br>
     * This property sets the center frequency for the “mid1” range.
     */
    public float mid1Center = 500f;

    /**
     * Range: 0.01 - 1.0, Default: 1.0<br>
     * This property controls the width of the “mid1” range.
     */
    public float mid1Width = 1f;

    /**
     * Range: 0.126 - 7.943, Default: 1.0<br>
     * This property allows you to cut / boost signal on the “mid2” range.
     */
    public float mid2Gain = 1f;

    /**
     * Range: 1000.0 - 8000.0, Default: 3000.0<br>
     * This property sets the center frequency for the “mid2” range.
     */
    public float mid2Center = 3000f;

    /**
     * Range: 0.01 - 1.0, Default: 1.0<br>
     * This property controls the width of the “mid2” range.
     */
    public float mid2Width = 1f;

    /**
     * Range: 0.126 - 7.943, Default: 1.0<br>
     * This property allows you to cut / boost the signal at high frequencies.
     */
    public float highGain = 1f;

    /**
     * Range: 4000.0 - 16000.0, Default: 6000.0<br>
     * This property controls the high frequency above which signal will be cut off.
     */
    public float highCutoff = 6000f;


    @Override
    void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EQUALIZER);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_LOW_GAIN, this.lowGain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_LOW_CUTOFF, this.lowCutoff);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_MID1_GAIN, this.mid1Gain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_MID1_CENTER, this.mid1Center);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_MID1_WIDTH, this.mid1Width);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_MID2_GAIN, this.mid2Gain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_MID2_CENTER, this.mid2Center);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_MID2_WIDTH, this.mid2Width);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_HIGH_GAIN, this.highGain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EQUALIZER_HIGH_CUTOFF, this.highCutoff);
    }

}
