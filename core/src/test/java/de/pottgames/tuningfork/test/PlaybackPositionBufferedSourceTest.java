package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class PlaybackPositionBufferedSourceTest extends ApplicationAdapter implements InputAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private BufferedSoundSource soundSource;


    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);

        // before we can do anything, we need to initialize our Audio instance
        final AudioConfig config = new AudioConfig();
        config.setLogger(new ConsoleLogger(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR));
        this.audio = Audio.init(config);

        // load a sound
        this.sound = WaveLoader.load(Gdx.files.internal("src/test/resources/numbers.wav"));

        // play the sound
        this.soundSource = this.audio.obtainSource(this.sound);
        this.soundSource.setLooping(true);
        this.soundSource.play();
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        final float seconds = Rng.get(-20f, 20f);
        System.out.println("setting position to: " + seconds);
        this.soundSource.setPlaybackPosition(seconds);
        return true;
    }


    @Override
    public void render() {
        System.out.println("playback position: " + this.soundSource.getPlaybackPosition());
    }


    @Override
    public void dispose() {
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PlaybackPositionBufferedSourceTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PlaybackPositionBufferedSourceTest(), config);
    }

}
