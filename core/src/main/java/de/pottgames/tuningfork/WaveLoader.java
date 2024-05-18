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

import java.io.File;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.bindings.ImaAdpcmData;
import de.pottgames.tuningfork.bindings.ImaAdpcmRs;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.misc.PcmUtil;

public abstract class WaveLoader {

    /**
     * Loads a wav file into a {@link SoundBuffer}.
     *
     * @param file the file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return WaveLoader.load(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads a wav file into a {@link SoundBuffer}.
     *
     * @param file the file
     * @param useJavaDecoder enforces the java decoder instead of the native one
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file, boolean useJavaDecoder) {
        return WaveLoader.load(Gdx.files.absolute(file.getAbsolutePath()), useJavaDecoder);
    }


    /**
     * Loads a wav file into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        final WavInputStream input = new WavInputStream(file, false);
        return WaveLoader.load(input);
    }


    /**
     * Loads a wav file into a {@link SoundBuffer}.
     *
     * @param file the file handle
     * @param useJavaDecoder enforces the java decoder instead of the native one
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file, boolean useJavaDecoder) {
        final WavInputStream input = new WavInputStream(file, useJavaDecoder);
        return WaveLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from an {@link InputStream} and closes it afterward.
     *
     * @param stream the input stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        final WavInputStream input = new WavInputStream(stream, false);
        return WaveLoader.load(input);
    }


    /**
     * Loads a {@link SoundBuffer} from a {@link WavInputStream}.
     *
     * @param input the WavInputStream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(WavInputStream input) {
        SoundBuffer result = null;
        try {
            final byte[] buffer = new byte[(int) input.bytesRemaining()];
            input.read(buffer);
            result = new SoundBuffer(buffer, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType(),
                    input.getBlockAlign());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }


    /**
     * Disclaimer: This is a special use case load method. If you just want to load a wav, no matter what encoding, use one of the other load methods.<br>
     * <br>
     * Loads an IMA ADPCM encoded wav file into a {@link SoundBuffer}. The file must be present on the file system (not packed into a jar), be careful with this
     * as packing everything is libGDXs default behavior. Loading is completely done in native code and therefore a bit faster. Leads to a crash when fed with a
     * wav file that is not IMA ADPCM encoded.
     *
     * @param path must be a file that exists on the file system
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadFastImaAdpcm(String path) {
        if (!Audio.get().isNativeDecodersAvailable()) {
            return null;
        }

        final ImaAdpcmRs decoder = new ImaAdpcmRs();
        final ImaAdpcmData data = decoder.decodeFile(path);
        if (data == null) {
            throw new TuningForkRuntimeException("Error decoding " + path);
        }
        return new SoundBuffer(data.pcmData, data.numChannels, data.sampleRate, 16, PcmDataType.INTEGER);
    }


    /**
     * Loads a wav file in reverse into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadReverse(FileHandle file) {
        final WavInputStream input = new WavInputStream(file, false);
        SoundBuffer result = null;
        try {
            if (input.getBitsPerSample() % 8 != 0) {
                throw new TuningForkRuntimeException("Reverse loading isn't supported for sample sizes that aren't divisible by 8.");
            }

            final byte[] buffer = new byte[(int) input.bytesRemaining()];
            input.read(buffer);
            final byte[] reversedPcm = PcmUtil.reverseAudio(buffer, input.getBitsPerSample() / 8);
            result = new SoundBuffer(reversedPcm, input.getChannels(), input.getSampleRate(), input.getBitsPerSample(), input.getPcmDataType(),
                    input.getBlockAlign());
        } finally {
            StreamUtils.closeQuietly(input);
        }

        return result;
    }

}
