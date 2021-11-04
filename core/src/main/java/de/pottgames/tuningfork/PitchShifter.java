package de.pottgames.tuningfork;

import org.lwjgl.openal.EXTEfx;

/**
 * The pitch shifter applies time-invariant pitch shifting to the input signal, over a one octave range and controllable at a semi-tone and cent resolution.
 *
 * @author Matthias
 *
 */
public class PitchShifter extends SoundEffectData {
    /**
     * Range: -12 - 12, Default: 12<br>
     * This sets the number of semitones by which the pitch is shifted. There are 12 semitones per octave. Negative values create a downwards shift in pitch,
     * positive values pitch the sound upwards.
     */
    public int coarseTune = 12;

    /**
     * Range: -50 - 50, Default: 0<br>
     * This sets the number of cents between Semitones a pitch is shifted. A Cent is 1/100th of a Semitone. Negative values create a downwards shift in pitch,
     * positive values pitch the sound upwards.
     */
    public int fineTune = 0;


    @Override
    void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_PITCH_SHIFTER);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_PITCH_SHIFTER_COARSE_TUNE, this.coarseTune);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_PITCH_SHIFTER_FINE_TUNE, this.fineTune);
    }

}
