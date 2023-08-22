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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.SOFTBlockAlignment;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.StreamManager.TaskAction;
import de.pottgames.tuningfork.decoder.AiffInputStream;
import de.pottgames.tuningfork.decoder.AudioStream;
import de.pottgames.tuningfork.decoder.FlacInputStream;
import de.pottgames.tuningfork.decoder.Mp3InputStream;
import de.pottgames.tuningfork.decoder.OggInputStream;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.decoder.util.Util;
import de.pottgames.tuningfork.jukebox.song.SongSource;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A {@link SoundSource} that streams audio data instead of loading all data at once into memory.
 *
 * @author Matthias
 *
 */
public class StreamedSoundSource extends SongSource implements Disposable {
    public static final int        BUFFER_SIZE_PER_CHANNEL  = 65536;
    public static final int        BUFFER_COUNT             = 3;
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private AudioStream            audioStream;
    private final float            secondsPerBuffer;
    private final IntBuffer        buffers;
    private final PcmFormat        pcmFormat;
    private final ByteBuffer       tempBuffer;
    private final byte[]           tempBytes;
    private final Audio            audio;
    private final AtomicBoolean    playing                  = new AtomicBoolean(false);
    private final AtomicBoolean    stopped                  = new AtomicBoolean(true);
    private volatile boolean       looping                  = false;
    private volatile float         loopStart                = 0f;
    private volatile float         loopEnd                  = 0f;
    private boolean                manuallySetBehindLoopEnd = false;
    private volatile boolean       readyToDispose           = false;
    private final float            duration;

    private final FloatArray bufferTimeQueue;
    private volatile float   processedTime;
    private float            queuedSeconds;
    private final float      bytesPerSecond;


    /**
     * Creates a new {@link StreamedSoundSource} and loads the first bits of sound data.
     *
     * @param file the file
     */
    public StreamedSoundSource(FileHandle file) {
        this(StreamedSoundSource.createAudioStream(file));
    }


    /**
     * Creates a new {@link StreamedSoundSource} from a {@link AudioStream} and loads the first bits of sound data.
     *
     * @param stream the stream
     */
    public StreamedSoundSource(AudioStream stream) {
        if (stream == null) {
            throw new TuningForkRuntimeException("stream is null");
        }

        // FETCH AND SET DEPENDENCIES
        this.bufferTimeQueue = new FloatArray(true, StreamedSoundSource.BUFFER_COUNT + 1);
        this.audio = Audio.get();
        if (this.audio == null) {
            throw new TuningForkRuntimeException("StreamedSoundSource cannot be created before Audio is initialized.");
        }
        this.logger = this.audio.getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);
        this.audioStream = stream;

        // FETCH DATA & FORMAT FROM INPUT STREAM
        this.duration = stream.getDuration();
        if (this.duration < 0f) {
            this.logger.debug(this.getClass(), "Unable to measure sound duration");
        }
        final int sampleRate = this.audioStream.getSampleRate();
        final int channels = this.audioStream.getChannels();
        final int sampleDepth = this.audioStream.getBitsPerSample();
        final int bytesPerSample = sampleDepth / 8;
        final PcmDataType pcmDataType = this.audioStream.getPcmDataType();
        this.pcmFormat = PcmFormat.determineFormat(channels, sampleDepth, pcmDataType);
        if (this.pcmFormat == null) {
            throw new TuningForkRuntimeException("Unsupported pcm format - channels: " + channels + ", sample depth: " + sampleDepth);
        }

        // CREATE BUFFERS
        final int blockSize = this.audioStream.getBlockSize();
        final int bufferSize = this.determineBufferSize(channels, blockSize, (int) Math.ceil(this.audioStream.getBitsPerSample() / 8d));
        this.tempBuffer = BufferUtils.createByteBuffer(bufferSize);
        this.tempBytes = new byte[bufferSize];
        this.secondsPerBuffer = (float) bufferSize / (bytesPerSample * channels * sampleRate);
        this.bytesPerSecond = bytesPerSample * channels * sampleRate;
        this.buffers = BufferUtils.createIntBuffer(StreamedSoundSource.BUFFER_COUNT);
        AL10.alGenBuffers(this.buffers);
        this.errorLogger.checkLogError("Buffers couldn't be created");
        if (blockSize > 0) {
            for (int i = 0; i < StreamedSoundSource.BUFFER_COUNT; i++) {
                final int bufferId = this.buffers.get(i);
                final int blockAlign = this.audioStream.getBlockAlign();
                AL11.alBufferi(bufferId, SOFTBlockAlignment.AL_UNPACK_BLOCK_ALIGNMENT_SOFT, blockAlign);
                this.errorLogger.checkLogError("Couldn't set blockAlign");
                this.logger.trace(this.getClass(), "setting block align to " + blockAlign);
            }
        }

