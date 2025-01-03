package de.pottgames.tuningfork;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.misc.PcmUtil;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

/**
 * This class provides static functions to load mp3 audio data.<br>
 * The mp3 file format is not officially supported by TuningFork. Use at your own risk.
 *
 * @author Matthias
 */
public abstract class Mp3Loader {

    /**
     * Loads a mp3 into a {@link SoundBuffer}.
     *
     * @param file the file
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(File file) {
        return Mp3Loader.load(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads a mp3 into a {@link ReadableSoundBuffer}.
     *
     * @param file the file
     *
     * @return the ReadableSoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(File file) {
        return Mp3Loader.loadReadable(Gdx.files.absolute(file.getAbsolutePath()));
    }


    /**
     * Loads sound data from a {@link FileHandle} into a {@link SoundBuffer} using the mp3 decoder and closes the stream afterward.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(FileHandle file) {
        return Mp3Loader.load(file.read());
    }


    /**
     * Loads sound data from a {@link FileHandle} into a {@link ReadableSoundBuffer} using the mp3 decoder and closes the stream afterward.
     *
     * @param file the file handle
     *
     * @return the ReadableSoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(FileHandle file) {
        return Mp3Loader.loadReadable(file.read());
    }


    /**
     * Loads a mp3 into a {@link SoundBuffer} and closes the InputStream afterward.
     *
     * @param stream the input stream
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer load(InputStream stream) {
        SoundBuffer result = null;

        final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        final Bitstream bitstream = new Bitstream(stream);
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
            result = new SoundBuffer(output.toByteArray(), channels, sampleRate, 16, PcmDataType.INTEGER);
        } catch (final Throwable ex) {
            throw new TuningForkRuntimeException("Error reading audio data.", ex);
        }

        return result;
    }


    /**
     * Loads a mp3 into a {@link ReadableSoundBuffer} and closes the InputStream afterward.
     *
     * @param stream the input stream
     *
     * @return the ReadableSoundBuffer
     */
    public static ReadableSoundBuffer loadReadable(InputStream stream) {
        ReadableSoundBuffer result = null;

        final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        final Bitstream bitstream = new Bitstream(stream);
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
            result = new ReadableSoundBuffer(output.toByteArray(), channels, sampleRate, 16, PcmDataType.INTEGER);
        } catch (final Throwable ex) {
            throw new TuningForkRuntimeException("Error reading audio data.", ex);
        }

        return result;
    }


    /**
     * Loads an mp3 file in reverse into a {@link SoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static SoundBuffer loadReverse(FileHandle file) {
        SoundBuffer result = null;

        final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        final Bitstream bitstream = new Bitstream(file.read());
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

            final byte[] pcmData = output.toByteArray();
            final byte[] reversedPcm = PcmUtil.reverseAudio(pcmData, 2);

            result = new SoundBuffer(reversedPcm, channels, sampleRate, 16, PcmDataType.INTEGER);
        } catch (final Throwable ex) {
            throw new TuningForkRuntimeException("Error reading audio data.", ex);
        }

        return result;
    }


    /**
     * Loads an mp3 file in reverse into a {@link ReadableSoundBuffer}.
     *
     * @param file the file handle
     *
     * @return the SoundBuffer
     */
    public static ReadableSoundBuffer loadReadableReverse(FileHandle file) {
        ReadableSoundBuffer result = null;

        final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        final Bitstream bitstream = new Bitstream(file.read());
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

            final byte[] pcmData = output.toByteArray();
            final byte[] reversedPcm = PcmUtil.reverseAudio(pcmData, 2);

            result = new ReadableSoundBuffer(reversedPcm, channels, sampleRate, 16, PcmDataType.INTEGER);
        } catch (final Throwable ex) {
            throw new TuningForkRuntimeException("Error reading audio data.", ex);
        }

        return result;
    }

}
