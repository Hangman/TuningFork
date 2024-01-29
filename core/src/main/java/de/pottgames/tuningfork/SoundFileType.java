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

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.decoder.util.Util;
import de.pottgames.tuningfork.misc.ExperimentalFeature;

public enum SoundFileType {
    OGG, WAV, FLAC, MP3, AIFF, QOA;


    /**
     * Retrieves a SoundFileType enum based on the provided file extension.
     *
     * @param fileExtension The file extension to be used for determining the SoundFileType.
     *
     * @return A SoundFileType enum representing the type of audio file associated with the given extension, or null if no matching type is found.
     */
    public static SoundFileType getByFileEnding(String fileExtension) {
        if ("ogg".equalsIgnoreCase(fileExtension) || "oga".equalsIgnoreCase(fileExtension) || "ogx".equalsIgnoreCase(fileExtension)
                || "opus".equalsIgnoreCase(fileExtension)) {
            return OGG;
        }
        if ("wav".equalsIgnoreCase(fileExtension) || "wave".equalsIgnoreCase(fileExtension)) {
            return WAV;
        }
        if ("flac".equalsIgnoreCase(fileExtension)) {
            return FLAC;
        }
        if ("mp3".equalsIgnoreCase(fileExtension)) {
            return MP3;
        }
        if ("aiff".equalsIgnoreCase(fileExtension) || "aif".equalsIgnoreCase(fileExtension) || "aifc".equalsIgnoreCase(fileExtension)) {
            return AIFF;
        }
        if ("qoa".equalsIgnoreCase(fileExtension)) {
            return QOA;
        }

        return null;
    }


    /**
     * Parses a sound file type from the provided FileHandle by examining its header data.
     *
     * @param file The FileHandle pointing to the sound file.
     *
     * @return A SoundFileType enum representing the type of the sound file, or null if it cannot be determined.
     *
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public static SoundFileType parseFromFile(FileHandle file) throws IOException {
        final byte[] streamData = new byte[12];
        final InputStream stream = file.read();
        try {
            Util.readAll(stream, streamData, streamData.length);
        } catch (final IOException e) {
            throw e;
        } finally {
            StreamUtils.closeQuietly(stream);
        }

        if (SoundFileType.isWavHeader(streamData)) {
            return WAV;
        }
        if (SoundFileType.isOggHeader(streamData)) {
            return OGG;
        }
        if (SoundFileType.isFlacHeader(streamData)) {
            return FLAC;
        }
        if (SoundFileType.isAiffHeader(streamData) || SoundFileType.isAifcHeader(streamData)) {
            return AIFF;
        }
        if (SoundFileType.isMp3File(file)) {
            return MP3;
        }
        if (SoundFileType.isQoaHeader(streamData)) {
            return QOA;
        }

        return null;
    }


    /**
     * Checks if the provided byte array represents an AIFF (Audio Interchange File Format) header.
     *
     * @param header The byte array containing the header data to be checked.
     *
     * @return true if the header represents an AIFF file, false otherwise.
     */
    public static boolean isAiffHeader(byte[] header) {
        if (header.length < 12) {
            return false;
        }
        if (header[0] != 'F' || header[1] != 'O' || header[2] != 'R' || header[3] != 'M') {
            return false;
        }
        if (header[8] != 'A' || header[9] != 'I' || header[10] != 'F' || header[11] != 'F') {
            return false;
        }
        return true;
    }


    /**
     * Checks if the provided byte array represents an AIFC header.
     *
     * @param header The byte array containing the header data to be checked.
     *
     * @return true if the header represents an AIFC file, false otherwise.
     */
    public static boolean isAifcHeader(byte[] header) {
        if (header.length < 12) {
            return false;
        }
        if (header[0] != 'F' || header[1] != 'O' || header[2] != 'R' || header[3] != 'M') {
            return false;
        }
        if (header[8] != 'A' || header[9] != 'I' || header[10] != 'F' || header[11] != 'C') {
            return false;
        }
        return true;
    }


    /**
     * Checks if the provided byte array represents a WAV header.
     *
     * @param header The byte array containing the header data to be checked.
     *
     * @return true if the header represents a WAV file, false otherwise.
     */
    public static boolean isWavHeader(byte[] header) {
        if (header.length < 12) {
            return false;
        }
        if (header[0] != 'R' || header[1] != 'I' || header[2] != 'F' || header[3] != 'F') {
            return false;
        }
        if (header[8] != 'W' || header[9] != 'A' || header[10] != 'V' || header[11] != 'E') {
            return false;
        }
        return true;
    }


    /**
     * Checks if the provided byte array represents a QOA header.
     *
     * @param header The byte array containing the header data to be checked.
     *
     * @return true if the header represents a QOA file, false otherwise.
     */
    public static boolean isQoaHeader(byte[] header) {
        if (header.length < 4) {
            return false;
        }
        return header[0] == 'q' && header[1] == 'o' && header[2] == 'a' && header[3] == 'f';
    }


    /**
     * This function always returns false, hence private.
     *
     * @param file
     *
     * @return
     */
    @ExperimentalFeature
    private static boolean isMp3File(FileHandle file) {
        // FIXME
        // As always.... mp3 is causing problems. Of course it doesn't provide a simple magic identifier at the beginning of the file.
        // The most suitable solution seems to be to parse frame headers and search for the sync code. Interpreting the header data to dertemine
        // the length of the frame, skip that far and look if there's another sync code. That does not guarantee it's a mp3 file but it's safe
        // enough to assume it. Right now, I don't want to invest the time to implement it. Maybe later.
        return false;
    }


    /**
     * Checks if the provided byte array represents a FLAC header.
     *
     * @param header The byte array containing the header data to be checked.
     *
     * @return true if the header represents a FLAC file, false otherwise.
     */
    public static boolean isFlacHeader(byte[] header) {
        if (header.length < 4) {
            return false;
        }
        return header[0] == 'f' && header[1] == 'L' && header[2] == 'a' && header[3] == 'C';
    }


    public static boolean isOggHeader(byte[] header) {
        if (header.length < 4) {
            return false;
        }
        return header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S';
    }

}
