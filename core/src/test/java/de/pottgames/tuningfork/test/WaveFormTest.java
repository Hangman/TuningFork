/**
 * Copyright 2024 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
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
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.misc.ExperimentalFeature;
import de.pottgames.tuningfork.misc.Objects;
import de.pottgames.tuningfork.misc.PcmUtil;

@ExperimentalFeature(description = "Note: This test isn't perfect yet and it needs more love (fix flaws, improve " +
                                   "accuracy, readability).")
public class WaveFormTest extends ApplicationAdapter {
    private static final float  VIEWPORT_WIDTH  = 1600f;
    private static final float  VIEWPORT_HEIGHT = VIEWPORT_WIDTH / 16f * 9f;
    private static final float  WAVEFORM_HEIGHT = 100f;
    private static final String TEST_FILE1      = "numbers.wav";
    private static final String TEST_FILE2      = "numbers_8bit_mono.wav";
    private static final String TEST_FILE3      = "quadrophonic.wav";
    private static final String TEST_FILE4      = "32bit_float_numbers.wav";
    private static final String TEST_FILE5      = "64bit_float_numbers.wav";

    private FitViewport        viewport;
    private OrthographicCamera camera;
    private ShapeRenderer      renderer;

    private Audio       audio;
    private SoundBuffer sound;

    private final float[][] waveform = new float[8][0];


    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        renderer = new ShapeRenderer();
        renderer.setColor(Color.WHITE);
        this.audio = Audio.init();

        // READ THE AUDIO DATA AND EXTRACT META DATA
        WavInputStream input = new WavInputStream(Gdx.files.internal(TEST_FILE3));
        byte[] buffer = new byte[0];
        try {
            buffer = new byte[(int) input.bytesRemaining()];
            input.read(buffer);
        } catch (Exception e) {
            System.err.println("Error reading the file.");
            System.exit(2);
        }
        final int channels = input.getChannels();
        final int sampleRate = input.getSampleRate();
        final int sampleDepth = input.getBitsPerSample();
        final PcmFormat.PcmDataType pcmDataType = input.getPcmDataType();
        final int blockAlign = input.getBlockAlign();
        StreamUtils.closeQuietly(input);

        // CREATE THE WAVEFORM
        for (int channel = 0; channel < channels; channel++) {
            this.waveform[channel] = analyzeWaveForm(buffer, channels, sampleDepth, pcmDataType, channel + 1);
        }
        normalizeWaveForm(this.waveform); // optional: normalizing the waveform

        sound = new SoundBuffer(buffer, channels, sampleRate, sampleDepth, pcmDataType, blockAlign);
        this.sound.play();
    }


    private float[] analyzeWaveForm(
            byte[] buffer, int channels, int sampleDepth, PcmFormat.PcmDataType pcmDataType, int channel) {
        final int bytesPerSample = sampleDepth / 8;
        final int screenWidth = Gdx.graphics.getBackBufferWidth();
        final int samplesPerPixel = buffer.length / channels / bytesPerSample / screenWidth;
        final PcmFormat pcmFormat = PcmFormat.determineFormat(channels, sampleDepth, pcmDataType);
        Objects.requireNonNull(pcmFormat);
        final float[] waveform = new float[screenWidth];

        for (int i = 0; i < waveform.length; i++) {
            final int startIndex = i * samplesPerPixel;
            waveform[i] = PcmUtil.averageSample(buffer, pcmFormat, startIndex, startIndex + samplesPerPixel, channel);
        }

        return waveform;
    }


    private void normalizeWaveForm(float[][] waveform) {
        float max = 0.0000001f;
        for (float[] channel : waveform) {
            for (float value : channel) {
                if (value > max) {
                    max = value;
                }
            }
        }
        float factor = 1f / max;
        for (float[] channel : waveform) {
            for (int i = 0; i < channel.length; i++) {
                channel[i] *= factor;
            }
        }
    }


    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int channel = 0; channel < this.waveform.length; channel++) {
            renderWaveForm(this.waveform[channel], 50 + channel * WAVEFORM_HEIGHT);
        }
        renderer.end();
    }


    private void renderWaveForm(float[] waveform, float centerY) {
        for (int i = 0; i < waveform.length; i++) {
            float height = waveform[i] * WAVEFORM_HEIGHT;
            renderer.rect(i, centerY - height / 2f, 1f, height);
        }
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("WaveFormTest");
        config.setWindowedMode((int) VIEWPORT_WIDTH, (int) VIEWPORT_HEIGHT);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new WaveFormTest(), config);
    }

}
