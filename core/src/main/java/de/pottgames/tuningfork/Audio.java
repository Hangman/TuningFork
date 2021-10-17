package de.pottgames.tuningfork;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * The main management and entry point of TuningFork. This class initializes the sound device and provides fire&forget features for sounds and also gives access
 * to SoundSource's for advanced manual playback control.
 *
 * @author Matthias
 *
 */
public class Audio implements Disposable {
    private static final int                       DEFAULT_IDLE_TASKS_POOL_CAPACITY = 10;
    private static final int                       DEFAULT_SOURCES_POOL_CAPACITY    = 20;
    final Object                                   lock                             = new Object();
    private long                                   device;
    private long                                   context;
    private SoundListener                          listener;
    private SoundSourcePool                        sourcePool;
    private final Thread                           updateThread;
    private volatile boolean                       running                          = true;
    private final Array<StreamedSound>             soundsToUpdate                   = new Array<>();
    private final ExecutorService                  taskService                      = Executors.newSingleThreadExecutor();
    private final ConcurrentLinkedQueue<AsyncTask> idleTasks                        = new ConcurrentLinkedQueue<>();


    /**
     * Creates an audio instance initialized on the default sound device of the OS.
     */
    public Audio() {
        this(Audio.DEFAULT_SOURCES_POOL_CAPACITY, Audio.DEFAULT_IDLE_TASKS_POOL_CAPACITY);
    }


    /**
     * Creates an audio instance initialized on the default sound device of the OS.
     *
     * @param simultaneousSources defines how many non-streamed sounds can be played simultaneously.
     */
    public Audio(int simultaneousSources) {
        this(simultaneousSources, Audio.DEFAULT_IDLE_TASKS_POOL_CAPACITY);
    }


    private Audio(int simultaneousSources, int idleTasks) {
        // INITIAL IDLE TASK CREATION FOR THE POOL
        for (int i = 0; i < idleTasks - 1; i++) {
            this.idleTasks.add(new AsyncTask());
        }
        // adding the last task by executing it for warm up
        this.taskService.execute(new AsyncTask());

        // OPEN THE DEFAULT SOUND DEVICE
        this.device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (this.device == 0L) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        // CREATE A CONTEXT AND SET IT ACTIVE
        final ALCCapabilities deviceCapabilities = ALC.createCapabilities(this.device);
        this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);
        if (this.context == 0L) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        ALC10.alcMakeContextCurrent(this.context);
        AL.createCapabilities(deviceCapabilities);

