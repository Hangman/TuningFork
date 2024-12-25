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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.PcmSoundSource;
import de.pottgames.tuningfork.decoder.WavInputStream;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;

public class PcmSoundSourceTest extends ApplicationAdapter {
    private static final int BUFFER_SIZE = 4096 * 10;
    private Audio            audio;
    private final byte[]     pcm         = new byte[PcmSoundSourceTest.BUFFER_SIZE];
    private WavInputStream   stream;
    private PcmSoundSource   pcmSource;
    private long             lastPcmPush;


    @Override
    public void create() {
        final AudioConfig config = new AudioConfig().setLogger(new ConsoleLogger(LogLevel.DEBUG_INFO_WARN_ERROR));
        audio = Audio.init(config);
        stream = new WavInputStream(Gdx.files.internal("numbers.wav"));
        final PcmFormat format = PcmFormat.determineFormat(stream.getChannels(), stream.getBitsPerSample(), stream.getPcmDataType());
        assert format != null;
        pcmSource = new PcmSoundSource(stream.getSampleRate(), format);
    }


    @Override
    public void render() {
        if (System.currentTimeMillis() > lastPcmPush + 500) {
            lastPcmPush = System.currentTimeMillis();
            final int readData = stream.read(pcm);
            if (readData != -1) {
                pcmSource.queueSamples(pcm, 0, readData);
                pcmSource.play();
            }
        }
    }


    @Override
    public void dispose() {
        pcmSource.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SimplePcmSoundSourceTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PcmSoundSourceTest(), config);
    }

}
