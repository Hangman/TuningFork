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

package de.pottgames.tuningfork;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.SoundBufferLoader.SoundBufferLoaderParameter;

/**
 * This class can be used to load {@link SoundBuffer}s asynchronously. Don't forget to tell your {@link com.badlogic.gdx.assets.AssetManager AssetManager} about
 * this loader.
 *
 * @author Matthias
 *
 */
public class SoundBufferLoader extends AsynchronousAssetLoader<SoundBuffer, SoundBufferLoaderParameter> {
    @SuppressWarnings("rawtypes")
    private final Array<AssetDescriptor> dependencies = new Array<>();
    private SoundBuffer                  asset;


    public SoundBufferLoader(FileHandleResolver resolver) {
        super(resolver);
    }


    private void reset() {
        this.asset = null;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SoundBufferLoaderParameter parameter) {
        return this.dependencies;
    }


    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, SoundBufferLoaderParameter parameter) {
        final String fileExtension = file.extension();
        final SoundFileType type = SoundFileType.getByFileEnding(fileExtension);
        switch (type) {
            case FLAC:
                this.asset = FlacLoader.load(file);
                break;
            case OGG:
                this.asset = OggLoader.load(file);
                break;
            case WAV:
                this.asset = WaveLoader.load(file);
                break;
            default:
                throw new TuningForkRuntimeException("Unsupported file '" + fileExtension + "'. Only ogg, flac and wav files are supported.");
        }
    }


    @Override
    public SoundBuffer loadSync(AssetManager manager, String fileName, FileHandle file, SoundBufferLoaderParameter parameter) {
        final SoundBuffer result = this.asset;
        this.reset();
        return result;
    }


    public static class SoundBufferLoaderParameter extends AssetLoaderParameters<SoundBuffer> {
        // unused but necessary for the interface
    }

}
