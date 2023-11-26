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
    private static final float[]  FADE_FACTORS     = new float[] { 1f, 0.975f, 0.95f, 0.925f, 0.9f, 0.875f, 0.85f, 0.825f, 0.8f, 0.775f, 0.75f, 0.725f, 0.7f,
            0.675f, 0.65f, 0.625f, 0.6f, 0.575f, 0.55f, 0.525f, 0.5f, 0.475f, 0.45f, 0.425f, 0.4f, 0.375f, 0.35f, 0.325f, 0.3f, 0.275f, 0.25f, 0.225f, 0.2f,
            0.175f, 0.15f, 0.125f, 0.1f, 0.075f, 0.05f, 0.025f, 0f };
    private static final int      SAMPLE_RATE      = 44100;
    private static final int      NPM              = 30;
    private static final int      SAMPLES_PER_BEAT = ProceduralSoundTest.SAMPLE_RATE * 60 / ProceduralSoundTest.NPM;
    private static final WaveForm WAVEFORM         = WaveForm.SINE;
    private Audio                 audio;
    private PcmSoundSource        pcmSource;
    private final float[]         pcm              = new float[ProceduralSoundTest.SAMPLE_RATE];
    private SongNote[]            song;
    private int                   noteIndex        = 0;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.pcmSource = new PcmSoundSource(ProceduralSoundTest.SAMPLE_RATE, PcmFormat.FLOAT_MONO_32_BIT);
        this.pcmSource.setVolume(0.5f);
        this.song = SongGenerator.createImperialMarch();
        this.queueNextNote();
        this.pcmSource.play();
    }


    @Override
    public void render() {
        // this isn't necessary, just to demonstrate how to check for an underflow
        if (!this.pcmSource.isPlaying()) {
            System.out.println("pcm underflow, resuming playback");
        }

        // we're playing a song on repeat, so we want to make sure there's always some samples in the queue (prevent underflow)
        final int queuedBuffers = this.pcmSource.queuedBuffers();
        for (int i = 0; i < 3 - queuedBuffers; i++) {
            this.queueNextNote();

            // this is best practice:
            // if an underflow happened for some reason, the source is in stopped-state and won't continue playing without this call
            this.pcmSource.play();
        }
    }


    private void queueNextNote() {
        // fetch the next note from the song
        final SongNote songNote = this.song[this.noteIndex];

        // sample count for this note
        final int sampleCount = (int) (ProceduralSoundTest.SAMPLES_PER_BEAT * songNote.durationFactor);

        // fill the array with samples
        if (songNote.note == Note.SILENCE) {
            Arrays.fill(this.pcm, 0f);
        } else {
            switch (ProceduralSoundTest.WAVEFORM) {
                case SINE:
                    this.createSineTonePcm(songNote.note, this.pcm, sampleCount);
                    break;
                case SQUARE:
                    this.createSquareTonePcm(songNote.note, this.pcm, sampleCount);
                    break;
            }

            // to prevent audio cracks, apply some fading at the beginning and the end
            this.fadeInAndOut(this.pcm, sampleCount);
        }

        // finally queue the samples on the source
        this.pcmSource.queueSamples(this.pcm, 0, sampleCount);

        // and prepare for the next call to this method
        this.noteIndex++;
        if (this.noteIndex >= this.song.length) {
            this.noteIndex = 0;
        }
    }


    private void createSineTonePcm(Note note, float[] target, int limit) {
        if (note == Note.SILENCE) {
            Arrays.fill(target, 0f);
            return;
        }

        final float samplesPerCycle = ProceduralSoundTest.SAMPLE_RATE / note.getFrequency();
        float cycleCounter = 0f;
        for (int i = 0; i < limit; i++) {
            final float cycleProgress = cycleCounter / samplesPerCycle;
            target[i] = MathUtils.sin(MathUtils.PI2 * cycleProgress);
            cycleCounter++;
            if (cycleCounter >= samplesPerCycle) {
                cycleCounter = 0f;
            }
        }
    }


    private void createSquareTonePcm(Note note, float[] target, int limit) {
        if (note == Note.SILENCE) {
            Arrays.fill(target, 0f);
            return;
        }

        final float samplesPerCycle = ProceduralSoundTest.SAMPLE_RATE / note.getFrequency();
        float cycleCounter = 0f;
        for (int i = 0; i < limit; i++) {
            final float cycleProgress = cycleCounter / samplesPerCycle;
            target[i] = cycleProgress < 0.5f ? -1f : 1f;
            cycleCounter++;
            if (cycleCounter >= samplesPerCycle) {
                cycleCounter = 0f;
            }
        }
    }


    private void fadeInAndOut(float[] target, int limit) {
        // flatten the samples at the beginning to prevent popping sounds
        for (int i = 0; i < ProceduralSoundTest.FADE_FACTORS.length; i++) {
            target[i] *= ProceduralSoundTest.FADE_FACTORS[ProceduralSoundTest.FADE_FACTORS.length - 1 - i];
        }

        // flatten the samples at the end to prevent popping sounds
        int fadeIndex = ProceduralSoundTest.FADE_FACTORS.length - 1;
        for (int i = limit - 1; i > limit - ProceduralSoundTest.FADE_FACTORS.length; i--) {
            target[i] *= ProceduralSoundTest.FADE_FACTORS[fadeIndex];
            fadeIndex--;
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
