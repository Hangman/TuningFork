package de.pottgames.tuningfork;

import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.openal.SOFTHRTF;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;

/**
 * A class that gives access to some audio hardware device specific settings. Allows to query and change hardware specific settings.
 *
 * @author Matthias
 *
 */
public class AudioDevice {
    private long                   device;
    private long                   context;
    private final TuningForkLogger logger;
    private final boolean          extensionHrtfSoftAvailable;
    private boolean                hrtfEnabled = false;


    AudioDevice(AudioDeviceConfig config, TuningForkLogger logger) throws OpenDeviceException, UnsupportedAudioDeviceException {
        this.logger = logger;

        if (config == null) {
            throw new TuningForkRuntimeException("AudioDeviceConfig is null");
        }

        // CHECK IF THE SPECIFIED DEVICE IS AVAILABLE
        if (config.deviceSpecifier != null) {
            final List<String> availableDevices = Audio.availableDevices();
            if (!availableDevices.contains(config.deviceSpecifier)) {
                throw new UnsupportedAudioDeviceException("Unable to open unsupported audio device: " + config.deviceSpecifier);
            }
        }
        String deviceName = config.deviceSpecifier;
        if (config.deviceSpecifier == null) {
            deviceName = "default";
        }

        // OPEN THE SOUND DEVICE
        this.device = ALC10.alcOpenDevice(config.deviceSpecifier);

        // CHECK IF DEVICE IS OPEN
        if (this.device == 0L) {
            throw new OpenDeviceException("Failed to open the " + deviceName + " OpenAL device.");
        }

        // CREATE A CONTEXT AND SET IT ACTIVE
        final ALCCapabilities deviceCapabilities = ALC.createCapabilities(this.device);
        this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);
        if (this.context == 0L) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        ALC10.alcMakeContextCurrent(this.context);
        AL.createCapabilities(deviceCapabilities);

        // CHECK IF EXTENSIONS ARE PRESENT
        if (!ALC10.alcIsExtensionPresent(this.device, "ALC_EXT_EFX")) {
            try {
                this.dispose(false);
            } catch (final Exception e) {
                logger.error(this.getClass(),
                        "The device was opened successfully, but didn't support a required feature. The attempt to close the device failed.");
            }
            throw new OpenDeviceException("The audio device " + deviceName + " doesn't support the EFX extension which is a requirement of TuningFork.");
        }
        logger.debug(this.getClass(), "ALC_EXT_EFX extension is present.");
        this.extensionHrtfSoftAvailable = ALC10.alcIsExtensionPresent(this.device, "ALC_SOFT_HRTF");
        logger.debug(this.getClass(), "ALC_SOFT_HRTF extension is present.");

        // CHECK OPENAL 1.0 API SUPPORT
        if (!deviceCapabilities.OpenALC10) {
            try {
                this.dispose(false);
            } catch (final Exception e) {
                logger.error(this.getClass(),
                        "The device was opened successfully, but didn't support a required feature. The attempt to close the device failed.");
            }
            throw new OpenDeviceException("The audio device " + deviceName + " doesn't support the OpenAL 1.0 API which is a requirement of TuningFork.");
        }
        logger.trace(this.getClass(), "OpenAL 1.0 supported.");

        // CHECK OPENAL 1.1 API SUPPORT
        if (!deviceCapabilities.OpenALC11) {
            try {
                this.dispose(false);
            } catch (final Exception e) {
                logger.error(this.getClass(),
                        "The device was opened successfully, but didn't support a required feature. The attempt to close the device failed.");
            }
            throw new OpenDeviceException("The audio device " + deviceName + " doesn't support the OpenAL 1.1 API which is a requirement of TuningFork.");
        }
        logger.trace(this.getClass(), "OpenAL 1.1 supported.");

        // CHECK AND LOG HRTF SETTINGS
        final int hrtfSoftStatus = ALC10.alcGetInteger(this.device, SOFTHRTF.ALC_HRTF_STATUS_SOFT);
        switch (hrtfSoftStatus) {
            case SOFTHRTF.ALC_HRTF_DISABLED_SOFT:
                this.hrtfEnabled = false;
                logger.trace(this.getClass(), "HRTF status is: ALC_HRTF_DISABLED_SOFT");
                break;
            case SOFTHRTF.ALC_HRTF_ENABLED_SOFT:
                this.hrtfEnabled = true;
                logger.trace(this.getClass(), "HRTF status is: ALC_HRTF_ENABLED_SOFT");
                break;
            case SOFTHRTF.ALC_HRTF_DENIED_SOFT:
                this.hrtfEnabled = false;
                logger.trace(this.getClass(), "HRTF status is: ALC_HRTF_DENIED_SOFT");
                break;
            case SOFTHRTF.ALC_HRTF_REQUIRED_SOFT:
                this.hrtfEnabled = true;
                logger.trace(this.getClass(), "HRTF status is: ALC_HRTF_REQUIRED_SOFT");
                break;
            case SOFTHRTF.ALC_HRTF_HEADPHONES_DETECTED_SOFT:
                this.hrtfEnabled = true;
                logger.trace(this.getClass(), "HRTF status is: ALC_HRTF_HEADPHONES_DETECTED_SOFT");
                break;
            case SOFTHRTF.ALC_HRTF_UNSUPPORTED_FORMAT_SOFT:
                this.hrtfEnabled = false;
                logger.trace(this.getClass(), "HRTF status is: ALC_HRTF_UNSUPPORTED_FORMAT_SOFT");
                break;
            default:
                this.hrtfEnabled = false;
                logger.trace(this.getClass(), "HRTF status is unknown: " + hrtfSoftStatus + " - TuningFork will report it as disabled.");
                break;
        }

