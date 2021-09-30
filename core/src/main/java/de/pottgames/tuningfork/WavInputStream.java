package de.pottgames.tuningfork;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

class WavInputStream extends FilterInputStream {
    int channels, sampleRate, dataRemaining;


    WavInputStream(FileHandle file) {
        super(file.read());
        try {
            if (this.read() != 'R' || this.read() != 'I' || this.read() != 'F' || this.read() != 'F') {
                throw new GdxRuntimeException("RIFF header not found: " + file);
            }

            this.skipFully(4);

            if (this.read() != 'W' || this.read() != 'A' || this.read() != 'V' || this.read() != 'E') {
                throw new GdxRuntimeException("Invalid wave file header: " + file);
            }

            final int fmtChunkLength = this.seekToChunk('f', 'm', 't', ' ');

            final int type = this.read() & 0xff | (this.read() & 0xff) << 8;
            if (type != 1) {
                throw new GdxRuntimeException("WAV files must be PCM: " + type);
            }

            this.channels = this.read() & 0xff | (this.read() & 0xff) << 8;
            if (this.channels != 1 && this.channels != 2) {
                throw new GdxRuntimeException("WAV files must have 1 or 2 channels: " + this.channels);
            }

            this.sampleRate = this.read() & 0xff | (this.read() & 0xff) << 8 | (this.read() & 0xff) << 16 | (this.read() & 0xff) << 24;

            this.skipFully(6);

            final int bitsPerSample = this.read() & 0xff | (this.read() & 0xff) << 8;
            if (bitsPerSample != 16) {
                throw new GdxRuntimeException("WAV files must have 16 bits per sample: " + bitsPerSample);
            }

            this.skipFully(fmtChunkLength - 16);

            this.dataRemaining = this.seekToChunk('d', 'a', 't', 'a');
        } catch (final Throwable ex) {
            StreamUtils.closeQuietly(this);
            throw new GdxRuntimeException("Error reading WAV file: " + file, ex);
        }
    }


    private int seekToChunk(char c1, char c2, char c3, char c4) throws IOException {
        while (true) {
            boolean found = this.read() == c1;
            found &= this.read() == c2;
            found &= this.read() == c3;
            found &= this.read() == c4;
            final int chunkLength = this.read() & 0xff | (this.read() & 0xff) << 8 | (this.read() & 0xff) << 16 | (this.read() & 0xff) << 24;
            if (chunkLength == -1) {
                throw new IOException("Chunk not found: " + c1 + c2 + c3 + c4);
            }
            if (found) {
                return chunkLength;
            }
            this.skipFully(chunkLength);
        }
    }


    private void skipFully(int count) throws IOException {
        while (count > 0) {
            final long skipped = this.in.skip(count);
            if (skipped <= 0) {
                throw new EOFException("Unable to skip.");
            }
            count -= skipped;
        }
    }


    @Override
    public int read(byte[] buffer) throws IOException {
        if (this.dataRemaining == 0) {
            return -1;
        }
        int offset = 0;
        do {
            final int length = Math.min(super.read(buffer, offset, buffer.length - offset), this.dataRemaining);
            if (length == -1) {
                if (offset > 0) {
                    return offset;
                }
                return -1;
            }
            offset += length;
            this.dataRemaining -= length;
        } while (offset < buffer.length);
        return offset;
    }
}
