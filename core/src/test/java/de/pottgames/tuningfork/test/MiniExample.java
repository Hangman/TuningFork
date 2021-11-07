package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class MiniExample extends ApplicationAdapter {

    @Override
    public void create() {

    }


    @Override
    public void render() {

    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("MiniExample");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new MiniExample(), config);
    }

}
