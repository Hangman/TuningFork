package de.pottgames.tuningfork;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.Audio.TaskAction;

public class StreamedSound implements Disposable {
    private static final int  BUFFER_SIZE                     = 4096 * 10;
    private static final int  BUFFER_COUNT                    = 3;
    private static final int  BYTES_PER_SAMPLE                = 2;
    private final FileHandle  file;
    private final SoundSource source;
    private AudioStream       audioStream;
    private final float       secondsPerBuffer;
    private final IntBuffer   buffers;
    private final int         audioFormat;
    private final ByteBuffer  tempBuffer                      = BufferUtils.createByteBuffer(StreamedSound.BUFFER_SIZE);
    private final byte[]      tempBytes                       = new byte[StreamedSound.BUFFER_SIZE];
    private final Audio       audio;
    private AtomicBoolean     playing                         = new AtomicBoolean(false);
    private boolean           stopped                         = true;
    private volatile boolean  looping                         = false;
    private volatile int      processedBuffers                = 0;
    private AtomicInteger     lastQueuedBufferId              = new AtomicInteger();                                    // TODO: ATOMIC NEEDED?
    private AtomicInteger     resetProcessedBuffersOnBufferId = new AtomicInteger();                                    // TODO: ATOMIC NEEDED?


    StreamedSound(Audio audio, FileHandle file) {
        this.audio = audio;
        this.file = file;

        // CREATE SOUND SOURCE
        this.source = new SoundSource();
        this.source.obtained = true;

        // CREATE INPUT STREAM
        this.initInputStream(false);

        // FETCH DATA & FORMAT FROM INPUT STREAM
        final int sampleRate = this.audioStream.getSampleRate();
        final int channels = this.audioStream.getChannels();
        this.audioFormat = channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;

        // CREATE BUFFERS
        this.secondsPerBuffer = (float) StreamedSound.BUFFER_SIZE / (StreamedSound.BYTES_PER_SAMPLE * channels * sampleRate);
        this.buffers = BufferUtils.createIntBuffer(StreamedSound.BUFFER_COUNT);
        AL10.alGenBuffers(this.buffers);

        // INITIAL BUFFER FILL
        this.fillAllBuffers();
    }


    private void initInputStream(boolean reuseInputStream) {
        this.resetProcessedBuffersOnBufferId.set(this.lastQueuedBufferId.get());
        if (this.audioStream != null && !this.audioStream.isClosed()) {
            StreamUtils.closeQuietly(this.audioStream);
        }

        final String fileExtension = this.file.extension();
        if ("ogg".equalsIgnoreCase(fileExtension)) {
            this.audioStream = new OggInputStream(this.file.read(),
                    reuseInputStream && this.audioStream instanceof OggInputStream ? (OggInputStream) this.audioStream : null);
        } else if ("wav".equalsIgnoreCase(fileExtension) || "wave".equalsIgnoreCase(fileExtension)) {
            this.audioStream = new WavInputStream(this.file);
            // TODO: CAN WE REUSE THE WAVINPUTSTREAM LIKE WITH OGG?
        } else {
            throw new TuningForkRuntimeException("Unsupported file '" + fileExtension + "', only ogg and wav files are supported.");
        }
    }


    void updateAsync() {
        int processedBufferCount = AL10.alGetSourcei(this.source.sourceId, AL10.AL_BUFFERS_PROCESSED);
        if (processedBufferCount > 0) {
            this.processedBuffers += processedBufferCount;
        }

        boolean end = false;
        while (processedBufferCount > 0) {
            processedBufferCount--;
            final int bufferId = AL10.alSourceUnqueueBuffers(this.source.sourceId);
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
                AL10.alSourceQueueBuffers(this.source.sourceId, bufferId);
                this.lastQueuedBufferId.set(bufferId);
            } else {
                end = true;
            }
        }
        if (end && AL10.alGetSourcei(this.source.sourceId, AL10.AL_BUFFERS_QUEUED) == 0) {
            this.stopAsync();
            this.playing.set(false);
            // if (onCompletionListener != null) onCompletionListener.onCompletion(this);
        }

