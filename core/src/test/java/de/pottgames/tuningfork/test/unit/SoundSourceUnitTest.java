package de.pottgames.tuningfork.test.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.Filter;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SoundSourceUnitTest {
    private Audio               audio;
    private SoundBuffer         sound;
    private BufferedSoundSource source;
    private Filter              filter;


    @BeforeAll
    public void setup() {
        Gdx.files = new Lwjgl3Files(); // hack setup gdx because we only need Gdx.files in order to run properly

        this.audio = Audio.init(new AudioConfig().setLogger(new ConsoleLogger(LogLevel.INFO_WARN_ERROR)));
        this.sound = WaveLoader.load(Gdx.files.internal("numbers.wav"));
        this.source = this.audio.obtainSource(this.sound);
        this.filter = new Filter(1f, 1f);
    }


    @Test
    public void testVolume() {
        Assertions.assertEquals(this.source.getVolume(), 1f);

        this.source.setVolume(0f);
        Assertions.assertEquals(this.source.getVolume(), 0f);

        this.source.setVolume(2f);
        Assertions.assertEquals(this.source.getVolume(), 1f);

        this.source.setVolume(-1f);
        Assertions.assertEquals(this.source.getVolume(), 0f);

        this.source.setVolume(0.5f);
        Assertions.assertEquals(this.source.getVolume(), 0.5f);

        this.source.setVolume(0.133333333333333333f);
        Assertions.assertEquals(this.source.getVolume(), 0.133333333333333333f);

        this.source.setVolume(1f);
        Assertions.assertEquals(this.source.getVolume(), 1f);
    }


    @Test
    public void testRelative() {
        Assertions.assertEquals(this.source.isRelative(), false);
        this.source.setRelative(true);
        Assertions.assertEquals(this.source.isRelative(), true);
        this.source.setRelative(false);
        Assertions.assertEquals(this.source.isRelative(), false);
    }


    @Test
    public void testPitch() {
        Assertions.assertEquals(this.source.getPitch(), 1f);
        this.source.setPitch(0f);
        Assertions.assertEquals(this.source.getPitch(), 0.5f);

        this.source.setPitch(2f);
        Assertions.assertEquals(this.source.getPitch(), 2f);

        this.source.setPitch(3f);
        Assertions.assertEquals(this.source.getPitch(), 2f);

        this.source.setPitch(-1f);
        Assertions.assertEquals(this.source.getPitch(), 0.5f);

        this.source.setPitch(0.5f);
        Assertions.assertEquals(this.source.getPitch(), 0.5f);

        this.source.setPitch(0.533333333333333333f);
        Assertions.assertEquals(this.source.getPitch(), 0.533333333333333333f);

        this.source.setPitch(1f);
        Assertions.assertEquals(this.source.getPitch(), 1f);
    }


    @Test
    public void testFilter() {
        Assertions.assertEquals(this.source.hasFilter(), false);
        this.source.setFilter(this.filter);
        Assertions.assertEquals(this.source.hasFilter(), true);
        this.source.setFilter(null);
        Assertions.assertEquals(this.source.hasFilter(), false);
    }


    @AfterAll
    public void cleanup() {
        this.filter.dispose();
        this.source.free();
        this.sound.dispose();
        this.audio.dispose();
    }

}
