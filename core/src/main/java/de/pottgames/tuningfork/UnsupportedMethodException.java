package de.pottgames.tuningfork;

public class UnsupportedMethodException extends TuningForkRuntimeException {
    private static final long serialVersionUID = 8049796754260879683L;


    public UnsupportedMethodException(String message) {
        super(message);
    }


    public UnsupportedMethodException(Throwable t) {
        super(t);
    }


    public UnsupportedMethodException(String message, Throwable t) {
        super(message, t);
    }

}
