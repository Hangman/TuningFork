/**
 * Copyright 2023 Matthias Finke
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
import de.pottgames.tuningfork.*;

public class AifcULawTest extends ApplicationAdapter {
    private static final String              FILE_PATH = "numbers-ulaw.aifc";
    private              Audio               audio;
    private              SoundBuffer         sound;
    private              SoundSource         bufferedSource;
    private              StreamedSoundSource streamedSource;
    private              SoundSource         activeSource;


    @Override
    public void create() {
        this.audio = Audio.init();
        this.sound = AiffLoader.load(Gdx.files.internal(AifcULawTest.FILE_PATH));
        this.streamedSource = new StreamedSoundSource(Gdx.files.internal(AifcULawTest.FILE_PATH));
        this.bufferedSource = this.audio.obtainSource(this.sound);
        System.out.println("Sound duration (streamed): " + this.streamedSource.getDuration() + "s");
        System.out.println("Sound duration (buffered): " + this.sound.getDuration() + "s");

        this.activeSource = this.bufferedSource;
    }


    @Override
    public void render() {
        if (!this.activeSource.isPlaying()) {
            if (this.activeSource == this.bufferedSource) {
                this.activeSource = this.streamedSource;
                System.out.println("streamed sound playing");
            } else {
                this.activeSource = this.bufferedSource;
                System.out.println("buffered sound playing");
            }

            this.activeSource.play();
        }
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("AifcULawTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new AifcULawTest(), config);
    }

}
