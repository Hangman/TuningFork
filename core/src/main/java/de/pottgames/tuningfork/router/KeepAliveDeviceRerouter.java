package de.pottgames.tuningfork.router;

import java.nio.IntBuffer;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTDisconnect;
import org.lwjgl.openal.SOFTReopenDevice;

import de.pottgames.tuningfork.TuningForkRuntimeException;

/**
 * A simple rerouter that connects to the default device when the connection to the current device is lost.
 *
 * @author Matthias
 *
 */
public class KeepAliveDeviceRerouter implements AudioDeviceRerouter {
    private volatile boolean active = false;
    private Thread           thread;
    private long             device;
    private boolean          setup  = false;


    @Override
    public void setup(long device, String desiredDeviceSpecifier) {
        this.device = device;
        this.setup = true;
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
                if (!SOFTReopenDevice.alcReopenDeviceSOFT(this.device, (String) null, (IntBuffer) null)) {
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
