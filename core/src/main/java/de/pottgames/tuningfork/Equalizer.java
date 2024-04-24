/**
 * Copyright 2022 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork;

import org.lwjgl.openal.EXTEfx;

import java.util.Objects;

/**
 * The OpenAL Effects Extension EQ is very flexible, providing tonal control over four different adjustable frequency
 * ranges. The lowest frequency range is
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
    protected void apply(int effectId) {
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


    @Override
    public int hashCode() {
        return Objects.hash(this.highCutoff, this.highGain, this.lowCutoff, this.lowGain, this.mid1Center,
                            this.mid1Gain, this.mid1Width, this.mid2Center, this.mid2Gain, this.mid2Width);
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
        final Equalizer other = (Equalizer) obj;
        return Float.floatToIntBits(this.highCutoff) == Float.floatToIntBits(other.highCutoff) &&
               Float.floatToIntBits(this.highGain) == Float.floatToIntBits(other.highGain) &&
               Float.floatToIntBits(this.lowCutoff) == Float.floatToIntBits(other.lowCutoff) &&
               Float.floatToIntBits(this.lowGain) == Float.floatToIntBits(other.lowGain) &&
               Float.floatToIntBits(this.mid1Center) == Float.floatToIntBits(other.mid1Center) &&
               Float.floatToIntBits(this.mid1Gain) == Float.floatToIntBits(other.mid1Gain) &&
               Float.floatToIntBits(this.mid1Width) == Float.floatToIntBits(other.mid1Width) &&
               Float.floatToIntBits(this.mid2Center) == Float.floatToIntBits(other.mid2Center) &&
               Float.floatToIntBits(this.mid2Gain) == Float.floatToIntBits(other.mid2Gain) &&
               Float.floatToIntBits(this.mid2Width) == Float.floatToIntBits(other.mid2Width);
    }


    @Override
    public String toString() {
        return "Equalizer [lowGain=" + this.lowGain + ", lowCutoff=" + this.lowCutoff + ", mid1Gain=" + this.mid1Gain +
               ", mid1Center=" + this.mid1Center + ", mid1Width=" + this.mid1Width + ", mid2Gain=" + this.mid2Gain +
               ", mid2Center=" + this.mid2Center + ", mid2Width=" + this.mid2Width + ", highGain=" + this.highGain +
               ", highCutoff=" + this.highCutoff + "]";
    }

}
