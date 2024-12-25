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
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class FilterTest extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private final float[][]     filters     = new float[3][2];
    private int                 filterIndex = 0;
    private BufferedSoundSource soundSource;


    @Override
    public void create() {
        // before we can do anything, we need to initialize our Audio instance
        final AudioConfig config = new AudioConfig();
        config.setLogger(new ConsoleLogger(LogLevel.DEBUG_INFO_WARN_ERROR));
        audio = Audio.init(config);

        // load a sound
        sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));

        // obtain sound source
        soundSource = audio.obtainSource(sound);

        // create filters
        // no effect
        filters[0][0] = 1f;
        filters[0][1] = 1f;

        // only high frequencies
        filters[1][0] = 0.01f;
        filters[1][1] = 1f;

        // only low frequencies
        filters[2][0] = 1f;
        filters[2][1] = 0.01f;
    }


    @Override
    public void render() {
        if (!soundSource.isPlaying()) {
            System.out.println("filter: " + filterIndex);
            System.out.println("using low freq: " + filters[filterIndex][0]);
            System.out.println("using low freq: " + filters[filterIndex][1]);
            System.out.println();
            soundSource.setFilter(filters[filterIndex][0], filters[filterIndex][1]);
            soundSource.play();
            filterIndex++;
            if (filterIndex >= filters.length) {
                filterIndex = 0;
            }
        }
    }


    @Override
    public void dispose() {
        soundSource.free();
        sound.dispose();

        // always dispose Audio last
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("FilterTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new FilterTest(), config);
    }

}
