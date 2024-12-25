package de.pottgames.tuningfork.decoder;

import com.badlogic.gdx.files.FileHandle;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

/**
 * An {@link AudioStream} that decodes mp3. TuningFork doesn't officially support the mp3 file format, use at your own risk. This implementation is as open as
 * it can be, you should be able to extend it easily and make changes and improvements to your needs.
 *
 * @author Matthias
 */
public class Mp3InputStream implements AudioStream {
    protected Bitstream    bitstream;
    protected OutputBuffer outputBuffer;
    protected MP3Decoder   decoder;
    protected int          channels;
    protected int          sampleRate;
    protected FileHandle   file;
    protected float        duration = -1f;
    protected boolean      closed   = false;


    public Mp3InputStream(FileHandle file) {
        init(file);
    }


    protected void init(FileHandle file) {
        this.file = file;
        bitstream = new Bitstream(file.read());
        decoder = new MP3Decoder();
        try {
            final Header header = bitstream.readFrame();
            if (header == null) {
                throw new TuningForkRuntimeException("Empty MP3");
            }
            channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
            duration = header.total_ms((int) file.length()) / 1000f;
            outputBuffer = new OutputBuffer(channels, false);
            decoder.setOutputBuffer(outputBuffer);
            sampleRate = header.getSampleRate();
        } catch (final BitstreamException e) {
            throw new TuningForkRuntimeException("error while preloading mp3", e);
        }
    }


    @Override
    public int read(byte[] bytes) {
        try {
            int totalLength = 0;
            final int minRequiredLength = bytes.length - OutputBuffer.BUFFERSIZE * 2;
            while (totalLength <= minRequiredLength) {
                final Header header = bitstream.readFrame();
                if (header == null) {
                    break;
                }
                try {
                    decoder.decodeFrame(header, bitstream);
                } catch (final Exception ignored) {
                    // JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes?!
                }
                bitstream.closeFrame();

                final int length = outputBuffer.reset();
                System.arraycopy(outputBuffer.getBuffer(), 0, bytes, totalLength, length);
                totalLength += length;
            }
            return totalLength;
        } catch (final Throwable ex) {
            reset();
            throw new TuningForkRuntimeException("Error reading audio data.", ex);
        }
    }


    @Override
    public float getDuration() {
        return duration;
    }


    @Override
    public AudioStream reset() {
        close();
        init(file);
        return this;
    }


    @Override
    public int getChannels() {
        return channels;
    }


    @Override
    public int getSampleRate() {
        return sampleRate;
    }


    @Override
    public int getBitsPerSample() {
        return 16;
    }


    @Override
    public PcmDataType getPcmDataType() {
        return PcmDataType.INTEGER;
    }


    @Override
    public void close() {
        if (!closed) {
            outputBuffer = null;
            decoder = null;
            try {
                bitstream.close();
            } catch (final BitstreamException e) {
                // ignore
            }
            bitstream = null;
        }
    }


    @Override
    public boolean isClosed() {
        return closed;
    }

}
