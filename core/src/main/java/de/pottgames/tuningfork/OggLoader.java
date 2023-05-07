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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.decoder.OggInputStream;

public abstract class OggLoader {

    /**
     * Loads an ogg into a {@link SoundBuffer}. This function uses a much faster decoder. However, the disadvantage is that the file must not be packed in a
     * jar. <b>So be careful with this, as packaging files into the jar is libGDXs default behavior on distribution.</b>
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadFast(File file) {
        return OggLoader.loadFast(file.getAbsolutePath());
    }


    /**
     * Loads an ogg into a {@link SoundBuffer}. This function uses a much faster decoder. However, the disadvantage is that the file must not be packed in a
     * jar. <b>So be careful with this, as packaging files into the jar is libGDXs default behavior on distribution.</b>
     *
     * @param path
     *
     * @return the SoundBuffer
     *
     */
    public static SoundBuffer loadFast(String path) {
        final SoundBuffer soundBuffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer channelsBuffer = stack.mallocInt(1);
            final IntBuffer sampleRateBuffer = stack.mallocInt(1);
            final ShortBuffer pcm = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
            final int channels = channelsBuffer.get(0);
            final int sampleRate = sampleRateBuffer.get(0);

            soundBuffer = new SoundBuffer(pcm, channels, sampleRate, 16, PcmDataType.INTEGER, -1);
        }

        return soundBuffer;
    }


    /**
     * Loads an ogg into a {@link SoundBuffer}.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return OggLoader.load(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads sound data from a {@link FileHandle} into a {@link SoundBuffer} using the ogg decoder and closes the stream afterwards.
     *
     * @param file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        final OggInputStream input = new OggInputStream(file, null);
        return OggLoader.load(input);
    }


    /**
     * Loads an ogg input stream into a {@link SoundBuffer} and closes it afterwards.
     *
     * @param stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        final OggInputStream input = new OggInputStream(stream);
        return OggLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link OggInputStream}.
     *
     * @param input
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(OggInputStream input) {
        SoundBuffer result = null;
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
            final byte[] buffer = new byte[2048];
            while (!input.atEnd()) {
                final int length = input.read(buffer);
                if (length == -1) {
                    break;
                }
                output.write(buffer, 0, length);
            }
            result = new SoundBuffer(output.toByteArray(), input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
