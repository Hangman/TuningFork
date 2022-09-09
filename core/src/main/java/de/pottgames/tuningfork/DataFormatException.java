package de.pottgames.tuningfork;

public class DataFormatException extends RuntimeException {
    private static final long serialVersionUID = -3244812830251168633L;


    public DataFormatException(String message) {
        super(message);
    }


    public DataFormatException(Throwable t) {
        super(t);
    }


    public DataFormatException(String message, Throwable t) {
        super(message, t);
    }

}
