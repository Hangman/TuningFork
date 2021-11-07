package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundBufferLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class AsyncLoadTest extends ApplicationAdapter {
    private static final String FILE_PATH = "src/test/resources/numbers.wav";
    private Audio               audio;
    private AssetManager        assetManager;
    private SoundBuffer         soundBuffer;


    @Override
    public void create() {
        // INIT AUDIO
        final ConsoleLogger logger = new ConsoleLogger();
        logger.setLogLevel(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR);
        final AudioConfig config = new AudioConfig();
        config.setLogger(logger);
        this.audio = Audio.init(config);

        // LOAD SOUND ASYNC
        this.assetManager = new AssetManager();
        final FileHandleResolver resolver = new InternalFileHandleResolver();
        this.assetManager.setLoader(SoundBuffer.class, new SoundBufferLoader(resolver));
        this.assetManager.load(AsyncLoadTest.FILE_PATH, SoundBuffer.class);
        this.assetManager.finishLoading();
        this.soundBuffer = this.assetManager.get(AsyncLoadTest.FILE_PATH, SoundBuffer.class);

        // PLAY SOUND
        this.audio.play(this.soundBuffer);
    }


    @Override
    public void render() {

    }


    @Override
    public void dispose() {
        this.assetManager.unload(AsyncLoadTest.FILE_PATH);
        this.assetManager.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("AsyncLoadTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new AsyncLoadTest(), config);
    }

}
