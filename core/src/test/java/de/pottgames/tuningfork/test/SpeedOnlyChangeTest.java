package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.Filter;
import de.pottgames.tuningfork.OggLoader;
import de.pottgames.tuningfork.PitchShifter;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundEffect;
import de.pottgames.tuningfork.SoundSource;

public class SpeedOnlyChangeTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;
    private SoundSource source;
    private SoundEffect effect;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.sound = OggLoader.load(Gdx.files.internal("carnivalrides.ogg"));
        this.source = this.audio.obtainSource(this.sound);

        // set source pitch to change the speed
        final float pitch = 1.2f;
        this.source.setPitch(pitch);

        // apply pitch correction
        this.effect = new SoundEffect(new PitchShifter().correctPitch(pitch));
        this.source.attachEffect(this.effect);

        // in order to only hear the pitch corrected sound, we must silence the original sound with a filter
        final Filter filter = new Filter(0f, 0f);
        this.source.setFilter(filter);
        filter.dispose();

        this.source.play();
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.effect.dispose();
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SpeedOnlyChangeTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new SpeedOnlyChangeTest(), config);
    }

}
