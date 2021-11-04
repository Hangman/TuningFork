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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * The main management and entry point of TuningFork. This class initializes the sound device and provides "fire & forget" features for sounds and also gives
 * access to SoundSource's for advanced manual playback control.
 *
 * @author Matthias
 *
 */
public class Audio implements Disposable {
    private static Audio                           instance;
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
    private final ExecutorService                  taskService;
    private final ConcurrentLinkedQueue<AsyncTask> idleTasks                        = new ConcurrentLinkedQueue<>();
    private float                                  defaultMinAttenuationDistance    = 1f;
    private float                                  defaultMaxAttenuationDistance    = Float.MAX_VALUE;
    private float                                  defaultAttenuationFactor         = 1f;
    final TuningForkLogger                         logger;


    static Audio get() {
        return Audio.instance;
    }


    /**
     * Creates an audio instance initialized on the default sound device of the OS.
     */
    public Audio() {
        this(Audio.DEFAULT_SOURCES_POOL_CAPACITY, Audio.DEFAULT_IDLE_TASKS_POOL_CAPACITY, new GdxLogger());
    }


    /**
     * Creates an audio instance initialized on the default sound device of the OS.
     *
     * @param simultaneousSources defines how many non-streamed sounds can be played simultaneously.
     */
    public Audio(int simultaneousSources) {
        this(simultaneousSources, Audio.DEFAULT_IDLE_TASKS_POOL_CAPACITY, new GdxLogger());
    }


    private Audio(int simultaneousSources, int idleTasks, TuningForkLogger logger) {
        if (Audio.instance != null) {
            throw new TuningForkRuntimeException("Only 1 Audio instance allowed at any time.");
        }
        Audio.instance = this;

        // SET LOGGER
        if (logger != null) {
            this.logger = logger;
        } else {
            this.logger = new MockLogger();
        }

        // INITIAL IDLE TASK CREATION FOR THE POOL
        for (int i = 0; i < idleTasks - 1; i++) {
            this.idleTasks.add(new AsyncTask());
        }

        // CREATE THE TASK SERVICE
        this.taskService = Executors.newSingleThreadExecutor(r -> {
            final Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });

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
        this.setDistanceAttenuationModel(DistanceAttenuationModel.INVERSE_DISTANCE_CLAMPED);

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
        this.updateThread.setName("TuningFork-Update-Thread");
        this.updateThread.setDaemon(true);
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


    /**
     * As the listener moves away from a sound source, the volume of the sound source decreases for the listener. The Attenuation Model is responsible for this
     * calculation.<br>
     * <br>
     * <b>Note:</b> Setting a DistanceAttenuationModel overwrites the default min and max attenuation distance as well as the attenuation factor that is used
     * for new sound sources. It won't affect existing ones.<br>
     * <br>
     * There are several {@link de.pottgames.tuningfork.DistanceAttenuationModel models} to choose from, including a
     * {@link de.pottgames.tuningfork.DistanceAttenuationModel#LINEAR_DISTANCE_CLAMPED linear model} and a
     * {@link de.pottgames.tuningfork.DistanceAttenuationModel#INVERSE_DISTANCE_CLAMPED semi-realistic model} based on the real world. By default the
     * {@link de.pottgames.tuningfork.DistanceAttenuationModel#INVERSE_DISTANCE_CLAMPED INVERSE_DISTANCE_CLAMPED} model is used.
     *
     * @param model the model
     */
    public void setDistanceAttenuationModel(DistanceAttenuationModel model) {
        AL10.alDistanceModel(model.getAlId());
        this.setDefaultAttenuationFactor(model.getAttenuationFactor());
        this.setDefaultAttenuationMinDistance(model.getAttenuationMinDistance());
        this.setDefaultAttenuationMaxDistance(model.getAttenuationMaxDistance());
    }


    public void setDefaultAttenuationMinDistance(float distance) {
        this.defaultMinAttenuationDistance = distance;
    }


    public void setDefaultAttenuationMaxDistance(float distance) {
        this.defaultMaxAttenuationDistance = distance;
    }


    public void setDefaultAttenuationFactor(float rolloff) {
        this.defaultAttenuationFactor = rolloff;
    }


    public float getDefaultAttenuationMinDistance() {
        return this.defaultMinAttenuationDistance;
    }


    public float getDefaultAttenuationMaxDistance() {
        return this.defaultMaxAttenuationDistance;
    }


    public float getDefaultAttenuationFactor() {
        return this.defaultAttenuationFactor;
    }


    public void setMasterVolume(float volume) {
        this.listener.setMasterVolume(MathUtils.clamp(volume, 0f, 1f));
    }


    /**
     * Changing the doppler factor exaggerates or deemphasizes the doppler effect. Physically accurate doppler calculation might not give the desired result, so
     * changing this to your needs is fine. The default doppler factor is 1. Values < 0 are ignored, 0 turns the doppler effect off, values greater than 1 will
     * increase the strength of the doppler effect.
     *
     * @param dopplerFactor (default 1)
     */
    public void setDopplerFactor(float dopplerFactor) {
        if (dopplerFactor > 0f) {
            AL10.alDopplerFactor(dopplerFactor);
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


    public void stopAll() {
        this.stopAllBufferedSources();
        this.stopAllStreamedSources();
    }


    public void stopAllStreamedSources() {
        this.postTask(TaskAction.STOP_ALL);
    }


    public void stopAllBufferedSources() {
        this.sourcePool.stopAll();
    }


    void addIdleTask(AsyncTask task) {
        this.idleTasks.offer(task);
    }


    public StreamedSound createStreamedSound(FileHandle fileHandle) {
        final StreamedSound sound = new StreamedSound(fileHandle);
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


    void postTask(TaskAction action) {
        AsyncTask task = this.idleTasks.poll();
        if (task == null) {
            task = new AsyncTask();
        }
        task.taskAction = action;
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
                this.logger.debug(this.getClass(), "The task service timed out on shutdown.");
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

        Audio.instance = null;
    }


    enum TaskAction {
        PLAY, STOP, PAUSE, SET_PLAYBACK_POSITION, INITIAL_BUFFER_FILL, STOP_ALL;
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
                        case STOP_ALL:
                            for (int i = 0; i < Audio.this.soundsToUpdate.size; i++) {
                                final StreamedSound sound = Audio.this.soundsToUpdate.get(i);
                                sound.stopAsync();
                            }
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
