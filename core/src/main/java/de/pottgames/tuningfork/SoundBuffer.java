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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * Stores sound data in an OpenAL buffer that can be used by sound sources. Needs to be disposed when no longer needed.
 *
 * @author Matthias
 *
 */
public class SoundBuffer implements Disposable {
    private final Audio            audio;
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final int              bufferId;
    private final float            duration;


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * <br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero.<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * The interleaved format also applies to surround sound.
     *
     * @param pcm
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType
     */
    public SoundBuffer(byte[] pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType) {
        this.audio = Audio.get();
        this.logger = this.audio.getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        // DETERMINE PCM FORMAT AND DURATION
        final int samplesPerChannel = pcm.length / (bitsPerSample / 8 * channels);
        this.duration = samplesPerChannel / (float) sampleRate;
        final PcmFormat pcmFormat = PcmFormat.determineFormat(channels, bitsPerSample, pcmDataType);
        if (pcmFormat == null) {
            throw new TuningForkRuntimeException("Unsupported pcm format - channels: " + channels + ", sample depth: " + bitsPerSample);
        }

        // PCM ARRAY TO TEMP BUFFER
        final ByteBuffer buffer = ByteBuffer.allocateDirect(pcm.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(pcm);
        buffer.flip();

        // GEN BUFFER AND UPLOAD PCM DATA
        this.bufferId = AL10.alGenBuffers();
        AL10.alBufferData(this.bufferId, pcmFormat.getAlId(), buffer.asShortBuffer(), sampleRate);

        // CHECK FOR ERRORS
        if (!this.errorLogger.checkLogError("Failed to create the SoundBuffer")) {
            this.logger.debug(this.getClass(), "SoundBuffer successfully created");
        }
    }


    /**
     * Plays the sound.
     *
     */
    public void play() {
        this.audio.play(this);
    }


    /**
     * Plays a sound with an effect.
     *
     * @param effect
     */
    public void play(SoundEffect effect) {
        this.audio.play(this, effect);
    }


    /**
     * Plays the sound with the given volume.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     */
    public void play(float volume) {
        this.audio.play(this, volume);
    }


    /**
     * Plays the sound with the given volume and effect.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param effect
     */
    public void play(float volume, SoundEffect effect) {
        this.audio.play(this, volume, effect);
    }


    /**
     * Plays the sound with the given volume and pitch.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster (default 1)
     */
    public void play(float volume, float pitch) {
        this.audio.play(this, volume, pitch);
    }


    /**
     * Plays the sound with the given volume, pitch and filter.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster (default 1)
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     */
    public void play(float volume, float pitch, float lowFreqVolume, float highFreqVolume) {
        this.audio.play(this, volume, pitch, lowFreqVolume, highFreqVolume);
    }


    /**
     * Plays the sound with the given volume, pitch and effect.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster (default 1)
     * @param effect
     */
    public void play(float volume, float pitch, SoundEffect effect) {
        this.audio.play(this, volume, pitch, effect);
    }


    /**
     * Plays the sound with the given volume, pitch and pan.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster (default 1)
     * @param pan in the range of -1.0 (full left) to 1.0 (full right). (default center 0.0)
     */
    public void play(float volume, float pitch, float pan) {
        this.audio.play(this, volume, pitch, pan);
    }


    /**
     * Plays the sound with the given volume, pitch, pan and effect.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster (default 1)
     * @param pan in the range of -1.0 (full left) to 1.0 (full right). (default center 0.0)
     * @param effect
     */
    public void play(float volume, float pitch, float pan, SoundEffect effect) {
        this.audio.play(this, volume, pitch, pan, effect);
    }


    /**
     * Plays a spatial sound at the given position.
     *
     * @param position
     */
    public void play3D(Vector3 position) {
        this.audio.play3D(this, position);
    }


    /**
     * Plays a spatial sound with the given filter at the given position.
     *
     * @param position
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     */
    public void play3D(Vector3 position, float lowFreqVolume, float highFreqVolume) {
        this.audio.play3D(this, position, lowFreqVolume, highFreqVolume);
    }


    /**
     * Plays a spatial sound at the given position with an effect.
     *
     * @param position
     * @param effect
     */
    public void play3D(Vector3 position, SoundEffect effect) {
        this.audio.play3D(this, position, effect);
    }


    /**
     * Plays a spatial sound at the given position with the given effect and filter.
     *
     * @param position
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param effect
     */
    public void play3D(Vector3 position, float lowFreqVolume, float highFreqVolume, SoundEffect effect) {
        this.audio.play3D(this, position, lowFreqVolume, highFreqVolume, effect);
    }


    /**
     * Plays a spatial sound with the given volume at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position
     */
    public void play3D(float volume, Vector3 position) {
        this.audio.play3D(this, volume, position);
    }


    /**
     * Plays a spatial sound with the given volume and filter at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     */
    public void play3D(float volume, Vector3 position, float lowFreqVolume, float highFreqVolume) {
        this.audio.play3D(this, volume, position, lowFreqVolume, highFreqVolume);
    }


    /**
     * Plays a spatial sound with the given volume and effect at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position
     * @param effect
     */
    public void play3D(float volume, Vector3 position, SoundEffect effect) {
        this.audio.play3D(this, volume, position, effect);
    }


    /**
     * Plays a spatial sound with the given volume and pitch at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster (default 1)
     * @param position
     */
    public void play3D(float volume, float pitch, Vector3 position) {
        this.audio.play3D(this, volume, pitch, position);
    }


    /**
     * Plays a spatial sound with the given volume, pitch and effect at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values < 1 making the sound slower and values > 1 making it faster (default 1)
     * @param position
     * @param effect
     */
    public void play3D(float volume, float pitch, Vector3 position, SoundEffect effect) {
        this.audio.play3D(this, volume, pitch, position, effect);
    }


    int getBufferId() {
        return this.bufferId;
    }


    /**
     * Returns the duration in seconds.
     *
     * @return the playback duration in seconds.
     */
    public float getDuration() {
        return this.duration;
    }


    @Override
    public void dispose() {
        Audio.get().onBufferDisposal(this);
        AL10.alDeleteBuffers(this.bufferId);
        if (!this.errorLogger.checkLogError("Failed to dispose the SoundBuffer")) {
            this.logger.debug(this.getClass(), "SoundBuffer successfully disposed");
        }
    }

}
