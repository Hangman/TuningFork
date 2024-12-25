package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;

public class LoopPointSoundBufferTest extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private BufferedSoundSource source;


    @Override
    public void create() {
        audio = Audio.init();

        sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));
        sound.setLoopPoints(3f, 7f);
        final float[] loopPoints = sound.getLoopPoints();
        System.out.println("Sound duration: " + sound.getDuration() + "s");
        System.out.println("loop points: " + loopPoints[0] + " | " + loopPoints[1]);

        source = audio.obtainSource(sound);
        source.setLooping(true);
        source.play();
    }


    @Override
    public void render() {
        System.out.println("playback position: " + source.getPlaybackPosition() + "s");
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("LoopPointSoundBufferTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new LoopPointSoundBufferTest(), config);
    }

}
