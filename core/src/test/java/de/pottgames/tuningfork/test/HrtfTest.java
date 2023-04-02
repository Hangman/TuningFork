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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.AudioDevice;
import de.pottgames.tuningfork.AudioDeviceConfig;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.DistanceAttenuationModel;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class HrtfTest extends ApplicationAdapter implements InputAdapter {
    private Audio               audio;
    private SoundBuffer         soundBuffer;
    private BufferedSoundSource soundSource;
    private float               angle;
    private String              hrtfName;


    @Override
    public void create() {
        // FETCH AVAILABLE DEVICES
        final List<String> deviceList = Audio.availableDevices();
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
        audioDeviceConfig.deviceSpecifier = deviceList.get(number);
        this.audio = Audio.init(new AudioConfig(audioDeviceConfig, DistanceAttenuationModel.NONE, 1, 0, logger));
        Gdx.input.setInputProcessor(this);

        // ENABLE HRTF
        final Array<String> hrtfs = this.audio.getDevice().getAvailableHrtfs();
        hrtfs.forEach(name -> System.out.println("available hrtf: " + name));
        if (!hrtfs.isEmpty()) {
            this.hrtfName = hrtfs.get(0);
            this.audio.getDevice().enableHrtf(hrtfs.get(0));
        } else {
            logger.error(this.getClass(), "no hrtf available");
        }

        // LOAD SOUND
        this.soundBuffer = WaveLoader.load(Gdx.files.internal("numbers.wav"));

        // OBTAIN SOURCE & PLAY SOUND
        this.soundSource = this.audio.obtainSource(this.soundBuffer);
        this.soundSource.setLooping(true);
        this.soundSource.setRelative(true);
        this.soundSource.play();

        System.out.println("Focus the application window and press space to toggle HRTF");
    }


    @Override
    public void render() {
        this.angle += Math.PI / 4f / 100f;
        this.soundSource.setPosition(MathUtils.sin(this.angle), 0f, -MathUtils.cos(this.angle));
    }


    @Override
    public boolean keyDown(int button) {
        if (button == Input.Keys.SPACE) {
            final AudioDevice device = this.audio.getDevice();
            if (device.isHrtfEnabled()) {
                device.disableHrtf();
                System.out.println("hrtf disabled");
            } else if (this.hrtfName != null) {
                device.enableHrtf(this.hrtfName);
                System.out.println("hrtf enabled");
            }
        }
        return true;
    }


    @Override
    public void dispose() {
        this.soundSource.free();
        this.soundBuffer.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("HrtfTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new HrtfTest(), config);
    }

}
