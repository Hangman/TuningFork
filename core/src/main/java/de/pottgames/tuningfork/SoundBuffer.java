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
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.SOFTBlockAlignment;
import org.lwjgl.openal.SOFTBufferLengthQuery;
import org.lwjgl.openal.SOFTLoopPoints;
import org.lwjgl.system.MemoryStack;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * Stores sound data in an OpenAL buffer that can be used by sound sources. Needs to be disposed when no longer needed.
 *
 * @author Matthias
 */
public class SoundBuffer implements Disposable {
    private final Audio            audio;
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final int              bufferId;
    private final PcmFormat        pcmFormat;
    private final float            duration;
    private final int              samplesPerChannel;
    private final float[]          loopPointCache = new float[2];


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * <br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero .<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * The interleaved format also applies to surround sound.
     *
     * @param pcm the pcm data
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType the pcm data type
     */
    public SoundBuffer(byte[] pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType) {
        this(pcm, channels, sampleRate, bitsPerSample, pcmDataType, -1);
    }


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero .<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * The interleaved format also applies to surround sound.
     *
     * @param pcm the pcm data buffer
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType the pcm data type
     * @param blockAlign the block alignment (currently only used for MS ADPCM data)
     */
    public SoundBuffer(ShortBuffer pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType, int blockAlign) {
        this.audio = Audio.get();
        this.logger = this.audio.getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        this.pcmFormat = PcmFormat.determineFormat(channels, bitsPerSample, pcmDataType);
        if (this.pcmFormat == null) {
            throw new TuningForkRuntimeException("Unsupported pcm format - channels: " + channels + ", sample depth: " + bitsPerSample);
        }
        this.bufferId = this.generateBufferAndUpload(pcm, blockAlign, sampleRate);
        this.samplesPerChannel = this.fetchSamplesPerChannel();
        this.duration = this.fetchDuration();
    }


