/**
 * Copyright 2023 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.router;

import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.AudioDevice;
import de.pottgames.tuningfork.AudioDeviceConfig;

/**
 * A device rerouter is responsible for routing the audio to another audio device when the connection to the current device is lost. May also be used to keep
 * track of the default audio device of the OS and switch to it when a new default device is reported by the OS.
 *
 * @author Matthias
 *
 */
public interface AudioDeviceRerouter extends Disposable {

    /**
     * {@link #setup(long, String) setup} is called before {@link #start() run}.
     *
     * @param device the OpenAL device handle
     * @param desiredDeviceSpecifier the device specifier that was specified in {@link AudioDeviceConfig#setDeviceSpecifier(String)}
     */
    void setup(long device, String desiredDeviceSpecifier);


    /**
     * This method gets called when the user changes the device at runtime via {@link AudioDevice#switchToDevice(String)}.
     *
     * @param desiredDeviceSpecifier
     */
    void setNewDesiredDevice(String desiredDeviceSpecifier);


    /**
     * Gets called right after {@link #setup(long, String) setup} and should start a background thread which is then responsible for rerouting.
     */
    void start();

}
