package de.pottgames.tuningfork;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

public abstract class WaveLoader {

    public static SoundBuffer load(FileHandle file) {
        SoundBuffer result = null;

        WavInputStream input = null;
        try {
            input = new WavInputStream(file);
            result = new SoundBuffer(StreamUtils.copyStreamToByteArray(input, input.dataRemaining), input.channels, input.sampleRate);
        } catch (final IOException ex) {
            throw new TuningForkRuntimeException("Error reading WAV file: " + file, ex);
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }


    public static SoundBuffer load(File file) {
        SoundBuffer result = null;

        WavInputStream input = null;
        try {
            input = new WavInputStream(new FileInputStream(file), file.getPath());
            result = new SoundBuffer(StreamUtils.copyStreamToByteArray(input, input.dataRemaining), input.channels, input.sampleRate);
        } catch (final IOException ex) {
            throw new TuningForkRuntimeException("Error reading WAV file: " + file, ex);
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
