/**
 * Copyright 2022 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork;

import java.util.Objects;

import org.lwjgl.openal.EXTEfx;

import com.badlogic.gdx.math.MathUtils;

/**
 * The pitch shifter applies time-invariant pitch shifting to the input signal, over a one octave range and controllable at a semi-tone and cent resolution.
 *
 * @author Matthias
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


    public static PitchShifter chipmunk() {
        final PitchShifter result = new PitchShifter();
        result.coarseTune = 8;
        return result;
    }


    public static PitchShifter demon() {
        final PitchShifter result = new PitchShifter();
        result.coarseTune = -9;
        return result;
    }


    public static PitchShifter maxPitch() {
        final PitchShifter result = new PitchShifter();
        result.coarseTune = 12;
        result.fineTune = 50;
        return result;
    }


    public static PitchShifter minPitch() {
        final PitchShifter result = new PitchShifter();
        result.coarseTune = -12;
        result.fineTune = -50;
        return result;
    }


    /**
     * Sets {@link PitchShifter#coarseTune coarseTune} and {@link PitchShifter#fineTune fineTune} to values that try to correct a pitch applied to a
     * {@link SoundSource}, so that the sound appears to be played back at a pitch of 1. This way, it is possible to change the playback speed of a sound
     * without changing its pitch.<br>
     * <br>
     * <b>Note:</b> In order to only hear the pitch corrected sound, the direct sound path must be muted with the help of
     * {@link SoundSource#setFilter(float, float)} set to [0,0].
     *
     * @param pitch - pitch correction only works for 0.5 &lt;= pitch &lt;= 2
     *
     * @return the PitchShifter for chaining
     */
    public PitchShifter correctPitch(float pitch) {
        pitch = MathUtils.clamp(pitch, 0.5f, 2f);

        final float semitones = (float) (12d / Math.log(2d) * Math.log(1d / pitch));
        this.coarseTune = Math.round(semitones);
        final float rest = semitones - this.coarseTune;
        this.fineTune = Math.round(rest * 100f);

        return this;
    }


    @Override
    protected void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_PITCH_SHIFTER);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_PITCH_SHIFTER_COARSE_TUNE, this.coarseTune);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_PITCH_SHIFTER_FINE_TUNE, this.fineTune);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.coarseTune, this.fineTune);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final PitchShifter other = (PitchShifter) obj;
        return this.coarseTune == other.coarseTune && this.fineTune == other.fineTune;
    }


    @Override
    public String toString() {
        return "PitchShifter [coarseTune=" + this.coarseTune + ", fineTune=" + this.fineTune + "]";
    }

}
