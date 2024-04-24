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

import org.lwjgl.openal.AL11;

/**
 * An enum that holds a list of available attenuation models.<br>
 *
 * @see <a href=
 *      "https://github.com/Hangman/TuningFork/wiki/The-Distance-Attenuation-Model">https://github
 *      .com/Hangman/TuningFork/wiki/The-Distance-Attenuation-Model</a>
 *
 * @author Matthias
 *
 */
public enum DistanceAttenuationModel {
    /**
     * Turns the distance attenuation off.
     */
    NONE(AL11.AL_NONE, 1f, 0f, Float.MAX_VALUE),

    /**
     * This text is taken from the official OpenAL documentation. You may want to take a look at
     * <a href="https://www.openal.org/documentation/openal-1.1-specification.pdf"> it</a>.<br>
     * <br>
     * Inverse distance rolloff model, which is equivalent to the IASIG I3DL2 model with the exception that
     * referenceDistance does not imply any clamping. gain
     * = referenceDistance / (referenceDistance + rolloffFactor * (distance - referenceDistance))<br>
     * The referenceDistance parameter used here is a per-source attribute which is the distance at which the
     * listener will experience gain (unless the
     * implementation had to clamp effective gain to the available dynamic range). rolloffFactor is per-source
     * parameter the application can use to increase or
     * decrease the range of a source by decreasing or increasing the attenuation, respectively. The default value is
     * 1. The implementation is free to optimize
     * for a rolloffFactor value of 0, which indicates that the application does not wish any distance attenuation on
     * the respective source.
     */
    INVERSE_DISTANCE(AL11.AL_INVERSE_DISTANCE, 1f, 1f, Float.MAX_VALUE),

    /**
     * This text is taken from the official OpenAL documentation. You may want to take a look at
     * <a href="https://www.openal.org/documentation/openal-1.1-specification.pdf"> it</a>.<br>
     * <br>
     * This is the Inverse Distance Rolloff Model model, extended to guarantee that for distances below
     * AL_REFERENCE_DISTANCE, gain is clamped. This mode is
     * equivalent to the IASIG I3DL2 distance model. distance = max(distance,AL_REFERENCE_DISTANCE); distance = min
     * (distance,AL_MAX_DISTANCE); gain =
     * AL_REFERENCE_DISTANCE / (AL_REFERENCE_DISTANCE + AL_ROLLOFF_FACTOR * (distance – AL_REFERENCE_DISTANCE));
     */
    INVERSE_DISTANCE_CLAMPED(AL11.AL_INVERSE_DISTANCE_CLAMPED, 1f, 1f, Float.MAX_VALUE),

    /**
     * This text is taken from the official OpenAL documentation. You may want to take a look at
     * <a href="https://www.openal.org/documentation/openal-1.1-specification.pdf"> it</a>.<br>
     * <br>
     * This models a linear drop-off in gain as distance increases between the source and listener. distance = min
     * (distance, AL_MAX_DISTANCE) // avoid negative
     * gain gain = (1 – AL_ROLLOFF_FACTOR * (distance – AL_REFERENCE_DISTANCE) / (AL_MAX_DISTANCE –
     * AL_REFERENCE_DISTANCE))
     */
    LINEAR_DISTANCE(AL11.AL_LINEAR_DISTANCE, 1f, 1f, 50f),

    /**
     * This text is taken from the official OpenAL documentation. You may want to take a look at
     * <a href="https://www.openal.org/documentation/openal-1.1-specification.pdf"> it</a>.<br>
     * <br>
     * This is the linear model, extended to guarantee that for distances below AL_REFERENCE_DISTANCE, gain is
     * clamped. distance = max(distance,
     * AL_REFERENCE_DISTANCE) distance = min(distance, AL_MAX_DISTANCE) gain = (1 – AL_ROLLOFF_FACTOR * (distance –
     * AL_REFERENCE_DISTANCE) / (AL_MAX_DISTANCE –
     * AL_REFERENCE_DISTANCE))
     */
    LINEAR_DISTANCE_CLAMPED(AL11.AL_LINEAR_DISTANCE_CLAMPED, 1f, 1f, 50f),

    /**
     * This text is taken from the official OpenAL documentation. You may want to take a look at
     * <a href="https://www.openal.org/documentation/openal-1.1-specification.pdf"> it</a>.<br>
     * <br>
     * This models an exponential dropoff in gain as distance increases between the source and listener. gain =
     * (distance / AL_REFERENCE_DISTANCE) ^ (-
     * AL_ROLLOFF_FACTOR) where the ^ operation raises its first operand to the power of its second operand.
     */
    EXPONENT_DISTANCE(AL11.AL_EXPONENT_DISTANCE, 1f, 1f, Float.MAX_VALUE),

    /**
     * This text is taken from the official OpenAL documentation. You may want to take a look at
     * <a href="https://www.openal.org/documentation/openal-1.1-specification.pdf"> it</a>.<br>
     * <br>
     * This is the exponential model, extended to guarantee that for distances below AL_REFERENCE_DISTANCE, gain is
     * clamped. distance = max(distance,
     * AL_REFERENCE_DISTANCE) distance = min(distance, AL_MAX_DISTANCE) gain = (distance / AL_REFERENCE_DISTANCE) ^
     * (- AL_ROLLOFF_FACTOR)
     *
     */
    EXPONENT_DISTANCE_CLAMPED(AL11.AL_EXPONENT_DISTANCE_CLAMPED, 1f, 1f, Float.MAX_VALUE);


    private final int alId;
    private final float attenuationFactor;
    private final float attenuationMinDistance;
    private final float attenuationMaxDistance;


    DistanceAttenuationModel(
            int alId, float attenuationFactor, float attenuationMinDistance, float attenuationMaxDistance) {
        this.alId = alId;
        this.attenuationFactor = attenuationFactor;
        this.attenuationMinDistance = attenuationMinDistance;
        this.attenuationMaxDistance = attenuationMaxDistance;
    }


    int getAlId() {
        return this.alId;
    }


    float getAttenuationFactor() {
        return this.attenuationFactor;
    }


    float getAttenuationMinDistance() {
        return this.attenuationMinDistance;
    }


    float getAttenuationMaxDistance() {
        return this.attenuationMaxDistance;
    }

}
