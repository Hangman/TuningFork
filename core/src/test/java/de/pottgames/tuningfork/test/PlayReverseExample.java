package de.pottgames.tuningfork.test;

import java.io.IOException;

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

        // open an audio file as a stream - see also Mp3InputStream, OggInputStream, FlacInputStream, AiffInputStream etc.
        // the stream is responsible for decoding the audio data
        final WavInputStream stream = new WavInputStream(Gdx.files.internal("numbers.wav"));

        // fetch all samples from the stream
        // we get unencoded raw pcm samples
        final byte[] pcmData = new byte[(int) stream.bytesRemaining()];
        stream.read(pcmData);

        // reverse sample-wise
        final byte[] reversed = PlayReverseExample.reverseAudio(pcmData, stream.getBitsPerSample() / 8);

        // create the sound with the reversed data
        this.sound = new SoundBuffer(reversed, stream.getChannels(), stream.getSampleRate(), stream.getBitsPerSample(), stream.getPcmDataType());

        // close the stream
        try {
            stream.close();
        } catch (final IOException e) {
            // ignore, it is just an example
        }

        // and finally play the sound
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
