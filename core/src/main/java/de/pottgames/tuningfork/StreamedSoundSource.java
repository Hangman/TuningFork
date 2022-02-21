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

import de.pottgames.com.jcraft.jorbis.JOrbisException;
import de.pottgames.com.jcraft.jorbis.VorbisFile;
import de.pottgames.tuningfork.Audio.TaskAction;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

public class StreamedSoundSource extends SoundSource implements Disposable {
    private static final int       BUFFER_SIZE                     = 4096 * 10;
    private static final int       BUFFER_COUNT                    = 3;
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final FileHandle       file;
    private AudioStream            audioStream;
    private final float            secondsPerBuffer;
    private final IntBuffer        buffers;
    private final PcmFormat        pcmFormat;
    private final ByteBuffer       tempBuffer                      = BufferUtils.createByteBuffer(StreamedSoundSource.BUFFER_SIZE);
    private final byte[]           tempBytes                       = new byte[StreamedSoundSource.BUFFER_SIZE];
    private final Audio            audio;
    private AtomicBoolean          playing                         = new AtomicBoolean(false);
    private AtomicBoolean          stopped                         = new AtomicBoolean(true);
    private volatile boolean       looping                         = false;
    private volatile int           processedBuffers                = 0;
    private AtomicInteger          lastQueuedBufferId              = new AtomicInteger();
    private AtomicInteger          resetProcessedBuffersOnBufferId = new AtomicInteger();
    private volatile boolean       readyToDispose                  = false;
    private float                  duration                        = -1f;
    private boolean                ogg                             = false;
    private boolean                wav                             = false;


    /**
     * Creates a new {@link StreamedSoundSource} and loads the first bits of sound data.
     *
     * @param file
     */
    public StreamedSoundSource(FileHandle file) {
        if (file == null) {
            throw new TuningForkRuntimeException("file is null");
        }

        // FETCH AND SET DEPENDENCIES
        this.audio = Audio.get();
        this.logger = this.audio.logger;
        this.errorLogger = new ErrorLogger(this.getClass(), this.logger);
        this.file = file;

        // SET DEFAULTS
        this.setAttenuationFactor(this.audio.getDefaultAttenuationFactor());
        this.setAttenuationMinDistance(this.audio.getDefaultAttenuationMinDistance());
        this.setAttenuationMaxDistance(this.audio.getDefaultAttenuationMaxDistance());

        // CHECK FILE TYPE
        final String fileExtension = file.extension();
        if ("ogg".equalsIgnoreCase(fileExtension) || "oga".equalsIgnoreCase(fileExtension) || "ogx".equalsIgnoreCase(fileExtension)
                || "opus".equalsIgnoreCase(fileExtension)) {
            this.ogg = true;
        } else if ("wav".equalsIgnoreCase(fileExtension) || "wave".equalsIgnoreCase(fileExtension)) {
            this.wav = true;
        } else {
            throw new TuningForkRuntimeException("Unsupported file '" + fileExtension + "', only ogg and wav files are supported.");
        }

        // CREATE INPUT STREAM
        this.initInputStream(false);

        // CHECK DURATION
        if (this.ogg) {
            try {
                final VorbisFile vorbisFile = new VorbisFile(file.file());
                this.duration = vorbisFile.time_total(-1);
            } catch (final JOrbisException e) {
                this.logger.error(this.getClass(), "Couldn't measure the sound duration of: " + file.path());
            }
        } else if (this.wav) {
            final WavInputStream stream = (WavInputStream) this.audioStream;
            this.duration = stream.getRemainingByteCount() / (stream.getSampleRate() * stream.getChannels() * stream.getBitsPerSample() / 8f);
        }

        // FETCH DATA & FORMAT FROM INPUT STREAM
        final int sampleRate = this.audioStream.getSampleRate();
        final int channels = this.audioStream.getChannels();
        final int sampleDepth = this.audioStream.getBitsPerSample();
        final int bytesPerSample = sampleDepth / 8;
        this.pcmFormat = PcmFormat.getBySampleDepthAndChannels(channels, sampleDepth);
        if (this.pcmFormat == null) {
            throw new TuningForkRuntimeException("Unsupported pcm format - channels: " + channels + ", sample depth: " + sampleDepth);
        }

        // CREATE BUFFERS
        this.secondsPerBuffer = (float) StreamedSoundSource.BUFFER_SIZE / (bytesPerSample * channels * sampleRate);
        this.buffers = BufferUtils.createIntBuffer(StreamedSoundSource.BUFFER_COUNT);
        AL10.alGenBuffers(this.buffers);

        // INITIAL BUFFER FILL
        this.audio.postTask(this, TaskAction.INITIAL_BUFFER_FILL);

        // REGISTER IN AUDIO
        this.audio.registerStreamedSoundSource(this);
    }


    private void initInputStream(boolean reuseInputStream) {
        this.resetProcessedBuffersOnBufferId.set(this.lastQueuedBufferId.get());
        if (this.audioStream != null && !this.audioStream.isClosed()) {
            StreamUtils.closeQuietly(this.audioStream);
        }

        if (this.ogg) {
            this.audioStream = new OggInputStream(this.file.read(),
                    reuseInputStream && this.audioStream instanceof OggInputStream ? (OggInputStream) this.audioStream : null);
        } else if (this.wav) {
            this.audioStream = new WavInputStream(this.file);
        }
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
        this.initInputStream(false);

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
        this.initInputStream(false);
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
            this.initInputStream(true);
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
        for (int i = 0; i < StreamedSoundSource.BUFFER_COUNT; i++) {
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
