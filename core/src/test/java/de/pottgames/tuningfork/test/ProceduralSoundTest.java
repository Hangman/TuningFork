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

import java.util.Arrays;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.PcmSoundSource;

public class ProceduralSoundTest extends ApplicationAdapter {
    private static final int      SAMPLE_RATE = 44100;
    private static final int      NPM         = 30;
    private static final WaveForm WAVEFORM    = WaveForm.SINE;
    private Audio                 audio;
    private PcmSoundSource        pcmSource;
    private final byte[]          pcm         = new byte[ProceduralSoundTest.SAMPLE_RATE];
    private SongNote[]            song;
    private int                   noteIndex   = 0;
    private long                  nextNoteTime;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.pcmSource = new PcmSoundSource(ProceduralSoundTest.SAMPLE_RATE, PcmFormat.MONO_8_BIT);
        this.pcmSource.setVolume(0.5f);
        this.song = SongGenerator.createImperialMarch();
    }


    @Override
    public void render() {
        final long millis = System.currentTimeMillis();

        if (millis >= this.nextNoteTime) {
            final SongNote songNote = this.song[this.noteIndex];
            switch (ProceduralSoundTest.WAVEFORM) {
                case SINE:
                    this.createSineTonePcm(songNote.note, this.pcm);
                    break;
                case SQUARE:
                    this.createSquareTonePcm(songNote.note, this.pcm);
                    break;
            }
            final int samplesPerBeat = ProceduralSoundTest.SAMPLE_RATE * 60 / ProceduralSoundTest.NPM;
            this.pcmSource.queueSamples(this.pcm, 0, (int) (samplesPerBeat * songNote.durationFactor));
            this.pcmSource.play();

            this.nextNoteTime = millis + (long) (60f / ProceduralSoundTest.NPM * 1000f * songNote.durationFactor);
            this.noteIndex++;
            if (this.noteIndex >= this.song.length) {
                this.noteIndex = 0;
            }
        }

    }


    private void createSineTonePcm(Note note, byte[] target) {
        if (note == Note.SILENCE) {
            Arrays.fill(target, (byte) 128);
            return;
        }

        final float samplesPerCycle = ProceduralSoundTest.SAMPLE_RATE / note.getFrequency();
        float cycleCounter = 0f;
        for (int i = 0; i < target.length; i++) {
            final float cycleProgress = cycleCounter / samplesPerCycle;
            target[i] = (byte) MathUtils.clamp(128 + 128 * MathUtils.sin(2f * MathUtils.PI * cycleProgress), 0, 255);
            cycleCounter++;
            if (cycleCounter >= samplesPerCycle) {
                cycleCounter = 0f;
            }
        }
    }


    private void createSquareTonePcm(Note note, byte[] target) {
        if (note == Note.SILENCE) {
            Arrays.fill(target, (byte) 128);
            return;
        }

        final float samplesPerCycle = ProceduralSoundTest.SAMPLE_RATE / note.getFrequency();
        float cycleCounter = 0f;
        for (int i = 0; i < target.length; i++) {
            final float cycleProgress = cycleCounter / samplesPerCycle;
            target[i] = (byte) (cycleProgress < 0.5f ? 0 : 255);
            cycleCounter++;
            if (cycleCounter >= samplesPerCycle) {
                cycleCounter = 0f;
            }
        }
    }


    @Override
    public void dispose() {
        this.pcmSource.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("ProceduralSoundTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new ProceduralSoundTest(), config);
    }


    private enum WaveForm {
        SINE, SQUARE;
    }

}
