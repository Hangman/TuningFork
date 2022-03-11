package de.pottgames.tuningfork.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.OggLoader;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.logger.MockLogger;

@State(Scope.Thread)
public class LoadQuality5Ogg {
    private Audio       audio;
    private SoundBuffer soundBuffer;


    @Benchmark
    public void load() throws FileNotFoundException {
        this.soundBuffer = OggLoader.load(new FileInputStream(new File("src/jmh/resources/bench_5.ogg")));
    }


    @Setup(Level.Iteration)
    public void setup() {
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
