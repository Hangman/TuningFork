package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;

class ErrorLogger {
    private static final String    TF_UNKNOWN_ERROR_CODE = "TF_UNKNOWN_ERROR_CODE";
    private static final String    AL_INVALID_NAME       = "AL_INVALID_NAME";
    private static final String    AL_INVALID_ENUM       = "AL_INVALID_ENUM";
    private static final String    AL_INVALID_VALUE      = "AL_INVALID_VALUE";
    private static final String    AL_INVALID_OPERATION  = "AL_INVALID_OPERATION";
    private static final String    AL_OUT_OF_MEMORY      = "AL_OUT_OF_MEMORY";
    private final Class<?>         clazz;
    private final TuningForkLogger logger;


    ErrorLogger(Class<?> clazz, TuningForkLogger logger) {
        this.clazz = clazz;
        this.logger = logger;
    }


    boolean checkLogError(String message) {
        final int alError = AL10.alGetError();
        if (alError != AL10.AL_NO_ERROR) {
            this.logger.error(this.clazz, message + " - " + ErrorLogger.alErrorToString(alError));
            return true;
        }

        return false;
    }


    static String alErrorToString(int alError) {
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

}
