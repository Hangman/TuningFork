package de.pottgames.tuningfork;

public class OpenDeviceException extends TuningForkException {
    private static final long serialVersionUID = -6963586216442321879L;


    public OpenDeviceException(String message) {
        super(message);
    }


    public OpenDeviceException(Throwable t) {
        super(t);
    }


    public OpenDeviceException(String message, Throwable t) {
        super(message, t);
    }

}
