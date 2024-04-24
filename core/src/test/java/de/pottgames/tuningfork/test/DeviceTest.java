/**
 * Copyright 2022 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import de.pottgames.tuningfork.*;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class DeviceTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        Gdx.files = new Lwjgl3Files();

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
        final int number = Integer.parseInt(br.readLine());
        br.close();

        // INIT AUDIO
        final ConsoleLogger logger = new ConsoleLogger();
        logger.setLogLevel(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR);
        final AudioDeviceConfig audioDeviceConfig = new AudioDeviceConfig();
        audioDeviceConfig.setDeviceSpecifier(deviceList.get(number));
        final Audio audio = Audio.init(new AudioConfig(audioDeviceConfig, DistanceAttenuationModel.NONE, 1, 0, logger));

        // LOAD SOUND
        final File soundFile = new File("src/test/resources/numbers.wav");
        final SoundBuffer sound = WaveLoader.load(soundFile);

        // PLAY SOUND
        sound.play();

        // WAIT 8s AND EXIT
        Thread.sleep(8000L);
        audio.stopAll();
        sound.dispose();
        audio.dispose();
    }

}
