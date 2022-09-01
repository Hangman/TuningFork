package de.pottgames.tuningfork.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.AudioDeviceConfig;
import de.pottgames.tuningfork.DistanceAttenuationModel;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class DeviceTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

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
        final int number = Integer.parseInt(br.readLine());
        br.close();

        // INIT AUDIO
        final ConsoleLogger logger = new ConsoleLogger();
        logger.setLogLevel(LogLevel.TRACE_DEBUG_INFO_WARN_ERROR);
        final AudioDeviceConfig audioDeviceConfig = new AudioDeviceConfig();
        audioDeviceConfig.deviceSpecifier = deviceList.get(number);
        final Audio audio = Audio.init(new AudioConfig(audioDeviceConfig, DistanceAttenuationModel.NONE, 1, 0, logger));

        // LOAD SOUND
        final File soundFile = new File("src/test/resources/numbers.wav");
        final SoundBuffer sound = WaveLoader.load(soundFile);

        // PLAY SOUND
        audio.play(sound);

        // WAIT 8s AND EXIT
        Thread.sleep(8000L);
        audio.stopAll();
        sound.dispose();
        audio.dispose();
    }

}
