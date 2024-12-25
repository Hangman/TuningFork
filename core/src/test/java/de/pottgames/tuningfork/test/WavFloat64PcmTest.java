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
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.WaveLoader;

public class WavFloat64PcmTest extends ApplicationAdapter {
    private static final String SOUND_PATH = "64bit_float_numbers.wav";
    private Audio               audio;
    private SoundBuffer         sound;
    private StreamedSoundSource streamedSource;


    @Override
    public void create() {
        audio = Audio.init();
        sound = WaveLoader.load(Gdx.files.internal(WavFloat64PcmTest.SOUND_PATH));
        final SoundSource bufferedSource = audio.obtainSource(sound);
        streamedSource = new StreamedSoundSource(Gdx.files.internal(WavFloat64PcmTest.SOUND_PATH));
        bufferedSource.setLooping(true);
        streamedSource.setLooping(true);
        bufferedSource.play();
        try {
            Thread.sleep(200);
        } catch (final InterruptedException e) {
            // ignore
        }
        streamedSource.play();
        System.out.println("buffered duration: " + sound.getDuration() + "s");
        System.out.println("streamed duration: " + streamedSource.getDuration() + "s");
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        sound.dispose();
        streamedSource.dispose();

        // always dispose Audio last
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("WavFloat64PcmTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new WavFloat64PcmTest(), config);
    }

}
