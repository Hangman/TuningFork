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

import org.lwjgl.openal.SOFTReopenDevice;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.ContextAttributes;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A simple rerouter that connects to the default device when the connection to the current device is lost.
 *
 * @author Matthias
 */
public class KeepAliveDeviceRerouter implements AudioDeviceRerouter {
    private long              device;
    private ContextAttributes attributes;
    private boolean           setup   = false;
    private boolean           started = false;


    @Override
    public void setup(long device, String desiredDeviceSpecifier, ContextAttributes attributes) {
        this.device = device;
        this.attributes = attributes;
        this.setup = true;
    }


    @Override
    public void updateDesiredDevice(String desiredDeviceSpecifier) {
        // this rerouter doesn't care about desires
    }


    @Override
    public void updateContextAttributes(ContextAttributes attributes) {
        this.attributes = attributes;
    }


    @Override
    public void start() {
        this.started = true;
    }


    @Override
    public void onDisconnect() {
        if (!this.setup || !this.started) {
            return;
        }

        if (!SOFTReopenDevice.alcReopenDeviceSOFT(this.device, (String) null, this.attributes.getBuffer())) {
            final TuningForkLogger logger = Audio.get().getLogger();
            if (logger != null) {
                logger.error(this.getClass(), "Failed to reopen audio device");
            }
        }
    }


    @Override
    public void dispose() {
        this.started = false;
    }

}
