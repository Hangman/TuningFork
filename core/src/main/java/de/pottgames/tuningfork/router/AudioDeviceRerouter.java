package de.pottgames.tuningfork.router;

import com.badlogic.gdx.utils.Disposable;

import de.pottgames.tuningfork.AudioDeviceConfig;

public interface AudioDeviceRerouter extends Disposable {

    /**
     * {@link #setup(long, String) setup} is called before {@link #start() run}.
     *
     * @param device the OpenAL device handle
     * @param desiredDeviceSpecifier the device specifier that was specified in {@link AudioDeviceConfig#deviceSpecifier}
     */
    void setup(long device, String desiredDeviceSpecifier);


    void start();

}
