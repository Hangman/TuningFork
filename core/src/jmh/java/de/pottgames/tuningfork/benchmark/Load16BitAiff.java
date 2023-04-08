package de.pottgames.tuningfork.benchmark;

import java.io.File;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import de.pottgames.tuningfork.AiffLoader;
import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.logger.MockLogger;

@State(Scope.Thread)
public class Load16BitAiff {
    private Audio       audio;
    private SoundBuffer soundBuffer;


    @Benchmark
    public void load() {
        this.soundBuffer = AiffLoader.load(new File("src/jmh/resources/bench_16bit.aiff"));
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
