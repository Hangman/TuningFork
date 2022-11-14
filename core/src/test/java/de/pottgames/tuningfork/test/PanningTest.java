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
import de.pottgames.tuningfork.WaveLoader;

public class PanningTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;
    private boolean     left;
    private long        soundPlayStartTime;
    private float       soundDuration;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.sound = WaveLoader.load(Gdx.files.internal("rhythm.wav"));
        this.soundDuration = this.sound.getDuration() * 1000f;
    }


    @Override
    public void render() {
        if (System.currentTimeMillis() > this.soundPlayStartTime + (long) this.soundDuration) {
            this.left = !this.left;
            this.playSound(this.left ? -1f : 1f);
        }
    }


    private void playSound(float pan) {
        this.sound.play(1f, 1f, pan);
        this.soundPlayStartTime = System.currentTimeMillis();
    }


    @Override
    public void dispose() {
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PanningTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PanningTest(), config);
    }

}
