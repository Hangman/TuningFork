package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class SoundEffect implements Disposable {
    private final int          auxSlotId;
    private final int          effectId;
    private Array<SoundSource> attachedSources = new Array<>();


    public SoundEffect(SoundEffectData data) {
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
    }


    /**
     * Sets whether this effect should be automtically adjusted by source and listener position. Enabling this leads to a more realistic impression of the
     * environment. This property should be enabled when using a reverb effect to simulate the environment surrounding a listener or a source.
     *
     * @param value whether this effect should be environmental or "pure"
     */
    public void setEnvironmental(boolean value) {
        EXTEfx.alAuxiliaryEffectSloti(this.auxSlotId, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, value ? AL10.AL_TRUE : AL10.AL_FALSE);
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
    }

}
