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
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RelativeUnitTest {
    Audio               audio;
    SoundBuffer         sound;
    BufferedSoundSource source;


    @BeforeAll
    public void setup() {
        Gdx.files = new Lwjgl3Files(); // hack setup gdx because we only need Gdx.files in order to run properly

        this.audio = Audio.init(new AudioConfig().setLogger(new ConsoleLogger(LogLevel.INFO_WARN_ERROR)));
        this.sound = WaveLoader.load(Gdx.files.internal("numbers.wav"));
        this.source = this.audio.obtainSource(this.sound);
    }


    @Test
    public void test() {
        Assertions.assertEquals(this.source.isRelative(), false);
        this.source.setRelative(true);
        Assertions.assertEquals(this.source.isRelative(), true);
        this.source.setRelative(false);
        Assertions.assertEquals(this.source.isRelative(), false);
    }


    @AfterAll
    public void cleanup() {
        this.source.free();
        this.sound.dispose();
        this.audio.dispose();
    }

}
