package de.pottgames.tuningfork;

import de.pottgames.tuningfork.logger.GdxLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

public class AudioConfig {
    private AudioDeviceConfig        deviceConfig;
    private DistanceAttenuationModel distanceAttenuationModel;
    private int                      simultaneousSources;
    private int                      idleTasks;
    private TuningForkLogger         logger;


    /**
     * Creates an AudioConfig with default settings.
     */
    public AudioConfig() {
        this.setDeviceConfig(new AudioDeviceConfig());
        this.setDistanceAttenuationModel(DistanceAttenuationModel.INVERSE_DISTANCE_CLAMPED);
        this.setLogger(new GdxLogger());
        this.setSimultaneousSources(20);
        this.setIdleTasks(10);
    }


    /**
     * Creates an AudioConfig with the given settings.
     *
     * @param deviceConfig
     * @param distanceAttenuationModel
     * @param simultaneousSources defines how many {@link BufferedSoundSource}s are allowed to play simultaneously
     * @param idleTasks the initial task pool capacity, 10 is the default, only go higher if you plan to make heavy use of {@link StreamedSound}s simultaneously
     * @param logger the logger to be used by TuningFork. You can implement the {@link TuningForkLogger} interface to write your own or choose one of the
     *            available logger implementations that are shipped with TuningFork.
     */
    public AudioConfig(AudioDeviceConfig deviceConfig, DistanceAttenuationModel distanceAttenuationModel, int simultaneousSources, int idleTasks,
            TuningForkLogger logger) {
        this.setDeviceConfig(deviceConfig);
        this.setDistanceAttenuationModel(distanceAttenuationModel);
        this.setSimultaneousSources(simultaneousSources);
        this.setIdleTasks(idleTasks);
        this.setLogger(logger);
    }


    public AudioDeviceConfig getDeviceConfig() {
        return this.deviceConfig;
    }


    /**
     * Sets the audio device config.
     *
     * @param deviceConfig
     */
    public void setDeviceConfig(AudioDeviceConfig deviceConfig) {
        this.deviceConfig = deviceConfig;
        if (deviceConfig == null) {
            this.deviceConfig = new AudioDeviceConfig();
        }
    }


    public DistanceAttenuationModel getDistanceAttenuationModel() {
        return this.distanceAttenuationModel;
    }


    /**
     * Sets the distance attenuation model.
     *
     * @param distanceAttenuationModel
     */
    public void setDistanceAttenuationModel(DistanceAttenuationModel distanceAttenuationModel) {
        this.distanceAttenuationModel = distanceAttenuationModel;
        if (distanceAttenuationModel == null) {
            this.distanceAttenuationModel = DistanceAttenuationModel.NONE;
        }
    }


    public int getSimultaneousSources() {
        return this.simultaneousSources;
    }


    /**
     * Defines how many {@link BufferedSoundSource}s are allowed to play simultaneously.
     *
     * @param simultaneousSources
     */
    public void setSimultaneousSources(int simultaneousSources) {
        this.simultaneousSources = simultaneousSources;
        if (simultaneousSources < 2) {
            this.simultaneousSources = 2;
        }
    }


    public int getIdleTasks() {
        return this.idleTasks;
    }


    /**
     * The initial task pool capacity, 10 is the default, only go higher if you plan to make heavy use of {@link StreamedSound}s simultaneously.
     *
     * @param idleTasks
     */
    public void setIdleTasks(int idleTasks) {
        this.idleTasks = idleTasks;
        if (idleTasks < 2) {
            this.idleTasks = 2;
        }
    }


    public TuningForkLogger getLogger() {
        return this.logger;
    }


    /**
     * Sets the logger to be used by TuningFork. You can implement the {@link TuningForkLogger} interface to write your own or choose one of the available
     * logger implementations that are shipped with TuningFork.
     *
     * @param logger
     */
    public void setLogger(TuningForkLogger logger) {
        this.logger = logger;
        if (logger == null) {
            this.logger = new MockLogger();
        }
    }

}
