/**
 * Copyright 2023 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.AudioDeviceConfig;
import de.pottgames.tuningfork.AutoWah;
import de.pottgames.tuningfork.Chorus;
import de.pottgames.tuningfork.Echo;
import de.pottgames.tuningfork.PitchShifter;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundEffect;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class AuxSendsTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;


    @Override
    public void create() {
        // AUDIO CONFIG
        final AudioConfig config = new AudioConfig();
        config.setLogger(new ConsoleLogger(LogLevel.INFO_WARN_ERROR));

        // AUDIO DEVICE CONFIG
        final AudioDeviceConfig deviceConfig = config.getDeviceConfig();
        deviceConfig.setEffectSlots(3);

        // INIT
        audio = Audio.init(config);
        sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));
        System.out.println("available effect slots: " + audio.getDevice().getNumberOfEffectSlots());

        // PLAY
        final SoundSource source = audio.obtainSource(sound);
        source.setLooping(true);
        source.setFilter(0f, 0f);
        source.attachEffect(new SoundEffect(new PitchShifter())); // this effect should be kicked out because we configured only 3 effect slots
        source.attachEffect(new SoundEffect(new AutoWah()));
        source.attachEffect(new SoundEffect(new Echo()));
        source.attachEffect(new SoundEffect(new Chorus()));
        source.play();
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("AuxSendsTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new AuxSendsTest(), config);
    }

}
