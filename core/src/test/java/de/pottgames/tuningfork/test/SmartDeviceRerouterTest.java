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

package de.pottgames.tuningfork.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.AudioDevice;
import de.pottgames.tuningfork.AudioDeviceConfig;
import de.pottgames.tuningfork.DistanceAttenuationModel;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.SoundSource;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;
import de.pottgames.tuningfork.router.SmartDeviceRerouter;

public class SmartDeviceRerouterTest extends ApplicationAdapter {

    private Audio       audio;
    private SoundBuffer sound;


    @Override
    public void create() {
        // FETCH AVAILABLE DEVICES
        final List<String> deviceList = AudioDevice.availableDevices();
        if (deviceList == null) {
            System.out.println("Error: deviceList is null");
            return;
        }

        // PRINT AVAILABLE DEVICE LIST
        final int[] i = new int[1];
        deviceList.forEach(device -> {
            System.out.println(i[0] + ": " + device);
            i[0]++;
        });

        // READ USER INPUT
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter device number: ");
        int number = 0;
        try {
            number = Integer.parseInt(br.readLine());
            br.close();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }

        // INIT AUDIO
        final ConsoleLogger logger = new ConsoleLogger();
        logger.setLogLevel(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR);
        final AudioDeviceConfig audioDeviceConfig = new AudioDeviceConfig();
        audioDeviceConfig.setDeviceSpecifier(deviceList.get(number));
        audioDeviceConfig.setRerouter(new SmartDeviceRerouter());
        this.audio = Audio.init(new AudioConfig(audioDeviceConfig, DistanceAttenuationModel.NONE, 1, 0, logger));

        this.sound = SoundLoader.load(Gdx.files.internal("numbers.wav"));

        final SoundSource source = this.audio.obtainSource(this.sound);
        source.setLooping(true);
        source.play();
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.sound.dispose();

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SmartDeviceRerouterTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new SmartDeviceRerouterTest(), config);
    }

}
