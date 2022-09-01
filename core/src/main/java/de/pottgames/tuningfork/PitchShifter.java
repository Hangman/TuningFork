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
