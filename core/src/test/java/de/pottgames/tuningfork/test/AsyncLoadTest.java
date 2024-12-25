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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.ReadableSoundBuffer;
import de.pottgames.tuningfork.ReadableSoundBufferLoader.ReadableSoundBufferLoaderParameter;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class AsyncLoadTest extends ApplicationAdapter {
    private static final String FILE_1                    = "guitar.wav";
    private static final String FILE_2                    = "numbers.aiff";
    private Audio               audio;
    private AssetManager        assetManager;
    private SoundSource         source;
    private boolean             playedSoundBuffer         = false;
    private boolean             playedReadableSoundBuffer = false;


    @Override
    public void create() {
        assetManager = new AssetManager();

        final AudioConfig config = new AudioConfig();
        config.setAssetManager(assetManager);
        config.setLogger(new ConsoleLogger(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR));
        audio = Audio.init(config);

        // If you cannot provide an AssetManager in the AudioConfig, use this:
        // this.audio.registerAssetManagerLoaders(this.assetManager);

        assetManager.load(AsyncLoadTest.FILE_1, SoundBuffer.class);
        final ReadableSoundBufferLoaderParameter parameter = new ReadableSoundBufferLoaderParameter();
        parameter.reverse = true;
        assetManager.load(AsyncLoadTest.FILE_2, ReadableSoundBuffer.class, parameter);
    }


    @Override
    public void render() {
        if (assetManager.update(15)) {
            if (!playedSoundBuffer) {
                final SoundBuffer soundBuffer = assetManager.get(AsyncLoadTest.FILE_1, SoundBuffer.class);
                source = audio.obtainSource(soundBuffer);
                source.play();
                playedSoundBuffer = true;
            } else if (source != null && !source.isPlaying() && !playedReadableSoundBuffer) {
                final ReadableSoundBuffer buffer = assetManager.get(AsyncLoadTest.FILE_2, ReadableSoundBuffer.class);
                buffer.play();
                playedReadableSoundBuffer = true;
            }

        }
    }


    @Override
    public void dispose() {
        assetManager.dispose();
        audio.dispose();
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
