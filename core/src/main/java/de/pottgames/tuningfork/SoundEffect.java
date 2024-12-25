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

/**
 * A sound effect that can be attached to a sound source via {@link SoundSource#attachEffect(SoundEffect)}. It uses native resources, call {@link #dispose()}
 * when you don't need it anymore.
 *
 * @see <a href="https://github.com/Hangman/TuningFork/wiki/Sound-Effects">The wiki entry</a>
 */
public class SoundEffect implements Disposable {
    private final ErrorLogger        errorLogger;
    private final TuningForkLogger   logger;
    private final int                auxSlotId;
    private final int                effectId;
    private final Array<SoundSource> attachedSources = new Array<>();


    /**
     * Creates a new SoundEffect from a template.
     *
     * @param data Available effects: {@link AutoWah}, {@link Chorus}, {@link Compressor}, {@link Distortion}, {@link EaxReverb}, {@link Echo},
     *            {@link Equalizer}, {@link Flanger}, {@link FrequencyShifter}, {@link PitchShifter}, {@link Reverb}, {@link RingModulator},
     *            {@link VocalMorpher}.
     *
     * @see <a href="https://github.com/Hangman/TuningFork/wiki/Sound-Effects">The wiki entry</a>
     */
    public SoundEffect(SoundEffectData data) {
        logger = Audio.get().getLogger();
        errorLogger = new ErrorLogger(this.getClass(), logger);

        // CREATE AUX SLOT
        auxSlotId = EXTEfx.alGenAuxiliaryEffectSlots();
        setEnvironmental(false);

        // CREATE EFFECT
        effectId = EXTEfx.alGenEffects();
        data.apply(effectId);

        // SET EFFECT TO AUX SLOT
        EXTEfx.alAuxiliaryEffectSloti(auxSlotId, EXTEfx.AL_EFFECTSLOT_EFFECT, effectId);

        if (!errorLogger.checkLogError("Failed to create the SoundEffect")) {
            logger.debug(this.getClass(), "SoundEffect successfully created");
        }
    }


    /**
     * Updates the sound effect data. This is also possible at runtime, when the sound effect is in active use.
     *
     * @param data Available effects: {@link AutoWah}, {@link Chorus}, {@link Compressor}, {@link Distortion}, {@link EaxReverb}, {@link Echo},
     *            {@link Equalizer}, {@link Flanger}, {@link FrequencyShifter}, {@link PitchShifter}, {@link Reverb}, {@link RingModulator},
     *            {@link VocalMorpher}.
     */
    public void updateEffect(SoundEffectData data) {
        // AL wants us to do it this way: detach effect from aux slot, re-attach altered effect to aux slot
        EXTEfx.alAuxiliaryEffectSloti(auxSlotId, EXTEfx.AL_EFFECTSLOT_EFFECT, EXTEfx.AL_EFFECT_NULL);
        data.apply(effectId);
        EXTEfx.alAuxiliaryEffectSloti(auxSlotId, EXTEfx.AL_EFFECTSLOT_EFFECT, effectId);

        errorLogger.checkLogError("Failed to update SoundEffect");
    }


    /**
     * Sets whether this effect should be automtically adjusted by source and listener position. Enabling this leads to a more realistic impression of the
     * environment. This property should be enabled when using a reverb effect to simulate the environment surrounding a listener or a source.
     *
     * @param value whether this effect should be environmental or "pure"
     */
    public void setEnvironmental(boolean value) {
        EXTEfx.alAuxiliaryEffectSloti(auxSlotId, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, value ? AL10.AL_TRUE : AL10.AL_FALSE);

        if (!errorLogger.checkLogError("Something went wrong")) {
            logger.trace(this.getClass(), "SoundEffect set to environmental");
        }
    }


    /**
     * Returns true if this sound effect is attached to one or more sources.
     *
     * @return true if attached, false otherwise
     */
    public boolean isAttached() {
        return !attachedSources.isEmpty();
    }


    /**
     * Saves all {@link SoundSource}s that this SoundEffect is currently attached to in the list specified in the parameter.
     *
     * @param saveToList provide an array to save the sources to
     *
     * @return true if the effect is attached to at least one source
     */
    public boolean getAttachedSources(Array<SoundSource> saveToList) {
        if (attachedSources.isEmpty()) {
            return false;
        }
        saveToList.addAll(attachedSources);
        return true;
    }


    int getAuxSlotId() {
        return auxSlotId;
    }


    void addSource(SoundSource source) {
        attachedSources.add(source);
    }


    void removeSource(SoundSource source) {
        attachedSources.removeValue(source, true);
    }


    /**
     * Releases all resources of this effect and detaches it from every sound source it is attached to.
     */
    @Override
    public void dispose() {
        EXTEfx.alDeleteEffects(effectId);
        for (final SoundSource source : attachedSources) {
            source.onEffectDisposal(this);
        }
        attachedSources.clear();
        EXTEfx.alDeleteAuxiliaryEffectSlots(auxSlotId);

        if (!errorLogger.checkLogError("Something went wrong")) {
            logger.trace(this.getClass(), "SoundEffect successfully disposed");
        }
    }

}
