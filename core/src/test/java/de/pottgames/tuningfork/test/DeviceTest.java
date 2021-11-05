package de.pottgames.tuningfork.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.ConsoleLogger;
import de.pottgames.tuningfork.ConsoleLogger.LogLevel;
import de.pottgames.tuningfork.OpenDeviceException;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;

public class DeviceTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        final List<String> deviceList = Audio.availableDevices();
        if (deviceList == null) {
            System.out.println("Error: deviceList is null");
            return;
        }

        final int[] i = new int[1];
        deviceList.forEach(device -> {
            System.out.println(i[0] + ": " + device);
            i[0]++;
        });

        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter device number: ");
        final int number = Integer.parseInt(br.readLine());

        Audio audio = null;
        try {
            final ConsoleLogger logger = new ConsoleLogger();
            logger.setLogLevel(LogLevel.DEBUG_INFO_WARN_ERROR);
            audio = new Audio(deviceList.get(number), logger);
        } catch (final OpenDeviceException e) {
            e.printStackTrace();
        }
        final File soundFile = new File("src/test/resources/numbers.wav");
        final SoundBuffer sound = WaveLoader.load(soundFile);
        audio.play(sound);
        Thread.sleep(10100L);
    }

}
