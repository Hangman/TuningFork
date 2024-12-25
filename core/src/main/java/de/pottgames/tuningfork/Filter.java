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

import com.badlogic.gdx.math.MathUtils;

import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A filter is used to remove high and low frequency content from a signal and can be applied to multiple sound sources. It's not possible to specify the
 * frequency ranges, use the {@link Equalizer} effect, if you want more control.
 *
 * @author Matthias
 *
 */
class Filter {
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final int              id;


    /**
     * Creates a band-pass filter with the given parameters.
     *
     * @param volumeLf volume of the low frequencies, range: 0 - 1
     * @param volumeHf volume of the high frequencies, range: 0 - 1
     */
    Filter(float volumeLf, float volumeHf) {
        logger = Audio.get().getLogger();
        errorLogger = new ErrorLogger(this.getClass(), logger);
        errorLogger.dismissError();

        id = EXTEfx.alGenFilters();
        EXTEfx.alFilteri(id, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_BANDPASS);
        if (!errorLogger.checkLogError("Failed to create filter")) {
            logger.debug(this.getClass(), "Successfully created filter.");
        }
        EXTEfx.alFilterf(id, EXTEfx.AL_BANDPASS_GAIN, 1f);
        setLowFrequencyVolume(volumeLf);
        setHighFrequencyVolume(volumeHf);
    }


    int getId() {
        return id;
    }


    /**
     * Sets the factor by which the low frequencies should be attenuated.
     *
     * @param volume range: 0 - 1, 0 means completely silent, 1 means full loudness
     */
    void setLowFrequencyVolume(float volume) {
        volume = MathUtils.clamp(volume, 0f, 1f);
        EXTEfx.alFilterf(id, EXTEfx.AL_BANDPASS_GAINLF, volume);
    }


    /**
     * Sets the factor by which the high frequencies should be attenuated.
     *
     * @param volume range: 0 - 1, 0 means completely silent, 1 means full loudness
     */
    void setHighFrequencyVolume(float volume) {
        volume = MathUtils.clamp(volume, 0f, 1f);
        EXTEfx.alFilterf(id, EXTEfx.AL_BANDPASS_GAINHF, volume);
    }


    void dispose() {
        errorLogger.dismissError();
        EXTEfx.alDeleteFilters(id);
        if (!errorLogger.checkLogError("Failed to dispose filter")) {
            logger.debug(this.getClass(), "Filter disposed.");
        }
    }

}