    /**
     * Creates a SoundBuffer with the given pcm data.<br>
     * Consider using {@link #SoundBuffer(byte[], int, int, int, PcmDataType)} instead if you're not providing MS_ADPCM data.<br>
     * 8-bit data is expressed as an unsigned value over the range 0 to 255, 128 being an audio output level of zero .<br>
     * 16-bit data is expressed as a signed value over the range -32768 to 32767, 0 being an audio output level of zero.<br>
     * Stereo data is expressed in an interleaved format, left channel sample followed by the right channel sample.<br>
     * The interleaved format also applies to surround sound.
     *
     * @param pcm the pcm data
     * @param channels number of channels
     * @param sampleRate number of samples per second
     * @param bitsPerSample number of bits per sample
     * @param pcmDataType the pcm data type
     * @param blockAlign the block alignment (currently only used for MS ADPCM data)
     */
    public SoundBuffer(byte[] pcm, int channels, int sampleRate, int bitsPerSample, PcmDataType pcmDataType, int blockAlign) {
        this.audio = Audio.get();
        this.logger = this.audio.getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        // PCM ARRAY TO TEMP BUFFER
        final ByteBuffer buffer = ByteBuffer.allocateDirect(pcm.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(pcm);
        buffer.flip();

        this.pcmFormat = PcmFormat.determineFormat(channels, bitsPerSample, pcmDataType);
        if (this.pcmFormat == null) {
            throw new TuningForkRuntimeException("Unsupported pcm format - channels: " + channels + ", sample depth: " + bitsPerSample);
        }
        this.bufferId = this.generateBufferAndUpload(buffer.asShortBuffer(), blockAlign, sampleRate);
        this.samplesPerChannel = this.fetchSamplesPerChannel();

        this.duration = this.fetchDuration();
    }


    protected int generateBufferAndUpload(ShortBuffer pcm, int blockAlign, int sampleRate) {
        final int bufferId = AL10.alGenBuffers();
        if (blockAlign > 0) {
            AL11.alBufferi(bufferId, SOFTBlockAlignment.AL_UNPACK_BLOCK_ALIGNMENT_SOFT, blockAlign);
        }
        AL10.alBufferData(bufferId, this.pcmFormat.getAlId(), pcm, sampleRate);

        if (!this.errorLogger.checkLogError("Failed to create the SoundBuffer")) {
            this.logger.debug(this.getClass(), "SoundBuffer successfully created");
        }

        return bufferId;
    }


    protected int fetchSamplesPerChannel() {
        return AL10.alGetBufferi(this.bufferId, SOFTBufferLengthQuery.AL_SAMPLE_LENGTH_SOFT);
    }


    protected float fetchDuration() {
        return AL10.alGetBufferf(this.bufferId, SOFTBufferLengthQuery.AL_SEC_LENGTH_SOFT);
    }


    /**
     * Specifies the two offsets the source will use to loop, expressed in seconds. This method will fail when the buffer is attached to a source like: <br>
     * <br>
     * <code>
     * SoundSource source = audio.obtain(soundBuffer); soundBuffer.setLoopPoints(1f, 2f); // this will fail
     * </code> <br>
     * <br>
     * If the playback position is manually set to something &gt; end, the source will not loop and instead stop playback when it reaches the end of the
     * sound.<br>
     * The method will throw an exception if start &gt;= end or if either is a negative value. Values &gt; sound duration are not considered invalid, but
     * they'll be clamped internally.
     *
     * @param start start position of the loop in seconds
     * @param end end position of the loop in seconds
     */
    public void setLoopPoints(float start, float end) {
        if (start >= end) {
            throw new TuningForkRuntimeException("Invalid loop points: start >= end");
        }
        if (start < 0) {
            throw new TuningForkRuntimeException("Invalid loop points: start and end must not be < 0");
        }

        int startSample = (int) (start / this.duration * this.samplesPerChannel);
        startSample = MathUtils.clamp(startSample, 0, this.samplesPerChannel - 1);
        int endSample = (int) (end / this.duration * this.samplesPerChannel);
        endSample = MathUtils.clamp(endSample, 1, this.samplesPerChannel);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer params = stack.mallocInt(2);
            params.put(0, startSample);
            params.put(1, endSample);
            AL11.alBufferiv(this.bufferId, SOFTLoopPoints.AL_LOOP_POINTS_SOFT, params);
        }

        this.errorLogger.checkLogError("Error setting loop points on the buffer");
    }


    /**
     * Returns the loop points set by {@link #setLoopPoints(float, float)}.<br>
     * The returned array has a length of 2, with <br>
     * index 0 =&gt; start<br>
     * index 1 =&gt; end<br>
     * The result might not exactly reflect the numbers from {@link #setLoopPoints(float, float)} because there's a conversion from seconds to samples
     * involved.<br>
     * The returned float array is "owned" by the {@link SoundBuffer} class, so you shouldn't hold a reference to it but rather copy the values.
     *
     * @return the loop points
     */
    public float[] getLoopPoints() {
        int startSample = 0;
        int endSample = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer params = stack.mallocInt(2);
            AL11.alGetBufferiv(this.bufferId, SOFTLoopPoints.AL_LOOP_POINTS_SOFT, params);
            startSample = params.get(0);
            endSample = params.get(1);
        }

        this.loopPointCache[0] = (float) startSample / this.samplesPerChannel * this.duration;
        this.loopPointCache[1] = (float) endSample / this.samplesPerChannel * this.duration;

