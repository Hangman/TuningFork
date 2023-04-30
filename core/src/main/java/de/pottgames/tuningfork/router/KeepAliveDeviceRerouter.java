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

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTDisconnect;
import org.lwjgl.openal.SOFTReopenDevice;

import de.pottgames.tuningfork.ContextAttributes;
import de.pottgames.tuningfork.TuningForkRuntimeException;

/**
 * A simple rerouter that connects to the default device when the connection to the current device is lost.
 *
 * @author Matthias
 *
 */
public class KeepAliveDeviceRerouter implements AudioDeviceRerouter {
    private volatile boolean           active = false;
    private Thread                     thread;
    private long                       device;
    private volatile ContextAttributes attributes;
    private boolean                    setup  = false;


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
        if (!this.setup) {
            throw new TuningForkRuntimeException("KeepAliveDeviceRerouter wasn't set up properly");
        }

        this.active = true;
        this.thread = new Thread(() -> {
            KeepAliveDeviceRerouter.this.loop();
        });
        this.thread.setName("TuningFork-KeepAliveDeviceRerouter-Thread");
        this.thread.setDaemon(true);
        this.thread.start();
    }


    private void loop() {
        while (this.active) {
            final boolean isConnected = ALC10.alcGetInteger(this.device, EXTDisconnect.ALC_CONNECTED) == ALC10.ALC_TRUE;
            if (!isConnected) {
                if (!SOFTReopenDevice.alcReopenDeviceSOFT(this.device, (String) null, this.attributes.getBuffer())) {
                    System.err.println("Failed to reopen audio device");
                }
            }

            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                this.dispose();
            }
        }
    }


    @Override
    public void dispose() {
        this.active = false;
    }

}
