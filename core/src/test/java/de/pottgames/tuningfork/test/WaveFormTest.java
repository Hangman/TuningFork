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
    private static final String[] TEST_FILES        = { "numbers.wav", "numbers_8bit_mono.wav", "quadrophonic.wav", "32bit_float_numbers.wav",
            "64bit_float_numbers.wav", "carnivalrides.ogg", "numbers.mp3", "numbers_16bit_stereo.flac", "42_accordion_melodious_phrase_stereo.qoa" };
    private static final int      VIEWPORT_WIDTH    = 1600;
    private static final int      VIEWPORT_HEIGHT   = (int) (WaveFormTest.VIEWPORT_WIDTH / 16f * 9f);
    private static final float    WAVEFORM_HEIGHT   = 100f;
    private static final float    CURSOR_WIDTH      = 4f;
    private static final float    CURSOR_WIDTH_HALF = WaveFormTest.CURSOR_WIDTH / 2f;

    private FitViewport        viewport;
    private OrthographicCamera camera;
    private ShapeRenderer      renderer;

    private Audio               audio;
    private ReadableSoundBuffer sound;
    private BufferedSoundSource soundSource;

    private float[][] waveform;


    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WaveFormTest.VIEWPORT_WIDTH, WaveFormTest.VIEWPORT_HEIGHT, camera);
        renderer = new ShapeRenderer();
        audio = Audio.init();

        // Load a ReadableSoundBuffer instead of just a default SoundBuffer.
        // That will allow us to read the audio data later on.
        sound = SoundLoader.loadReadable(Gdx.files.internal(WaveFormTest.TEST_FILES[8]));

        // Extract the data we need to create the waveform
        final PcmFormat format = sound.getPcmFormat();
        final int channels = format.getChannels();

        // Create the waveform of each channel
        waveform = new float[channels][0];
        for (int channel = 0; channel < channels; channel++) {
            waveform[channel] = analyzeWaveForm(sound.getAudioData(), format, channel + 1);
        }

        // Totally optional but often desired: normalizing the wave form
        normalizeWaveForm(waveform);

        soundSource = audio.obtainSource(sound);
        soundSource.setLooping(true);
        soundSource.play();
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
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int channel = 0; channel < waveform.length; channel++) {
            renderWaveForm(waveform[channel], 50 + channel * WaveFormTest.WAVEFORM_HEIGHT);
        }
        renderCursor();

        renderer.end();
    }


    private void renderWaveForm(float[] waveform, float centerY) {
        renderer.setColor(Color.WHITE);
        for (int i = 0; i < waveform.length; i++) {
            final float height = waveform[i] * WaveFormTest.WAVEFORM_HEIGHT;
            renderer.rect(i, centerY - height / 2f, 1f, height);
        }
    }


    private void renderCursor() {
        renderer.setColor(Color.RED);
        final float progress = soundSource.getPlaybackPosition() / sound.getDuration();
        renderer.rect(progress * WaveFormTest.VIEWPORT_WIDTH - WaveFormTest.CURSOR_WIDTH_HALF, 0f, WaveFormTest.CURSOR_WIDTH,
                WaveFormTest.WAVEFORM_HEIGHT * waveform.length);
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
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
