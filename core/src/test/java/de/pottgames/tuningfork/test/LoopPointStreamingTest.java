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
        this.audio = Audio.init();

        this.source = new StreamedSoundSource(Gdx.files.internal("numbers.wav"));
        this.source.setLoopPoints(3f, 7f);
        this.source.setLooping(true);
        this.source.play();
        System.out.println("Sound duration: " + this.source.getDuration() + "s");

    }


    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            this.source.setPlaybackPosition(9f);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            this.source.play();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            this.source.setPlaybackPosition(1f);
        }
        System.out.println("playback position: " + this.source.getPlaybackPosition() + "s");
    }


    @Override
    public void dispose() {
        this.source.dispose();
        this.audio.dispose();
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
