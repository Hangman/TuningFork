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

import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.openal.EXTDisconnect;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.openal.EnumerateAllExt;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.openal.SOFTOutputLimiter;
import org.lwjgl.openal.SOFTReopenDevice;
import org.lwjgl.openal.SOFTSourceResampler;
import org.lwjgl.openal.SOFTXHoldOnDisconnect;
import org.lwjgl.system.MemoryUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectMap;

import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;
import de.pottgames.tuningfork.router.AudioDeviceRerouter;

/**
 * A class that gives access to some audio hardware device specific settings. Allows to query and change hardware specific settings.
 *
 * @author Matthias
 *
 */
public class AudioDevice {
    private final long                            deviceHandle;
    private final long                            context;
    private final TuningForkLogger                logger;
    private final ErrorLogger                     errorLogger;
    private boolean                               hrtfEnabled           = false;
    private final AudioDeviceConfig               config;
    private final int[]                           tempSingleIntResult   = new int[1];
    private final ObjectMap<ALExtension, Boolean> extensionAvailableMap = new ObjectMap<>();
    private final Array<String>                   resamplers            = new Array<>();
    private final int                             effectSlots;
    private AudioDeviceRerouter                   deviceRerouter;


    /**
     * Returns a list of identifiers of available sound devices. You can use an identifier in {@link AudioDeviceConfig#deviceSpecifier} to request a specific
     * sound device for audio playback.
     *
     * @return the list
     */
    public static List<String> availableDevices() {
        return ALUtil.getStringList(MemoryUtil.NULL, EnumerateAllExt.ALC_ALL_DEVICES_SPECIFIER);
    }


