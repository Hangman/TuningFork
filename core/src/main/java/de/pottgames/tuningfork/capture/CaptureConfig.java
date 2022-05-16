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

import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.logger.GdxLogger;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A class that helps configuring a {@link CaptureDevice}.
 *
 * @author Matthias
 *
 */
public class CaptureConfig {
    private PcmFormat        pcmFormat;
    private int              frequency;
    private int              bufferSize;
    private String           deviceSpecifier;
    private TuningForkLogger logger;


    /**
     * Creates a new {@link CaptureConfig} with default settings.
     */
    public CaptureConfig() {
        this.pcmFormat = PcmFormat.MONO_16_BIT;
        this.frequency = 44100;
        this.bufferSize = 4096 * 10;
        this.deviceSpecifier = null;
        this.logger = new GdxLogger();
    }


    /**
     * Creates a new {@link CaptureConfig} with all values specified.
     *
     * @param deviceSpecifier
     * @param pcmFormat
     * @param frequency
     * @param bufferSize
     * @param logger
     */
    public CaptureConfig(String deviceSpecifier, PcmFormat pcmFormat, int frequency, int bufferSize, TuningForkLogger logger) {
        this.deviceSpecifier = deviceSpecifier;
        this.pcmFormat = pcmFormat;
        this.frequency = frequency;
        this.bufferSize = bufferSize;
        this.logger = logger;
    }


    /**
     * Returns the configs value for the pcm format.
     *
     * @return the pcm format
     */
    public PcmFormat getPcmFormat() {
        return this.pcmFormat;
    }


    /**
     * Returns the configs value for the frequency.
     *
     * @return the frequency
     */
    public int getFrequency() {
        return this.frequency;
    }


    /**
     * Returns the configs value for the buffer size.
     *
     * @return the buffer size
     */
    public int getBufferSize() {
        return this.bufferSize;
    }


    /**
     * Returns the configs value for the device specifier.
     *
     * @return the device specifier
     */
    public String getDeviceSpecifier() {
        return this.deviceSpecifier;
    }


    /**
     * Returns the configs value for the logger.
     *
     * @return the logger
     */
    public TuningForkLogger getLogger() {
        return this.logger;
    }


    /**
     * Sets the requested pcm format for the {@link CaptureDevice}.
     *
     * @param pcmFormat
     *
     * @return this
     */
    public CaptureConfig setPcmFormat(PcmFormat pcmFormat) {
        this.pcmFormat = pcmFormat;
        return this;
    }


    /**
     * Sets the requested frequency for the {@link CaptureDevice}. Use common values like 44100, 48000, etc. - other values might not be supported by the
     * driver.
     *
     * @param frequency
     *
     * @return this
     */
    public CaptureConfig setFrequency(int frequency) {
        this.frequency = frequency;
        return this;
    }


    /**
     * OpenAL uses a ring buffer internally to record audio data. If the buffer is full, it starts to overwrite the existing data. The buffer size defines how
     * much audio data can be recorded before it gets overwritten.<br>
     * <b>Note:</b> The implementation may use a larger buffer than requested if it needs to, but the implementation will set up a buffer of at least the
     * requested size.
     *
     * @param bufferSize in samples
     *
     * @return this
     */
    public CaptureConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }


    /**
     * Sets the requested device name specifier. May be null if you want to request the default input device.
     *
     * @param deviceSpecifier
     *
     * @return this
     */
    public CaptureConfig setDeviceSpecifier(String deviceSpecifier) {
        this.deviceSpecifier = deviceSpecifier;
        return this;
    }


    /**
     * Sets the logger to use for the {@link CaptureDevice}. May be null to disable logging.
     *
     * @param logger
     *
     * @return this
     */
    public CaptureConfig setLogger(TuningForkLogger logger) {
        this.logger = logger;
        return this;
    }

}
