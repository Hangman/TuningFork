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
        if ("ogg".equalsIgnoreCase(fileExtension) || "oga".equalsIgnoreCase(fileExtension) || "ogx".equalsIgnoreCase(fileExtension)
                || "opus".equalsIgnoreCase(fileExtension)) {
            this.asset = OggLoader.load(file);
        } else if ("wav".equalsIgnoreCase(fileExtension) || "wave".equalsIgnoreCase(fileExtension)) {
            this.asset = WaveLoader.load(file);
        } else {
            throw new TuningForkRuntimeException("Unsupported file '" + fileExtension + "', only ogg and wav files are supported.");
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
