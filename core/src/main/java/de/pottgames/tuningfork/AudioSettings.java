package de.pottgames.tuningfork;

import de.pottgames.tuningfork.AudioConfig.Spatialization;
import de.pottgames.tuningfork.AudioConfig.Virtualization;

public class AudioSettings {
    private float          minAttenuationDistance = 1f;
    private float          maxAttenuationDistance = Float.MAX_VALUE;
    private float          attenuationFactor      = 1f;
    private Virtualization virtualization         = Virtualization.ON;
    private Spatialization spatialization         = Spatialization.ON;
    private int            resamplerIndex         = -1;


    public float getMinAttenuationDistance() {
        return this.minAttenuationDistance;
    }


    public float getMaxAttenuationDistance() {
        return this.maxAttenuationDistance;
    }


    public float getAttenuationFactor() {
        return this.attenuationFactor;
    }


    public Virtualization getVirtualization() {
        return this.virtualization;
    }


    public Spatialization getSpatialization() {
        return this.spatialization;
    }


    public int getResamplerIndex() {
        return this.resamplerIndex;
    }


    protected void setMinAttenuationDistance(float minAttenuationDistance) {
        this.minAttenuationDistance = minAttenuationDistance;
    }


    protected void setMaxAttenuationDistance(float maxAttenuationDistance) {
        this.maxAttenuationDistance = maxAttenuationDistance;
    }


    protected void setAttenuationFactor(float attenuationFactor) {
        this.attenuationFactor = attenuationFactor;
    }


    protected void setVirtualization(Virtualization virtualization) {
        this.virtualization = virtualization;
    }


    protected void setSpatialization(Spatialization spatialization) {
        this.spatialization = spatialization;
    }


    protected void setResamplerIndex(int resamplerIndex) {
        this.resamplerIndex = resamplerIndex;
    }

}