        // SET DISTANCE ATTENUATION MODEL
        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);

        // CREATE LISTENER
        this.listener = new SoundListener();

        // CREATE SOURCES
        this.sourcePool = new SoundSourcePool(simultaneousSources);

        // START UPDATE THREAD
        this.updateThread = new Thread() {
            @Override
            public void run() {
                while (Audio.this.running) {
                    Audio.this.updateAsync();
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException e) {
                        // ignore
                    }
                }
            }
        };
        this.updateThread.start();
    }


    private void updateAsync() {
        synchronized (this.lock) {
            for (int i = 0; i < this.soundsToUpdate.size; i++) {
                final StreamedSound sound = this.soundsToUpdate.get(i);
                sound.updateAsync();
            }
        }
    }


    public BufferedSoundSource obtainSource(SoundBuffer buffer) {
        return this.obtainSource(buffer, false);
    }


    public BufferedSoundSource obtainSource(SoundBuffer buffer, boolean allowNull) {
        // FIND FREE SOUND SOURCE
        final BufferedSoundSource source = this.sourcePool.findFreeSource();

        // THROW EXCEPTION IF ALL SOUND SOURCES ARE BUSY/OBTAINED
        if (!allowNull && source == null) {
            throw new TuningForkRuntimeException(
                    "All SoundSources are busy. Make sure to call free on obtained SoundSources when the sound finished playing. Otherwise consider increasing the simultaneousSources.");
        }

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer);
        source.setRelative(false);

        return source;
    }


    private BufferedSoundSource obtainRelativeSource(SoundBuffer buffer, boolean looping) {
        // FIND FREE SOUND SOURCE
        final BufferedSoundSource source = this.sourcePool.findFreeSource();

        // THROW EXCEPTION IF ALL SOUND SOURCES ARE BUSY/OBTAINED
        if (source == null) {
            throw new TuningForkRuntimeException(
                    "All SoundSources are busy. Make sure to call free on obtained SoundSources when the sound finished playing. Otherwise consider increasing the simultaneousSources.");
        }

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer);
        source.setRelative(true);

        return source;
    }


    public void play(SoundBuffer buffer) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.play();
        source.obtained = false;
    }


    public void play(SoundBuffer buffer, float volume) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.play();
        source.obtained = false;
    }


    public void play(SoundBuffer buffer, float volume, float pitch) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.play();
        source.obtained = false;
    }


    public void play(SoundBuffer buffer, float volume, float pitch, float pan) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        AL10.alSource3f(source.sourceId, AL10.AL_POSITION, MathUtils.cos((pan - 1f) * MathUtils.PI / 2f), 0f, MathUtils.sin((pan + 1f) * MathUtils.PI / 2f));
        source.play();
        source.obtained = false;
    }


    public void play3D(SoundBuffer buffer, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    public void play3D(SoundBuffer buffer, float volume, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    public void play3D(SoundBuffer buffer, float volume, float pitch, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    void addIdleTask(AsyncTask task) {
        this.idleTasks.offer(task);
    }


    public StreamedSound createStreamedSound(FileHandle fileHandle) {
        final StreamedSound sound = new StreamedSound(this, fileHandle);
        synchronized (this.lock) {
            this.soundsToUpdate.add(sound);
        }
        return sound;
    }


    void removeStreamedSound(StreamedSound sound) {
        synchronized (this.lock) {
            this.soundsToUpdate.removeValue(sound, true);
        }
    }


    public SoundListener getListener() {
        return this.listener;
    }


    void postTask(StreamedSound sound, TaskAction action) {
        this.postTask(sound, action, 0f);
    }


    void postTask(StreamedSound sound, TaskAction action, float floatParam) {
        AsyncTask task = this.idleTasks.poll();
        if (task == null) {
            task = new AsyncTask();
        }
        task.sound = sound;
        task.taskAction = action;
        task.floatParam = floatParam;
        this.taskService.execute(task);
    }


    @Override
    public void dispose() {
        // TERMINATE UPDATE THREAD
        this.running = false;
        try {
            this.updateThread.join();
        } catch (final InterruptedException e1) {
            // ignore
        }

        // SHUTDOWN TASK SERVICE
        this.taskService.shutdown();
        try {
            if (!this.taskService.awaitTermination(500L, TimeUnit.MILLISECONDS)) {
                Gdx.app.debug("TuningFork", "The task service timed out on shutdown.");
            }
        } catch (final InterruptedException e) {
            this.taskService.shutdownNow();
        }

        // DISPOSE SOUND SOURCE POOL
        this.sourcePool.dispose();

        // SHUTDOWN OPEN AL
        if (this.context != 0L) {
            ALC10.alcDestroyContext(this.context);
        }
        if (this.device != 0L) {
            ALC10.alcCloseDevice(this.device);
        }
    }


    enum TaskAction {
        PLAY, STOP, PAUSE, SET_PLAYBACK_POSITION, INITIAL_BUFFER_FILL;
    }


    private class AsyncTask implements Runnable {
        private volatile StreamedSound sound;
        private volatile TaskAction    taskAction;
        private volatile float         floatParam;


        @Override
        public void run() {
            if (this.sound != null) {
                synchronized (Audio.this.lock) {
                    switch (this.taskAction) {
                        case PAUSE:
                            this.sound.pauseAsync();
                            break;
                        case PLAY:
                            this.sound.playAsync();
                            break;
                        case STOP:
                            this.sound.stopAsync();
                            break;
                        case SET_PLAYBACK_POSITION:
                            this.sound.setPlaybackPositionAsync(this.floatParam);
                            break;
                        case INITIAL_BUFFER_FILL:
                            this.sound.fillAllBuffers();
                            break;
                        default:
                            break;
                    }
                }

                // CLEAN UP
                this.reset();
                Audio.this.addIdleTask(this);
            }
        }


        private void reset() {
            this.sound = null;
            this.taskAction = null;
            this.floatParam = 0f;
        }

    }

}
