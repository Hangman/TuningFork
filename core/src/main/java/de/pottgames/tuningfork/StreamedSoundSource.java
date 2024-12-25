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
import de.pottgames.tuningfork.decoder.QoaInputStream;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.decoder.util.Util;
import de.pottgames.tuningfork.jukebox.song.SongSource;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A {@link SoundSource} that streams audio data instead of loading all data at once into memory.
 *
 * @author Matthias
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
        bufferTimeQueue = new FloatArray(true, StreamedSoundSource.BUFFER_COUNT + 1);
        audio = Audio.get();
        if (audio == null) {
            throw new TuningForkRuntimeException("StreamedSoundSource cannot be created before Audio is initialized.");
        }
        logger = audio.getLogger();
        errorLogger = new ErrorLogger(this.getClass(), logger);
        audioStream = stream;

        // FETCH DATA & FORMAT FROM INPUT STREAM
        duration = stream.getDuration();
        if (duration < 0f) {
            logger.debug(this.getClass(), "Unable to measure sound duration");
        }
        final int sampleRate = audioStream.getSampleRate();
        final int channels = audioStream.getChannels();
        final int sampleDepth = audioStream.getBitsPerSample();
        final int bytesPerSample = sampleDepth / 8;
        final PcmDataType pcmDataType = audioStream.getPcmDataType();
        pcmFormat = PcmFormat.determineFormat(channels, sampleDepth, pcmDataType);
        if (pcmFormat == null) {
            throw new TuningForkRuntimeException("Unsupported pcm format - channels: " + channels + ", sample depth: " + sampleDepth);
        }

        // CREATE BUFFERS
        final int blockSize = audioStream.getBlockSize();
        final int bufferSize = determineBufferSize(channels, blockSize, (int) Math.ceil(audioStream.getBitsPerSample() / 8d));
        tempBuffer = BufferUtils.createByteBuffer(bufferSize);
        tempBytes = new byte[bufferSize];
        secondsPerBuffer = (float) bufferSize / (bytesPerSample * channels * sampleRate);
        bytesPerSecond = bytesPerSample * channels * sampleRate;
        buffers = BufferUtils.createIntBuffer(StreamedSoundSource.BUFFER_COUNT);
        AL10.alGenBuffers(buffers);
        errorLogger.checkLogError("Buffers couldn't be created");
        if (blockSize > 0) {
            for (int i = 0; i < StreamedSoundSource.BUFFER_COUNT; i++) {
                final int bufferId = buffers.get(i);
                final int blockAlign = audioStream.getBlockAlign();
                AL11.alBufferi(bufferId, SOFTBlockAlignment.AL_UNPACK_BLOCK_ALIGNMENT_SOFT, blockAlign);
                errorLogger.checkLogError("Couldn't set blockAlign");
                logger.trace(this.getClass(), "setting block align to " + blockAlign);
            }
        }

        // INITIAL BUFFER FILL
        fillAllBuffersInternal();
        errorLogger.checkLogError("An error occured while pre-buffering");

        // REGISTER IN AUDIO
        audio.streamManager.registerSource(this);
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
        audioStream = audioStream.reset();
    }


    void updateAsync() {
        synchronized (this) {
            int processedBufferCount = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED);
            checkPlaybackPosResetAsync();

            boolean end = false;
            while (processedBufferCount > 0) {
                processedBufferCount--;

                final float processedSeconds = bufferTimeQueue.removeIndex(0);
                processedTime += processedSeconds;
                checkPlaybackPosResetAsync();

                final int bufferId = AL10.alSourceUnqueueBuffers(sourceId);
                if (bufferId == AL10.AL_INVALID_VALUE) {
                    break;
                }

                if (end) {
                    continue;
                }
                if (fillBufferInternal(bufferId)) {
                    AL10.alSourceQueueBuffers(sourceId, bufferId);
                } else {
                    end = true;
                }
            }

            final int queuedBuffers = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED);

            if (end && queuedBuffers == 0) {
                stopInternal();
                manuallySetBehindLoopEnd = false;
            } else if (playing.get() && AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING && queuedBuffers > 0) {
                // A buffer underflow will cause the source to stop, so we should resume playback in this case.
                AL10.alSourcePlay(sourceId);
            }
        }
    }


    private void checkPlaybackPosResetAsync() {
        if (bufferTimeQueue.size > 0) {
            final float value = bufferTimeQueue.get(0);
            if (value == Float.MAX_VALUE) {
                bufferTimeQueue.removeIndex(0);
                processedTime = 0f;
            } else if (value == Float.MIN_VALUE) {
                bufferTimeQueue.removeIndex(0);
                processedTime = loopStart;
            }
        }
    }


    /**
     * Returns the current playback position in seconds.<br>
     * <b>Note that the returned value is subject to small inaccuracies due to the asynchronous nature of this source .</b>
     *
     * @return the playback position
     */
    @Override
    public float getPlaybackPosition() {
        return processedTime + AL10.alGetSourcef(sourceId, AL11.AL_SEC_OFFSET);
    }


    /**
     * Specifies the two offsets the source will use to loop, expressed in seconds.<br>
     * If the playback position is manually set to something &gt; end, the source will not loop and instead stop playback when it reaches the end of the
     * sound.<br>
     * The method will throw an exception if start &gt; end or if either is a negative value. Values &gt; sound duration are not considered invalid, but they'll
     * be clamped internally.<br>
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

        loopStart = start;
        loopEnd = end;
    }


    void skipStreamToPosition(final float seconds) {
        synchronized (this) {
            resetStream();
            int bytesToSkip = (int) (seconds * bytesPerSecond);
            final int overbytes = bytesToSkip % (audioStream.getBitsPerSample() / 8);
            bytesToSkip -= overbytes;
            final byte[] buffer = new byte[bytesToSkip];
            audioStream.read(buffer);
        }
    }


    /**
     * Sets the playback position of this sound source. Invalid values are ignored but an error is logged.
     *
     * @param seconds position in seconds
     */
    public void setPlaybackPosition(float seconds) {
        if (seconds >= 0f) {
            audio.streamManager.postTask(this, TaskAction.SET_PLAYBACK_POSITION, seconds);
        } else {
            logger.error(this.getClass(), "Can't set playback position to values < 0");
        }
    }


    void setPlaybackPositionAsync(final float seconds) {
        synchronized (this) {
            final boolean playing = this.playing.get();
            final boolean stopped = this.stopped.get();

            // FULL RESET
            AL10.alSourceStop(sourceId);
            resetStream();
            bufferTimeQueue.clear();

            // SKIP THE INPUT STREAM UNTIL THE NEW POSITION IS IN REACH
            float currentSeconds = 0f;
            boolean unreachable = false;
            while (currentSeconds < seconds - secondsPerBuffer) {
                final int skippedBytes = audioStream.read(tempBytes);
                if (skippedBytes <= 0) {
                    unreachable = true;
                    break;
                }
                currentSeconds += skippedBytes / bytesPerSecond;
            }
            processedTime = currentSeconds;
            queuedSeconds = currentSeconds;
            manuallySetBehindLoopEnd = queuedSeconds > loopEnd && loopEnd > 0f;

            if (unreachable) {
                stopInternal();
                if (looping && playing) {
                    playInternal();
                }
                return;
            }

            // REFILL BUFFERS
            final int filledBufferCount = fillAllBuffersInternal();

            // SKIP TO PERFECT POSITION IN BUFFER
            if (filledBufferCount > 0) {
                AL10.alSourcef(sourceId, AL11.AL_SEC_OFFSET, seconds - currentSeconds);
            }

            // RESTORE SOURCE STATE
            if (filledBufferCount > 0 && playing) {
                AL10.alSourcePlay(sourceId);
            } else if (filledBufferCount > 0 && !stopped) {
                AL10.alSourcePlay(sourceId);
                AL10.alSourcePause(sourceId);
            } else if (playing) {
                stopInternal();
                if (looping) {
                    playInternal();
                }
            }
        }
    }


    @Override
    public void setLooping(boolean value) {
        looping = value;
    }


    void pauseAsync() {
        synchronized (this) {
            pauseInternal();
        }
    }


    void pauseInternal() {
        super.pause();
        playing.set(false);
        stopped.set(false);
    }


    @Override
    public void pause() {
        if (playing.compareAndSet(true, false)) {
            audio.streamManager.postTask(this, TaskAction.PAUSE);
            stopped.set(false);
        }
    }


    void playAsync() {
        synchronized (this) {
            playInternal();
        }
    }


    void playInternal() {
        super.play();
        playing.set(true);
        stopped.set(false);
    }


    @Override
    public void play() {
        if (playing.compareAndSet(false, true)) {
            audio.streamManager.postTask(this, TaskAction.PLAY);
            stopped.set(false);
        }
    }


    void stopAsync() {
        synchronized (this) {
            stopInternal();
        }
    }


    void stopInternal() {
        super.stop();
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        resetStream();
        processedTime = 0f;
        queuedSeconds = 0f;
        bufferTimeQueue.clear();
        fillAllBuffersInternal();
        playing.set(false);
        stopped.set(true);
    }


    @Override
    public void stop() {
        if (!stopped.get()) {
            audio.streamManager.postTask(this, TaskAction.STOP);
            playing.set(false);
            stopped.set(true);
        }
    }


    private boolean fillBufferInternal(int bufferId) {
        int length = audioStream.read(tempBytes);
        if (length <= 0) {
            if (!looping || manuallySetBehindLoopEnd) {
                bufferTimeQueue.add(Float.MAX_VALUE);
                return false;
            }
            skipStreamToPosition(loopStart);
            queuedSeconds = loopStart;
            bufferTimeQueue.add(Float.MIN_VALUE);
            length = audioStream.read(tempBytes);
            if (length <= 0) {
                return false;
            }
        }

        float secondsInUploadBuffer = length / bytesPerSecond;
        int bytesToUpload = length;
        boolean loopEndCut = false;
        if (looping && loopEnd > 0f && loopEnd > loopStart) {
            if (queuedSeconds + secondsInUploadBuffer >= loopEnd && !manuallySetBehindLoopEnd) {
                secondsInUploadBuffer = loopEnd - queuedSeconds;
                bytesToUpload = (int) (bytesPerSecond * secondsInUploadBuffer);
                final int overbytes = bytesToUpload % (audioStream.getBitsPerSample() / 8);
                bytesToUpload = MathUtils.clamp(bytesToUpload - overbytes, 0, length);
                loopEndCut = true;
                skipStreamToPosition(loopStart);
                queuedSeconds = loopStart;
            }
        }

        bufferTimeQueue.add(secondsInUploadBuffer);
        queuedSeconds += secondsInUploadBuffer;
        if (loopEndCut) {
            bufferTimeQueue.add(Float.MIN_VALUE);
            queuedSeconds = loopStart;
        }
        tempBuffer.clear();
        tempBuffer.put(tempBytes, 0, bytesToUpload).flip();
        AL10.alBufferData(bufferId, pcmFormat.getAlId(), tempBuffer, audioStream.getSampleRate());
        return true;
    }


    private int fillAllBuffersInternal() {
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        errorLogger.checkLogError("error removing buffers from the source");
        int filledBufferCount = 0;
        for (int i = 0; i < StreamedSoundSource.BUFFER_COUNT; i++) {
            final int bufferId = buffers.get(i);
            if (!fillBufferInternal(bufferId)) {
                break;
            }
            filledBufferCount++;
            AL10.alSourceQueueBuffers(sourceId, bufferId);
            errorLogger.checkLogError("error queueing buffers on the source");
        }

        return filledBufferCount;
    }


    @Override
    public boolean isPlaying() {
        return playing.get();
    }


    @Override
    public boolean isPaused() {
        return !playing.get() && !stopped.get();
    }


    /**
     * Returns the duration of the attached sound in seconds.
     *
     * @return the duration of the attached sound<br>
     *         Returns -1f if the duration couldn't be measured.
     */
    @Override
    public float getDuration() {
        return duration;
    }


    void readyToDispose() {
        readyToDispose = true;
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
                case QOA:
                    return new QoaInputStream(file);
            }
        }

        return null;
    }


    /**
     * Disposes the sound sources native resources. You should never use this sound source after disposing it.
     */
    @Override
    public void dispose() {
        audio.streamManager.removeSource(this);
        audio.streamManager.postTask(this, TaskAction.STOP);
        audio.streamManager.postTask(this, TaskAction.DISPOSE_CALLBACK);
        while (!readyToDispose) {
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                // ignore
            }
        }
        super.dispose();
        AL10.alDeleteBuffers(buffers);
        errorLogger.checkLogError("Failed to dispose the SoundSources buffers");
        StreamUtils.closeQuietly(audioStream);
    }

}
