/**
 * Copyright 2023 Matthias Finke
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

import com.badlogic.gdx.files.FileHandle;

/**
 * The SoundLoader class provides utility methods for loading audio files into SoundBuffers.<br>
 * It supports various audio formats such as FLAC, OGG, WAV, MP3, and AIFF.<br>
 * If you know the file type, I recommend using the file format loaders directly:<br>
 * <ul>
 * <li>{@link WaveLoader}</li>
 * <li>{@link OggLoader}</li>
 * <li>{@link Mp3Loader}</li>
 * <li>{@link FlacLoader}</li>
 * <li>{@link AiffLoader}</li>
 * <li>{@link QoaLoader}</li>
 * </ul>
 */
public abstract class SoundLoader {

    /**
     * Loads an audio file from the specified FileHandle and returns a SoundBuffer.
     *
     * @param file The FileHandle pointing to the audio file.
     *
     * @return A SoundBuffer containing the audio data.
     *
     * @throws TuningForkRuntimeException If there is an error during loading or if the file type cannot be identified.
     */
    public static SoundBuffer load(FileHandle file) {
        return SoundLoader.load(file, false);
    }


    /**
     * Loads an audio file from the specified FileHandle and returns a SoundBuffer.
     *
     * @param file The FileHandle pointing to the audio file.
     *
     * @return A ReadableSoundBuffer containing the audio data.
     *
     * @throws TuningForkRuntimeException If there is an error during loading or if the file type cannot be identified.
     */
    public static ReadableSoundBuffer loadReadable(FileHandle file) {
        return SoundLoader.loadReadable(file, false);
    }


    /**
     * Loads an audio file from the specified FileHandle and reverses the audio data.
     *
     * @param file The FileHandle pointing to the audio file.
     *
     * @return A SoundBuffer containing the reversed audio data.
     *
     * @throws TuningForkRuntimeException If there is an error during loading or if the file type cannot be identified.
     */
    public static SoundBuffer loadReverse(FileHandle file) {
        return SoundLoader.load(file, true);
    }


    /**
     * Loads an audio file from the specified FileHandle and reverses the audio data.
     *
     * @param file The FileHandle pointing to the audio file.
     *
     * @return A ReadableSoundBuffer containing the reversed audio data.
     *
     * @throws TuningForkRuntimeException If there is an error during loading or if the file type cannot be identified.
     */
    public static ReadableSoundBuffer loadReadableReverse(FileHandle file) {
        return SoundLoader.loadReadable(file, true);
    }


    /**
     * Loads an audio file from the specified FileHandle and returns a SoundBuffer. Optionally, it can load the audio in reverse order if the 'reverse'
     * parameter is set to true.
     *
     * @param file The FileHandle pointing to the audio file.
     * @param reverse Whether to load the audio in reverse order.
     *
     * @return A SoundBuffer containing the audio data.
     *
     * @throws TuningForkRuntimeException If there is an error during loading or if the file type cannot be identified.
     */
    public static SoundBuffer load(FileHandle file, boolean reverse) {
        if (file == null) {
            throw new TuningForkRuntimeException("file must not be null");
        }

        final String fileExtension = file.extension();
        SoundFileType soundFileType = SoundFileType.getByFileEnding(fileExtension);
        if (soundFileType == null) {
            try {
                soundFileType = SoundFileType.parseFromFile(file);
            } catch (final IOException e) {
                throw new TuningForkRuntimeException(e);
            }
        }

        if (soundFileType != null) {
            switch (soundFileType) {
                case FLAC:
                    return reverse ? FlacLoader.loadReverse(file) : FlacLoader.load(file);
                case OGG:
                    return reverse ? OggLoader.loadReverse(file) : OggLoader.load(file);
                case WAV:
                    return reverse ? WaveLoader.loadReverse(file) : WaveLoader.load(file);
                case MP3:
                    return reverse ? Mp3Loader.loadReverse(file) : Mp3Loader.load(file);
                case AIFF:
                    return reverse ? AiffLoader.loadReverse(file) : AiffLoader.load(file);
                case QOA:
                    return reverse ? QoaLoader.loadReverse(file) : QoaLoader.load(file);
            }
        }

        throw new TuningForkRuntimeException("Couldn't identify file type: " + file);
    }


    /**
     * Loads an audio file from the specified FileHandle and returns a ReadableSoundBuffer. Optionally, it can load the audio in reverse order if the 'reverse'
     * parameter is set to true.
     *
     * @param file The FileHandle pointing to the audio file.
     * @param reverse Whether to load the audio in reverse order.
     *
     * @return A ReadableSoundBuffer containing the audio data.
     *
     *
     * @throws TuningForkRuntimeException If there is an error during loading or if the file type cannot be identified.
     *
     * @see ReadableSoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(FileHandle file, boolean reverse) {
        if (file == null) {
            throw new TuningForkRuntimeException("file must not be null");
        }

        final String fileExtension = file.extension();
        SoundFileType soundFileType = SoundFileType.getByFileEnding(fileExtension);
        if (soundFileType == null) {
            try {
                soundFileType = SoundFileType.parseFromFile(file);
            } catch (final IOException e) {
                throw new TuningForkRuntimeException(e);
            }
        }

        if (soundFileType != null) {
            switch (soundFileType) {
                case FLAC:
                    return reverse ? FlacLoader.loadReadableReverse(file) : FlacLoader.loadReadable(file);
                case OGG:
                    return reverse ? OggLoader.loadReadableReverse(file) : OggLoader.loadReadable(file);
                case WAV:
                    return reverse ? WaveLoader.loadReadableReverse(file) : WaveLoader.loadReadable(file);
                case MP3:
                    return reverse ? Mp3Loader.loadReadableReverse(file) : Mp3Loader.loadReadable(file);
                case AIFF:
                    return reverse ? AiffLoader.loadReadableReverse(file) : AiffLoader.loadReadable(file);
                case QOA:
                    return reverse ? QoaLoader.loadReadableReverse(file) : QoaLoader.loadReadable(file);
            }
        }

        throw new TuningForkRuntimeException("Couldn't identify file type: " + file);
    }

}
