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

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

public class SoundEffect implements Disposable {
    private final ErrorLogger      errorLogger;
    private final TuningForkLogger logger;
    private final int              auxSlotId;
    private final int              effectId;
    private Array<SoundSource>     attachedSources = new Array<>();


    public SoundEffect(SoundEffectData data) {
        this.logger = Audio.get().getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        // CREATE AUX SLOT
        this.auxSlotId = EXTEfx.alGenAuxiliaryEffectSlots();
        this.setEnvironmental(false);

        // CREATE EFFECT
        this.effectId = EXTEfx.alGenEffects();
        data.apply(this.effectId);

        // SET EFFECT TO AUX SLOT
        EXTEfx.alAuxiliaryEffectSloti(this.auxSlotId, EXTEfx.AL_EFFECTSLOT_EFFECT, this.effectId);

        // DELETE EFFECT
        EXTEfx.alDeleteEffects(this.effectId);

        if (!this.errorLogger.checkLogError("Failed to create the SoundEffect")) {
            this.logger.debug(this.getClass(), "SoundEffect successfully created");
        }
    }


    /**
     * Sets whether this effect should be automtically adjusted by source and listener position. Enabling this leads to a more realistic impression of the
     * environment. This property should be enabled when using a reverb effect to simulate the environment surrounding a listener or a source.
     *
     * @param value whether this effect should be environmental or "pure"
     */
    public void setEnvironmental(boolean value) {
        EXTEfx.alAuxiliaryEffectSloti(this.auxSlotId, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, value ? AL10.AL_TRUE : AL10.AL_FALSE);

        if (!this.errorLogger.checkLogError("Something went wrong")) {
            this.logger.trace(this.getClass(), "SoundEffect set to environmental");
        }
    }


    int getAuxSlotId() {
        return this.auxSlotId;
    }


    void addSource(SoundSource source) {
        this.attachedSources.add(source);
    }


    void removeSource(SoundSource source) {
        this.attachedSources.removeValue(source, true);
    }


    /**
     * Releases all resources of this effect and detaches it from every sound source it is attached to.
     */
    @Override
    public void dispose() {
        for (final SoundSource source : this.attachedSources) {
            source.onEffectDisposal(this);
        }
        this.attachedSources.clear();
        EXTEfx.alDeleteAuxiliaryEffectSlots(this.auxSlotId);

        if (!this.errorLogger.checkLogError("Something went wrong")) {
            this.logger.trace(this.getClass(), "SoundEffect successfully disposed");
        }
    }

}
