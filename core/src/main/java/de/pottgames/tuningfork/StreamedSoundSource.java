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
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.Audio.TaskAction;
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.decoder.AudioStream;
import de.pottgames.tuningfork.decoder.FlacInputStream;
import de.pottgames.tuningfork.decoder.OggInputStream;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A {@link SoundSource} that streams audio data instead of loading all data at once into memory.
 *
 * @author Matthias
 *
 */
public class StreamedSoundSource extends SoundSource implements Disposable {
    public static int              bufferSizePerChannel            = 32000;
    public static int              bufferCount                     = 3;
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private AudioStream            audioStream;
    private final float            secondsPerBuffer;
    private final IntBuffer        buffers;
    private final PcmFormat        pcmFormat;
    private final int              bufferSize;
    private final ByteBuffer       tempBuffer;
    private final byte[]           tempBytes;
    private final Audio            audio;
    private final AtomicBoolean    playing                         = new AtomicBoolean(false);
    private final AtomicBoolean    stopped                         = new AtomicBoolean(true);
    private volatile boolean       looping                         = false;
    private volatile int           processedBuffers                = 0;
    private final AtomicInteger    lastQueuedBufferId              = new AtomicInteger();
    private final AtomicInteger    resetProcessedBuffersOnBufferId = new AtomicInteger();
    private volatile boolean       readyToDispose                  = false;
    private float                  duration                        = -1f;


    /**
     * Creates a new {@link StreamedSoundSource} and loads the first bits of sound data.
     *
     * @param file
     */
    public StreamedSoundSource(FileHandle file) {
        this(StreamedSoundSource.createAudioStream(file));
    }


    /**
     * Creates a new {@link StreamedSoundSource} from a {@link AudioStream} and loads the first bits of sound data.
     *
     * @param stream
     */
    public StreamedSoundSource(AudioStream stream) {
        if (stream == null) {
            throw new TuningForkRuntimeException("stream is null");
        }

        // FETCH AND SET DEPENDENCIES
        this.audio = Audio.get();
        this.logger = this.audio.getLogger();
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);

        // SET DEFAULTS
        this.setAttenuationFactor(this.audio.getDefaultAttenuationFactor());
        this.setAttenuationMinDistance(this.audio.getDefaultAttenuationMinDistance());
        this.setAttenuationMaxDistance(this.audio.getDefaultAttenuationMaxDistance());

        // CREATE INPUT STREAM
        this.audioStream = stream;
        this.resetProcessedBuffersOnBufferId.set(this.lastQueuedBufferId.get());

        // FETCH DATA & FORMAT FROM INPUT STREAM
        this.duration = stream.getDuration();
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
        this.bufferSize = StreamedSoundSource.bufferSizePerChannel * channels;
        this.tempBuffer = BufferUtils.createByteBuffer(this.bufferSize);
        this.tempBytes = new byte[this.bufferSize];
        this.secondsPerBuffer = (float) this.bufferSize / (bytesPerSample * channels * sampleRate);
        this.buffers = BufferUtils.createIntBuffer(StreamedSoundSource.bufferCount);
        AL10.alGenBuffers(this.buffers);

        // INITIAL BUFFER FILL
        this.audio.postTask(this, TaskAction.INITIAL_BUFFER_FILL);