    protected AudioDevice(AudioDeviceConfig config, TuningForkLogger logger) throws OpenDeviceException, UnsupportedAudioDeviceException {
        this.config = config;
        this.logger = logger;
        this.errorLogger = new ErrorLogger(this.getClass(), logger);

        if (config == null) {
            throw new TuningForkRuntimeException("AudioDeviceConfig is null");
        }

        // CHECK IF THE SPECIFIED DEVICE IS AVAILABLE
        if (config.deviceSpecifier != null) {
            final List<String> availableDevices = AudioDevice.availableDevices();
            if (!availableDevices.contains(config.deviceSpecifier)) {
                throw new UnsupportedAudioDeviceException("Unable to open unsupported audio device: " + config.deviceSpecifier);
            }
        }
        String deviceName = config.deviceSpecifier;
        if (config.deviceSpecifier == null) {
            deviceName = "default";
        }

        // OPEN THE SOUND DEVICE
        this.deviceHandle = ALC10.alcOpenDevice(config.deviceSpecifier);
        if (this.deviceHandle == 0L) {
            throw new OpenDeviceException("Failed to open the " + deviceName + " OpenAL device.");
        }

        // SET CONTEXT ATTRIBUTES
        final IntBuffer contextAttributes = BufferUtils.newIntBuffer(10);
        contextAttributes.put(EXTEfx.ALC_MAX_AUXILIARY_SENDS);
        contextAttributes.put(config.effectSlots);
        contextAttributes.put(SOFTOutputLimiter.ALC_OUTPUT_LIMITER_SOFT);
        contextAttributes.put(config.enableOutputLimiter ? ALC10.ALC_TRUE : ALC10.ALC_FALSE);
        contextAttributes.put(0);
        contextAttributes.flip();

        // CREATE A CONTEXT AND SET IT ACTIVE
        final ALCCapabilities deviceCapabilities = ALC.createCapabilities(this.deviceHandle);
        this.context = ALC10.alcCreateContext(this.deviceHandle, contextAttributes);
        if (this.context == 0L) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        ALC10.alcMakeContextCurrent(this.context);
        AL.createCapabilities(deviceCapabilities);

        // CHECK OPENAL API SUPPORT
        this.checkAL10Support(logger, deviceName, deviceCapabilities);
        this.checkAL11Support(logger, deviceName, deviceCapabilities);

        // CHECK IF EXTENSIONS ARE PRESENT
        this.checkAvailableExtensions();
        this.checkRequiredExtension(ALExtension.ALC_EXT_EFX);
        this.checkRequiredExtension(ALExtension.AL_SOFT_DIRECT_CHANNELS);
        this.checkRequiredExtension(ALExtension.AL_EXT_MCFORMATS);
        this.checkRequiredExtension(ALExtension.AL_SOFTX_HOLD_ON_DISCONNECT);
        this.checkRequiredExtension(ALExtension.ALC_SOFT_REOPEN_DEVICE);
        this.checkRequiredExtension(ALExtension.ALC_ENUMERATE_ALL_EXT);

        // CONFIGURE CONTEXT
        AL10.alDisable(SOFTXHoldOnDisconnect.AL_STOP_SOURCES_ON_DISCONNECT_SOFT);

        // LOG OUTPUT LIMITER STATE
        if (config.enableOutputLimiter) {
            final int[] outputLimiterEnabled = new int[1];
            outputLimiterEnabled[0] = ALC10.ALC_FALSE;
            if (this.isExtensionAvailable(ALExtension.ALC_SOFT_OUTPUT_LIMITER)) {
                ALC10.alcGetIntegerv(this.deviceHandle, SOFTOutputLimiter.ALC_OUTPUT_LIMITER_SOFT, outputLimiterEnabled);
            }
            logger.debug(this.getClass(), "Output limiter: " + (outputLimiterEnabled[0] == ALC10.ALC_TRUE ? "enabled" : "disabled"));
        }

        // CHECK AND LOG HRTF SETTINGS
        if (this.isExtensionAvailable(ALExtension.ALC_SOFT_HRTF)) {
            final int hrtfSoftStatus = ALC10.alcGetInteger(this.deviceHandle, SOFTHRTF.ALC_HRTF_STATUS_SOFT);
            switch (hrtfSoftStatus) {
                case SOFTHRTF.ALC_HRTF_DISABLED_SOFT:
                    this.hrtfEnabled = false;
                    logger.debug(this.getClass(), "HRTF status is: ALC_HRTF_DISABLED_SOFT");
                    break;
                case SOFTHRTF.ALC_HRTF_ENABLED_SOFT:
                    this.hrtfEnabled = true;
                    logger.debug(this.getClass(), "HRTF status is: ALC_HRTF_ENABLED_SOFT");
                    break;
                case SOFTHRTF.ALC_HRTF_DENIED_SOFT:
                    this.hrtfEnabled = false;
                    logger.debug(this.getClass(), "HRTF status is: ALC_HRTF_DENIED_SOFT");
                    break;
                case SOFTHRTF.ALC_HRTF_REQUIRED_SOFT:
                    this.hrtfEnabled = true;
                    logger.debug(this.getClass(), "HRTF status is: ALC_HRTF_REQUIRED_SOFT");
                    break;
                case SOFTHRTF.ALC_HRTF_HEADPHONES_DETECTED_SOFT:
                    this.hrtfEnabled = true;
                    logger.debug(this.getClass(), "HRTF status is: ALC_HRTF_HEADPHONES_DETECTED_SOFT");
                    break;
                case SOFTHRTF.ALC_HRTF_UNSUPPORTED_FORMAT_SOFT:
                    this.hrtfEnabled = false;
                    logger.debug(this.getClass(), "HRTF status is: ALC_HRTF_UNSUPPORTED_FORMAT_SOFT");
                    break;
                default:
                    this.hrtfEnabled = false;
                    logger.debug(this.getClass(), "HRTF status is unknown: " + hrtfSoftStatus + " - TuningFork will report it as disabled.");
                    break;
            }
        }

        // CHECK AVAILABLE AUXILIARY SENDS
        final IntBuffer auxSends = BufferUtils.newIntBuffer(1);
        ALC10.alcGetIntegerv(this.deviceHandle, EXTEfx.ALC_MAX_AUXILIARY_SENDS, auxSends);
        this.effectSlots = auxSends.get(0);
        logger.debug(this.getClass(), "Available auxiliary sends: " + this.effectSlots);
        if (this.effectSlots != config.effectSlots) {
            logger.error(this.getClass(), "The audio device rejected the requested number of effect slots (" + config.effectSlots + ").");
        }

        // FIND RESAMPLERS
        this.getAvailableResamplers();

        // DEVICE CONNECTION REROUTER
        this.deviceRerouter = config.rerouter;
        if (this.deviceRerouter != null) {
            this.deviceRerouter.setup(this.deviceHandle, config.deviceSpecifier);
            this.deviceRerouter.start();
        }

        // LOG ERRORS
        this.errorLogger.checkLogAlcError(this.deviceHandle, "There was at least one ALC error upon audio device initialization");
        this.errorLogger.checkLogError("There was at least one AL error upon audio device initialization");
    }


    private void checkAL11Support(TuningForkLogger logger, String deviceName, final ALCCapabilities deviceCapabilities) throws OpenDeviceException {
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
    }


    private void checkAL10Support(TuningForkLogger logger, String deviceName, final ALCCapabilities deviceCapabilities) throws OpenDeviceException {
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
    }


