package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AutoWah;
import de.pottgames.tuningfork.Chorus;
import de.pottgames.tuningfork.Compressor;
import de.pottgames.tuningfork.Distortion;
import de.pottgames.tuningfork.EaxReverb;
import de.pottgames.tuningfork.Echo;
import de.pottgames.tuningfork.Equalizer;
import de.pottgames.tuningfork.Flanger;
import de.pottgames.tuningfork.FrequencyShifter;
import de.pottgames.tuningfork.PitchShifter;
import de.pottgames.tuningfork.Reverb;
import de.pottgames.tuningfork.RingModulator;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundEffect;
import de.pottgames.tuningfork.SoundEffectData;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.VocalMorpher;

public class UpdateSoundEffectTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;
    private SoundSource source;
    private SoundEffect effect;
    private long        tick = System.currentTimeMillis();

    private SoundEffectData[] effectData      =
            { AutoWah.funkyBeats(), Chorus.chore(), new Compressor(), Distortion.rattle(), EaxReverb.auditorium(), Echo.farAway(), new Equalizer(),
                    Flanger.robotMetallic(), new FrequencyShifter(), PitchShifter.chipmunk(), new Reverb(), RingModulator.tremolo(), new VocalMorpher() };
    private int               effectDataIndex = effectData.length;


    @Override
    public void create() {
        audio = Audio.init();
        sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));
        source = audio.obtainSource(sound);
        source.setFilter(0f, 0f);
        source.setLooping(true);
        source.play();

        effect = new SoundEffect(getNextEffectData());
        source.attachEffect(effect);
    }


    @Override
    public void render() {
        if (tick + 2000 < System.currentTimeMillis()) {
            tick = System.currentTimeMillis();
            final SoundEffectData data = getNextEffectData();
            effect.updateEffect(data);
        }
    }


    private SoundEffectData getNextEffectData() {
        effectDataIndex++;
        if (effectDataIndex >= effectData.length) {
            effectDataIndex = 0;
        }
        final SoundEffectData data = effectData[effectDataIndex];
        System.out.println(data.getClass().toString());
        return data;
    }


    @Override
    public void dispose() {
        effect.dispose();
        sound.dispose();
        audio.dispose();
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
