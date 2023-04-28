package de.pottgames.tuningfork.router;

import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTDisconnect;
import org.lwjgl.openal.EnumerateAllExt;
import org.lwjgl.openal.SOFTReopenDevice;
import org.lwjgl.system.MemoryUtil;

import de.pottgames.tuningfork.AudioDevice;
import de.pottgames.tuningfork.TuningForkRuntimeException;

/**
 * <b>Warning</b>: This is an experimental router that has not been tested on all platforms and OpenAL backends. Use at your own risk.<br>
 * A task that runs on a daemon thread which periodically checks if the audio device lost connection. If a connection loss is detected or AL was opened on the
 * default device and the OS reports a new one, the task reopens AL on the new default audio device.
 *
 * @author Matthias
 *
 */
public class SmartDeviceRerouter implements AudioDeviceRerouter {
    private volatile boolean active            = false;
    private Thread           thread;
    private long             device;
    private volatile String  desiredDeviceSpecifier;
    private volatile boolean defaultDeviceMode = true;
    private boolean          setup             = false;


    @Override
    public void setup(long device, String desiredDeviceSpecifier) {
        this.device = device;
        this.setNewDesiredDevice(desiredDeviceSpecifier);
        this.setup = true;
    }


    @Override
    public void setNewDesiredDevice(String desiredDeviceSpecifier) {
        if (desiredDeviceSpecifier != null) {
            this.desiredDeviceSpecifier = desiredDeviceSpecifier;
            this.defaultDeviceMode = false;
        } else {
            this.desiredDeviceSpecifier = this.fetchDefaultDeviceName();
            this.defaultDeviceMode = true;
        }
    }


    /**
     * Starts the thread.
     */
    @Override
    public void start() {
        if (!this.setup) {
            throw new TuningForkRuntimeException("SmartDeviceRerouter wasn't set up properly");
        }

        this.active = true;
        this.thread = new Thread(() -> {
            SmartDeviceRerouter.this.loop();
        });
        this.thread.setName("TuningFork-SmartDeviceRerouter-Thread");
        this.thread.setDaemon(true);
        this.thread.start();
    }


    private void loop() {
        while (this.active) {
            final boolean isConnected = ALC10.alcGetInteger(this.device, EXTDisconnect.ALC_CONNECTED) == ALC10.ALC_TRUE;
            final String defaultDeviceName = this.fetchDefaultDeviceName();
            boolean newDefaultDevice = false;

            if (this.defaultDeviceMode && !this.desiredDeviceSpecifier.equals(defaultDeviceName)) {
                newDefaultDevice = true;
                this.desiredDeviceSpecifier = defaultDeviceName;
            }

            if (!isConnected || newDefaultDevice) {
                this.tryReopen();
            }

            try {
                Thread.sleep(1500);
            } catch (final InterruptedException e) {
                this.dispose();
            }
        }
    }


    private void tryReopen() {
        final List<String> availableDevices = AudioDevice.availableDevices();
        if (availableDevices.contains(this.desiredDeviceSpecifier)) {
            this.reopen(this.desiredDeviceSpecifier);
        } else {
            this.reopen(null);
            this.desiredDeviceSpecifier = this.fetchDefaultDeviceName();
        }
    }


    private String fetchDefaultDeviceName() {
        // FIXME: Works on Windows 11 (with idk what audio backend OpenAL chose) but other backends/OSes aren't tested
        return ALC10.alcGetString(MemoryUtil.NULL, EnumerateAllExt.ALC_ALL_DEVICES_SPECIFIER);
    }


    private void reopen(String deviceSpecifier) {
        if (!SOFTReopenDevice.alcReopenDeviceSOFT(this.device, deviceSpecifier, (IntBuffer) null)) {
            System.err.println("Failed to reopen audio device");
        }
    }


    @Override
    public void dispose() {
        this.active = false;
    }

}