    private void checkRequiredExtension(ALExtension extension) throws OpenDeviceException {
        final Boolean checkResult = this.extensionAvailableMap.get(extension);
        if (checkResult == null || !checkResult) {
            try {
                this.dispose(false);
            } catch (final Exception e) {
                this.logger.error(this.getClass(),
                        "The device was opened successfully, but didn't support " + extension.getAlSpecifier() + ". The attempt to close the device failed.");
            }
            throw new OpenDeviceException("The audio device doesn't support " + extension.getAlSpecifier() + " which is a requirement of TuningFork.");
        }

        this.logger.trace(this.getClass(), "Extension available: " + extension.getAlSpecifier());
    }


    private void checkAvailableExtensions() {
        for (final ALExtension extension : ALExtension.values()) {
            if (extension.isAlc()) {
                this.extensionAvailableMap.put(extension, ALC10.alcIsExtensionPresent(this.deviceHandle, extension.getAlSpecifier()));
            } else {
                this.extensionAvailableMap.put(extension, AL10.alIsExtensionPresent(extension.getAlSpecifier()));
            }
        }
    }


    protected boolean isExtensionAvailable(ALExtension extension) {
        final Boolean result = this.extensionAvailableMap.get(extension);
        if (result == null) {
            return false;
        }
        return result;
    }


    /**
     * Returns whether the audio output device is still connected. While most people are using either PCI audio cards or a chip welded to their motherboard,
     * there are many devices that are more dynamic in nature, such as USB and Firewire based-units. Such units may lose external power or may have their cables
     * unplugged at runtime.<br>
     * <br>
     * <b>Note:</b> Not all operating systems and/or drivers will report a disconnected device.
     *
     * @return true if the device is connected, false otherwise
     */
    public boolean isConnected() {
        if (this.isExtensionAvailable(ALExtension.ALC_EXT_DISCONNECT)) {
            ALC10.alcGetIntegerv(this.deviceHandle, EXTDisconnect.ALC_CONNECTED, this.tempSingleIntResult);
            return this.tempSingleIntResult[0] == ALC10.ALC_TRUE;
        }

        // If the extension is not available, always report true
        return true;
    }


    /**
     * Switches to another audio device.
     *
     * @param deviceSpecifier must be one of the devices returned by {@link AudioDevice#availableDevices()} or null to switch to the default device.
     *
     * @return true if successful
     */
    public boolean switchToDevice(String deviceSpecifier) {
        final boolean success = SOFTReopenDevice.alcReopenDeviceSOFT(this.deviceHandle, deviceSpecifier, (IntBuffer) null);
        if (success && this.deviceRerouter != null) {
            this.deviceRerouter.setNewDesiredDevice(deviceSpecifier);
        }
        return success;
    }


    /**
     * Returns true if HRTF is supported by this device. This does not necessarily mean that a hrtf profile is available, call {@link #getAvailableHrtfs()} to
     * query for profiles.
     *
     * @return true if supported
     */
    public boolean isHrtfSupported() {
        return this.isExtensionAvailable(ALExtension.ALC_SOFT_HRTF);
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
            final int num_hrtf = ALC10.alcGetInteger(this.deviceHandle, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
            for (int i = 0; i < num_hrtf; i++) {
                final String name = Objects.requireNonNull(SOFTHRTF.alcGetStringiSOFT(this.deviceHandle, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT, i));
                hrtfs.add(name);
            }
        }

        return hrtfs;
    }


