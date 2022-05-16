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

package de.pottgames.tuningfork.capture;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;

import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.logger.ErrorLogger;
import de.pottgames.tuningfork.logger.MockLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * This class helps to record audio from input devices like microphones.
 *
 * @author Matthias
 *
 */
public class CaptureDevice implements Disposable {
    private final TuningForkLogger logger;
    private final ErrorLogger      errorLogger;
    private final long             alDeviceHandle;
    private final String           alDeviceName;
    private final PcmFormat        format;
    private final int              frequency;
    private final int              bufferSize;


    private CaptureDevice(long handle, PcmFormat format, int frequency, int bufferSize, TuningForkLogger logger) {
        if (logger == null) {
            this.logger = new MockLogger();
        } else {
            this.logger = logger;
        }
        this.errorLogger = new ErrorLogger(this.getClass(), logger);
        this.alDeviceHandle = handle;
        this.format = format;
        this.frequency = frequency;
        this.bufferSize = bufferSize;
        this.alDeviceName = ALC10.alcGetString(handle, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
    }


    /**
     * Starts the capture. If there were previously recorded samples, they will be overwritten by this method.
     */
    public void startCapture() {
        ALC11.alcCaptureStart(this.alDeviceHandle);
        if (!this.errorLogger.checkLogAlcError(this.alDeviceHandle, "failed to start capturing")) {
            this.logger.trace(this.getClass(), "capturing started");
        }
    }


    /**
     * Returns the number of captured samples.
     *
     * @return number of samples
     */
    public int capturedSamples() {
        return ALC10.alcGetInteger(this.alDeviceHandle, ALC11.ALC_CAPTURE_SAMPLES);
    }


    /**
     * Retrieves pcm data from the input device and saves it to a ByteBuffer. Use this for 8-Bit data only unless you know what you're doing.
     *
     * @param buffer
     * @param samples number of samples to fetch
     */
    public void fetch8BitSamples(ByteBuffer buffer, int samples) {
        ALC11.alcCaptureSamples(this.alDeviceHandle, buffer, samples);
    }


    /**
     * Retrieves pcm data from the input device and saves it to a short array. Use this for 16-Bit data only unless you know what you're doing.
     *
     * @param buffer
     * @param samples number of samples to fetch
     */
    public void fetch16BitSamples(short[] buffer, int samples) {
        ALC11.alcCaptureSamples(this.alDeviceHandle, buffer, samples);
    }


    /**
     * Retrieves pcm data from the input device and saves it to a ShortBuffer. Use this for 16-Bit data only unless you know what you're doing.
     *
     * @param buffer
     * @param samples number of samples to fetch
     */
    public void fetch16BitSamples(ShortBuffer buffer, int samples) {
        ALC11.alcCaptureSamples(this.alDeviceHandle, buffer, samples);
    }


    /**
     * Stops the capture.
     */
    public void stopCapture() {
        ALC11.alcCaptureStop(this.alDeviceHandle);
        if (!this.errorLogger.checkLogAlcError(this.alDeviceHandle, "failed to stop capturing")) {
            this.logger.trace(this.getClass(), "capturing stopped");
        }
    }


    /**
     * Returns the device name.
     *
     * @return the device name
     */
    public String getDeviceName() {
        return this.alDeviceName;
    }


    /**
     * Returns the pcm format of the device.
     *
     * @return the format
     */
    public PcmFormat getPcmFormat() {
        return this.format;
    }


    /**
     * Returns the frequency of the device.
     *
     * @return the frequency
     */
    public int getFrequency() {
        return this.frequency;
    }


    /**
     * Returns the size of the internal buffer of the device.
     *
     * @return the buffer size
     */
    public int getBufferSize() {
        return this.bufferSize;
    }


    @Override
    public void dispose() {
        if (!ALC11.alcCaptureCloseDevice(this.alDeviceHandle)) {
            this.logger.error(this.getClass(), "Failed to dispose the CaptureDevice");
        }
    }


    /**
     * Returns a list of available input devices.
     *
     * @return list of devices
     */
    public static List<String> availableDevices() {
        return ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
    }


    /**
     * Returns the name of the default input device.
     *
     * @return name of the default input device
     */
    public static String getDefaultDeviceName() {
        return ALC10.alcGetString(0L, ALC11.ALC_CAPTURE_DEFAULT_DEVICE_SPECIFIER);
    }


    /**
     * Opens the default input device with default settings.
     *
     * @return the device
     */
    public static CaptureDevice open() {
        return CaptureDevice.open(new CaptureConfig());
    }


    /**
     * Opens an input device with the given {@link CaptureConfig}.
     *
     * @param config
     *
     * @return the device
     */
    public static CaptureDevice open(CaptureConfig config) {
        CaptureDevice captureDevice = null;

        final long deviceHandle = ALC11.alcCaptureOpenDevice(config.getDeviceSpecifier(), config.getFrequency(), config.getPcmFormat().getAlId(),
                config.getBufferSize());
        if (deviceHandle != 0L) {
            captureDevice = new CaptureDevice(deviceHandle, config.getPcmFormat(), config.getFrequency(), config.getBufferSize(), config.getLogger());
        }

        return captureDevice;
    }

}
