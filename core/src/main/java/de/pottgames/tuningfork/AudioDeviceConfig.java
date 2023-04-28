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

import de.pottgames.tuningfork.router.AudioDeviceRerouter;
import de.pottgames.tuningfork.router.KeepAliveDeviceRerouter;

public class AudioDeviceConfig {

    /**
     * Must be one of the device specifiers you can query with {@link AudioDevice#availableDevices()}. Leave it null to use the default audio device.
     */
    protected String deviceSpecifier;

    /**
     * Whether to use the OpenAL output limiter that prevents clipping on the output.
     */
    protected boolean enableOutputLimiter = true;

    /**
     * Defines how many effects can be attached to sound sources. It's not guaranteed that the device will provide as many slots as requested. Call
     * {@link AudioDevice#getNumberOfEffectSlots()} to check how many effects are actually available.<br>
     * Default is 2, 16 is the maximum on my system for example (to give you an idea about reasonable numbers).
     */
    protected int effectSlots = 2;

    /**
     * A device rerouter is responsible for routing the audio to another audio device when the connection to the current device is lost. May also be used to
     * keep track of the default audio device of the OS and switch to it when a new default device is reported by the OS. Default:
     * {@link KeepAliveDeviceRerouter}
     */
    protected AudioDeviceRerouter rerouter = new KeepAliveDeviceRerouter();


    public String getDeviceSpecifier() {
        return this.deviceSpecifier;
    }


    /**
     * Must be one of the device specifiers you can query with {@link AudioDevice#availableDevices()}. Leave it null to use the default audio device.
     *
     * @param deviceSpecifier
     */
    public void setDeviceSpecifier(String deviceSpecifier) {
        this.deviceSpecifier = deviceSpecifier;
    }


    public boolean isEnableOutputLimiter() {
        return this.enableOutputLimiter;
    }


    /**
     * Whether to use the OpenAL output limiter that prevents clipping on the output.
     *
     * @param enableOutputLimiter
     */
    public void setEnableOutputLimiter(boolean enableOutputLimiter) {
        this.enableOutputLimiter = enableOutputLimiter;
    }


    public int getEffectSlots() {
        return this.effectSlots;
    }


    /**
     * Defines how many effects can be attached to sound sources. It's not guaranteed that the device will provide as many slots as requested. Call
     * {@link AudioDevice#getNumberOfEffectSlots()} to check how many effects are actually available.<br>
     * Default is 2, 16 is the maximum on my system for example (to give you an idea about reasonable numbers).
     *
     * @param effectSlots
     */
    public void setEffectSlots(int effectSlots) {
        this.effectSlots = effectSlots;
    }


    public AudioDeviceRerouter getRerouter() {
        return this.rerouter;
    }


    /**
     * A device rerouter is responsible for routing the audio to another audio device when the connection to the current device is lost. May also be used to
     * keep track of the default audio device of the OS and switch to it when a new default device is reported by the OS. Default:
     * {@link KeepAliveDeviceRerouter}
     *
     * @param rerouter
     */
    public void setRerouter(AudioDeviceRerouter rerouter) {
        this.rerouter = rerouter;
    }

}
