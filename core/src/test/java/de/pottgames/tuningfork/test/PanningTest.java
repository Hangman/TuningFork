package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;

public class PanningTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;
    private boolean     left;
    private long        soundPlayStartTime;
    private float       soundDuration;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.sound = WaveLoader.load(Gdx.files.internal("rhythm.wav"));
        this.soundDuration = this.sound.getDuration() * 1000f;
    }


    @Override
    public void render() {
        if (System.currentTimeMillis() > this.soundPlayStartTime + (long) this.soundDuration) {
            this.left = !this.left;
            this.playSound(this.left ? -1f : 1f);
        }
    }


    private void playSound(float pan) {
        this.sound.play(1f, 1f, pan);
        this.soundPlayStartTime = System.currentTimeMillis();
    }


    @Override
    public void dispose() {
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PanningTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PanningTest(), config);
    }

}
