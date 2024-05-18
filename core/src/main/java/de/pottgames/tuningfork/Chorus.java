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

/**
 * The chorus effect essentially replays the input audio accompanied by another slightly delayed version of the signal, creating a ‘doubling’ effect. This was
 * originally intended to emulate the effect of several musicians playing the same notes simultaneously, to create a thicker, more satisfying sound. To add some
 * variation to the effect, the delay time of the delayed versions of the input signal is modulated by an adjustable oscillating waveform. This causes subtle
 * shifts in the pitch of the delayed signals, emphasizing the thickening effect.
 *
 * @author Matthias
 */
public class Chorus extends SoundEffectData {
    /**
     * 0 = sinusoid, 1 = triangle<br>
     * This property sets the waveform shape of the LFO that controls the delay time of the delayed signals.
     */
    public int waveForm = 1;

    /**
     * Range: -180 - 180, Default: 90<br>
     * This property controls the phase difference between the left and right LFO’s. At zero degrees the two LFOs are synchronized. Use this parameter to create
     * the illusion of an expanded stereo field of the output signal.
     */
    public int phase = 90;

    /**
     * Range: 0.0 - 10.0, Default: 1.1<br>
     * This property sets the modulation rate of the LFO that controls the delay time of the delayed signals.
     */
    public float rate = 1.1f;

    /**
     * Range: 0.0 - 1.0, Default: 0.1<br>
     * This property controls the amount by which the delay time is modulated by the LFO.
     */
    public float depth = 0.1f;

    /**
     * Range: -1.0 - 1.0, Default: 0.25<br>
     * This property controls the amount of processed signal that is fed back to the input of the chorus effect. Negative values will reverse the phase of the
     * feedback signal. At full magnitude the identical sample will repeat endlessly. At lower magnitudes the sample will repeat and fade out over time. Use
     * this parameter to create a “cascading” chorus effect.
     */
    public float feedback = 0.25f;

    /**
     * Range: 0.0 - 0.016, Default: 0.016<br>
     * This property controls the average amount of time the sample is delayed before it is played back, and with feedback, the amount of time between
     * iterations of the sample. Larger values lower the pitch. Smaller values make the chorus sound like a flanger, but with different frequency
     * characteristics.
     */
    public float delay = 0.016f;


    public static Chorus chore() {
        final Chorus result = new Chorus();
        result.waveForm = 0;
        result.phase = 0;
        result.rate = 3f;
        result.depth = 0.15f;
        result.feedback = 0.55f;
        result.delay = 0.016f;
        return result;
    }


    public static Chorus voiceBreak() {
        final Chorus result = new Chorus();
        result.waveForm = 0;
        result.phase = 0;
        result.rate = 3f;
        result.depth = 0.2f;
        result.feedback = 0.75f;
        result.delay = 0.016f;
        return result;
    }


    public static Chorus goofy() {
        final Chorus result = new Chorus();
        result.waveForm = 0;
        result.phase = 0;
        result.rate = 3f;
        result.depth = 0.6f;
        result.feedback = 0.75f;
        result.delay = 0.016f;
        return result;
    }


    public static Chorus goofyRobot() {
        final Chorus result = new Chorus();
        result.waveForm = 1;
        result.phase = 0;
        result.rate = 3f;
        result.depth = 0.6f;
        result.feedback = 0.75f;
        result.delay = 0.016f;
        return result;
    }


    @Override
    protected void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_CHORUS);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_CHORUS_WAVEFORM, this.waveForm);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_CHORUS_PHASE, this.phase);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_CHORUS_RATE, this.rate);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_CHORUS_DEPTH, this.depth);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_CHORUS_FEEDBACK, this.feedback);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_CHORUS_DELAY, this.delay);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.delay, this.depth, this.feedback, this.phase, this.rate, this.waveForm);
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
        final Chorus other = (Chorus) obj;
        return Float.floatToIntBits(this.delay) == Float.floatToIntBits(other.delay) && Float.floatToIntBits(this.depth) == Float.floatToIntBits(other.depth)
                && Float.floatToIntBits(this.feedback) == Float.floatToIntBits(other.feedback) && this.phase == other.phase
                && Float.floatToIntBits(this.rate) == Float.floatToIntBits(other.rate) && this.waveForm == other.waveForm;
    }


    @Override
    public String toString() {
        return "Chorus [waveForm=" + this.waveForm + ", phase=" + this.phase + ", rate=" + this.rate + ", depth=" + this.depth + ", feedback=" + this.feedback
                + ", delay=" + this.delay + "]";
    }

}
