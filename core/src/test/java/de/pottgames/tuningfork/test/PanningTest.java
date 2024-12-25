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
import de.pottgames.tuningfork.SoundLoader;

public class PanningTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;
    private boolean     left;
    private long        soundPlayStartTime;
    private float       soundDuration;


    @Override
    public void create() {
        audio = Audio.init();
        sound = SoundLoader.load(Gdx.files.internal("rhythm.wav"));
        soundDuration = sound.getDuration() * 1000f;
    }


    @Override
    public void render() {
        if (System.currentTimeMillis() > soundPlayStartTime + (long) soundDuration) {
            left = !left;
            playSound(left ? -1f : 1f);
            System.out.println(left ? "left" : "right");
        }
    }


    private void playSound(float pan) {
        sound.play(1f, 1f, pan);
        soundPlayStartTime = System.currentTimeMillis();
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
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
