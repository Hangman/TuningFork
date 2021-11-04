package de.pottgames.tuningfork;

import org.lwjgl.openal.EXTEfx;

/**
 * The vocal morpher consists of a pair of 4-band formant filters, used to impose vocal tract effects upon the input signal. If the input signal is a broadband
 * sound such as pink noise or a car engine, the vocal morpher can provide a wide variety of filtering effects. A low-frequency oscillator can be used to morph
 * the filtering effect between two different phonemes. The vocal morpher is not necessarily intended for use on voice signals; it is primarily intended for
 * pitched noise effects, vocal-like wind effects, etc.
 *
 * @author Matthias
 *
 */
public class VocalMorpher extends SoundEffectData {
    /**
     * Range: 0 - 29, Default: 0<br>
     * If both parameters are set to the same phoneme, that determines the filtering effect that will be heard. If these two parameters are set to different
     * phonemes, the filtering effect will morph between the two settings at a rate specified by AL_VOCAL_MORPHER_RATE.
     */
    public int phonemea = 0;

    /**
     * Range: 0 - 29, Default: 0<br>
     * If both parameters are set to the same phoneme, that determines the filtering effect that will be heard. If these two parameters are set to different
     * phonemes, the filtering effect will morph between the two settings at a rate specified by AL_VOCAL_MORPHER_RATE.
     */
    public int phonemeb = 10;

    /**
     * Range: -24 - 24, Default: 0<br>
     * These are used to adjust the pitch of phoneme filters A and B in 1-semitone increments.
     */
    public int phonemeaCoarseTuning = 0;

    /**
     * Range: -24 - 24, Default: 0<br>
     * These are used to adjust the pitch of phoneme filters A and B in 1-semitone increments.
     */
    public int phonemebCoarseTuning = 0;

    /**
     * 0 = sinus, 1 = triangle, 2 = saw<br>
     * This controls the shape of the low-frequency oscillator used to morph between the two phoneme filters. By selecting a saw tooth wave and a slow
     * AL_VOCAL_MORPHER_RATE, one can create a filtering effect that slowly increases or decreases in pitch (depending on which of the two phoneme filters A or
     * B is perceived as being higher-pitched).
     */
    public int waveform = 0;

    /**
     * Range: 0.0 - 10.0, Default: 1.41<br>
     * This controls the frequency of the low-frequency oscillator used to morph between the two phoneme filters
     */
    public float rate = 1.41f;


    @Override
    void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_VOCAL_MORPHER);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_VOCMORPHER_PHONEMEA, this.phonemea);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_VOCMORPHER_PHONEMEB, this.phonemeb);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_VOCMORPHER_PHONEMEA_COARSE_TUNING, this.phonemeaCoarseTuning);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_VOCMORPHER_PHONEMEB_COARSE_TUNING, this.phonemebCoarseTuning);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_VOCMORPHER_WAVEFORM, this.waveform);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_VOCMORPHER_RATE, this.rate);
    }

}
