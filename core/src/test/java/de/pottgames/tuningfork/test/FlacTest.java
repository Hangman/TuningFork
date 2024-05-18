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
import de.pottgames.tuningfork.FlacLoader;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.StreamedSoundSource;

/**
 * @author Matthias
 *
 */
public class FlacTest extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private StreamedSoundSource streamedSound;


    @Override
    public void create() {
        // before we can do anything, we need to initialize our Audio instance
        this.audio = Audio.init();

        // load and play SoundBuffer
        this.sound = FlacLoader.load(Gdx.files.internal("numbers_8bit_mono.flac"));
        System.out.println("Sound duration: " + this.sound.getDuration() + "s");
        this.sound.play();

        // load and play StreamedSoundSource delayed
        this.streamedSound = new StreamedSoundSource(Gdx.files.internal("numbers_16bit_stereo.flac"));
        System.out.println("Streamed sound duration: " + this.streamedSound.getDuration() + "s");
        this.streamedSound.setLooping(true);
        try {
            Thread.sleep(100);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        this.streamedSound.play();
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.streamedSound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("FlacTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new FlacTest(), config);
    }

}
