package de.pottgames.tuningfork;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public abstract class WaveLoader {

    public static SoundBuffer load(FileHandle file) {
        SoundBuffer result = null;

        WavInputStream input = null;
        try {
            input = new WavInputStream(file);
            result = new SoundBuffer(StreamUtils.copyStreamToByteArray(input, input.dataRemaining), input.channels, input.sampleRate);
        } catch (final IOException ex) {
            throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