        // A buffer underflow will cause the source to stop, so we should resume playback in this case.
        if (this.playing.get() && AL10.alGetSourcei(this.source.sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
            AL10.alSourcePlay(this.source.sourceId);
        }
    }


    public void setPlaybackPosition(float seconds) {
        if (seconds >= 0f) {
            this.audio.postTask(this, TaskAction.SET_PLAYBACK_POSITION, seconds);
        } else {
            Gdx.app.error("TuningFork", "Can't setPlaybackPosition to values < 0");
        }
    }


    void setPlaybackPositionAsync(final float seconds) {
        // STOP THE SOURCE
        AL10.alSourceStop(this.source.sourceId);

        // TODO: CHECK IF NEEDED
        this.resetProcessedBuffersOnBufferId.set(0);
        this.lastQueuedBufferId.set(0);
        //
        this.initInputStream(false);
        float currentSeconds = 0f;

        // SKIP THE INPUT STREAM UNTIL THE NEW POSITION IS IN REACH
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
            AL10.alSourcef(this.source.sourceId, AL11.AL_SEC_OFFSET, seconds - currentSeconds);
        }

        // CONTINUE PLAYING IF THAT WAS THE LAST STATE
        final boolean playing = this.playing.get();
        if (filledBufferCount > 0 && playing) {
            AL10.alSourcePlay(this.source.sourceId);
        } else if (playing) {
            this.playing.set(false);
            // TODO: WHAT IF THE SOURCE IS LOOPING?
        }
    }


    public void setLooping(boolean value) {
        this.looping = value;
    }


    public float getPlaybackPosition() {
        return this.processedBuffers * this.secondsPerBuffer + AL10.alGetSourcef(this.source.sourceId, AL11.AL_SEC_OFFSET);
    }


    void pauseAsync() {
        AL10.alSourcePause(this.source.sourceId);
    }


    public void pause() {
        if (this.playing.get()) {
            this.audio.postTask(this, TaskAction.PAUSE);
            this.playing.set(false);
            this.stopped = false;
        }
    }


    void playAsync() {
        AL10.alSourcePlay(this.source.sourceId);
    }


    public void play() {
        if (!this.playing.get()) {
            this.audio.postTask(this, TaskAction.PLAY);
            this.playing.set(true);
            this.stopped = false;
        }
    }


    void stopAsync() {
        AL10.alSourceRewind(this.source.sourceId);
        AL10.alSourcei(this.source.sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        // TODO: CHECK IF NEEDED
        this.resetProcessedBuffersOnBufferId.set(0);
        this.lastQueuedBufferId.set(0);
        this.processedBuffers = 0;
        //
        this.initInputStream(false);
        this.fillAllBuffers();
    }


    public void stop() {
        if (!this.stopped) {
            this.audio.postTask(this, TaskAction.STOP);
            this.playing.set(false);
            this.stopped = true;
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
        AL10.alBufferData(bufferId, this.audioFormat, this.tempBuffer, this.audioStream.getSampleRate());
        return true;
    }


    private int fillAllBuffers() {
        AL10.alSourcei(this.source.sourceId, AL10.AL_BUFFER, 0); // removes all buffers from the source
        // TODO: CHECK IF NEEDED
        this.resetProcessedBuffersOnBufferId.set(0);
        this.lastQueuedBufferId.set(0);
        //
        int filledBufferCount = 0;
        for (int i = 0; i < StreamedSound.BUFFER_COUNT; i++) {
            final int bufferId = this.buffers.get(i);
            if (!this.fillBuffer(bufferId)) {
                break;
            }
            filledBufferCount++;
            AL10.alSourceQueueBuffers(this.source.sourceId, bufferId);
            this.lastQueuedBufferId.set(bufferId);
        }

        return filledBufferCount;
    }


    @Override
    public void dispose() {
        this.audio.removeStreamedSound(this);
        this.source.dispose();
        StreamUtils.closeQuietly(this.audioStream);
        // TODO: CLEANUP OPENAL STUFF: SOURCE, BUFFER, ETC.
    }

}
