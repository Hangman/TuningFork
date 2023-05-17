package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.OpenDeviceException;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.UnsupportedAudioDeviceException;
import de.pottgames.tuningfork.WaveLoader;

public class JniTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;


    @Override
    public void create() {
        try {
            this.audio = Audio.initSafe();
        } catch (OpenDeviceException | UnsupportedAudioDeviceException e) {
            e.printStackTrace();
        }

        this.sound = WaveLoader.load(Gdx.files.internal("ima_adpcm_stereo.wav"), false);
        System.out.println("Sound duration: " + this.sound.getDuration() + "s");
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
        config.setTitle("JniTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new JniTest(), config);
    }

}