        // INITIAL BUFFER FILL
        this.fillAllBuffersInternal();
        this.errorLogger.checkLogError("An error occured while pre-buffering");

        // REGISTER IN AUDIO
        this.audio.streamManager.registerSource(this);
    }


    private int determineBufferSize(int channels, int blockSize, int bytesPerSample) {
        int bufferSize = StreamedSoundSource.BUFFER_SIZE_PER_CHANNEL * channels;

        // keep block alignment
        if (blockSize > 0) {
            bufferSize = blockSize;
            while (bufferSize < StreamedSoundSource.BUFFER_SIZE_PER_CHANNEL) {
                bufferSize += blockSize;
            }
            return bufferSize;
        }

        bufferSize = Util.nextPowerOfTwo(bufferSize);
        return bufferSize;
    }


    private void resetStream() {
        this.audioStream = this.audioStream.reset();
    }


    void updateAsync() {
        synchronized (this) {
            int processedBufferCount = AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_PROCESSED);
            this.checkPlaybackPosResetAsync();

            boolean end = false;
            while (processedBufferCount > 0) {
                processedBufferCount--;

                final float processedSeconds = this.bufferTimeQueue.removeIndex(0);
                this.processedTime += processedSeconds;
                this.checkPlaybackPosResetAsync();

                final int bufferId = AL10.alSourceUnqueueBuffers(this.sourceId);
                if (bufferId == AL10.AL_INVALID_VALUE) {
                    break;
                }

                if (end) {
                    continue;
                }
                if (this.fillBufferInternal(bufferId)) {
                    AL10.alSourceQueueBuffers(this.sourceId, bufferId);
                } else {
                    end = true;
                }
            }

            final int queuedBuffers = AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_QUEUED);

            if (end && queuedBuffers == 0) {
                this.stopInternal();
                this.manuallySetBehindLoopEnd = false;
            } else if (this.playing.get() && AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING && queuedBuffers > 0) {
                // A buffer underflow will cause the source to stop, so we should resume playback in this case.
                AL10.alSourcePlay(this.sourceId);
            }
        }
    }


    private void checkPlaybackPosResetAsync() {
        if (this.bufferTimeQueue.size > 0) {
            final float value = this.bufferTimeQueue.get(0);
            if (value == Float.MAX_VALUE) {
                this.bufferTimeQueue.removeIndex(0);
                this.processedTime = 0f;
            } else if (value == Float.MIN_VALUE) {
                this.bufferTimeQueue.removeIndex(0);
                this.processedTime = this.loopStart;
            }
        }
    }


    /**
     * Returns the current playback position in seconds.<br>
     * <b>Note that the returned value is subject to small inaccuracies due to the asynchronous nature of this source.</b>
     *
     * @return the playback position
     */
    @Override
    public float getPlaybackPosition() {
        return this.processedTime + AL10.alGetSourcef(this.sourceId, AL11.AL_SEC_OFFSET);
    }


    /**
     * Specifies the two offsets the source will use to loop, expressed in seconds.<br>
     * If the playback position is manually set to something > end, the source will not loop and instead stop playback when it reaches the end of the sound.<br>
     * The method will throw an exception if start > end or if either is a negative value. Values > sound duration are not considered invalid, but they'll be
     * clamped internally.<br>
     * Setting start and end both to 0, deactivates the loop point mechanic.
     *
     * @param start start position of the loop in seconds
     * @param end end position of the loop in seconds
     */
    public void setLoopPoints(float start, float end) {
        if (start > end) {
            throw new TuningForkRuntimeException("Invalid loop points: start > end");
        }
        if (start < 0 || end < 0) {
            throw new TuningForkRuntimeException("Invalid loop points: start and end must not be > 0");
        }

        this.loopStart = start;
        this.loopEnd = end;
    }


    void skipStreamToPosition(final float seconds) {
        synchronized (this) {
            this.resetStream();
            int bytesToSkip = (int) (seconds * this.bytesPerSecond);
            final int overbytes = bytesToSkip % (this.audioStream.getBitsPerSample() / 8);
            bytesToSkip -= overbytes;
            final byte[] buffer = new byte[bytesToSkip];
            this.audioStream.read(buffer);
        }
    }


    /**
     * Sets the playback position of this sound source. Invalid values are ignored but an error is logged.
     *
     * @param seconds position in seconds
     */
    public void setPlaybackPosition(float seconds) {
        if (seconds >= 0f) {
            this.audio.streamManager.postTask(this, TaskAction.SET_PLAYBACK_POSITION, seconds);
        } else {
            this.logger.error(this.getClass(), "Can't set playback position to values < 0");
        }
    }


    void setPlaybackPositionAsync(final float seconds) {
        synchronized (this) {
            final boolean playing = this.playing.get();
            final boolean stopped = this.stopped.get();

            // FULL RESET
            AL10.alSourceStop(this.sourceId);
            this.resetStream();
            this.bufferTimeQueue.clear();

            // SKIP THE INPUT STREAM UNTIL THE NEW POSITION IS IN REACH
            float currentSeconds = 0f;
            boolean unreachable = false;
            while (currentSeconds < seconds - this.secondsPerBuffer) {
                final int skippedBytes = this.audioStream.read(this.tempBytes);
                if (skippedBytes <= 0) {
                    unreachable = true;
                    break;
                }
                currentSeconds += skippedBytes / this.bytesPerSecond;
            }
            this.processedTime = currentSeconds;
            this.queuedSeconds = currentSeconds;
            this.manuallySetBehindLoopEnd = this.queuedSeconds > this.loopEnd && this.loopEnd > 0f;

            if (unreachable) {
                this.stopInternal();
                if (this.looping && playing) {
                    this.playInternal();
                }
                return;
            }

            // REFILL BUFFERS
            final int filledBufferCount = this.fillAllBuffersInternal();

            // SKIP TO PERFECT POSITION IN BUFFER
            if (filledBufferCount > 0) {
                AL10.alSourcef(this.sourceId, AL11.AL_SEC_OFFSET, seconds - currentSeconds);
            }

            // RESTORE SOURCE STATE
            if (filledBufferCount > 0 && playing) {
                AL10.alSourcePlay(this.sourceId);
            } else if (filledBufferCount > 0 && !playing && !stopped) {
                AL10.alSourcePlay(this.sourceId);
                AL10.alSourcePause(this.sourceId);
            } else if (playing) {
                this.stopInternal();
                if (this.looping) {
                    this.playInternal();
                }
            }
        }
    }


    @Override
    public void setLooping(boolean value) {
        this.looping = value;
    }


    void pauseAsync() {
        synchronized (this) {
            this.pauseInternal();
        }
    }


    void pauseInternal() {
        super.pause();
        this.playing.set(false);
        this.stopped.set(false);
    }


    @Override
    public void pause() {
        if (this.playing.compareAndSet(true, false)) {
            this.audio.streamManager.postTask(this, TaskAction.PAUSE);
            this.stopped.set(false);
        }
    }


    void playAsync() {
        synchronized (this) {
            this.playInternal();
        }
    }


    void playInternal() {
        super.play();
        this.playing.set(true);
        this.stopped.set(false);
    }


    @Override
    public void play() {
        if (this.playing.compareAndSet(false, true)) {
            this.audio.streamManager.postTask(this, TaskAction.PLAY);
            this.stopped.set(false);
        }
    }


    void stopAsync() {
        synchronized (this) {
            this.stopInternal();
        }
    }


    void stopInternal() {
        super.stop();
        AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        this.resetStream();
        this.processedTime = 0f;
        this.queuedSeconds = 0f;
        this.bufferTimeQueue.clear();
        this.fillAllBuffersInternal();
        this.playing.set(false);
        this.stopped.set(true);
    }


    @Override
    public void stop() {
        if (!this.stopped.get()) {
            this.audio.streamManager.postTask(this, TaskAction.STOP);
            this.playing.set(false);
            this.stopped.set(true);
        }
    }


    private boolean fillBufferInternal(int bufferId) {
        int length = this.audioStream.read(this.tempBytes);
        if (length <= 0) {
            if (!this.looping || this.manuallySetBehindLoopEnd) {
                this.bufferTimeQueue.add(Float.MAX_VALUE);
                return false;
            }
            this.skipStreamToPosition(this.loopStart);
            this.queuedSeconds = this.loopStart;
            this.bufferTimeQueue.add(Float.MIN_VALUE);
            length = this.audioStream.read(this.tempBytes);
            if (length <= 0) {
                return false;
            }
        }

        float secondsInUploadBuffer = length / this.bytesPerSecond;
        int bytesToUpload = length;
        boolean loopEndCut = false;
        if (this.looping && this.loopEnd > 0f && this.loopEnd > this.loopStart) {
            if (this.queuedSeconds + secondsInUploadBuffer >= this.loopEnd && !this.manuallySetBehindLoopEnd) {
                secondsInUploadBuffer = this.loopEnd - this.queuedSeconds;
                bytesToUpload = (int) (this.bytesPerSecond * secondsInUploadBuffer);
                final int overbytes = bytesToUpload % (this.audioStream.getBitsPerSample() / 8);
                bytesToUpload = MathUtils.clamp(bytesToUpload - overbytes, 0, length);
                loopEndCut = true;
                this.skipStreamToPosition(this.loopStart);
                this.queuedSeconds = this.loopStart;
            }
        }

        this.bufferTimeQueue.add(secondsInUploadBuffer);
        this.queuedSeconds += secondsInUploadBuffer;
        if (loopEndCut) {
            this.bufferTimeQueue.add(Float.MIN_VALUE);
            this.queuedSeconds = this.loopStart;
        }
        this.tempBuffer.clear();
        this.tempBuffer.put(this.tempBytes, 0, bytesToUpload).flip();
        AL10.alBufferData(bufferId, this.pcmFormat.getAlId(), this.tempBuffer, this.audioStream.getSampleRate());
        return true;
    }


    private int fillAllBuffersInternal() {
        AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        this.errorLogger.checkLogError("error removing buffers from the source");
        int filledBufferCount = 0;
        for (int i = 0; i < StreamedSoundSource.BUFFER_COUNT; i++) {
            final int bufferId = this.buffers.get(i);
            if (!this.fillBufferInternal(bufferId)) {
                break;
            }
            filledBufferCount++;
            AL10.alSourceQueueBuffers(this.sourceId, bufferId);
            this.errorLogger.checkLogError("error queueing buffers on the source");
        }

        return filledBufferCount;
    }


    @Override
    public boolean isPlaying() {
        return this.playing.get();
    }


    @Override
    public boolean isPaused() {
        return !this.playing.get() && !this.stopped.get();
    }


    /**
     * Returns the duration of the attached sound in seconds.
     *
     * @return the duration of the attached sound<br>
     *         Returns -1f if the duration couldn't be measured.
     */
    @Override
    public float getDuration() {
        return this.duration;
    }


    void readyToDispose() {
        this.readyToDispose = true;
    }


    private static AudioStream createAudioStream(FileHandle file) {
        final String fileExtension = file.extension();
        SoundFileType soundFileType = SoundFileType.getByFileEnding(fileExtension);
        if (soundFileType == null) {
            try {
                soundFileType = SoundFileType.parseFromFile(file);
            } catch (final IOException e) {
                // ignore
            }
        }

        if (soundFileType != null) {
            switch (soundFileType) {
                case FLAC:
                    return new FlacInputStream(file);
                case OGG:
                    return new OggInputStream(file, null);
                case WAV:
                    return new WavInputStream(file);
                case MP3:
                    return new Mp3InputStream(file);
                case AIFF:
                    return new AiffInputStream(file);
            }
        }

        return null;
    }


    /**
     * Disposes the sound sources native resources. You should never use this sound source after disposing it.
     */
    @Override
    public void dispose() {
        this.audio.streamManager.removeSource(this);
        this.audio.streamManager.postTask(this, TaskAction.STOP);
        this.audio.streamManager.postTask(this, TaskAction.DISPOSE_CALLBACK);
        while (!this.readyToDispose) {
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                // ignore
            }
        }
        super.dispose();
        AL10.alDeleteBuffers(this.buffers);
        this.errorLogger.checkLogError("Failed to dispose the SoundSources buffers");
        StreamUtils.closeQuietly(this.audioStream);
    }

}