        return this.loopPointCache;
    }


    /**
     * Plays the sound.
     */
    public void play() {
        this.audio.play(this);
    }


    /**
     * Plays the sound at the specified time. Negative values for time will result in an error log entry but do nothing else. Positive values that point to the
     * past will make the source play immediately.
     *
     * @param time the absolute time in nanoseconds, use {@link AudioDevice#getClockTime()} to get the current time
     */
    public void playAtTime(long time) {
        if (time < 0) {
            this.logger.error(this.getClass(), "Invalid time parameter on playAtTime(): " + time);
            return;
        }
        this.audio.playAtTime(this, time);
    }


    /**
     * Plays a sound with an effect.
     *
     * @param effect the sound effect
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
     * @param effect the sound effect
     */
    public void play(float volume, SoundEffect effect) {
        this.audio.play(this, volume, effect);
    }


    /**
     * Plays the sound with the given volume and pitch.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     */
    public void play(float volume, float pitch) {
        this.audio.play(this, volume, pitch);
    }


    /**
     * Plays the sound with the given volume, pitch and filter.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
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
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param effect the sound effect
     */
    public void play(float volume, float pitch, SoundEffect effect) {
        this.audio.play(this, volume, pitch, effect);
    }


    /**
     * Plays the sound with the given volume, pitch and pan.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param pan in the range of -1.0 (full left) to 1.0 (full right). (default center 0.0)
     */
    public void play(float volume, float pitch, float pan) {
        this.audio.play(this, volume, pitch, pan);
    }


    /**
     * Plays the sound with the given volume, pitch, pan and effect.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param pan in the range of -1.0 (full left) to 1.0 (full right). (default center 0.0)
     * @param effect the sound effect
     */
    public void play(float volume, float pitch, float pan, SoundEffect effect) {
        this.audio.play(this, volume, pitch, pan, effect);
    }


    /**
     * Plays a spatial sound at the given position.
     *
     * @param position the position in 3D space
     */
    public void play3D(Vector3 position) {
        this.audio.play3D(this, position);
    }


    /**
     * Plays a spatial sound with the given filter at the given position.
     *
     * @param position the position in 3D space
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     */
    public void play3D(Vector3 position, float lowFreqVolume, float highFreqVolume) {
        this.audio.play3D(this, position, lowFreqVolume, highFreqVolume);
    }


    /**
     * Plays a spatial sound at the given position with an effect.
     *
     * @param position the position in 3D space
     * @param effect the sound effect
     */
    public void play3D(Vector3 position, SoundEffect effect) {
        this.audio.play3D(this, position, effect);
    }


    /**
     * Plays a spatial sound at the given position with the given effect and filter.
     *
     * @param position the position in 3D space
     * @param lowFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param highFreqVolume range: 0 - 1, 0 means completely silent, 1 means full loudness
     * @param effect the sound effect
     */
    public void play3D(Vector3 position, float lowFreqVolume, float highFreqVolume, SoundEffect effect) {
        this.audio.play3D(this, position, lowFreqVolume, highFreqVolume, effect);
    }


    /**
     * Plays a spatial sound with the given volume at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position the position in 3D space
     */
    public void play3D(float volume, Vector3 position) {
        this.audio.play3D(this, volume, position);
    }


    /**
     * Plays a spatial sound with the given volume and filter at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param position the position in 3D space
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
     * @param position the position in 3D space
     * @param effect the sound effect
     */
    public void play3D(float volume, Vector3 position, SoundEffect effect) {
        this.audio.play3D(this, volume, position, effect);
    }


    /**
     * Plays a spatial sound with the given volume and pitch at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param position the position in 3D space
     */
    public void play3D(float volume, float pitch, Vector3 position) {
        this.audio.play3D(this, volume, pitch, position);
    }


    /**
     * Plays a spatial sound with the given volume, pitch and effect at the given position.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume. (default 1)
     * @param pitch in the range of 0.5 - 2.0 with values &lt; 1 making the sound slower and values &gt; 1 making it faster (default 1)
     * @param position the position in 3D space
     * @param effect the effect
     */
    public void play3D(float volume, float pitch, Vector3 position, SoundEffect effect) {
        this.audio.play3D(this, volume, pitch, position, effect);
    }


    int getBufferId() {
        return this.bufferId;
    }


    /**
     * Returns the PcmFormat of this SoundBuffer.
     *
     * @return the PcmFormat
     */
    public PcmFormat getPcmFormat() {
        return this.pcmFormat;
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
