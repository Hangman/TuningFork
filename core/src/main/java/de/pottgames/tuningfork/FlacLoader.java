package de.pottgames.tuningfork;

import java.io.ByteArrayOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

public abstract class FlacLoader {

    public static SoundBuffer load(FileHandle file) {
        SoundBuffer result = null;
        FlacInputStream input = null;
        try {
            input = new FlacInputStream(file);
            final ByteArrayOutputStream output = new ByteArrayOutputStream(StreamedSoundSource.BUFFER_SIZE);
            final byte[] buffer = new byte[StreamedSoundSource.BUFFER_SIZE];
            int readBytes = 0;
            while ((readBytes = input.read(buffer)) > 0) {
                output.write(buffer, 0, readBytes);
            }
            result = new SoundBuffer(output.toByteArray(), input.getChannels(), input.getSampleRate(), input.getBitsPerSample());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
