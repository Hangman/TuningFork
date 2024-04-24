/**
 * Copyright 2022 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork;

import de.pottgames.tuningfork.router.AudioDeviceRerouter;
import de.pottgames.tuningfork.router.KeepAliveDeviceRerouter;
import de.pottgames.tuningfork.router.SmartDeviceRerouter;

public class AudioDeviceConfig {

    /**
     * Must be one of the device specifiers you can query with {@link AudioDevice#availableDevices()}. Leave it null to
     * use the default audio device.
     */
    protected String deviceSpecifier;

    /**
     * Whether to use the OpenAL output limiter that prevents clipping on the output.
     */
    protected boolean enableOutputLimiter = true;

    /**
     * Defines how many effects can be attached to sound sources. It's not guaranteed that the device will provide as
     * many slots as requested. Call {@link AudioDevice#getNumberOfEffectSlots()} to check how many effects are actually
     * available.<br> Default is 2, 16 is the maximum on my system for example (to give you an idea about reasonable
     * numbers).
     */
    protected int effectSlots = 2;

    /**
     * A device rerouter is responsible for routing the audio to another audio device when the connection to the current
     * device is lost. May also be used to keep track of the default audio device of the OS and switch to it when a new
     * default device is reported by the OS. Default: {@link SmartDeviceRerouter}
     */
    protected AudioDeviceRerouter rerouter = new SmartDeviceRerouter();

    /**
     * The desired output mode. It is just a hint for OpenAL and it might not give you the exact mode you wanted but its
     * closest relative. Set {@link OutputMode#ANY} to let the system find the most fitting mode for you.
     */
    protected OutputMode outputMode = OutputMode.ANY;


    public String getDeviceSpecifier() {
        return this.deviceSpecifier;
    }


    /**
     * Must be one of the device specifiers you can query with {@link AudioDevice#availableDevices()}. Leave it null to
     * use the default audio device.
     *
     * @param deviceSpecifier the device name
     * @return this
     */
    public AudioDeviceConfig setDeviceSpecifier(String deviceSpecifier) {
        this.deviceSpecifier = deviceSpecifier;
        return this;
    }


    public boolean isEnableOutputLimiter() {
        return this.enableOutputLimiter;
    }


    /**
     * Whether to use the OpenAL output limiter that prevents clipping on the output.
     *
     * @param enableOutputLimiter true to enable
     * @return this
     */
    public AudioDeviceConfig setEnableOutputLimiter(boolean enableOutputLimiter) {
        this.enableOutputLimiter = enableOutputLimiter;
        return this;
    }


    public int getEffectSlots() {
        return this.effectSlots;
    }


    /**
     * Defines how many effects can be attached to sound sources. It's not guaranteed that the device will provide as
     * many slots as requested. Call {@link AudioDevice#getNumberOfEffectSlots()} to check how many effects are actually
     * available.<br> Default is 2, 16 is the maximum on my system for example (to give you an idea about reasonable
     * numbers).
     *
     * @param effectSlots the number of effect slots
     * @return this
     */
    public AudioDeviceConfig setEffectSlots(int effectSlots) {
        this.effectSlots = effectSlots;
        return this;
    }


    public AudioDeviceRerouter getRerouter() {
        return this.rerouter;
    }


    /**
     * A device rerouter is responsible for routing the audio to another audio device when the connection to the current
     * device is lost. May also be used to keep track of the default audio device of the OS and switch to it when a new
     * default device is reported by the OS. Default: {@link KeepAliveDeviceRerouter}
     *
     * @param rerouter the audio device rerouter
     * @return this
     */
    public AudioDeviceConfig setRerouter(AudioDeviceRerouter rerouter) {
        this.rerouter = rerouter;
        return this;
    }


    public OutputMode getOutputMode() {
        return this.outputMode;
    }


    /**
     * Request an output mode of your choice. It is just a hint for OpenAL and it might not give you the exact mode you
     * wanted but its closest relative. Set {@link OutputMode#ANY} to let the system find the most fitting mode for you,
     * this is the default and recommended unless you have a specific reason to use another output mode.<br>
     * <br>
     * You can check the current output mode after initialization with {@link AudioDevice#getOutputMode()}.
     *
     * @param mode the desired output mode
     * @return this
     */
    public AudioDeviceConfig setOutputMode(OutputMode mode) {
        if (mode == null) {
            mode = OutputMode.ANY;
        }
        this.outputMode = mode;
        return this;
    }

}
