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

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.FlacLoader;
import de.pottgames.tuningfork.OggLoader;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.WaveLoader;

public class LoaderInputStreamTest extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer wav;
    private SoundBuffer flac;
    private SoundBuffer ogg;
    private SoundBuffer playing;
    private long        startTime;
    private float       duration;


    @Override
    public void create() {
        this.audio = Audio.init();

        InputStream wavStream = null;
        InputStream flacStream = null;
        InputStream oggStream = null;
        try {
            wavStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers.wav")));
            flacStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers_16bit_mono.flac")));
            oggStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/numbers2.ogg")));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.wav = WaveLoader.load(wavStream);
        this.flac = FlacLoader.load(flacStream);
        this.ogg = OggLoader.load(oggStream);
    }


    @Override
    public void render() {
        if (System.currentTimeMillis() > this.startTime + (long) this.duration) {
            SoundBuffer toPlay;
            if (this.playing == this.wav) {
                toPlay = this.flac;
                System.out.println("flac");
            } else if (this.playing == this.flac) {
                toPlay = this.ogg;
                System.out.println("ogg");
            } else if (this.playing == this.ogg) {
                toPlay = this.wav;
                System.out.println("wav");
            } else {
                toPlay = this.wav;
                System.out.println("wav");
            }
            this.playing = toPlay;
            this.startTime = System.currentTimeMillis();
            this.duration = toPlay.getDuration() * 1000f;
            toPlay.play();
        }
    }


    @Override
    public void dispose() {
        this.wav.dispose();
        this.flac.dispose();
        this.ogg.dispose();

        // always dispose Audio last
        this.audio.dispose();
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
