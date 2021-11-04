package de.pottgames.tuningfork;

import org.lwjgl.openal.EXTEfx;

/**
 * The frequency shifter is a single-sideband modulator, which translates all the component frequencies of the input signal by an equal amount. Unlike the pitch
 * shifter, which attempts to maintain harmonic relationships in the signal, the frequency shifter disrupts harmonic relationships and radically alters the
 * sonic qualities of the signal. Applications of the frequency shifter include the creation of bizarre distortion, phaser, stereo widening and rotating speaker
 * effects.
 *
 * @author Matthias
 *
 */
public class FrequencyShifter extends SoundEffectData {
    /**
     * Range: 0.0 - 24000.0, Default: 0.0<br>
     * This is the carrier frequency. For carrier frequencies below the audible range, the single-sideband modulator may produce phaser effects, spatial effects
     * or a slight pitch-shift. As the carrier frequency increases, the timbre of the sound is affected; a piano or guitar note becomes like a bell's chime, and
     * a human voice sounds extraterrestrial!
     */
    public float frequency = 0f;

    /**
     * 0 = down, 1 = up, 2 = off<br>
     * These select which internal signals are added together to produce the output. Different combinations of values will produce slightly different tonal and
     * spatial effects.
     */
    public int leftDirection = 0;

    /**
     * 0 = down, 1 = up, 2 = off<br>
     * These select which internal signals are added together to produce the output. Different combinations of values will produce slightly different tonal and
     * spatial effects.
     */
    public int rightDirection = 0;


    @Override
    void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_FREQUENCY_SHIFTER);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_FREQUENCY_SHIFTER_FREQUENCY, this.frequency);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_FREQUENCY_SHIFTER_LEFT_DIRECTION, this.leftDirection);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_FREQUENCY_SHIFTER_RIGHT_DIRECTION, this.rightDirection);
    }

}
