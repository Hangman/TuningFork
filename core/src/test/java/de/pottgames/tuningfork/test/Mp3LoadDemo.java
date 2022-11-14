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
import de.pottgames.tuningfork.Mp3Loader;
import de.pottgames.tuningfork.SoundBuffer;

/**
 * TuningFork doesn't officially support the mp3 file format. If you ultimatively have to play mp3 for whatever reason, this should demonstrate how you can
 * decode an mp3 file and play it through TuningFork.<br>
 * Note that looping/seeking may cause problems related to the mp3 file format. Also the duration returned by TuningFork might not be as precise as with the
 * supported formats due to the same reason.
 *
 * @author Matthias
 *
 */
public class Mp3LoadDemo extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.sound = Mp3Loader.load(Gdx.files.internal("numbers.mp3"));
        System.out.println("Sound duration: " + this.sound.getDuration());
        this.sound.play();
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mp3LoadDemo");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new Mp3LoadDemo(), config);
    }

}
