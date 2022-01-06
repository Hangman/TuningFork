package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.PcmSoundSource;
import de.pottgames.tuningfork.WavInputStream;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class SimplePcmSoundSourceTest extends ApplicationAdapter {
    private static final int BUFFER_SIZE = 4096 * 10;
    private Audio            audio;
    private byte[]           pcm         = new byte[SimplePcmSoundSourceTest.BUFFER_SIZE];
    private WavInputStream   stream;
    private PcmSoundSource   pcmSource;
    private long             lastPcmPush;


    @Override
    public void create() {
        final AudioConfig config = new AudioConfig();
        config.setLogger(new ConsoleLogger(LogLevel.DEBUG_INFO_WARN_ERROR));
        this.audio = Audio.init(config);
        this.stream = new WavInputStream(Gdx.files.internal("src/test/resources/numbers.wav"));
        this.pcmSource = new PcmSoundSource(this.stream.getSampleRate(), PcmFormat.MONO_16_BIT);
        this.pcmSource.setLooping(false);
    }


    @Override
    public void render() {
        if (System.currentTimeMillis() > this.lastPcmPush + 500) {
            this.lastPcmPush = System.currentTimeMillis();
            final int readData = this.stream.read(this.pcm);
            if (readData != -1) {
                this.pcmSource.queueSamples(this.pcm, 0, readData);
                this.pcmSource.play();
            }
        }
    }


    @Override
    public void dispose() {
        this.pcmSource.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SimplePcmSoundSourceTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new SimplePcmSoundSourceTest(), config);
    }

}
