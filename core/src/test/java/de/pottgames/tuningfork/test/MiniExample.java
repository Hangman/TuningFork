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
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.FlacLoader;
import de.pottgames.tuningfork.SoundBuffer;

public class MiniExample extends ApplicationAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private BufferedSoundSource source;


    @Override
    public void create() {
        // before we can do anything, we need to initialize our Audio instance
        this.audio = Audio.init();

        // load a sound
        this.sound = FlacLoader.load(Gdx.files.internal("short.flac"));
        System.out.println("Sound duration: " + this.sound.getDuration() + "s");

        this.source = this.audio.obtainSource(this.sound);
        // play the sound
        // this.sound.play();
        this.source.play();
    }


    @Override
    public void render() {
        // we chill in a black window
        System.out.println("playback position: " + this.source.getPlaybackPosition());
    }


    @Override
    public void dispose() {
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("MiniExample");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new MiniExample(), config);
    }

}
