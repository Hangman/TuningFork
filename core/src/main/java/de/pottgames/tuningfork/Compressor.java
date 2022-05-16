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
 * The Automatic Gain Control effect performs the same task as a studio compressor – evening out the audio dynamic range of an input sound. This results in
 * audio exhibiting smaller variation in intensity between the loudest and quietest portions. The AGC Compressor will boost quieter portions of the audio, while
 * louder portions will stay the same or may even be reduced. The Compressor effect cannot be tweaked in depth – it can just be switched on and off.
 *
 * @author Matthias
 *
 */
public class Compressor extends SoundEffectData {
    /**
     * 0 = on, 1 = off, Default = 1<br>
     * The OpenAL Effect Extension Compressor can only be switched on and off – it cannot be adjusted.
     */
    public int onOff = 1;


    @Override
    void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_COMPRESSOR);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_COMPRESSOR_ONOFF, this.onOff);
    }

}
