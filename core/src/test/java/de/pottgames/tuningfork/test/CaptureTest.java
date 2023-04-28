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
import java.nio.ShortBuffer;
import java.util.List;

import com.badlogic.gdx.utils.BufferUtils;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.AudioDevice;
import de.pottgames.tuningfork.AudioDeviceConfig;
import de.pottgames.tuningfork.DistanceAttenuationModel;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.PcmSoundSource;
import de.pottgames.tuningfork.PitchShifter;
import de.pottgames.tuningfork.SoundEffect;
import de.pottgames.tuningfork.capture.CaptureConfig;
import de.pottgames.tuningfork.capture.CaptureDevice;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class CaptureTest {

    public static void main(String[] args) throws NumberFormatException, IOException {
        final int frequency = 44100;
        final int bufferSize = frequency / 10;

        // FETCH AVAILABLE OUTPUT DEVICES
        final List<String> outputDeviceList = AudioDevice.availableDevices();
        if (outputDeviceList == null || outputDeviceList.isEmpty()) {
            System.out.println("Error: no output device found");
            return;
        }

        // PRINT AVAILABLE OUTPUT DEVICE LIST
        final int[] i = new int[1];
        outputDeviceList.forEach(device -> {
            System.out.println(i[0] + ": " + device);
            i[0]++;
        });

        // READ USER INPUT
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Select output device!");
        System.out.print("Enter device number: ");
        final int outputNumber = Integer.parseInt(reader.readLine());

        // INIT AUDIO
        final ConsoleLogger logger = new ConsoleLogger();
        logger.setLogLevel(LogLevel.WARN_ERROR);
        final AudioDeviceConfig audioDeviceConfig = new AudioDeviceConfig();
        audioDeviceConfig.setDeviceSpecifier(outputDeviceList.get(outputNumber));
        final Audio audio = Audio.init(new AudioConfig(audioDeviceConfig, DistanceAttenuationModel.NONE, 1, 0, logger));

        // CREATE SOUND SOURCE
        final PcmSoundSource source = new PcmSoundSource(frequency, PcmFormat.MONO_16_BIT);
        final SoundEffect effect = new SoundEffect(new PitchShifter());
        source.attachEffect(effect);

        // FETCH AVAILABLE INPUT DEVICES
        final List<String> inputDeviceList = CaptureDevice.availableDevices();
        if (inputDeviceList == null || inputDeviceList.isEmpty()) {
            System.out.println("Error: no input device found");
            return;
        }

        // PRINT AVAILABLE INPUT DEVICE LIST
        System.out.println("");
        i[0] = 0;
        inputDeviceList.forEach(device -> {
            System.out.println(i[0] + ": " + device);
            i[0]++;
        });

        // READ USER INPUT
        System.out.println("");
        System.out.println("Select input device!");
        System.out.println("Default input device: " + CaptureDevice.getDefaultDeviceName());
        System.out.print("Enter device number: ");
        final int inputNumber = Integer.parseInt(reader.readLine());
        reader.close();

        final CaptureConfig config = new CaptureConfig();
        config.setPcmFormat(PcmFormat.MONO_16_BIT);
        config.setDeviceSpecifier(inputDeviceList.get(inputNumber));
        final CaptureDevice device = CaptureDevice.open(config);
        System.out.println("Device Name: " + device.getDeviceName());

        final ShortBuffer buffer = BufferUtils.newShortBuffer(bufferSize);

        final long captureStartTime = System.currentTimeMillis();
        device.startCapture();

        while (System.currentTimeMillis() < captureStartTime + 30000L) {
            while (device.capturedSamples() >= bufferSize) {
                buffer.clear();
                device.fetch16BitSamples(buffer, bufferSize);
                source.queueSamples(buffer);
                source.play();
            }

            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                // ignore
            }
        }

        device.stopCapture();
        effect.dispose();
        source.dispose();
        device.dispose();
        audio.dispose();
    }

}
