package de.pottgames.tuningfork.logger;

import com.badlogic.gdx.Gdx;

public class GdxLogger implements TuningForkLogger {
    private static final String TAG = "TuningFork";


    @Override
    public void error(Class<?> clazz, String message) {
        Gdx.app.error(GdxLogger.TAG, message);
    }


    @Override
    public void warn(Class<?> clazz, String message) {
        Gdx.app.error(GdxLogger.TAG, message);
    }


    @Override
    public void info(Class<?> clazz, String message) {
        Gdx.app.log(GdxLogger.TAG, message);
    }


    @Override
    public void debug(Class<?> clazz, String message) {
        Gdx.app.debug(GdxLogger.TAG, message);
    }


    @Override
    public void trace(Class<?> clazz, String message) {
        Gdx.app.debug(GdxLogger.TAG, message);
    }

}
