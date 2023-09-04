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
 * The flanger effect creates a “tearing” or “whooshing” sound (like a jet flying overhead). It works by sampling a portion of the input signal, delaying it by
 * a period modulated between 0 and 4ms by a low-frequency oscillator, and then mixing it with the source signal.
 *
 * @author Matthias
 *
 */
public class Flanger extends SoundEffectData {
    /**
     * 0 = sinusoid, 1 = triangle<br>
     * Selects the shape of the LFO waveform that controls the amount of the delay of the sampled signal. Zero is a sinusoid and one is a triangle.
     */
    public int waveform = 1;

    /**
     * Range: -180 - 180, Default: 0<br>
     * This changes the phase difference between the left and right LFO’s. At zero degrees the two LFOs are synchronized.
     */
    public int phase = 0;

    /**
     * Range: 0.0 - 10.0, Default: 0.27<br>
     * The number of times per second the LFO controlling the amount of delay repeats. Higher values increase the pitch modulation.
     */
    public float rate = 0.27f;

    /**
     * Range: 0.0 - 1.0, Default: 1.0<br>
     * The ratio by which the delay time is modulated by the LFO. Use this parameter to increase the pitch modulation.
     */
    public float depth = 1f;

    /**
     * Range: -1.0 - 1.0, Default: -0.5<br>
     * This is the amount of the output signal level fed back into the effect’s input. A negative value will reverse the phase of the feedback signal. Use this
     * parameter to create an “intense metallic” effect. At full magnitude, the identical sample will repeat endlessly. At less than full magnitude, the sample
     * will repeat and fade out over time.
     */
    public float feedback = -0.5f;

    /**
     * Range: 0.0 - 0.004, Default: 0.002<br>
     * The average amount of time the sample is delayed before it is played back; with feedback, the amount of time between iterations of the sample.
     */
    public float delay = 0.002f;


    public static Flanger robotHigh() {
        final Flanger result = new Flanger();
        result.waveform = 1;
        result.phase = 180;
        result.rate = 2f;
        result.depth = 0.5f;
        result.feedback = 0.9f;
        result.delay = 0.002f;
        return result;
    }


    public static Flanger robotLow() {
        final Flanger result = new Flanger();
        result.waveform = 1;
        result.phase = 180;
        result.rate = 2f;
        result.depth = 0.5f;
        result.feedback = 0.9f;
        result.delay = 0.004f;
        return result;
    }


    public static Flanger robotMetallic() {
        final Flanger result = new Flanger();
        result.waveform = 1;
        result.phase = 180;
        result.rate = 2f;
        result.depth = 0.5f;
        result.feedback = -0.95f;
        result.delay = 0.004f;
        return result;
    }


    @Override
    protected void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_FLANGER);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_FLANGER_WAVEFORM, this.waveform);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_FLANGER_PHASE, this.phase);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_FLANGER_RATE, this.rate);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_FLANGER_DEPTH, this.depth);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_FLANGER_FEEDBACK, this.feedback);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_FLANGER_DELAY, this.delay);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.delay, this.depth, this.feedback, this.phase, this.rate, this.waveform);
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
        final Flanger other = (Flanger) obj;
        return Float.floatToIntBits(this.delay) == Float.floatToIntBits(other.delay) && Float.floatToIntBits(this.depth) == Float.floatToIntBits(other.depth)
                && Float.floatToIntBits(this.feedback) == Float.floatToIntBits(other.feedback) && this.phase == other.phase
                && Float.floatToIntBits(this.rate) == Float.floatToIntBits(other.rate) && this.waveform == other.waveform;
    }


    @Override
    public String toString() {
        return "Flanger [waveform=" + this.waveform + ", phase=" + this.phase + ", rate=" + this.rate + ", depth=" + this.depth + ", feedback=" + this.feedback
                + ", delay=" + this.delay + "]";
    }

}
