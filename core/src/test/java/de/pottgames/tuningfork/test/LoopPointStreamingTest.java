package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.StreamedSoundSource;

public class LoopPointStreamingTest extends ApplicationAdapter {
    private Audio               audio;
    private StreamedSoundSource source;


    @Override
    public void create() {
        audio = Audio.init();

        source = new StreamedSoundSource(Gdx.files.internal("numbers.wav"));
        source.setLoopPoints(3f, 7f);
        source.setLooping(true);
        source.play();
        System.out.println("Sound duration: " + source.getDuration() + "s");

    }


    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            source.setPlaybackPosition(9f);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            source.play();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            source.setPlaybackPosition(1f);
        }
        System.out.println("playback position: " + source.getPlaybackPosition() + "s");
    }


    @Override
    public void dispose() {
        source.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("LoopPointStreamingTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new LoopPointStreamingTest(), config);
    }

}
