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

package de.pottgames.tuningfork.logger;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;

public class ErrorLogger {
    private static final String    TF_UNKNOWN_ERROR_CODE = "TF_UNKNOWN_ERROR_CODE";
    private static final String    AL_INVALID_NAME       = "AL_INVALID_NAME";
    private static final String    AL_INVALID_ENUM       = "AL_INVALID_ENUM";
    private static final String    AL_INVALID_VALUE      = "AL_INVALID_VALUE";
    private static final String    AL_INVALID_OPERATION  = "AL_INVALID_OPERATION";
    private static final String    AL_OUT_OF_MEMORY      = "AL_OUT_OF_MEMORY";
    private static final String    ALC_INVALID_DEVICE    = "ALC_INVALID_DEVICE";
    private static final String    ALC_INVALID_CONTEXT   = "ALC_INVALID_CONTEXT";
    private static final String    ALC_INVALID_ENUM      = "ALC_INVALID_ENUM";
    private static final String    ALC_INVALID_VALUE     = "ALC_INVALID_VALUE";
    private static final String    ALC_OUT_OF_MEMORY     = "ALC_OUT_OF_MEMORY";
    private final Class<?>         clazz;
    private final TuningForkLogger logger;


    public ErrorLogger(Class<?> clazz, TuningForkLogger logger) {
        this.clazz = clazz;
        this.logger = logger;
    }


    public void dismissError() {
        AL10.alGetError();
    }


    public boolean checkLogAlcError(long deviceHandle, String message) {
        final int alcError = ALC10.alcGetError(deviceHandle);
        if (alcError != ALC10.ALC_NO_ERROR) {
            this.logger.error(this.clazz, message + " - " + ErrorLogger.alcErrorToString(alcError));
            return true;
        }

        return false;
    }


    public boolean checkLogError(String message) {
        final int alError = AL10.alGetError();
        if (alError != AL10.AL_NO_ERROR) {
            this.logger.error(this.clazz, message + " - " + ErrorLogger.alErrorToString(alError));
            return true;
        }

        return false;
    }


    public static String alErrorToString(int alError) {
        switch (alError) {
            case AL10.AL_INVALID_NAME:
                return ErrorLogger.AL_INVALID_NAME;
            case AL10.AL_INVALID_ENUM:
                return ErrorLogger.AL_INVALID_ENUM;
            case AL10.AL_INVALID_VALUE:
                return ErrorLogger.AL_INVALID_VALUE;
            case AL10.AL_INVALID_OPERATION:
                return ErrorLogger.AL_INVALID_OPERATION;
            case AL10.AL_OUT_OF_MEMORY:
                return ErrorLogger.AL_OUT_OF_MEMORY;
            default:
                return ErrorLogger.TF_UNKNOWN_ERROR_CODE;
        }
    }


    public static String alcErrorToString(int alcError) {
        switch (alcError) {
            case ALC10.ALC_INVALID_DEVICE:
                return ErrorLogger.ALC_INVALID_DEVICE;
            case ALC10.ALC_INVALID_CONTEXT:
                return ErrorLogger.ALC_INVALID_CONTEXT;
            case ALC10.ALC_INVALID_ENUM:
                return ErrorLogger.ALC_INVALID_ENUM;
            case ALC10.ALC_INVALID_VALUE:
                return ErrorLogger.ALC_INVALID_VALUE;
            case ALC10.ALC_OUT_OF_MEMORY:
                return ErrorLogger.ALC_OUT_OF_MEMORY;
            default:
                return ErrorLogger.TF_UNKNOWN_ERROR_CODE;
        }
    }

}
