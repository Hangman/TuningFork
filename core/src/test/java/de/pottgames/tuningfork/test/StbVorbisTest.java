package de.pottgames.tuningfork.test;

import java.io.File;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.OggLoader;
import de.pottgames.tuningfork.SoundBuffer;

public class StbVorbisTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound, sound2;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.sound = OggLoader.loadNonPacked("src/test/resources/numbers2.ogg");
        this.sound2 = OggLoader.load(new File("src/test/resources/numbers2.ogg"));
        System.out.println("Sound duration: " + this.sound.getDuration() + "s");
        System.out.println("Sound2 duration: " + this.sound.getDuration() + "s");

        this.sound.play();
        try {
            Thread.sleep(100);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        this.sound2.play();
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.sound2.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("StbVorbisTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new StbVorbisTest(), config);
    }

}
