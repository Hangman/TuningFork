package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.Filter;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class FilterTest extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private Filter[]            filters     = new Filter[3];
    private int                 filterIndex = 0;
    private BufferedSoundSource soundSource;


    @Override
    public void create() {
        // before we can do anything, we need to initialize our Audio instance
        final AudioConfig config = new AudioConfig();
        config.setLogger(new ConsoleLogger(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR));
        this.audio = Audio.init(config);

        // load a sound
        this.sound = WaveLoader.load(Gdx.files.internal("src/test/resources/numbers.wav"));

        // obtain sound source
        this.soundSource = this.audio.obtainSource(this.sound);

        // create filters
        this.filters[0] = new Filter(1f, 1f); // has no effect
        this.filters[1] = new Filter(0.01f, 1f); // only high frequencies
        this.filters[2] = new Filter(1f, 0.01f); // only low frequencies
    }


    @Override
    public void render() {
        if (!this.soundSource.isPlaying()) {
            this.soundSource.setFilter(this.filters[this.filterIndex]);
            this.soundSource.play();
            this.filterIndex++;
            if (this.filterIndex >= this.filters.length) {
                this.filterIndex = 0;
            }
        }
    }


    @Override
    public void dispose() {
        for (final Filter filter : this.filters) {
            filter.dispose();
        }
        this.soundSource.free();
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("FilterTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new FilterTest(), config);
    }

}
