package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.decoder.AudioStream;
import de.pottgames.tuningfork.decoder.Mp3InputStream;

/**
 * TuningFork doesn't officially support the mp3 file format. If you ultimatively have to play mp3 for whatever reason, this should demonstrate how you can
 * stream it via TuningFork.<br>
 * Note that looping/seeking may cause problems related to the mp3 file format. Also the duration is not available when a mp3 is streamed. Use
 * {@link SoundBuffer} to get the duration.
 *
 * @author Matthias
 *
 */
public class Mp3StreamDemo extends ApplicationAdapter {
    private Audio               audio;
    private StreamedSoundSource source;


    @Override
    public void create() {
        this.audio = Audio.init();

        final AudioStream stream = new Mp3InputStream(Gdx.files.internal("numbers_stereo.mp3"));
        this.source = new StreamedSoundSource(stream);
        System.out.println("Sound duration: " + this.source.getDuration() + "s"); // this will always be -1
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
        config.setTitle("Mp3StreamDemo");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new Mp3StreamDemo(), config);
    }

}
