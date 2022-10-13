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
 *
 */
public class Mp3InputStream implements AudioStream {
    protected Bitstream    bitstream;
    protected OutputBuffer outputBuffer;
    protected MP3Decoder   decoder;
    protected int          channels;
    protected int          sampleRate;
    protected FileHandle   file;
    protected boolean      closed = false;


    public Mp3InputStream(FileHandle file) {
        this.init(file);
    }


    protected void init(FileHandle file) {
        this.file = file;
        this.bitstream = new Bitstream(file.read());
        this.decoder = new MP3Decoder();
        try {
            final Header header = this.bitstream.readFrame();
            if (header == null) {
                throw new TuningForkRuntimeException("Empty MP3");
            }
            this.channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
            this.outputBuffer = new OutputBuffer(this.channels, false);
            this.decoder.setOutputBuffer(this.outputBuffer);
            this.sampleRate = header.getSampleRate();
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
                final Header header = this.bitstream.readFrame();
                if (header == null) {
                    break;
                }
                try {
                    this.decoder.decodeFrame(header, this.bitstream);
                } catch (final Exception ignored) {
                    // JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes?!
                }
                this.bitstream.closeFrame();

                final int length = this.outputBuffer.reset();
                System.arraycopy(this.outputBuffer.getBuffer(), 0, bytes, totalLength, length);
                totalLength += length;
            }
            return totalLength;
        } catch (final Throwable ex) {
            this.reset();
            throw new TuningForkRuntimeException("Error reading audio data.", ex);
        }
    }


    @Override
    public float getDuration() {
        return -1f;
    }


    @Override
    public AudioStream reset() {
        this.close();
        this.init(this.file);
        return this;
    }


    @Override
    public int getChannels() {
        return this.channels;
    }


    @Override
    public int getSampleRate() {
        return this.sampleRate;
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
        if (!this.closed) {
            this.outputBuffer = null;
            this.decoder = null;
            try {
                this.bitstream.close();
            } catch (final BitstreamException e) {
                // ignore
            }
            this.bitstream = null;
        }
    }


    @Override
    public boolean isClosed() {
        return this.closed;
    }

}
