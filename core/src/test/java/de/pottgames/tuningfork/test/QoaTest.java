package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import de.pottgames.tuningfork.*;

public class QoaTest extends ApplicationAdapter {
    private static final String              SOUND_FILE_PATH1 = "42_accordion_melodious_phrase_stereo.qoa";
    private static final String              SOUND_FILE_PATH2 = "04_electronic_gong_400hz_mono.qoa";
    private static final String              SOUND_FILE_PATH3 = "ambience_forest_birds_mono_02.qoa";
    private              Audio               audio;
    private              SoundBuffer         sound;
    private              SoundBuffer         asyncSound       = null;
    private              StreamedSoundSource streamedSound;
    private              AssetManager        assetManager;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.assetManager = new AssetManager();
        this.assetManager.setLoader(SoundBuffer.class, new SoundBufferLoader(new InternalFileHandleResolver()));

        // SoundBuffer
        this.sound = SoundLoader.load(Gdx.files.internal(QoaTest.SOUND_FILE_PATH1));
        System.out.println("SoundBuffer duration: " + this.sound.getDuration() + "s");
        this.sound.play();

        // StreamedSoundSource
        this.streamedSound = new StreamedSoundSource(Gdx.files.internal(QoaTest.SOUND_FILE_PATH2));
        System.out.println("Streamed sound duration: " + this.streamedSound.getDuration() + "s");
        this.streamedSound.play();

        // Async loading
        this.assetManager.load(QoaTest.SOUND_FILE_PATH3, SoundBuffer.class);
    }


    @Override
    public void render() {
        if (this.asyncSound == null && this.assetManager.update()) {
            this.asyncSound = this.assetManager.get(QoaTest.SOUND_FILE_PATH3, SoundBuffer.class);
            System.out.println("Async SoundBuffer duration: " + this.asyncSound.getDuration() + "s");
            this.asyncSound.play();
        }
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.streamedSound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("QoaTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new QoaTest(), config);
    }

}
