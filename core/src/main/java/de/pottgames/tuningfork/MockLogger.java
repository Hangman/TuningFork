package de.pottgames.tuningfork;

import de.pottgames.tuningfork.logger.TuningForkLogger;

class MockLogger implements TuningForkLogger {

    @Override
    public void error(Class<?> clazz, String message) {
    }


    @Override
    public void warn(Class<?> clazz, String message) {
    }


    @Override
    public void info(Class<?> clazz, String message) {
    }


    @Override
    public void debug(Class<?> clazz, String message) {
    }


    @Override
    public void trace(Class<?> clazz, String message) {
    }

}
