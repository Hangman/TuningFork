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
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.WaveLoader;

public class WavULawTest extends ApplicationAdapter {
    private static final String FILE_PATH = "numbers-ulaw.wav";
    private Audio               audio;
    private SoundBuffer         sound;
    private SoundSource         bufferedSource;
    private StreamedSoundSource streamedSource;
    private SoundSource         activeSource;


    @Override
    public void create() {
        audio = Audio.init();
        sound = WaveLoader.load(Gdx.files.internal(WavULawTest.FILE_PATH));
        streamedSource = new StreamedSoundSource(Gdx.files.internal(WavULawTest.FILE_PATH));
        bufferedSource = audio.obtainSource(sound);
        System.out.println("Sound duration (streamed): " + streamedSource.getDuration() + "s");
        System.out.println("Sound duration (buffered): " + sound.getDuration() + "s");

        activeSource = bufferedSource;
    }


    @Override
    public void render() {
        if (!activeSource.isPlaying()) {
            if (activeSource == bufferedSource) {
                activeSource = streamedSource;
                System.out.println("streamed sound playing");
            } else {
                activeSource = bufferedSource;
                System.out.println("buffered sound playing");
            }

            activeSource.play();
        }
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("WavULawTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new WavULawTest(), config);
    }

}
