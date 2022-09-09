package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.EaxReverb;
import de.pottgames.tuningfork.Flanger;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundEffect;
import de.pottgames.tuningfork.WaveLoader;

public class EffectTest extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private SoundEffect         effect1;
    private SoundEffect         effect2;
    private BufferedSoundSource soundSource;


    @Override
    public void create() {
        // before we can do anything, we need to initialize our Audio instance
        this.audio = Audio.init();

        // load a sound
        this.sound = WaveLoader.load(Gdx.files.internal("numbers.wav"));

        // obtain sound source
        this.soundSource = this.audio.obtainSource(this.sound);
        this.soundSource.setLooping(true);
        this.soundSource.play();

        // create effects
        this.effect1 = new SoundEffect(EaxReverb.domeSaintPauls());
        final Flanger flanger = new Flanger();
        flanger.rate = 7f;
        this.effect2 = new SoundEffect(flanger);

        // attach the effects to the source
        this.soundSource.attachEffect(this.effect1);
        this.soundSource.attachEffect(this.effect2);
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.effect1.dispose();
        this.effect2.dispose();
        this.soundSource.free();
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("EffectTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new EffectTest(), config);
    }

}
