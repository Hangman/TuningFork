package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.decoder.WavInputStream;

public class PlayReverseExample extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;


    @Override
    public void create() {
        this.audio = Audio.init();

        final WavInputStream stream = new WavInputStream(Gdx.files.internal("numbers.wav"));
        final byte[] pcmData = new byte[(int) stream.bytesRemaining()];
        stream.read(pcmData);
        final byte[] reversed = PlayReverseExample.reverseAudio(pcmData, stream.getBitsPerSample() / 8);

        this.sound = new SoundBuffer(reversed, stream.getChannels(), stream.getSampleRate(), stream.getBitsPerSample(), stream.getPcmDataType());
        this.sound.play();
    }


    public static byte[] reverseAudio(byte[] pcmData, int sampleSizeInBytes) {
        final int numSamples = pcmData.length / sampleSizeInBytes;
        final byte[] reversedData = new byte[pcmData.length];

        for (int i = 0; i < numSamples; i++) {
            final int srcStart = i * sampleSizeInBytes;
            final int destStart = (numSamples - i - 1) * sampleSizeInBytes;
            System.arraycopy(pcmData, srcStart, reversedData, destStart, sampleSizeInBytes);
        }

        return reversedData;
    }


    @Override
    public void render() {
        // we chill in a black window
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PlayReverseExample");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new PlayReverseExample(), config);
    }

}
