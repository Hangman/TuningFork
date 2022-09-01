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
 * The distortion effect simulates turning up (overdriving) the gain stage on a guitar amplifier or adding a distortion pedal to an instrument’s output. It is
 * achieved by clipping the signal (adding more square wave-like components) and adding rich harmonics. The distortion effect could be very useful for adding
 * extra dynamics to engine sounds in a driving simulator, or modifying samples such as vocal communications. The OpenAL Effects Extension distortion effect
 * also includes EQ on the output signal, to help ‘rein in’ excessive frequency content in distorted audio. A low-pass filter is applied to input signal before
 * the distortion effect, to limit excessive distorted signals at high frequencies.
 *
 * @author Matthias
 *
 */
public class Distortion extends SoundEffectData {
    /**
     * Range: 0.0 - 1.0, Default: 0.2<br>
     * This property controls the shape of the distortion. The higher the value for Edge, the ‘dirtier’ and ‘fuzzier’ the effect.
     */
    public float edge = 0.2f;

    /**
     * Range: 0.01 - 1.0, Default: 0.05<br>
     * This property allows you to attenuate the distorted sound.
     */
    public float gain = 0.05f;

    /**
     * Range: 80.0 - 24000.0, Default: 8000.0<br>
     * Input signal can have a low pass filter applied, to limit the amount of high frequency signal feeding into the distortion effect.
     */
    public float lowpassCutoff = 8000f;

    /**
     * Range: 80.0 - 24000.0, Default: 3600.0<br>
     * This property controls the frequency at which the post-distortion attenuation (Distortion Gain) is active.
     */
    public float eqCenter = 3600f;

    /**
     * Range: 80.0 - 24000.0, Default: 3600.0<br>
     * This property controls the bandwidth of the post-distortion attenuation.
     */
    public float eqBandwidth = 3600f;


    @Override
    void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_DISTORTION);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_DISTORTION_EDGE, this.edge);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_DISTORTION_GAIN, this.gain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_DISTORTION_LOWPASS_CUTOFF, this.lowpassCutoff);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_DISTORTION_EQCENTER, this.eqCenter);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_DISTORTION_EQBANDWIDTH, this.eqBandwidth);
    }

}
