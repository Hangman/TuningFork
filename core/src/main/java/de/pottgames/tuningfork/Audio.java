package de.pottgames.tuningfork;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * The main management and entry point of TuningFork. This class initializes the sound device and provides "fire & forget" features for sounds and also gives
 * access to SoundSource's for advanced manual playback control.
 *
 * @author Matthias
 *
 */
public class Audio implements Disposable {
    private static Audio instance;

    final Object                                   lock                          = new Object();
    private SoundListener                          listener;
    private SoundSourcePool                        sourcePool;
    private final Thread                           updateThread;
    private volatile boolean                       running                       = true;
    private final Array<StreamedSoundSource>       soundsToUpdate                = new Array<>();
    private final ExecutorService                  taskService;
    private final ConcurrentLinkedQueue<AsyncTask> idleTasks                     = new ConcurrentLinkedQueue<>();
    private float                                  defaultMinAttenuationDistance = 1f;
    private float                                  defaultMaxAttenuationDistance = Float.MAX_VALUE;
    private float                                  defaultAttenuationFactor      = 1f;
    private boolean                                virtualizationEnabled         = true;
    final TuningForkLogger                         logger;
    private final AudioDevice                      device;


    /**
     * Returns a list of identifiers of available sound devices. You can use an identifier in {@link AudioDeviceConfig#deviceSpecifier} to request a specific
     * sound device for audio playback.
     *
     * @return the list
     */
    public static List<String> availableDevices() {
        return ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
    }


    /**
     * Initializes an Audio instance with the default {@link AudioConfig}. Errors are logged but exceptions are silently ignored. Call
     * {@link Audio#initSafe(AudioConfig)} instead, if you want to handle exceptions.
     *
     * @return the initialized Audio instance or null on failure
     */
    public static Audio init() {
        return Audio.init(new AudioConfig());
    }


    /**
     * Initializes an Audio instance with the default {@link AudioConfig}. If you don't want to take care of exceptions, call {@link Audio#init(AudioConfig)}
     * instead.
     *
     * @return the initialized Audio instance
     *
     * @throws OpenDeviceException
     * @throws UnsupportedAudioDeviceException
     */
    public static Audio initSafe() throws OpenDeviceException, UnsupportedAudioDeviceException {
        return Audio.initSafe(new AudioConfig());
    }


    /**
     * Initializes an Audio instance with the given {@link AudioConfig}. Errors are logged but exceptions are silently ignored. Call
     * {@link Audio#initSafe(AudioConfig)} instead, if you want to handle exceptions.
     *
     * @param config
     *
     * @return the initialized Audio instance or null on failure
     */
    public static Audio init(AudioConfig config) {
        AudioDevice device;
        Audio audio = null;
        try {
            device = new AudioDevice(config.getDeviceConfig(), config.getLogger());
            audio = new Audio(device, config);
        } catch (final Exception e) {
            config.getLogger().error(Audio.class, "Failed to init Audio. Details: " + e.getMessage());
        }

        return audio;
    }


    /**
     * Initializes an Audio instance with the given {@link AudioConfig}. If you don't want to take care of exceptions, call {@link Audio#init(AudioConfig)}
     * instead.
     *
     * @param config
     *
     * @return the initialized Audio instance
     *
     * @throws OpenDeviceException
     * @throws UnsupportedAudioDeviceException
     */
    public static Audio initSafe(AudioConfig config) throws OpenDeviceException, UnsupportedAudioDeviceException {
        final AudioDevice device = new AudioDevice(config.getDeviceConfig(), config.getLogger());
        return new Audio(device, config);
    }


