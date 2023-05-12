package de.pottgames.tuningfork;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.AudioConfig.Virtualization;
import de.pottgames.tuningfork.logger.TuningForkLogger;

public class StreamManager {
    private final ConcurrentLinkedQueue<AsyncTask> idleTasks      = new ConcurrentLinkedQueue<>();
    private final ExecutorService                  taskService;
    private final Array<StreamedSoundSource>       soundsToUpdate = new Array<>();
    private final Object                           lock           = new Object();
    private final Thread                           updateThread;
    private volatile boolean                       running        = true;
    private final TuningForkLogger                 logger;


    protected StreamManager(AudioConfig config, TuningForkLogger logger) {
        this.logger = logger;

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

        // START UPDATE THREAD
        this.updateThread = new Thread(() -> {
            while (this.running) {
                this.updateAsync();
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    // ignore
                }
            }
        });
        this.updateThread.setName("TuningFork-Update-Thread");
        this.updateThread.setDaemon(true);
        this.updateThread.start();
    }


    protected void updateAsync() {
        synchronized (this.lock) {
            for (int i = 0; i < this.soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = this.soundsToUpdate.get(i);
                this.postTask(sound, TaskAction.UPDATE);
            }
        }
    }


    protected void setDefaultResampler(int resamplerIndex) {
        synchronized (this.lock) {
            for (int i = 0; i < this.soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = this.soundsToUpdate.get(i);
                sound.setResamplerByIndex(resamplerIndex);
            }
        }
    }


    protected void setDefaultVirtualization(Virtualization virtualization) {
        synchronized (this.lock) {
            for (int i = 0; i < this.soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = this.soundsToUpdate.get(i);
                sound.setVirtualization(virtualization);
            }
        }
    }


    protected void registerSource(StreamedSoundSource source) {
        synchronized (this.lock) {
            this.soundsToUpdate.add(source);
        }
    }


    protected void removeSource(StreamedSoundSource sound) {
        synchronized (this.lock) {
            this.soundsToUpdate.removeValue(sound, true);
        }
    }


    protected void pauseAll() {
        this.postTask(TaskAction.PAUSE_ALL);
    }


    protected void resumeAll() {
        this.postTask(TaskAction.RESUME_ALL);
    }


    protected void stopAll() {
        this.postTask(TaskAction.STOP_ALL);
    }


    protected void addIdleTask(AsyncTask task) {
        this.idleTasks.offer(task);
    }


    protected void postTask(StreamedSoundSource sound, TaskAction action) {
        this.postTask(sound, action, 0f);
    }


    protected void postTask(StreamedSoundSource sound, TaskAction action, float floatParam) {
        AsyncTask task = this.idleTasks.poll();
        if (task == null) {
            task = new AsyncTask();
        }
        task.sound = sound;
        task.taskAction = action;
        task.floatParam = floatParam;
        this.taskService.execute(task);
    }


    protected void postTask(TaskAction action) {
        AsyncTask task = this.idleTasks.poll();
        if (task == null) {
            task = new AsyncTask();
        }
        task.taskAction = action;
        this.taskService.execute(task);
    }


    protected void dispose() {
        // TERMINATE UPDATE THREAD
        this.running = false;
        try {
            this.updateThread.join(2000);
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
    }


    enum TaskAction {
        PLAY, STOP, PAUSE, UPDATE, SET_PLAYBACK_POSITION, STOP_ALL, PAUSE_ALL, RESUME_ALL, DISPOSE_CALLBACK;
    }


    public class AsyncTask implements Runnable {
        protected volatile StreamedSoundSource sound;
        protected volatile TaskAction          taskAction;
        protected volatile float               floatParam;


        @Override
        public void run() {
            if (this.sound != null) {
                synchronized (StreamManager.this.lock) {
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
                        case UPDATE:
                            this.sound.updateAsync();
                            break;
                        case SET_PLAYBACK_POSITION:
                            this.sound.setPlaybackPositionAsync(this.floatParam);
                            break;
                        case STOP_ALL:
                            for (int i = 0; i < StreamManager.this.soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = StreamManager.this.soundsToUpdate.get(i);
                                sound.stopAsync();
                            }
                            break;
                        case PAUSE_ALL:
                            for (int i = 0; i < StreamManager.this.soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = StreamManager.this.soundsToUpdate.get(i);
                                if (sound.isPlaying()) {
                                    sound.pauseAsync();
                                }
                            }
                            break;
                        case RESUME_ALL:
                            for (int i = 0; i < StreamManager.this.soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = StreamManager.this.soundsToUpdate.get(i);
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
                StreamManager.this.addIdleTask(this);
            }
        }


        protected void reset() {
            this.sound = null;
            this.taskAction = null;
            this.floatParam = 0f;
        }

    }

}
