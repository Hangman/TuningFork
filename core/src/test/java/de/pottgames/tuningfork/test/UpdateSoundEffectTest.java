package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import de.pottgames.tuningfork.*;

public class UpdateSoundEffectTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;
    private SoundSource source;
    private SoundEffect effect;
    private long        tick = System.currentTimeMillis();

    private SoundEffectData[] effectData      =
            {AutoWah.funkyBeats(), Chorus.chore(), new Compressor(), Distortion.rattle(), EaxReverb.auditorium(),
                    Echo.farAway(), new Equalizer(), Flanger.robotMetallic(), new FrequencyShifter(),
                    PitchShifter.chipmunk(), new Reverb(), RingModulator.tremolo(), new VocalMorpher()};
    private int               effectDataIndex = this.effectData.length;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));
        this.source = this.audio.obtainSource(this.sound);
        this.source.setFilter(0f, 0f);
        this.source.setLooping(true);
        this.source.play();

        this.effect = new SoundEffect(this.getNextEffectData());
        this.source.attachEffect(this.effect);
    }


    @Override
    public void render() {
        if (this.tick + 2000 < System.currentTimeMillis()) {
            this.tick = System.currentTimeMillis();
            final SoundEffectData data = this.getNextEffectData();
            this.effect.updateEffect(data);
        }
    }


    private SoundEffectData getNextEffectData() {
        this.effectDataIndex++;
        if (this.effectDataIndex >= this.effectData.length) {
            this.effectDataIndex = 0;
        }
        final SoundEffectData data = this.effectData[this.effectDataIndex];
        System.out.println(data.getClass().toString());
        return data;
    }


    @Override
    public void dispose() {
        this.effect.dispose();
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("UpdateSoundEffectTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new UpdateSoundEffectTest(), config);
    }

}
