package de.pottgames.tuningfork;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

public abstract class FlacLoader {

    public static SoundBuffer load(FileHandle file) {
        return FlacLoader.load(file.file());
    }


    public static SoundBuffer load(File file) {

        SoundBuffer result = null;
        FlacInputStream input = null;

        try {
            input = new FlacInputStream(file);
            final byte[] buffer = new byte[(int) input.totalSamples() * input.getBytesPerSample() * input.getChannels()];
            input.read(buffer);
            result = new SoundBuffer(buffer, input.getChannels(), input.getSampleRate(), input.getBitsPerSample());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
