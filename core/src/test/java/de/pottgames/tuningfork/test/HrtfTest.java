package de.pottgames.tuningfork.test;

import java.io.File;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.ConsoleLogger;
import de.pottgames.tuningfork.ConsoleLogger.LogLevel;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;

public class HrtfTest extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         soundBuffer;
    private BufferedSoundSource soundSource;
    private float               angle;


    @Override
    public void create() {
        // INIT AUDIO
        final ConsoleLogger logger = new ConsoleLogger();
        logger.setLogLevel(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR);
        final AudioConfig config = new AudioConfig();
        config.setLogger(logger);
        this.audio = Audio.init(config);

        // ENABLE HRTF
        final Array<String> hrtfs = this.audio.getDevice().getAvailableHrtfs();
        if (hrtfs.isEmpty()) {
            System.out.println("no hrtfs available");
            return;
        }
        hrtfs.forEach(name -> System.out.println("available hrtf: " + name));
        this.audio.getDevice().enableHrtf(hrtfs.get(0));
        // this.audio.getDevice().disableHrtf();

        // LOAD SOUND
        final File soundFile = new File("src/test/resources/numbers.wav");
        this.soundBuffer = WaveLoader.load(soundFile);

        // OBTAIN SOURCE & PLAY SOUND
        this.soundSource = this.audio.obtainSource(this.soundBuffer);
        this.soundSource.setLooping(true);
        this.soundSource.setRelative(true);
        this.soundSource.play();
    }


    @Override
    public void render() {
        this.angle += Math.PI / 4f / 100f;
        this.soundSource.setPosition(MathUtils.sin(this.angle), 0f, -MathUtils.cos(this.angle));
    }


    @Override
    public void dispose() {
        this.soundSource.free();
        this.soundBuffer.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("MiniExample");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        new Lwjgl3Application(new HrtfTest(), config);
    }

}
