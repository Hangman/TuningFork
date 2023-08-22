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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundBufferLoader;
import de.pottgames.tuningfork.SoundBufferLoader.SoundBufferLoaderParameter;

public class SoundBufferLoaderParameterTest extends ApplicationAdapter {
    private AssetManager        assetManager;
    private Audio               audio;
    private SoundBuffer         sound;
    private SoundBuffer         soundReverse;
    private BufferedSoundSource source;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.assetManager = new AssetManager();
        final FileHandleResolver resolver = new InternalFileHandleResolver();
        this.assetManager.setLoader(SoundBuffer.class, new SoundBufferLoader(resolver));

        // queue fordward playback asset for loading as usual
        this.assetManager.load("numbers.wav", SoundBuffer.class);

        // queue reverse playback asset for loading
        // it's the same file so we need to trick the AssetManager into thinking it's a different file
        final SoundBufferLoaderParameter parameter = new SoundBufferLoaderParameter();
        parameter.reverse = true;
        parameter.file = Gdx.files.internal("numbers.wav");
        this.assetManager.load("numerinos_wavus", SoundBuffer.class, parameter);

        // we don't load asynchronously because it's just a test
        this.assetManager.finishLoading();

        // fetch assets
        this.sound = this.assetManager.get("numbers.wav", SoundBuffer.class);
        this.soundReverse = this.assetManager.get("numerinos_wavus", SoundBuffer.class);

        this.source = this.audio.obtainSource(this.sound);
        this.source.play();
    }


    @Override
    public void render() {
        if (!this.source.isPlaying()) {
            final SoundBuffer playedSound = this.source.getBuffer();
            this.source.free();
            this.source = this.audio.obtainSource(playedSound == this.sound ? this.soundReverse : this.sound);
            this.source.play();
        }
    }


    @Override
    public void dispose() {
        this.assetManager.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SoundBufferLoaderParameterTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new SoundBufferLoaderParameterTest(), config);
    }

}
