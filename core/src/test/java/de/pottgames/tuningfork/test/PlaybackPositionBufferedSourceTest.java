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

public class PlaybackPositionBufferedSourceTest extends ApplicationAdapter implements InputAdapter {
    private Audio               audio;
    private SoundBuffer         sound;
    private BufferedSoundSource soundSource;


    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);

        // before we can do anything, we need to initialize our Audio instance
        final AudioConfig config = new AudioConfig();
        config.setLogger(new ConsoleLogger(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR));
        audio = Audio.init(config);

        // load a sound
        sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));

        // play the sound
        soundSource = audio.obtainSource(sound);
        soundSource.setLooping(true);
        soundSource.play();
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        final float seconds = Rng.get(-20f, 20f);
        System.out.println("setting position to: " + seconds);
        soundSource.setPlaybackPosition(seconds);
        return true;
    }


    @Override
    public void render() {
        System.out.println("playback position: " + soundSource.getPlaybackPosition());
    }


    @Override
    public void dispose() {
        sound.dispose();

        // always dispose Audio last
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PlaybackPositionBufferedSourceTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PlaybackPositionBufferedSourceTest(), config);
    }

}
