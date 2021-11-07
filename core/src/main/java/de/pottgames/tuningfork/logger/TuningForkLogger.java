package de.pottgames.tuningfork.logger;

public interface TuningForkLogger {

    void error(Class<?> clazz, String message);


    void warn(Class<?> clazz, String message);


    void info(Class<?> clazz, String message);


    void debug(Class<?> clazz, String message);


    void trace(Class<?> clazz, String message);

}
