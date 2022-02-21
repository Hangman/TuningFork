package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.StreamedSoundSource;

public class StreamedSoundSourceTest extends ApplicationAdapter {
    private Audio               audio;
    private StreamedSoundSource source;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.source = new StreamedSoundSource(Gdx.files.internal("src/test/resources/numbers_8bit_mono.wav"));
        System.out.println("Sound duration: " + this.source.getDuration() + "s");
        this.source.setLooping(true);
        this.source.play();
    }


    @Override
    public void render() {
        System.out.println("current playback position: " + this.source.getPlaybackPosition() + "s");

        // PRESS SPACE TO SKIP TO 5s
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            this.source.setPlaybackPosition(5f);
        }
    }


    @Override
    public void dispose() {
        this.source.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("StreamedSoundSourceTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new StreamedSoundSourceTest(), config);
    }

}
