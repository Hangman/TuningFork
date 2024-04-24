package de.pottgames.tuningfork.benchmark;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.MockLogger;
import org.openjdk.jmh.annotations.*;

import java.io.File;

@State(Scope.Thread)
public class LoadULawWav {
    private Audio       audio;
    private SoundBuffer soundBuffer;


    @Benchmark
    public void load() {
        this.soundBuffer = WaveLoader.load(new File("src/jmh/resources/bench_ulaw.wav"));
    }


    @Setup(Level.Iteration)
    public void setup() {
        Gdx.files = new Lwjgl3Files();
        final AudioConfig config = new AudioConfig();
        config.setLogger(new MockLogger());
        this.audio = Audio.init(config);
    }


    @TearDown(Level.Iteration)
    public void teardown() {
        this.soundBuffer.dispose();
        this.audio.dispose();
    }

}
