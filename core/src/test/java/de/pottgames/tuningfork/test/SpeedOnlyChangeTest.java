/**
 * Copyright 2022 Matthias Finke
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
import de.pottgames.tuningfork.PitchShifter;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundEffect;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.SoundSource;

public class SpeedOnlyChangeTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;
    private SoundEffect effect;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.sound = SoundLoader.load(Gdx.files.absolute("src/test/resources/carnivalrides.ogg"));
        final SoundSource source = this.audio.obtainSource(this.sound);

        // set source pitch to change the speed
        final float pitch = 1.2f;
        source.setPitch(pitch);

        // apply pitch correction
        this.effect = new SoundEffect(new PitchShifter().correctPitch(pitch));
        source.attachEffect(this.effect);

        // in order to only hear the pitch corrected sound, we must silence the original sound with a filter
        source.setFilter(0f, 0f);

        source.play();
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
