package de.pottgames.tuningfork;

public class UnsupportedAudioDeviceException extends TuningForkException {
    private static final long serialVersionUID = 5966425090417508598L;


    public UnsupportedAudioDeviceException(String message) {
        super(message);
    }


    public UnsupportedAudioDeviceException(Throwable t) {
        super(t);
    }


    public UnsupportedAudioDeviceException(String message, Throwable t) {
        super(message, t);
    }

}
