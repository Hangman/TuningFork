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

package de.pottgames.tuningfork.test.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SoundSourceUnitTest {
    private Audio               audio;
    private SoundBuffer         sound;
    private BufferedSoundSource source;


    @BeforeAll
    public void setup() {
        Gdx.files = new Lwjgl3Files(); // hack setup gdx because we only need Gdx.files in order to run properly

        audio = Audio.init(new AudioConfig().setLogger(new ConsoleLogger(LogLevel.INFO_WARN_ERROR)));
        sound = WaveLoader.load(Gdx.files.internal("numbers.wav"));
        source = audio.obtainSource(sound);
    }


    @Test
    public void testVolume() {
        Assertions.assertEquals(source.getVolume(), 1f);

        source.setVolume(0f);
        Assertions.assertEquals(source.getVolume(), 0f);

        source.setVolume(2f);
        Assertions.assertEquals(source.getVolume(), 1f);

        source.setVolume(-1f);
        Assertions.assertEquals(source.getVolume(), 0f);

        source.setVolume(0.5f);
        Assertions.assertEquals(source.getVolume(), 0.5f);

        source.setVolume(0.133333333333333333f);
        Assertions.assertEquals(source.getVolume(), 0.133333333333333333f);

        source.setVolume(1f);
        Assertions.assertEquals(source.getVolume(), 1f);
    }


    @Test
    public void testRelative() {
        Assertions.assertEquals(source.isRelative(), false);
        source.setRelative(true);
        Assertions.assertEquals(source.isRelative(), true);
        source.setRelative(false);
        Assertions.assertEquals(source.isRelative(), false);
    }


    @Test
    public void testPitch() {
        Assertions.assertEquals(source.getPitch(), 1f);
        source.setPitch(0f);
        Assertions.assertEquals(source.getPitch(), 0f);

        source.setPitch(2f);
        Assertions.assertEquals(source.getPitch(), 2f);

        source.setPitch(3f);
        Assertions.assertEquals(source.getPitch(), 3f);

        source.setPitch(-1f);
        Assertions.assertEquals(source.getPitch(), 0f);

        source.setPitch(0.5f);
        Assertions.assertEquals(source.getPitch(), 0.5f);

        source.setPitch(0.533333333333333333f);
        Assertions.assertEquals(source.getPitch(), 0.533333333333333333f);

        source.setPitch(1f);
        Assertions.assertEquals(source.getPitch(), 1f);
    }


    @Test
    public void testFilter() {
        Assertions.assertEquals(source.hasFilter(), false);
        source.setFilter(0f, 0.5f);
        Assertions.assertEquals(source.hasFilter(), true);
        source.setFilter(1f, 1f);
        Assertions.assertEquals(source.hasFilter(), false);
    }


    @AfterAll
    public void cleanup() {
        source.free();
        sound.dispose();
        audio.dispose();
    }

}
