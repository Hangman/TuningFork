package de.pottgames.tuningfork;

public class TuningForkRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 5472319899981925340L;


    public TuningForkRuntimeException(String message) {
        super(message);
    }


    public TuningForkRuntimeException(Throwable t) {
        super(t);
    }


    public TuningForkRuntimeException(String message, Throwable t) {
        super(message, t);
    }

}
