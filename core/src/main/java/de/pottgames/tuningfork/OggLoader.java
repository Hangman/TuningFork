package de.pottgames.tuningfork;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

public abstract class OggLoader {

    public static SoundBuffer load(FileHandle file) {
        return OggLoader.load(file.read());
    }


    public static SoundBuffer load(InputStream stream) {
        SoundBuffer result = null;
        OggInputStream input = null;
        try {
            input = new OggInputStream(stream);
            final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
            final byte[] buffer = new byte[2048];
            while (!input.atEnd()) {
                final int length = input.read(buffer);
                if (length == -1) {
                    break;
                }
                output.write(buffer, 0, length);
            }
            result = new SoundBuffer(output.toByteArray(), input.getChannels(), input.getSampleRate(), input.getBitsPerSample());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
