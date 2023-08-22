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
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundBufferLoader;
import de.pottgames.tuningfork.SoundBufferLoader.SoundBufferLoaderParameter;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.WaveLoader;

public class PlayReverseExample extends ApplicationAdapter {
    private boolean      jobDone = false;
    private AssetManager assetManager;
    private Audio        audio;
    private SoundBuffer  sound;
    private SoundBuffer  asyncLoadedSound;
    private SoundSource  source;


    @Override
    public void create() {
        this.audio = Audio.init();

        this.setupAssetManager();
        this.load();
        this.loadAsync();
        this.source = this.audio.obtainSource(this.sound);
        this.source.play();
    }


    public void setupAssetManager() {
        this.assetManager = new AssetManager();
        final FileHandleResolver resolver = new InternalFileHandleResolver();
        this.assetManager.setLoader(SoundBuffer.class, new SoundBufferLoader(resolver));
    }


    public void load() {
        // use any of these

        // this.sound = AiffLoader.loadReverse(Gdx.files.internal("numbers.aiff"));
        // this.sound = FlacLoader.loadReverse(Gdx.files.internal("numbers_16bit_stereo.flac"));
        // this.sound = Mp3Loader.loadReverse(Gdx.files.internal("numbers.mp3"));
        // this.sound = OggLoader.loadReverse(Gdx.files.internal("numbers2.ogg"));
        this.sound = WaveLoader.loadReverse(Gdx.files.internal("numbers.wav"));
    }


    private void loadAsync() {
        final SoundBufferLoaderParameter parameter = new SoundBufferLoaderParameter();
        parameter.reverse = true;
        this.assetManager.load("carnivalrides.ogg", SoundBuffer.class, parameter);
    }


    @Override
    public void render() {
        if (!this.jobDone && this.assetManager.update() && !this.source.isPlaying()) {
            this.asyncLoadedSound = this.assetManager.get("carnivalrides.ogg");
            this.asyncLoadedSound.play();
            this.jobDone = true;
        }
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PlayReverseExample");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PlayReverseExample(), config);
    }

}
