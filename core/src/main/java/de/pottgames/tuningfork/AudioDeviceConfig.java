package de.pottgames.tuningfork;

public class AudioDeviceConfig {
    /**
     * Must be one of the device specifiers you can query with {@link Audio#availableDevices()}. Leave it null to use the default audio device.
     */
    public String deviceSpecifier;

    /**
     * Whether to use the OpenAL output limiter that prevents clipping on the output.
     */
    public boolean enableOutputLimiter = true;
}
