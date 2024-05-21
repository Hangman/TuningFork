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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.decoder.OggInputStream;
import de.pottgames.tuningfork.decoder.util.Util;
import de.pottgames.tuningfork.misc.PcmUtil;

public abstract class OggLoader {

    /**
     * Loads an ogg into a {@link SoundBuffer}.
     *
     * @param file the file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return OggLoader.loadNonPacked(file.getAbsolutePath());
    }


    /**
     * Loads an ogg into a {@link ReadableSoundBuffer}.
     *
     * @param file the file
     *
     * @return the SoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(File file) {
        return OggLoader.loadNonPackedReadable(file.getAbsolutePath());
    }


    /**
     * Loads an ogg into a {@link SoundBuffer}.
     *
     * @param fileHandle the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle fileHandle) {
        final File file = fileHandle.file();
        if (file != null && file.exists()) {
            return OggLoader.loadNonPacked(file.getAbsolutePath());
        }
        return OggLoader.load(fileHandle.read());
    }


    /**
     * Loads an ogg into a {@link ReadableSoundBuffer}.
     *
     * @param fileHandle the file handle
     *
     * @return the SoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(FileHandle fileHandle) {
        final File file = fileHandle.file();
        if (file != null && file.exists()) {
            return OggLoader.loadNonPackedReadable(file.getAbsolutePath());
        }
        return OggLoader.loadReadable(fileHandle.read());
    }


    /**
     * Loads an ogg into a {@link SoundBuffer}. <b>Referenced file must not be packed into a jar. Be careful with this as packaging files into the jar is
     * libGDXs default behavior on distribution.</b>
     *
     * @param path the path to the file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadNonPacked(String path) {
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
     * Loads an ogg into a {@link ReadableSoundBuffer}. <b>Referenced file must not be packed into a jar. Be careful with this as packaging files into the jar
     * is libGDXs default behavior on distribution.</b>
     *
     * @param path the path to the file
     *
     * @return the SoundBuffer
     */
    public static ReadableSoundBuffer loadNonPackedReadable(String path) {
        final ReadableSoundBuffer soundBuffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer channelsBuffer = stack.mallocInt(1);
            final IntBuffer sampleRateBuffer = stack.mallocInt(1);
            final ShortBuffer pcm = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
            final int channels = channelsBuffer.get(0);
            final int sampleRate = sampleRateBuffer.get(0);

            soundBuffer = new ReadableSoundBuffer(pcm, channels, sampleRate, 16, PcmDataType.INTEGER, -1);
        }

        return soundBuffer;
    }


    /**
     * Loads an ogg into a {@link SoundBuffer} and closes the InputStream afterward.
     *
     * @param stream the input stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        byte[] streamData = null;
        try {
            streamData = Util.toByteArray(stream);
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
        StreamUtils.closeQuietly(stream);
        final ByteBuffer originalData = BufferUtils.createByteBuffer(streamData.length);
        originalData.put(streamData);
        originalData.flip();

        final SoundBuffer soundBuffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer channelsBuffer = stack.mallocInt(1);
            final IntBuffer sampleRateBuffer = stack.mallocInt(1);
            final ShortBuffer pcm = STBVorbis.stb_vorbis_decode_memory(originalData, channelsBuffer, sampleRateBuffer);
            final int channels = channelsBuffer.get(0);
            final int sampleRate = sampleRateBuffer.get(0);

            soundBuffer = new SoundBuffer(pcm, channels, sampleRate, 16, PcmDataType.INTEGER, -1);
        }

        return soundBuffer;
    }


    /**
     * Loads an ogg into a {@link SoundBuffer} and closes the InputStream afterward.
     *
     * @param stream the input stream
     *
     * @return the SoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(InputStream stream) {
        byte[] streamData = null;
        try {
            streamData = Util.toByteArray(stream);
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
        StreamUtils.closeQuietly(stream);
        final ByteBuffer originalData = BufferUtils.createByteBuffer(streamData.length);
        originalData.put(streamData);
        originalData.flip();

        final ReadableSoundBuffer soundBuffer;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer channelsBuffer = stack.mallocInt(1);
            final IntBuffer sampleRateBuffer = stack.mallocInt(1);
            final ShortBuffer pcm = STBVorbis.stb_vorbis_decode_memory(originalData, channelsBuffer, sampleRateBuffer);
            final int channels = channelsBuffer.get(0);
            final int sampleRate = sampleRateBuffer.get(0);

            soundBuffer = new ReadableSoundBuffer(pcm, channels, sampleRate, 16, PcmDataType.INTEGER, -1);
        }

        return soundBuffer;
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link OggInputStream}.
     *
     * @param input the OggInputStream
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


    /**
     * Loads a {@link ReadableSoundBuffer} from a {@link OggInputStream}.
     *
     * @param input the OggInputStream
     *
     * @return the ReadableSoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(OggInputStream input) {
        ReadableSoundBuffer result = null;
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
            result = new ReadableSoundBuffer(output.toByteArray(), input.getChannels(), input.getSampleRate(), input.getBitsPerSample(),
                    input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }


    /**
     * Loads an ogg file in reverse into a {@link SoundBuffer}.
     *
     * @param fileHandle the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadReverse(FileHandle fileHandle) {
        final OggInputStream input = new OggInputStream(fileHandle.read());
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
            final byte[] reversedPcm = PcmUtil.reverseAudio(output.toByteArray(), input.getBitsPerSample() / 8);
            result = new SoundBuffer(reversedPcm, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }


    /**
     * Loads an ogg file in reverse into a {@link ReadableSoundBuffer}.
     *
     * @param fileHandle the file handle
     *
     * @return the SoundBuffer
     */
    public static ReadableSoundBuffer loadReadableReverse(FileHandle fileHandle) {
        final OggInputStream input = new OggInputStream(fileHandle.read());
        ReadableSoundBuffer result = null;
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
            final byte[] reversedPcm = PcmUtil.reverseAudio(output.toByteArray(), input.getBitsPerSample() / 8);
            result = new ReadableSoundBuffer(reversedPcm, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