        // REGISTER IN AUDIO
        this.audio.registerStreamedSoundSource(this);
    }


    private void resetStream() {
        this.resetProcessedBuffersOnBufferId.set(this.lastQueuedBufferId.get());
        this.audioStream = this.audioStream.reset();
    }


    void updateAsync() {
        int processedBufferCount = AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_PROCESSED);
        if (processedBufferCount > 0) {
            this.processedBuffers += processedBufferCount;
        }

        boolean end = false;
        while (processedBufferCount > 0) {
            processedBufferCount--;
            final int bufferId = AL10.alSourceUnqueueBuffers(this.sourceId);
            if (bufferId == AL10.AL_INVALID_VALUE) {
                break;
            }

            if (this.resetProcessedBuffersOnBufferId.compareAndSet(bufferId, 0)) {
                this.processedBuffers = 0;
            }

            if (end) {
                continue;
            }
            if (this.fillBuffer(bufferId)) {
                AL10.alSourceQueueBuffers(this.sourceId, bufferId);
                this.lastQueuedBufferId.set(bufferId);
            } else {
                end = true;
            }
        }
        if (end && AL10.alGetSourcei(this.sourceId, AL10.AL_BUFFERS_QUEUED) == 0) {
            this.stopAsync();
            this.playing.set(false);
            // TODO: if (onCompletionListener != null) onCompletionListener.onCompletion(this);
        }

        // A buffer underflow will cause the source to stop, so we should resume playback in this case.
        if (this.playing.get() && AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
            AL10.alSourcePlay(this.sourceId);
        }
    }


    /**
     * Returns the current playback position in seconds.
     *
     * @return the playback position
     */
    public float getPlaybackPosition() {
        return this.processedBuffers * this.secondsPerBuffer + AL10.alGetSourcef(this.sourceId, AL11.AL_SEC_OFFSET);
    }


    /**
     * Sets the playback position of this sound source. Invalid values are ignored but an error is logged.
     *
     * @param seconds
     */
    public void setPlaybackPosition(float seconds) {
        if (seconds >= 0f) {
            this.audio.postTask(this, TaskAction.SET_PLAYBACK_POSITION, seconds);
        } else {
            this.logger.error(this.getClass(), "Can't setPlaybackPosition to values < 0");
        }
    }


    void setPlaybackPositionAsync(final float seconds) {
        // STOP THE SOURCE
        AL10.alSourceStop(this.sourceId);

        // FULL RESET
        this.resetProcessedBuffersOnBufferId.set(0);
        this.lastQueuedBufferId.set(0);
        this.resetStream();

        // SKIP THE INPUT STREAM UNTIL THE NEW POSITION IS IN REACH
        float currentSeconds = 0f;
        int buffersSkipped = 0;
        boolean unreachable = false;
        while (currentSeconds < seconds - this.secondsPerBuffer) {
            if (this.audioStream.read(this.tempBytes) <= 0) {
                unreachable = true;
                break;
            }
            currentSeconds += this.secondsPerBuffer;
            buffersSkipped++;
        }
        if (unreachable) {
            this.processedBuffers = 0;
        } else {
            this.processedBuffers = buffersSkipped;
        }

        // REFILL BUFFERS
        final int filledBufferCount = this.fillAllBuffers();

        // SKIP TO PERFECT POSITION IN BUFFER
        if (filledBufferCount > 0 && !unreachable) {
            AL10.alSourcef(this.sourceId, AL11.AL_SEC_OFFSET, seconds - currentSeconds);
        }

        // CONTINUE PLAYING IF THAT WAS THE LAST STATE
        final boolean playing = this.playing.get();
        if (filledBufferCount > 0 && playing) {
            AL10.alSourcePlay(this.sourceId);
        } else if (playing) {
            this.playing.set(false);
            // TODO: WHAT IF THE SOURCE IS LOOPING?
        }
    }


    @Override
    public void setLooping(boolean value) {
        this.looping = value;
    }


    void pauseAsync() {
        super.pause();
        this.playing.set(false);
        this.stopped.set(false);
    }


    @Override
    public void pause() {
        if (this.playing.get()) {
            this.audio.postTask(this, TaskAction.PAUSE);
            this.playing.set(false);
            this.stopped.set(false);
        }
    }


    void playAsync() {
        super.play();
        this.playing.set(true);
        this.stopped.set(false);
    }


    @Override
    public void play() {
        if (!this.playing.get()) {
            this.audio.postTask(this, TaskAction.PLAY);
            this.playing.set(true);
            this.stopped.set(false);
        }
    }


    void stopAsync() {
        super.stop();
        AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        this.resetProcessedBuffersOnBufferId.set(0);
        this.lastQueuedBufferId.set(0);
        this.processedBuffers = 0;
        this.resetStream();
        this.fillAllBuffers();
        this.playing.set(false);
        this.stopped.set(true);
    }


    @Override
    public void stop() {
        if (!this.stopped.get()) {
            this.audio.postTask(this, TaskAction.STOP);
            this.playing.set(false);
            this.stopped.set(true);
        }
    }


    private boolean fillBuffer(int bufferId) {
        this.tempBuffer.clear();
        int length = this.audioStream.read(this.tempBytes);
        if (length <= 0) {
            if (!this.looping) {
                return false;
            }
            this.resetStream();
            length = this.audioStream.read(this.tempBytes);
            if (length <= 0) {
                return false;
            }
        }

        this.tempBuffer.put(this.tempBytes, 0, length).flip();
        AL10.alBufferData(bufferId, this.pcmFormat.getAlId(), this.tempBuffer, this.audioStream.getSampleRate());
        return true;
    }


    int fillAllBuffers() {
        AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        this.resetProcessedBuffersOnBufferId.set(0);
        this.lastQueuedBufferId.set(0);
        int filledBufferCount = 0;
        for (int i = 0; i < StreamedSoundSource.bufferCount; i++) {
            final int bufferId = this.buffers.get(i);
            if (!this.fillBuffer(bufferId)) {
                break;
            }
            filledBufferCount++;
            AL10.alSourceQueueBuffers(this.sourceId, bufferId);
            this.lastQueuedBufferId.set(bufferId);
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
    public float getDuration() {
        return this.duration;
    }


    void readyToDispose() {
        this.readyToDispose = true;
    }


    private static AudioStream createAudioStream(FileHandle file) {
        final String fileExtension = file.extension();
        final SoundFileType soundFileType = SoundFileType.getByFileEnding(fileExtension);

        switch (soundFileType) {
            case FLAC:
                return new FlacInputStream(file);
            case OGG:
                return new OggInputStream(file, null);
            case WAV:
                return new WavInputStream(file);
        }

        return null;
    }


    /**
     * Disposes the sound sources native resources. You should never use this sound source after disposing it.
     */
    @Override
    public void dispose() {
        this.audio.removeStreamedSound(this);
        this.audio.postTask(this, TaskAction.STOP);
        this.audio.postTask(this, TaskAction.DISPOSE_CALLBACK);
        while (!this.readyToDispose) {
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                // ignore
            }
        }
        super.dispose();
        AL10.alDeleteBuffers(this.buffers);
        this.errorLogger.checkLogError("Failed to dispose the SoundSources Buffers");
        StreamUtils.closeQuietly(this.audioStream);
    }

}
