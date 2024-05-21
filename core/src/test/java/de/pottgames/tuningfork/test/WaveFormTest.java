/**
 * Copyright 2024 Matthias Finke
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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.ReadableSoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.misc.PcmUtil;

/**
 * A test to demonstrate how a wave form of a wav sound file could be rendered. This also works with minor adjustments for all other file formats that are
 * supported by TuningFork.
 *
 * @author Matthias
 */
public class WaveFormTest extends ApplicationAdapter {
    private static final int   VIEWPORT_WIDTH    = 1600;
    private static final int   VIEWPORT_HEIGHT   = (int) (WaveFormTest.VIEWPORT_WIDTH / 16f * 9f);
    private static final float WAVEFORM_HEIGHT   = 100f;
    private static final float CURSOR_WIDTH      = 4f;
    private static final float CURSOR_WIDTH_HALF = WaveFormTest.CURSOR_WIDTH / 2f;

    private FitViewport        viewport;
    private OrthographicCamera camera;
    private ShapeRenderer      renderer;

    private Audio               audio;
    private ReadableSoundBuffer sound;
    private BufferedSoundSource soundSource;

    private float[][] waveform = new float[8][0];


    @Override
    public void create() {
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WaveFormTest.VIEWPORT_WIDTH, WaveFormTest.VIEWPORT_HEIGHT, this.camera);
        this.renderer = new ShapeRenderer();
        this.audio = Audio.init();

        // Some files to test this with
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("numbers.wav"));
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("numbers_8bit_mono.wav"));
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("quadrophonic.wav"));
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("32bit_float_numbers.wav"));
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("64bit_float_numbers.wav"));
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("carnivalrides.ogg"));
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("numbers.mp3"));
        // this.sound = SoundLoader.loadReadable(Gdx.files.internal("numbers_16bit_stereo.flac"));
        this.sound = SoundLoader.loadReadable(Gdx.files.internal("42_accordion_melodious_phrase_stereo.qoa"));

        // Extract the data we need to create the waveform
        final PcmFormat format = this.sound.getPcmFormat();
        final int channels = format.getChannels();

        // Create the waveform of each channel
        this.waveform = new float[channels][0];
        for (int channel = 0; channel < channels; channel++) {
            this.waveform[channel] = this.analyzeWaveForm(this.sound.getAudioData(), format, channel + 1);
        }

        // Totally optional but often desired: normalizing the wave form
        this.normalizeWaveForm(this.waveform);

        // And finally the usual TuningFork sound stuff you're familiar with to play the sound. The only difference here is that you create the SoundBuffer
        // manually instead of letting the SoundLoader do the magic for you.
        this.soundSource = this.audio.obtainSource(this.sound);
        this.soundSource.setLooping(true);
        this.soundSource.play();
    }


    private float[] analyzeWaveForm(byte[] buffer, PcmFormat format, int channel) {
        final int bytesPerSample = format.getBitsPerSample() / 8;

        // This defines the resolution of the wave form (how many samples a single point of the waveform represents)
        final int samplesPerUnit = buffer.length / format.getChannels() / bytesPerSample / WaveFormTest.VIEWPORT_WIDTH;
        final float[] waveform = new float[WaveFormTest.VIEWPORT_WIDTH];

        // For each point in the waveform, calculate the average amplitude of the samples it represents
        for (int i = 0; i < waveform.length; i++) {
            final int startIndex = i * samplesPerUnit;
            waveform[i] = PcmUtil.averageSample(buffer, format, startIndex, startIndex + samplesPerUnit, channel);
        }

        return waveform;
    }


    private void normalizeWaveForm(float[][] waveform) {
        // Find the highest amplitude
        float max = 0.0000001f;
        for (final float[] channel : waveform) {
            for (final float value : channel) {
                if (value > max) {
                    max = value;
                }
            }
        }

        // Apply normalization factor to all samples
        final float factor = 1f / max;
        for (final float[] channel : waveform) {
            for (int i = 0; i < channel.length; i++) {
                channel[i] *= factor;
            }
        }
    }


    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        this.renderer.setProjectionMatrix(this.camera.combined);
        this.renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int channel = 0; channel < this.waveform.length; channel++) {
            this.renderWaveForm(this.waveform[channel], 50 + channel * WaveFormTest.WAVEFORM_HEIGHT);
        }
        this.renderCursor();

        this.renderer.end();
    }


    private void renderWaveForm(float[] waveform, float centerY) {
        this.renderer.setColor(Color.WHITE);
        for (int i = 0; i < waveform.length; i++) {
            final float height = waveform[i] * WaveFormTest.WAVEFORM_HEIGHT;
            this.renderer.rect(i, centerY - height / 2f, 1f, height);
        }
    }


    private void renderCursor() {
        this.renderer.setColor(Color.RED);
        final float progress = this.soundSource.getPlaybackPosition() / this.sound.getDuration();
        this.renderer.rect(progress * WaveFormTest.VIEWPORT_WIDTH - WaveFormTest.CURSOR_WIDTH_HALF, 0f, WaveFormTest.CURSOR_WIDTH,
                WaveFormTest.WAVEFORM_HEIGHT * this.waveform.length);
    }


    @Override
    public void resize(int width, int height) {
        this.viewport.update(width, height, true);
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("WaveFormTest");
        config.setWindowedMode(WaveFormTest.VIEWPORT_WIDTH, WaveFormTest.VIEWPORT_HEIGHT);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new WaveFormTest(), config);
    }

}
