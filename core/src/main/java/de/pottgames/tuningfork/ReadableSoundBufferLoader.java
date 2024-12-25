/**
 * Copyright 2024 Matthias Finke
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

import java.io.IOException;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.ReadableSoundBufferLoader.ReadableSoundBufferLoaderParameter;

/**
 * This class can be used to load {@link ReadableSoundBuffer}s asynchronously. Don't forget to tell your {@link com.badlogic.gdx.assets.AssetManager
 * AssetManager} about this loader.
 *
 * @author Matthias
 */
public class ReadableSoundBufferLoader extends AsynchronousAssetLoader<ReadableSoundBuffer, ReadableSoundBufferLoaderParameter> {
    @SuppressWarnings("rawtypes")
    private final Array<AssetDescriptor> dependencies = new Array<>();
    private volatile ReadableSoundBuffer asset;


    public ReadableSoundBufferLoader(FileHandleResolver resolver) {
        super(resolver);
    }


    private void reset() {
        asset = null;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ReadableSoundBufferLoaderParameter parameter) {
        return dependencies;
    }


    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, ReadableSoundBufferLoaderParameter parameter) {
        file = parameter != null && parameter.file != null ? parameter.file : file;
        final boolean reverse = parameter != null && parameter.reverse;
        final String fileExtension = file.extension();
        SoundFileType type = SoundFileType.getByFileEnding(fileExtension);
        if (type == null) {
            try {
                type = SoundFileType.parseFromFile(file);
            } catch (final IOException e) {
                // ignore
            }
        }
        if (type == null) {
            throw new TuningForkRuntimeException("Unsupported file '" + fileExtension + "'. Only ogg, flac, mp3, aiff, wav and qoa files are supported.");
        }
        switch (type) {
            case FLAC:
                asset = reverse ? FlacLoader.loadReadableReverse(file) : FlacLoader.loadReadable(file);
                break;
            case OGG:
                asset = reverse ? OggLoader.loadReadableReverse(file) : OggLoader.loadReadable(file);
                break;
            case WAV:
                asset = reverse ? WaveLoader.loadReadableReverse(file) : WaveLoader.loadReadable(file);
                break;
            case MP3:
                asset = reverse ? Mp3Loader.loadReadableReverse(file) : Mp3Loader.loadReadable(file);
                break;
            case AIFF:
                asset = reverse ? AiffLoader.loadReadableReverse(file) : AiffLoader.loadReadable(file);
                break;
            case QOA:
                asset = reverse ? QoaLoader.loadReadableReverse(file) : QoaLoader.loadReadable(file);
                break;
        }
    }


    @Override
    public ReadableSoundBuffer loadSync(AssetManager manager, String fileName, FileHandle file, ReadableSoundBufferLoaderParameter parameter) {
        final ReadableSoundBuffer result = asset;
        reset();
        return result;
    }


    public static class ReadableSoundBufferLoaderParameter extends AssetLoaderParameters<ReadableSoundBuffer> {
        /**
         * Loads the file for reversed playback.
         */
        public boolean reverse = false;

        /**
         * A custom FileHandle object that can be set to specify an alternative file path for the asset.<br>
         * If this is set, this path takes priority over the file name String given to the load function of the asset manager.<br>
         * You can give the asset manager load method an arbitrary String that is just used to identify the asset, it must not point to the real file.<br>
         * This is useful when multiple instances of the same asset need to be loaded with different configurations.
         */
        public FileHandle file;

    }

}
