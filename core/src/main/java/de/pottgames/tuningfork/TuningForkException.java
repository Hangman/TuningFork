package de.pottgames.tuningfork;

public class TuningForkException extends Exception {
    private static final long serialVersionUID = -8319979968218789848L;


    public TuningForkException(String message) {
        super(message);
    }


    public TuningForkException(Throwable t) {
        super(t);
    }


    public TuningForkException(String message, Throwable t) {
        super(message, t);
    }

}
