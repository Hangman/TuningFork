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
 * The Auto-wah effect emulates the sound of a wah-wah pedal used with an electric guitar, or a mute on a brass
 * instrument. Such effects allow a musician to
 * control the tone of their instrument by varying the point at which high frequencies are cut off. This OpenAL
 * Effects Extension effect is called Auto-wah
 * because there is no user input for modulating the cut-off point. Instead the effect is achieved by analysing the
 * input signal, and applying a band-pass
 * filter according the intensity of the incoming audio.
 *
 * @author Matthias
 *
 */
public class AutoWah extends SoundEffectData {
    /**
     * Range: 0.0001 - 1.0, Default: 0.06<br>
     * This property controls the time the filtering effect takes to sweep from minimum to maximum center frequency
     * when it is triggered by input signal.
     */
    public float attackTime = 0.06f;

    /**
     * Range: 0.0001 - 1.0, Default: 0.06<br>
     * This property controls the time the filtering effect takes to sweep from maximum back to base center
     * frequency, when the input signal ends.
     */
    public float releaseTime = 0.06f;

    /**
     * Range: 2.0 - 1000.0, Default: 1000.0<br>
     * This property controls the resonant peak, sometimes known as emphasis or Q, of the auto-wah band-pass filter.
     * Resonance occurs when the effect boosts the
     * frequency content of the sound 115/144 around the point at which the filter is working. A high value promotes
     * a highly resonant, sharp sounding effect.
     */
    public float resonance = 1000f;

    /**
     * Range: 0.00003 - 31621.0, Default: 11.22<br>
     * This property controls the input signal level at which the band-pass filter will be fully opened.
     */
    public float peakGain = 11.22f;


    public static AutoWah scrambled() {
        final AutoWah result = new AutoWah();
        result.attackTime = 0.0001f;
        result.releaseTime = 0.01f;
        result.resonance = 1000f;
        result.peakGain = 6900f;
        return result;
    }


    public static AutoWah funkyBeats() {
        final AutoWah result = new AutoWah();
        result.attackTime = 0.0001f;
        result.releaseTime = 0.07f;
        result.resonance = 1000f;
        result.peakGain = 4100f;
        return result;
    }


    public static AutoWah resonantWaves() {
        final AutoWah result = new AutoWah();
        result.attackTime = 0.01f;
        result.releaseTime = 0.13f;
        result.resonance = 1000f;
        result.peakGain = 12000f;
        return result;
    }


    public static AutoWah wahGhosts() {
        final AutoWah result = new AutoWah();
        result.attackTime = 0.04f;
        result.releaseTime = 0.73f;
        result.resonance = 85f;
        result.peakGain = 4750f;
        return result;
    }


    public static AutoWah windyNights() {
        final AutoWah result = new AutoWah();
        result.attackTime = 0.12f;
        result.releaseTime = 0.3f;
        result.resonance = 650f;
        result.peakGain = 24000f;
        return result;
    }


    @Override
    protected void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_AUTOWAH);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_AUTOWAH_ATTACK_TIME, this.attackTime);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_AUTOWAH_RELEASE_TIME, this.releaseTime);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_AUTOWAH_RESONANCE, this.resonance);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_AUTOWAH_PEAK_GAIN, this.peakGain);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.attackTime, this.peakGain, this.releaseTime, this.resonance);
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
        final AutoWah other = (AutoWah) obj;
        return Float.floatToIntBits(this.attackTime) == Float.floatToIntBits(other.attackTime) &&
               Float.floatToIntBits(this.peakGain) == Float.floatToIntBits(other.peakGain) &&
               Float.floatToIntBits(this.releaseTime) == Float.floatToIntBits(other.releaseTime) &&
               Float.floatToIntBits(this.resonance) == Float.floatToIntBits(other.resonance);
    }


    @Override
    public String toString() {
        return "AutoWah [attackTime=" + this.attackTime + ", releaseTime=" + this.releaseTime + ", resonance=" +
               this.resonance + ", peakGain=" + this.peakGain + "]";
    }

}