    private Audio(AudioDevice device, AudioConfig config) {
        this.logger = config.getLogger();
        this.device = device;
        Audio.instance = this;

        // INITIAL IDLE TASK CREATION FOR THE POOL
        for (int i = 0; i < config.getIdleTasks() - 1; i++) {
            this.idleTasks.add(new AsyncTask());
        }

        // CREATE THE TASK SERVICE
        this.taskService = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName("TuningFork-Task-Thread");
            thread.setDaemon(true);
            return thread;
        });

        // adding the last task by executing it for warm up
        this.taskService.execute(new AsyncTask());

        // SET DEFAULTS
        this.setDistanceAttenuationModel(config.getDistanceAttenuationModel());
        this.virtualizationEnabled = config.isVirtualizationEnabled();

        // CREATE LISTENER
        this.listener = new SoundListener();

        // CREATE SOURCES
        this.sourcePool = new SoundSourcePool(config.getSimultaneousSources());

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


    /**
     * Returns the currently active instance of Audio.
     *
     * @return the instance
     */
    public static Audio get() {
        return Audio.instance;
    }


    /**
     * Returns the currently used AudioDevice.
     *
     * @return the device in charge
     */
    public AudioDevice getDevice() {
        return this.device;
    }


    private void updateAsync() {
        synchronized (this.lock) {
            for (int i = 0; i < this.soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = this.soundsToUpdate.get(i);
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


    /**
     * Sets the distance the listener must be from the sound source at which the attenuation should begin. The attenuation itself is controlled by the
     * attenuation model and the attenuation factor of the source. This value is used for all sources that are created/obtained afterwards, it doesn't affect
     * existing or already obtained sources. If you want to set this per source, you can do so: {@link SoundSource#setAttenuationMinDistance(float)}.
     *
     * @param distance (default depends on the attenuation model)
     */
    public void setDefaultAttenuationMinDistance(float distance) {
        this.defaultMinAttenuationDistance = distance;
    }


    /**
     * Sets the distance the listener must be from the sound source at which the attenuation should stop. The attenuation itself is controlled by the
     * attenuation model and the attenuation factor of the source. This value is used for all sources that are created/obtained afterwards, it doesn't affect
     * existing or already obtained sources. If you want to set this per source, you can do so: {@link SoundSource#setAttenuationMaxDistance(float)}.
     *
     * @param distance (default depends on the attenuation model)
     */
    public void setDefaultAttenuationMaxDistance(float distance) {
        this.defaultMaxAttenuationDistance = distance;
    }


    /**
     * This factor determines how slowly or how quickly the sound source loses volume as the listener moves away from the source. A factor of 0.5 reduces the
     * volume loss by half. With a factor of 2, the source loses volume twice as fast. This factor is used for all sources that are created/obtained afterwards,
     * it doesn't affect existing or already obtained sources. If you want to set this per source, you can do so:
     * {@link SoundSource#setAttenuationFactor(float)}.
     *
     * @param rolloff (default depends on the attenuation model)
     */
    public void setDefaultAttenuationFactor(float rolloff) {
        this.defaultAttenuationFactor = rolloff;
    }


    /**
     * Returns the default attenuation minimum distance that is used to calculate the attenuation by the current default attenuation model.
     *
     * @return the default attenuation min distance
     */
    public float getDefaultAttenuationMinDistance() {
        return this.defaultMinAttenuationDistance;
    }


    /**
     * Returns the default attenuation maximum distance that is used to calculate the attenuation by the current default attenuation model.
     *
     * @return the default attenuation max distance
     */
    public float getDefaultAttenuationMaxDistance() {
        return this.defaultMaxAttenuationDistance;
    }


    /**
     * Returns the default attenuation factor that is used to calculate the attenuation by the current default attenuation model.
     *
     * @return the default attenuation factor
     */
    public float getDefaultAttenuationFactor() {
        return this.defaultAttenuationFactor;
    }


    /**
     * Returns whether virtualization is enabled or disabled by default for all sound sources. See {@link AudioConfig#setVirtualizationEnabled(boolean)} for
     * more info.
     *
     * @return enabled
     */
    public boolean isVirtualizationEnabled() {
        return this.virtualizationEnabled;
    }


    /**
     * Sets the global volume that is applied to all sources.
     *
     * @param volume range: 0 - 1
     */
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


    /**
     * Returns a {@link BufferedSoundSource} for permanent use. Call {@link BufferedSoundSource#free() free()} on it to return it to the pool of available
     * sources.
     *
     * @param buffer
     *
     * @return the {@link BufferedSoundSource}
     */
    public BufferedSoundSource obtainSource(SoundBuffer buffer) {
        // FIND FREE SOUND SOURCE
        final BufferedSoundSource source = this.sourcePool.findFreeSource();

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer);
        source.setRelative(false);

        return source;
    }


    private BufferedSoundSource obtainRelativeSource(SoundBuffer buffer, boolean looping) {
        // FIND FREE SOUND SOURCE
        final BufferedSoundSource source = this.sourcePool.findFreeSource();

        // PREPARE SOURCE
        source.obtained = true;
        source.setBuffer(buffer);
        source.setRelative(true);

        return source;
    }


    /**
     * Plays the sound.
     *
     * @param buffer
     */
    public void play(SoundBuffer buffer) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given filter.
     *
     * @param buffer
     * @param filter
     */
    public void play(SoundBuffer buffer, Filter filter) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setFilter(filter);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a sound with an effect.
     *
     * @param buffer
     * @param effect
     */
    public void play(SoundBuffer buffer, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume.
     *
     * @param buffer
     * @param volume
     */
    public void play(SoundBuffer buffer, float volume) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume and filter.
     *
     * @param buffer
     * @param volume
     * @param filter
     */
    public void play(SoundBuffer buffer, float volume, Filter filter) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setFilter(filter);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume and effect.
     *
     * @param buffer
     * @param volume
     * @param effect
     */
    public void play(SoundBuffer buffer, float volume, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume and pitch.
     *
     * @param buffer
     * @param volume
     * @param pitch
     */
    public void play(SoundBuffer buffer, float volume, float pitch) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch and filter.
     *
     * @param buffer
     * @param volume
     * @param pitch
     * @param filter
     */
    public void play(SoundBuffer buffer, float volume, float pitch, Filter filter) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setFilter(filter);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch and effect.
     *
     * @param buffer
     * @param volume
     * @param pitch
     * @param effect
     */
    public void play(SoundBuffer buffer, float volume, float pitch, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch and pan.
     *
     * @param buffer
     * @param volume
     * @param pitch
     * @param pan
     */
    public void play(SoundBuffer buffer, float volume, float pitch, float pan) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        AL10.alSource3f(source.sourceId, AL10.AL_POSITION, MathUtils.cos((pan - 1f) * MathUtils.PI / 2f), 0f, MathUtils.sin((pan + 1f) * MathUtils.PI / 2f));
        source.play();
        source.obtained = false;
    }


    /**
     * Plays the sound with the given volume, pitch, pan and effect.
     *
     * @param buffer
     * @param volume
     * @param pitch
     * @param pan
     * @param effect
     */
    public void play(SoundBuffer buffer, float volume, float pitch, float pan, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainRelativeSource(buffer, false);
        source.setVolume(volume);
        source.setPitch(pitch);
        AL10.alSource3f(source.sourceId, AL10.AL_POSITION, MathUtils.cos((pan - 1f) * MathUtils.PI / 2f), 0f, MathUtils.sin((pan + 1f) * MathUtils.PI / 2f));
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound at the given position.
     *
     * @param buffer
     * @param position
     */
    public void play3D(SoundBuffer buffer, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given filter at the given position.
     *
     * @param buffer
     * @param position
     * @param filter
     */
    public void play3D(SoundBuffer buffer, Vector3 position, Filter filter) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.setFilter(filter);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound at the given position with an effect.
     *
     * @param buffer
     * @param position
     * @param effect
     */
    public void play3D(SoundBuffer buffer, Vector3 position, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound at the given position with the given effect and filter.
     *
     * @param buffer
     * @param position
     * @param filter
     * @param effect
     */
    public void play3D(SoundBuffer buffer, Vector3 position, Filter filter, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setPosition(position);
        source.attachEffect(effect);
        source.setFilter(filter);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume at the given position.
     *
     * @param buffer
     * @param volume
     * @param position
     */
    public void play3D(SoundBuffer buffer, float volume, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume and filter at the given position.
     *
     * @param buffer
     * @param volume
     * @param position
     * @param filter
     */
    public void play3D(SoundBuffer buffer, float volume, Vector3 position, Filter filter) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPosition(position);
        source.setFilter(filter);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume and effect at the given position.
     *
     * @param buffer
     * @param volume
     * @param position
     * @param effect
     */
    public void play3D(SoundBuffer buffer, float volume, Vector3 position, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPosition(position);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume and pitch at the given position.
     *
     * @param buffer
     * @param volume
     * @param pitch
     * @param position
     */
    public void play3D(SoundBuffer buffer, float volume, float pitch, Vector3 position) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setPosition(position);
        source.play();
        source.obtained = false;
    }


    /**
     * Plays a spatial sound with the given volume, pitch and effect at the given position.
     *
     * @param buffer
     * @param volume
     * @param pitch
     * @param position
     * @param effect
     */
    public void play3D(SoundBuffer buffer, float volume, float pitch, Vector3 position, SoundEffect effect) {
        final BufferedSoundSource source = this.obtainSource(buffer);
        source.setVolume(volume);
        source.setPitch(pitch);
        source.setPosition(position);
        source.attachEffect(effect);
        source.play();
        source.obtained = false;
    }


    /**
     * Resumes to play all sounds that are paused.
     */
    public void resumeAll() {
        this.resumeAllBufferedSources();
        this.resumeAllStreamedSources();
    }


    /**
     * Resumes to play all {@link StreamedSoundSource}s that are paused at the moment.
     */
    public void resumeAllStreamedSources() {
        this.postTask(TaskAction.RESUME_ALL);
    }


    /**
     * Resumes to play all {@link BufferedSoundSource}s that are paused at the moment.
     */
    public void resumeAllBufferedSources() {
        this.sourcePool.resumeAll();
    }


    /**
     * Pauses all sounds.
     */
    public void pauseAll() {
        this.pauseAllBufferedSources();
        this.pauseAllStreamedSources();
    }


    /**
     * Pauses all {@link StreamedSoundSource}.
     */
    public void pauseAllStreamedSources() {
        this.postTask(TaskAction.PAUSE_ALL);
    }


    /**
     * Pauses all {@link BufferedSoundSource}s.
     */
    public void pauseAllBufferedSources() {
        this.sourcePool.pauseAll();
    }


    /**
     * Stops all sounds.
     */
    public void stopAll() {
        this.stopAllBufferedSources();
        this.stopAllStreamedSources();
    }


    /**
     * Stops all {@link StreamedSoundSource}s.
     */
    public void stopAllStreamedSources() {
        this.postTask(TaskAction.STOP_ALL);
    }


    /**
     * Stops all {@link BufferedSoundSource}s.
     */
    public void stopAllBufferedSources() {
        this.sourcePool.stopAll();
    }


    void addIdleTask(AsyncTask task) {
        this.idleTasks.offer(task);
    }


    void registerStreamedSoundSource(StreamedSoundSource source) {
        synchronized (this.lock) {
            this.soundsToUpdate.add(source);
        }
    }


    void removeStreamedSound(StreamedSoundSource sound) {
        synchronized (this.lock) {
            this.soundsToUpdate.removeValue(sound, true);
        }
    }


    /**
     * Returns the {@link SoundListener}.
     *
     * @return the {@link SoundListener}
     */
    public SoundListener getListener() {
        return this.listener;
    }


    void postTask(StreamedSoundSource sound, TaskAction action) {
        this.postTask(sound, action, 0f);
    }


    void postTask(StreamedSoundSource sound, TaskAction action, float floatParam) {
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


    void onBufferDisposal(SoundBuffer buffer) {
        this.sourcePool.onBufferDisposal(buffer);
    }


    /**
     * Shuts down TuningFork.
     */
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

        // STOP ALL BUFFERED SOURCES
        this.stopAllBufferedSources();

        // DISPOSE SOUND SOURCE POOL
        this.sourcePool.dispose();

        // DISPOSE DEVICE LAST
        this.device.dispose(true);

        Audio.instance = null;
    }


    enum TaskAction {
        PLAY, STOP, PAUSE, SET_PLAYBACK_POSITION, INITIAL_BUFFER_FILL, STOP_ALL, PAUSE_ALL, RESUME_ALL, DISPOSE_CALLBACK;
    }


    private class AsyncTask implements Runnable {
        private volatile StreamedSoundSource sound;
        private volatile TaskAction          taskAction;
        private volatile float               floatParam;


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
                                final StreamedSoundSource sound = Audio.this.soundsToUpdate.get(i);
                                sound.stopAsync();
                            }
                            break;
                        case PAUSE_ALL:
                            for (int i = 0; i < Audio.this.soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = Audio.this.soundsToUpdate.get(i);
                                if (sound.isPlaying()) {
                                    sound.pauseAsync();
                                }
                            }
                            break;
                        case RESUME_ALL:
                            for (int i = 0; i < Audio.this.soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = Audio.this.soundsToUpdate.get(i);
                                if (sound.isPaused()) {
                                    sound.playAsync();
                                }
                            }
                            break;
                        case DISPOSE_CALLBACK:
                            this.sound.readyToDispose();
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
