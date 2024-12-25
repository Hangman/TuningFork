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
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SoundLoaderUnitTest {
    private Audio audio;


    @BeforeAll
    public void setup() {
        Gdx.files = new Lwjgl3Files(); // hack setup gdx because we only need Gdx.files in order to run properly
        audio = Audio.init(new AudioConfig().setLogger(new ConsoleLogger(LogLevel.INFO_WARN_ERROR)));
    }


    @Test
    public void loadWav() {
        final SoundBuffer sound = SoundLoader.load(Gdx.files.absolute("src/test/resources/numbers.wav"));
        Assertions.assertNotNull(sound);
        Assertions.assertTrue(sound.getDuration() > 0f);
    }


    @Test
    public void loadAiff() {
        final SoundBuffer sound = SoundLoader.load(Gdx.files.absolute("src/test/resources/numbers.aiff"));
        Assertions.assertNotNull(sound);
        Assertions.assertTrue(sound.getDuration() > 0f);
    }


    @Test
    public void loadOgg() {
        final SoundBuffer sound = SoundLoader.load(Gdx.files.absolute("src/test/resources/numbers2.ogg"));
        Assertions.assertNotNull(sound);
        Assertions.assertTrue(sound.getDuration() > 0f);
    }


    @Test
    public void loadMp3() {
        final SoundBuffer sound = SoundLoader.load(Gdx.files.absolute("src/test/resources/numbers.mp3"));
        Assertions.assertNotNull(sound);
        Assertions.assertTrue(sound.getDuration() > 0f);
    }


    @Test
    public void loadFlac() {
        final SoundBuffer sound = SoundLoader.load(Gdx.files.absolute("src/test/resources/numbers_8bit_mono.flac"));
        Assertions.assertNotNull(sound);
        Assertions.assertTrue(sound.getDuration() > 0f);
    }


    @AfterAll
    public void cleanup() {
        audio.dispose();
    }

}
