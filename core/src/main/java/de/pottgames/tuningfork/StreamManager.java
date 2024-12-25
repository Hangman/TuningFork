package de.pottgames.tuningfork;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.AudioConfig.Spatialization;
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
            idleTasks.add(new AsyncTask());
        }

        // CREATE THE TASK SERVICE
        taskService = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName("TuningFork-Task-Thread");
            thread.setDaemon(true);
            return thread;
        });

        // adding the last task by executing it for warm up
        taskService.execute(new AsyncTask());

        // START UPDATE THREAD
        updateThread = new Thread(() -> {
            while (running) {
                updateAsync();
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    // ignore
                }
            }
        });
        updateThread.setName("TuningFork-Update-Thread");
        updateThread.setDaemon(true);
        updateThread.start();
    }


    protected void updateAsync() {
        synchronized (lock) {
            for (int i = 0; i < soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = soundsToUpdate.get(i);
                this.postTask(sound, TaskAction.UPDATE);
            }
        }
    }


    protected void setDefaultResampler(int resamplerIndex) {
        synchronized (lock) {
            for (int i = 0; i < soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = soundsToUpdate.get(i);
                sound.setResamplerByIndex(resamplerIndex);
            }
        }
    }


    protected void setDefaultVirtualization(Virtualization virtualization) {
        synchronized (lock) {
            for (int i = 0; i < soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = soundsToUpdate.get(i);
                sound.setVirtualization(virtualization);
            }
        }
    }


    protected void setDefaultSpatialization(Spatialization spatialization) {
        synchronized (lock) {
            for (int i = 0; i < soundsToUpdate.size; i++) {
                final StreamedSoundSource sound = soundsToUpdate.get(i);
                sound.setSpatialization(spatialization);
            }
        }
    }


    protected void registerSource(StreamedSoundSource source) {
        synchronized (lock) {
            soundsToUpdate.add(source);
        }
    }


    protected void removeSource(StreamedSoundSource sound) {
        synchronized (lock) {
            soundsToUpdate.removeValue(sound, true);
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
        idleTasks.offer(task);
    }


    protected void postTask(StreamedSoundSource sound, TaskAction action) {
        this.postTask(sound, action, 0f);
    }


    protected void postTask(StreamedSoundSource sound, TaskAction action, float floatParam) {
        AsyncTask task = idleTasks.poll();
        if (task == null) {
            task = new AsyncTask();
        }
        task.sound = sound;
        task.taskAction = action;
        task.floatParam = floatParam;
        taskService.execute(task);
    }


    protected void postTask(TaskAction action) {
        AsyncTask task = idleTasks.poll();
        if (task == null) {
            task = new AsyncTask();
        }
        task.taskAction = action;
        taskService.execute(task);
    }


    protected void dispose() {
        // TERMINATE UPDATE THREAD
        running = false;
        try {
            updateThread.join(2000);
        } catch (final InterruptedException e1) {
            // ignore
        }

        // SHUTDOWN TASK SERVICE
        taskService.shutdown();
        try {
            if (!taskService.awaitTermination(500L, TimeUnit.MILLISECONDS)) {
                logger.debug(this.getClass(), "The task service timed out on shutdown.");
            }
        } catch (final InterruptedException e) {
            taskService.shutdownNow();
        }
    }


    public enum TaskAction {
        PLAY, STOP, PAUSE, UPDATE, SET_PLAYBACK_POSITION, STOP_ALL, PAUSE_ALL, RESUME_ALL, DISPOSE_CALLBACK;
    }


    public class AsyncTask implements Runnable {
        protected volatile StreamedSoundSource sound;
        protected volatile TaskAction          taskAction;
        protected volatile float               floatParam;


        @Override
        public void run() {
            if (sound != null) {
                synchronized (lock) {
                    switch (taskAction) {
                        case PAUSE:
                            sound.pauseAsync();
                            break;
                        case PLAY:
                            sound.playAsync();
                            break;
                        case STOP:
                            sound.stopAsync();
                            break;
                        case UPDATE:
                            sound.updateAsync();
                            break;
                        case SET_PLAYBACK_POSITION:
                            sound.setPlaybackPositionAsync(floatParam);
                            break;
                        case STOP_ALL:
                            for (int i = 0; i < soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = soundsToUpdate.get(i);
                                sound.stopAsync();
                            }
                            break;
                        case PAUSE_ALL:
                            for (int i = 0; i < soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = soundsToUpdate.get(i);
                                if (sound.isPlaying()) {
                                    sound.pauseAsync();
                                }
                            }
                            break;
                        case RESUME_ALL:
                            for (int i = 0; i < soundsToUpdate.size; i++) {
                                final StreamedSoundSource sound = soundsToUpdate.get(i);
                                if (sound.isPaused()) {
                                    sound.playAsync();
                                }
                            }
                            break;
                        case DISPOSE_CALLBACK:
                            sound.readyToDispose();
                            break;
                    }
                }

                // CLEAN UP
                reset();
                addIdleTask(this);
            }
        }


        protected void reset() {
            sound = null;
            taskAction = null;
            floatParam = 0f;
        }

    }

}