    /**
     * Enables hrtf on this device.
     *
     * @param specifier the hrtf configuration specifier. Must be one of the strings included in the list from {@link #getAvailableHrtfs()}
     *
     * @return true on success, false on failure
     */
    public boolean enableHrtf(String specifier) {
        if (this.isHrtfSupported()) {
            final int num_hrtf = ALC10.alcGetInteger(this.deviceHandle, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);

            // FIND HRTF INDEX BY SPECIFIER
            int hrtfIndex = -1;
            for (int i = 0; i < num_hrtf; i++) {
                final String name = Objects.requireNonNull(SOFTHRTF.alcGetStringiSOFT(this.deviceHandle, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT, i));
                if (name.equals(specifier)) {
                    hrtfIndex = i;
                    break;
                }
            }

            if (hrtfIndex >= 0) {
                // SET NEW DEVICE ATTRIBUTES
                final IntBuffer contextAttributes = BufferUtils.newIntBuffer(10);
                contextAttributes.put(SOFTHRTF.ALC_HRTF_SOFT).put(ALC10.ALC_TRUE); // enable hrtf
                contextAttributes.put(SOFTHRTF.ALC_HRTF_ID_SOFT).put(hrtfIndex); // set hrtf configuration
                contextAttributes.put(EXTEfx.ALC_MAX_AUXILIARY_SENDS);
                contextAttributes.put(this.config.effectSlots);
                contextAttributes.put(SOFTOutputLimiter.ALC_OUTPUT_LIMITER_SOFT);
                contextAttributes.put(this.config.enableOutputLimiter ? ALC10.ALC_TRUE : ALC10.ALC_FALSE);
                contextAttributes.put(0);
                contextAttributes.flip();

                // RESET DEVICE
                if (!SOFTHRTF.alcResetDeviceSOFT(this.deviceHandle, contextAttributes)) {
                    this.logger.error(this.getClass(),
                            "Failed to reset device: " + ALC10.alcGetString(this.deviceHandle, ALC10.alcGetError(this.deviceHandle)));
                    this.hrtfEnabled = false;
                    return false;
                }

                // CHECK CURRENT HRTF STATE
                final int hrtfState = ALC10.alcGetInteger(this.deviceHandle, SOFTHRTF.ALC_HRTF_SOFT);
                if (hrtfState != 0) {
                    final String name = ALC10.alcGetString(this.deviceHandle, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT);
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
     * Disables hrtf on this device.
     */
    public void disableHrtf() {
        if (this.isHrtfSupported()) {
            // SET NEW DEVICE ATTRIBUTES
            final IntBuffer attr = BufferUtils.newIntBuffer(10);
            attr.put(SOFTHRTF.ALC_HRTF_SOFT).put(ALC10.ALC_FALSE); // disable hrtf
            attr.put(0);
            attr.flip();

            // RESET DEVICE
            if (SOFTHRTF.alcResetDeviceSOFT(this.deviceHandle, attr)) {
                this.hrtfEnabled = false;
                this.logger.info(this.getClass(), "HRTF disabled.");
            } else {
                this.logger.error(this.getClass(), "Failed to reset device: " + ALC10.alcGetString(this.deviceHandle, ALC10.alcGetError(this.deviceHandle)));
            }
        } else {
            this.logger.warn(this.getClass(), "HRTF is not supported by this device and was therefore never enabled.");
        }
    }


    /**
     * Returns a list of available resamplers for this device. The list is ordered by performance impact. That is, indices closer to 0 are of lower impact, and
     * the higher index values have higher impact.<br>
     * Mostly, a higher performance impact also means a better result in terms of quality, though this isn't true in all cases.<br>
     * <br>
     * If you need more information on what resampler is best for you, here's a video recommendation:
     * <a href="https://www.youtube.com/watch?v=62U6UnaUGDE">https://www.youtube.com/watch?v=62U6UnaUGDE</a>
     *
     * @return list of available resamplers
     */
    public Array<String> getAvailableResamplers() {
        this.resamplers.clear();

        final int numberOfResamplers = AL10.alGetInteger(SOFTSourceResampler.AL_NUM_RESAMPLERS_SOFT);
        for (int index = 0; index < numberOfResamplers; index++) {
            final String resamplerName = SOFTSourceResampler.alGetStringiSOFT(SOFTSourceResampler.AL_RESAMPLER_NAME_SOFT, index);
            this.resamplers.add(resamplerName);
        }

        return new Array<>(this.resamplers);
    }


    /**
     * Returns the number of available effect slots. If you want to change the number, see {@link AudioDeviceConfig#effectSlots} in the AudioDeviceConfig.
     *
     * @return the number of effect slots available for each source
     */
    public int getNumberOfEffectSlots() {
        return this.effectSlots;
    }


    /**
     * Returns the index of the first occurrence of the value in the array, or -1 if no such value exists.
     *
     * @param name
     *
     * @return the index of the first occurrence of the value in the array or -1 if no such value exists
     */
    protected int getResamplerIndexByName(String name) {
        return this.resamplers.indexOf(name, false);
    }


    String getResamplerNameByIndex(int index) {
        return SOFTSourceResampler.alGetStringiSOFT(SOFTSourceResampler.AL_RESAMPLER_NAME_SOFT, index);
    }


    protected int getDefaultResamplerIndex() {
        return AL10.alGetInteger(SOFTSourceResampler.AL_DEFAULT_RESAMPLER_SOFT);
    }


    /**
     * Returns the name of the default resampler currently in use.
     *
     * @return name of the default resampler
     */
    public String getDefaultResampler() {
        return this.getResamplerNameByIndex(this.getDefaultResamplerIndex());
    }


    protected void dispose(boolean log) {
        if (this.deviceRerouter != null) {
            this.deviceRerouter.dispose();
        }
        if (this.context != 0L) {
            ALC10.alcDestroyContext(this.context);
        }
        if (this.deviceHandle != 0L) {
            if (!ALC10.alcCloseDevice(this.deviceHandle) && log) {
                this.logger.error(this.getClass(), "The audio device did not close properly.");
            }
        }
    }

}
