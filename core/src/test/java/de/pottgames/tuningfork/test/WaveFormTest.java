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
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.misc.Objects;
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
    private SoundBuffer         sound;
    private BufferedSoundSource soundSource;

    private float[][] waveform = new float[8][0];


    @Override
    public void create() {
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WaveFormTest.VIEWPORT_WIDTH, WaveFormTest.VIEWPORT_HEIGHT, this.camera);
        this.renderer = new ShapeRenderer();
        this.audio = Audio.init();

        // Some files to test this with
        final WavInputStream input = new WavInputStream(Gdx.files.internal("numbers.wav"));
        // final AudioStream input = new WavInputStream(Gdx.files.internal("numbers_8bit_mono.wav"));
        // final AudioStream input = new WavInputStream(Gdx.files.internal("quadrophonic.wav"));
        // final AudioStream input = new WavInputStream(Gdx.files.internal("32bit_float_numbers.wav"));
        // final AudioStream input = new WavInputStream(Gdx.files.internal("64bit_float_numbers.wav"));

        // Extract meta data from the stream
        final int channels = input.getChannels();
        final int sampleRate = input.getSampleRate();
        final int sampleDepth = input.getBitsPerSample();
        final int blockAlign = input.getBlockAlign();
        final PcmFormat format = PcmFormat.determineFormat(channels, sampleDepth, input.getPcmDataType());

        // Read the audio data
        final byte[] buffer = new byte[(int) input.bytesRemaining()];
        input.read(buffer);

        // We have all the data we need, so let's close the stream
        StreamUtils.closeQuietly(input);

        // Create the wave form of each channel
        this.waveform = new float[channels][0];
        for (int channel = 0; channel < channels; channel++) {
            this.waveform[channel] = this.analyzeWaveForm(buffer, format, channel + 1);
        }

        // Totally optional but often desired: normalizing the wave form
        this.normalizeWaveForm(this.waveform);

        // And finally the usual TuningFork sound stuff you're familiar with to play the sound. The only difference here is that you create the SoundBuffer
        // manually instead of letting the SoundLoader do the magic for you.
        this.sound = new SoundBuffer(buffer, channels, sampleRate, sampleDepth, format.getDataType(), blockAlign);
        this.soundSource = this.audio.obtainSource(this.sound);
        this.soundSource.setLooping(true);
        this.soundSource.play();
    }


    private float[] analyzeWaveForm(byte[] buffer, PcmFormat format, int channel) {
        Objects.requireNonNull(format);
        final int bytesPerSample = format.getBitsPerSample() / 8;

        // This defines the resolution of the wave form (how many samples a single point of the visual wave form represents)
        final int samplesPerUnit = buffer.length / format.getChannels() / bytesPerSample / WaveFormTest.VIEWPORT_WIDTH;
        final float[] waveform = new float[WaveFormTest.VIEWPORT_WIDTH];

        // For each point in the visual wave form, calculate the average amplitude of the samples it represents
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
