package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;

public class PlayStartDelayTest extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private BufferedSoundSource source;


    @Override
    public void create() {
        audio = Audio.init();

        final long currentTime = audio.getDevice().getClockTime();

        // load a sound
        sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));

        // play via fire & forget but delayed
        final long startTime = currentTime + PlayStartDelayTest.milliToNano(1000L);
        sound.playAtTime(-5L);
        System.out.println("The error is expected. If no error is logged, this test failed.");
        sound.playAtTime(startTime);

        // play via source right after
        source = audio.obtainSource(sound);
        source.playAtTime(startTime + PlayStartDelayTest.milliToNano((long) (source.getDuration() * 1000)));
        source.play(); // this should have no effect

        System.out.println("The test will finish after counting to ten twice.");
    }


    @Override
    public void render() {
        if (!source.isPlaying()) {
            System.out.println("Test finished");
            System.exit(0);
        }
    }


    private static long milliToNano(long millis) {
        return millis * 1_000_000;
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PlayStartDelayTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PlayStartDelayTest(), config);
    }

}
