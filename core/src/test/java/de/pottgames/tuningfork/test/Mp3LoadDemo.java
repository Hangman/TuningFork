package de.pottgames.tuningfork.test;

import java.io.ByteArrayOutputStream;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

/**
 * TuningFork doesn't officially support the mp3 file format. If you ultimatively have to play mp3 for whatever reason, this should demonstrate how you can
 * decode an mp3 file and play it through TuningFork. You might need to move <code>api "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"</code> from the
 * lwjgl3 sub-project to core in the gradle scripts in order to get access to the javazoom/JLayer decoder.<br>
 * <br>
 * Note that looping/seeking may cause problems related to the mp3 file format. Also the duration returned by TuningFork might not be as precise as with the
 * supported formats due to the same reason.
 *
 * @author Matthias
 *
 */
public class Mp3LoadDemo extends ApplicationAdapter {
    private Audio       audio;
    private SoundBuffer sound;


    /**
     * The code to decode mp3 audio data is an adapted version of the mp3 decoding part found in
     * <a href="https://github.com/libgdx/libgdx/blob/master/backends/gdx-backend-lwjgl3/src/com/badlogic/gdx/backends/lwjgl3/audio/Mp3.java">libGDX</a>.
     *
     */
    @Override
    public void create() {
        this.audio = Audio.init();

        final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        final Bitstream bitstream = new Bitstream(Gdx.files.internal("numbers.mp3").read());
        final MP3Decoder decoder = new MP3Decoder();
        try {
            OutputBuffer outputBuffer = null;
            int sampleRate = -1, channels = -1;
            while (true) {
                final Header header = bitstream.readFrame();
                if (header == null) {
                    break;
                }
                if (outputBuffer == null) {
                    channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2; // not checked mp3 surround sound
                    outputBuffer = new OutputBuffer(channels, false);
                    decoder.setOutputBuffer(outputBuffer);
                    sampleRate = header.getSampleRate();
                }
                try {
                    decoder.decodeFrame(header, bitstream);
                } catch (final Exception ignored) {
                    // JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
                }
                bitstream.closeFrame();
                output.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
            }
            bitstream.close();
            this.sound = new SoundBuffer(output.toByteArray(), channels, sampleRate, 16, PcmDataType.INTEGER);
        } catch (final Throwable ex) {
            throw new TuningForkRuntimeException("Error reading audio data.", ex);
        }

        this.audio.play(this.sound);
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
        config.setTitle("Mp3LoadDemo");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new Mp3LoadDemo(), config);
    }

}
