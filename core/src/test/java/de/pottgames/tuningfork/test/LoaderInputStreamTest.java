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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.AiffLoader;
import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.FlacLoader;
import de.pottgames.tuningfork.Mp3Loader;
import de.pottgames.tuningfork.OggLoader;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;

public class LoaderInputStreamTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer wav;
    private SoundBuffer flac;
    private SoundBuffer ogg;
    private SoundBuffer aiff;
    private SoundBuffer mp3;
    private SoundBuffer playing;
    private long        startTime;
    private float       duration;


    @Override
    public void create() {
        audio = Audio.init();

        InputStream wavStream = null;
        InputStream flacStream = null;
        InputStream oggStream = null;
        InputStream aiffStream = null;
        InputStream mp3Stream = null;
        try {
            wavStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers.wav")));
            flacStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers_16bit_mono.flac")));
            oggStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers2.ogg")));
            aiffStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers.aiff")));
            mp3Stream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers.mp3")));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        wav = WaveLoader.load(wavStream);
        flac = FlacLoader.load(flacStream);
        ogg = OggLoader.load(oggStream);
        aiff = AiffLoader.load(aiffStream);
        mp3 = Mp3Loader.load(mp3Stream);
    }


    @Override
    public void render() {
        if (System.currentTimeMillis() > startTime + (long) duration) {
            SoundBuffer toPlay;
            if (playing == wav) {
                toPlay = flac;
                System.out.println("flac");
            } else if (playing == flac) {
                toPlay = ogg;
                System.out.println("ogg");
            } else if (playing == ogg) {
                toPlay = aiff;
                System.out.println("aiff");
            } else if (playing == aiff) {
                toPlay = mp3;
                System.out.println("mp3");
            } else if (playing == mp3) {
                toPlay = wav;
                System.out.println("wav");
            } else {
                toPlay = wav;
                System.out.println("wav");
            }
            playing = toPlay;
            startTime = System.currentTimeMillis();
            duration = toPlay.getDuration() * 1000f;
            toPlay.play();
        }
    }


    @Override
    public void dispose() {
        wav.dispose();
        flac.dispose();
        ogg.dispose();
        aiff.dispose();
        mp3.dispose();

        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("LoaderInputStreamTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new LoaderInputStreamTest(), config);
    }

}
