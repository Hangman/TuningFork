package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.WaveLoader;

public class WavALawTest extends ApplicationAdapter {
    private static final String FILE_PATH = "numbers-alaw.wav";
    private Audio               audio;
    private SoundBuffer         sound;
    private SoundSource         bufferedSource;
    private StreamedSoundSource streamedSource;
    private SoundSource         activeSource;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.sound = WaveLoader.load(Gdx.files.internal(WavALawTest.FILE_PATH));
        this.streamedSource = new StreamedSoundSource(Gdx.files.internal(WavALawTest.FILE_PATH));
        this.bufferedSource = this.audio.obtainSource(this.sound);
        System.out.println("Sound duration (streamed): " + this.streamedSource.getDuration() + "s");
        System.out.println("Sound duration (buffered): " + this.sound.getDuration() + "s");

        this.activeSource = this.bufferedSource;
    }


    @Override
    public void render() {
        if (!this.activeSource.isPlaying()) {
            if (this.activeSource == this.bufferedSource) {
                this.activeSource = this.streamedSource;
                System.out.println("streamed sound playing");
            } else {
                this.activeSource = this.bufferedSource;
                System.out.println("buffered sound playing");
            }

            this.activeSource.play();
        }
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("WavALawTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new WavALawTest(), config);
    }
}
