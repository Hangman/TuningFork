package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.Mp3Loader;
import de.pottgames.tuningfork.SoundBuffer;

/**
 * TuningFork doesn't officially support the mp3 file format. If you ultimatively have to play mp3 for whatever reason, this should demonstrate how you can
 * decode an mp3 file and play it through TuningFork.<br>
 * Note that looping/seeking may cause problems related to the mp3 file format. Also the duration returned by TuningFork might not be as precise as with the
 * supported formats due to the same reason.
 *
 * @author Matthias
 *
 */
public class Mp3LoadDemo extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.sound = Mp3Loader.load(Gdx.files.internal("numbers.mp3"));
        System.out.println("Sound duration: " + this.sound.getDuration());
        this.sound.play();
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mp3LoadDemo");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new Mp3LoadDemo(), config);
    }

}
