package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.decoder.AudioStream;
import de.pottgames.tuningfork.decoder.WavInputStream;

public class SoundLoaderAudioStreamTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;


    @Override
    public void create() {
        audio = Audio.init();
        final AudioStream stream = new WavInputStream(Gdx.files.internal("numbers.wav"));
        sound = SoundLoader.load(stream);
        sound.play();
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SoundLoaderAudioStreamTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new SoundLoaderAudioStreamTest(), config);
    }

}
