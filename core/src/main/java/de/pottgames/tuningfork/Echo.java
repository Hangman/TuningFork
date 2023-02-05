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
 * The echo effect generates discrete, delayed instances of the input signal. The amount of delay and feedback is controllable. The delay is ‘two tap’ – you can
 * control the interaction between two separate instances of echoes.
 *
 * @author Matthias
 *
 */
public class Echo extends SoundEffectData {
    /**
     * Range: 0.0 - 0.207, Default: 0.1<br>
     * This property controls the delay between the original sound and the first ‘tap’, or echo instance. Subsequently, the value for Echo Delay is used to
     * determine the time delay between each ‘second tap’ and the next ‘first tap’.
     */
    public float delay = 0.1f;

    /**
     * Range: 0.0 - 0.404, Default: 0.1<br>
     * This property controls the delay between the first ‘tap’ and the second ‘tap’. Subsequently, the value for Echo LR Delay is used to determine the time
     * delay between each ‘first tap’ and the next ‘second tap’.
     */
    public float lrDelay = 0.1f;

    /**
     * Range: 0.0 - 0.99, Default: 0.5<br>
     * This property controls the amount of high frequency damping applied to each echo. As the sound is subsequently fed back for further echoes, damping
     * results in an echo which progressively gets softer in tone as well as intensity.
     */
    public float damping = 0.5f;

    /**
     * Range: 0.0 - 1.0, Default: 0.5<br>
     * This property controls the amount of feedback the output signal fed back into the input. Use this parameter to create “cascading” echoes. At full
     * magnitude, the identical sample will repeat endlessly. Below full magnitude, the sample will repeat and fade.
     */
    public float feedback = 0.5f;

    /**
     * Range: -1.0 - 1.0, Default: -1.0<br>
     * This property controls how hard panned the individual echoes are. With a value of 1.0, the first ‘tap’ will be panned hard left, and the second tap hard
     * right. A value of –1.0 gives the opposite result. Settings nearer to 0.0 result in less emphasized panning.
     */
    public float spread = -1.0f;


    public static Echo veryFarAway() {
        final Echo result = new Echo();
        result.delay = 0.207f;
        result.lrDelay = 0.404f;
        result.damping = 0.6f;
        result.feedback = 0.7f;
        result.spread = 0f;
        return result;
    }


    public static Echo farAway() {
        final Echo result = new Echo();
        result.delay = 0.207f;
        result.lrDelay = 0.404f;
        result.damping = 0.5f;
        result.feedback = 0.5f;
        result.spread = 0f;
        return result;
    }


    public static Echo pingPongLeft() {
        final Echo result = new Echo();
        result.delay = 0.05f;
        result.lrDelay = 0.1f;
        result.damping = 0.1f;
        result.feedback = 0.8f;
        result.spread = 1f;
        return result;
    }


    public static Echo pingPongRight() {
        final Echo result = Echo.pingPongLeft();
        result.spread = -1f;
        return result;
    }


    public static Echo pingPongCenter() {
        final Echo result = Echo.pingPongLeft();
        result.spread = 0f;
        return result;
    }


    public static Echo doppelganger() {
        final Echo result = new Echo();
        result.delay = 0.1f;
        result.lrDelay = 0.1f;
        result.damping = 0.4f;
        result.feedback = 0.5f;
        result.spread = 0.2f;
        return result;
    }


    @Override
    protected void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_ECHO);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_ECHO_DELAY, this.delay);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_ECHO_LRDELAY, this.lrDelay);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_ECHO_DAMPING, this.damping);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_ECHO_FEEDBACK, this.feedback);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_ECHO_SPREAD, this.spread);
    }

}
