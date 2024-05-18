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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioDevice;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.StreamedSoundSource;

public class ResamplerTest extends ApplicationAdapter implements InputAdapter {
    private Audio               audio;
    private Array<String>       resamplers;
    private StreamedSoundSource streamedSource;
    private SoundBuffer         sound;
    private BufferedSoundSource soundSource;


    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);
        this.audio = Audio.init();
        final AudioDevice device = this.audio.getDevice();
        System.out.println("default resampler: " + device.getDefaultResampler());
        this.resamplers = device.getAvailableResamplers();
        for (int i = 0; i < this.resamplers.size; i++) {
            System.out.println("Press " + i + " to set the " + this.resamplers.get(i) + " resampler");
        }
        System.out.println("Press A to play the BufferedSoundSource");
        System.out.println("Press B to play the StreamedSoundSource");

        this.streamedSource = new StreamedSoundSource(Gdx.files.internal("numbers_8bit_mono_8kHz.wav"));
        this.streamedSource.setLooping(true);

        this.sound = SoundLoader.load(Gdx.files.internal("numbers_8bit_mono_8kHz.wav"));
        this.soundSource = this.audio.obtainSource(this.sound);
        this.soundSource.setLooping(true);
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public boolean keyUp(int keycode) {
        int resamplerIndex = -1;
        switch (keycode) {
            case Input.Keys.A:
                this.soundSource.play();
                this.streamedSource.stop();
                break;
            case Input.Keys.B:
                this.soundSource.stop();
                this.streamedSource.play();
                break;
            case Input.Keys.NUM_0:
            case Input.Keys.NUMPAD_0:
                resamplerIndex = 0;
                break;
            case Input.Keys.NUM_1:
            case Input.Keys.NUMPAD_1:
                resamplerIndex = 1;
                break;
            case Input.Keys.NUM_2:
            case Input.Keys.NUMPAD_2:
                resamplerIndex = 2;
                break;
            case Input.Keys.NUM_3:
            case Input.Keys.NUMPAD_3:
                resamplerIndex = 3;
                break;
            case Input.Keys.NUM_4:
            case Input.Keys.NUMPAD_4:
                resamplerIndex = 4;
                break;
            case Input.Keys.NUM_5:
            case Input.Keys.NUMPAD_5:
                resamplerIndex = 5;
                break;
            case Input.Keys.NUM_6:
            case Input.Keys.NUMPAD_6:
                resamplerIndex = 6;
                break;
            case Input.Keys.NUM_7:
            case Input.Keys.NUMPAD_7:
                resamplerIndex = 7;
                break;
            case Input.Keys.NUM_8:
            case Input.Keys.NUMPAD_8:
                resamplerIndex = 8;
                break;
            case Input.Keys.NUM_9:
            case Input.Keys.NUMPAD_9:
                resamplerIndex = 9;
                break;
            default:
                return false;
        }

        if (resamplerIndex >= 0 && resamplerIndex < this.resamplers.size) {
            final String resampler = this.resamplers.get(resamplerIndex);
            this.audio.setDefaultResampler(resampler);
            System.out.println("resampler set to: " + resampler);
        }

        return true;
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Resampler Test");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new ResamplerTest(), config);
    }

}
