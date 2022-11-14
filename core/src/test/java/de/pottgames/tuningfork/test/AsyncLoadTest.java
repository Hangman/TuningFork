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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundBufferLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class AsyncLoadTest extends ApplicationAdapter {
    private static final String FILE_PATH = "numbers.wav";
    private Audio               audio;
    private AssetManager        assetManager;
    private boolean             played    = false;


    @Override
    public void create() {
        // INIT AUDIO
        final AudioConfig config = new AudioConfig();
        config.setLogger(new ConsoleLogger(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR));
        this.audio = Audio.init(config);

        // LOAD SOUND ASYNC
        this.assetManager = new AssetManager();
        final FileHandleResolver resolver = new InternalFileHandleResolver();
        this.assetManager.setLoader(SoundBuffer.class, new SoundBufferLoader(resolver));
        this.assetManager.load(AsyncLoadTest.FILE_PATH, SoundBuffer.class);
    }


    @Override
    public void render() {
        if (this.assetManager.update(15) && !this.played) {
            final SoundBuffer soundBuffer = this.assetManager.get(AsyncLoadTest.FILE_PATH, SoundBuffer.class);
            soundBuffer.play();
            this.played = true;
        }
    }


    @Override
    public void dispose() {
        this.assetManager.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("AsyncLoadTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new AsyncLoadTest(), config);
    }

}
