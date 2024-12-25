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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.StreamedSoundSource;

public class StreamedSoundSourceTest extends ApplicationAdapter {
    private Audio               audio;
    private StreamedSoundSource source;


    @Override
    public void create() {
        audio = Audio.init();
        source = new StreamedSoundSource(Gdx.files.absolute("src/test/resources/numbers.wav"));
        System.out.println("Sound duration: " + source.getDuration() + "s");
        source.setLooping(true);
        source.play();
    }


    @Override
    public void render() {
        final float pos = source.getPlaybackPosition();
        System.out.println("current playback position: " + pos + "s");

        // PRESS SPACE TO SKIP TO 5s
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            source.setPlaybackPosition(5f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (source.isPlaying()) {
                source.pause();
            } else {
                source.play();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (source.isPlaying()) {
                source.stop();
            } else {
                source.play();
            }
        }
    }


    @Override
    public void dispose() {
        source.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("StreamedSoundSourceTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new StreamedSoundSourceTest(), config);
    }

}
