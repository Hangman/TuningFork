/**
 * Copyright 2023 Matthias Finke
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

import de.pottgames.tuningfork.AudioConfig.Spatialization;
import de.pottgames.tuningfork.AudioConfig.Virtualization;

class AudioSettings {
    private float          minAttenuationDistance = 1f;
    private float          maxAttenuationDistance = Float.MAX_VALUE;
    private float          attenuationFactor      = 1f;
    private Virtualization virtualization         = Virtualization.ON;
    private Spatialization spatialization         = Spatialization.ON;
    private int            resamplerIndex         = -1;


    public float getMinAttenuationDistance() {
        return minAttenuationDistance;
    }


    public float getMaxAttenuationDistance() {
        return maxAttenuationDistance;
    }


    public float getAttenuationFactor() {
        return attenuationFactor;
    }


    public Virtualization getVirtualization() {
        return virtualization;
    }


    public Spatialization getSpatialization() {
        return spatialization;
    }


    public int getResamplerIndex() {
        return resamplerIndex;
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