        // CHECK AVAILABLE AUXILIARY SENDS
        final IntBuffer auxSends = BufferUtils.newIntBuffer(1);
        ALC10.alcGetIntegerv(this.device, EXTEfx.ALC_MAX_AUXILIARY_SENDS, auxSends);
        logger.debug(this.getClass(), "Available auxiliary sends: " + auxSends.get(0));
        if (auxSends.get(0) < 2) {
            try {
                this.dispose(false);
            } catch (final Exception e) {
                logger.debug(this.getClass(),
                        "The device was opened successfully, but didn't support a required feature. The attempt to close the device failed.");
            }
            throw new OpenDeviceException(
                    "The audio device " + deviceName + " doesn't support at least 2 auxiliary sends, which is a requirement of TuningFork.");
        }
    }


    /**
     * Returns true if HRTF is supported by this device.
     *
     * @return true if supported
     */
    public boolean isHrtfSupported() {
        return this.extensionHrtfSoftAvailable;
    }


    /**
     * Returns true if HRTF is enabled.
     *
     * @return true if enabled
     */
    public boolean isHrtfEnabled() {
        return this.hrtfEnabled;
    }


    /**
     * Returns a list of available hrtf configurations of this device.
     *
     * @return list of available hrtf configurations
     */
    public Array<String> getAvailableHrtfs() {
        final Array<String> hrtfs = new Array<>();

        if (this.isHrtfSupported()) {
            final int num_hrtf = ALC10.alcGetInteger(this.device, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
            for (int i = 0; i < num_hrtf; i++) {
                final String name = Objects.requireNonNull(SOFTHRTF.alcGetStringiSOFT(this.device, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT, i));
                hrtfs.add(name);
            }
        }

        return hrtfs;
    }


    /**
     * Enables hrtf on this device.
     *
     * @param specifier the hrtf configuration specifier. Must be one of the strings included in the list from {@link AudioDevice#getAvailableHrtfs()}
     *
     * @return true on success, false on failure
     */
    public boolean enableHrtf(String specifier) {
        if (this.isHrtfSupported()) {
            final int num_hrtf = ALC10.alcGetInteger(this.device, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);

            // FIND HRTF INDEX BY SPECIFIER
            int hrtfIndex = -1;
            for (int i = 0; i < num_hrtf; i++) {
                final String name = Objects.requireNonNull(SOFTHRTF.alcGetStringiSOFT(this.device, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT, i));
                if (specifier != null && name.equals(specifier)) {
                    hrtfIndex = i;
                    break;
                }
            }

            if (hrtfIndex >= 0) {
                // SET NEW DEVICE ATTRIBUTES
                final IntBuffer attr = BufferUtils.newIntBuffer(10);
                attr.put(SOFTHRTF.ALC_HRTF_SOFT).put(ALC10.ALC_TRUE); // enable hrtf
                attr.put(SOFTHRTF.ALC_HRTF_ID_SOFT).put(hrtfIndex); // set hrtf configuration
                attr.put(0);
                attr.flip();

                // RESET DEVICE
                if (!SOFTHRTF.alcResetDeviceSOFT(this.device, attr)) {
                    this.logger.error(this.getClass(), "Failed to reset device: " + ALC10.alcGetString(this.device, ALC10.alcGetError(this.device)));
                    this.hrtfEnabled = false;
                    return false;
                }

                // CHECK CURRENT HRTF STATE
                final int hrtfState = ALC10.alcGetInteger(this.device, SOFTHRTF.ALC_HRTF_SOFT);
                if (hrtfState != 0) {
                    final String name = ALC10.alcGetString(this.device, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT);
                    this.logger.info(this.getClass(), "Using HRTF configuration: " + name);
                    this.hrtfEnabled = true;
                    return true;
                }
            }
        }

        this.logger.error(this.getClass(), "HRTF is not enabled, reason: unknown");
        this.hrtfEnabled = false;
        return false;
    }


    /**
     * Disables the current
     */
    public void disableHrtf() {
        if (this.isHrtfSupported()) {
            // SET NEW DEVICE ATTRIBUTES
            final IntBuffer attr = BufferUtils.newIntBuffer(10);
            attr.put(SOFTHRTF.ALC_HRTF_SOFT).put(ALC10.ALC_FALSE); // disable hrtf
            attr.put(0);
            attr.flip();

            // RESET DEVICE
            if (SOFTHRTF.alcResetDeviceSOFT(this.device, attr)) {
                this.hrtfEnabled = false;
                this.logger.info(this.getClass(), "HRTF disabled.");
            } else {
                this.logger.error(this.getClass(), "Failed to reset device: " + ALC10.alcGetString(this.device, ALC10.alcGetError(this.device)));
            }
        } else {
            this.logger.warn(this.getClass(), "HRTF is not supported by this device and was therefore never enabled.");
        }
    }


    void dispose(boolean log) {
        if (this.context != 0L) {
            ALC10.alcDestroyContext(this.context);
        }
        if (this.device != 0L) {
            if (!ALC10.alcCloseDevice(this.device) && log) {
                this.logger.error(this.getClass(), "The audio device did not close properly.");
            }
        }
    }

}
