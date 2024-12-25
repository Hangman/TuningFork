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
 * The ring modulator multiplies an input signal by a carrier signal in the time domain, resulting in tremolo or inharmonic effects.
 *
 * @author Matthias
 *
 */
public class RingModulator extends SoundEffectData {
    /**
     * Range: 0.0 - 8000.0, Default: 440.0<br>
     * This is the frequency of the carrier signal. If the carrier signal is slowly varying (less than 20 Hz), the result is a tremolo (slow amplitude
     * variation) effect. If the carrier signal is in the audio range, audible upper and lower sidebands begin to appear, causing an inharmonic effect. The
     * carrier signal itself is not heard in the output.
     */
    public float frequency = 440f;

    /**
     * Range: 0.0 - 24000.0, Default: 800.0<br>
     * This controls the cutoff frequency at which the input signal is high-pass filtered before being ring modulated . If the cutoff frequency is 0, the entire
     * signal will be ring modulated. If the cutoff 114/144 frequency is high, very little of the signal (only those parts above the cutoff) will be ring
     * modulated.
     */
    public float highpassCutoff = 800f;

    /**
     * 0 = sinus, 1 = saw, 2 = square<br>
     * This controls which waveform is used as the carrier signal. Traditional ring modulator and tremolo effects generally use a sinusoidal carrier. Sawtooth
     * and square waveforms are may cause unpleasant aliasing.
     */
    public int waveform = 0;


    public static RingModulator tremolo() {
        final RingModulator result = new RingModulator();
        result.frequency = 3.5f;
        result.highpassCutoff = 0f;
        result.waveform = 0;
        return result;
    }


    public static RingModulator slowTremolo() {
        final RingModulator result = new RingModulator();
        result.frequency = 1.5f;
        result.highpassCutoff = 0f;
        result.waveform = 0;
        return result;
    }


    public static RingModulator fastTremolo() {
        final RingModulator result = new RingModulator();
        result.frequency = 5f;
        result.highpassCutoff = 0f;
        result.waveform = 0;
        return result;
    }


    @Override
    protected void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_RING_MODULATOR);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_RING_MODULATOR_FREQUENCY, frequency);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_RING_MODULATOR_HIGHPASS_CUTOFF, highpassCutoff);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_RING_MODULATOR_WAVEFORM, waveform);
    }


    @Override
    public int hashCode() {
        return Objects.hash(frequency, highpassCutoff, waveform);
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
        final RingModulator other = (RingModulator) obj;
        return Float.floatToIntBits(frequency) == Float.floatToIntBits(other.frequency)
                && Float.floatToIntBits(highpassCutoff) == Float.floatToIntBits(other.highpassCutoff) && waveform == other.waveform;
    }


    @Override
    public String toString() {
        return "RingModulator [frequency=" + frequency + ", highpassCutoff=" + highpassCutoff + ", waveform=" + waveform + "]";
    }

}
