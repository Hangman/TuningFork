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

public class WavFloat64PcmTest extends ApplicationAdapter {
    private static final String SOUND_PATH = "64bit_float_numbers.wav";
    private Audio               audio;
    private SoundBuffer         sound;
    private SoundSource         bufferedSource;
    private StreamedSoundSource streamedSource;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.sound = WaveLoader.load(Gdx.files.internal(WavFloat64PcmTest.SOUND_PATH));
        this.bufferedSource = this.audio.obtainSource(this.sound);
        this.streamedSource = new StreamedSoundSource(Gdx.files.internal(WavFloat64PcmTest.SOUND_PATH));
        this.bufferedSource.setLooping(true);
        this.streamedSource.setLooping(true);
        this.bufferedSource.play();
        try {
            Thread.sleep(200);
        } catch (final InterruptedException e) {
            // ignore
        }
        this.streamedSource.play();
        System.out.println("buffered duration: " + this.sound.getDuration() + "s");
        System.out.println("streamed duration: " + this.streamedSource.getDuration() + "s");
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("WavFloat64PcmTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new WavFloat64PcmTest(), config);
    }
}