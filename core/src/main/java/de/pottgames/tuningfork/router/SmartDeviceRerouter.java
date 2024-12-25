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

import java.util.List;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EnumerateAllExt;
import org.lwjgl.openal.SOFTReopenDevice;
import org.lwjgl.system.MemoryUtil;

import de.pottgames.tuningfork.AudioDevice;
import de.pottgames.tuningfork.ContextAttributes;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.misc.Objects;

/**
 * The SmartDeviceRerouter checks every 1.5 seconds (configurable) whether the connection to the audio device still exists and whether it is the optimal
 * connection. If not, it tries to establish a connection with the following prioritization:<br>
 * <ul>
 * <li>desired device</li>
 * <li>current default device</li>
 * </ul>
 * The router is able to restore a previously lost connection to the desired device when it becomes available again. When not connected to the desired device,
 * the router will establish a connection to the default device and also keep track of it, so when the user selects a new default device in the OS, the router
 * will do the same accordingly.
 *
 * @author Matthias
 *
 */
public class SmartDeviceRerouter implements AudioDeviceRerouter {
    /**
     * Defines how often the background thread will check the connection and try to reconnect to audio devies.
     */
    private final long checkInterval;

    private volatile boolean           active                 = false;
    private long                       device;
    private volatile ContextAttributes attributes;
    private volatile String            desiredDeviceSpecifier = null;
    private volatile String            currentDeviceSpecifier = "none";
    private boolean                    setup                  = false;


    /**
     * Creates a new {@link SmartDeviceRerouter} with the default check interval.
     */
    public SmartDeviceRerouter() {
        this(1500L);
    }


    /**
     * Creates a new {@link SmartDeviceRerouter} with the given check interval for the background thread.
     *
     * @param checkInterval sleep time of the background thread in milliseconds
     */
    public SmartDeviceRerouter(long checkInterval) {
        this.checkInterval = checkInterval;
    }


    @Override
    public void setup(long device, String desiredDeviceSpecifier, ContextAttributes attributes) {
        this.device = device;
        this.attributes = attributes;
        currentDeviceSpecifier = fetchCurrentDeviceSpecifier();
        updateDesiredDevice(desiredDeviceSpecifier);
        setup = true;
    }


    @Override
    public void updateDesiredDevice(String desiredDeviceSpecifier) {
        this.desiredDeviceSpecifier = desiredDeviceSpecifier;
        currentDeviceSpecifier = fetchCurrentDeviceSpecifier();
    }


    @Override
    public void updateContextAttributes(ContextAttributes attributes) {
        this.attributes = attributes;
    }


    /**
     * Starts the thread.
     */
    @Override
    public void start() {
        if (!setup) {
            throw new TuningForkRuntimeException("SmartDeviceRerouter wasn't set up properly");
        }

        active = true;
        final Thread thread = new Thread(SmartDeviceRerouter.this::loop);
        thread.setName("TuningFork-SmartDeviceRerouter-Thread");
        thread.setDaemon(true);
        thread.start();
    }


    @Override
    public void onDisconnect() {
        if (!active) {
            return;
        }

        currentDeviceSpecifier = "none";
        tryReopen();
    }


    private void loop() {
        while (active) {
            tryReopen();
            try {
                Thread.sleep(checkInterval);
            } catch (final InterruptedException e) {
                dispose();
            }
        }
    }


    private synchronized void tryReopen() {
        if (desiredDeviceSpecifier == null) {
            tryReopenOnDefaultDevice();
        } else {
            tryReopenOnDesiredDevice();
        }
    }


    private void tryReopenOnDesiredDevice() {
        if (!currentDeviceSpecifier.equals(desiredDeviceSpecifier)) {
            final List<String> availableDevices = AudioDevice.availableDevices();
            if (availableDevices != null && availableDevices.contains(desiredDeviceSpecifier)) {
                reopen(desiredDeviceSpecifier);
            } else {
                tryReopenOnDefaultDevice();
            }
        }
    }


    private void tryReopenOnDefaultDevice() {
        final String defaultDeviceSpecifier = fetchDefaultDeviceSpecifier();
        if (!currentDeviceSpecifier.equals(defaultDeviceSpecifier)) {
            reopen(defaultDeviceSpecifier);
        }
    }


    private String fetchDefaultDeviceSpecifier() {
        return Objects.requireNonNullElse(ALC10.alcGetString(MemoryUtil.NULL, EnumerateAllExt.ALC_ALL_DEVICES_SPECIFIER), "none");
    }


    private String fetchCurrentDeviceSpecifier() {
        return ALC10.alcGetString(device, EnumerateAllExt.ALC_ALL_DEVICES_SPECIFIER);
    }


    private void reopen(String deviceSpecifier) {
        if (SOFTReopenDevice.alcReopenDeviceSOFT(device, deviceSpecifier, attributes.getBuffer())) {
            currentDeviceSpecifier = fetchCurrentDeviceSpecifier();
        }
    }


    @Override
    public void dispose() {
        active = false;
    }

}
