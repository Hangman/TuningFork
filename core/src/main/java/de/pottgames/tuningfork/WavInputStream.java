/*******************************************************************************
 * Copyright 2011 See AUTHORS file. https://github.com/libgdx/libgdx/blob/master/AUTHORS
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package de.pottgames.tuningfork;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.logger.TuningForkLogger;

public class WavInputStream extends FilterInputStream implements AudioStream {
    private final TuningForkLogger logger;
    int                            channels;
    int                            sampleRate;
    int                            dataRemaining;
    private int                    bitsPerSample;
    private boolean                closed = false;


    WavInputStream(InputStream input, String fileName) {
        super(input);
        this.logger = Audio.get().logger;
        this.initialRead(fileName);
    }


    public WavInputStream(FileHandle file) {
        super(file.read());
        this.logger = Audio.get().logger;
        this.initialRead(file.toString());
    }


    private void initialRead(String fileName) {
        try {
            if (this.read() != 'R' || this.read() != 'I' || this.read() != 'F' || this.read() != 'F') {
                throw new GdxRuntimeException("RIFF header not found: " + fileName);
            }

            this.skipFully(4);

            if (this.read() != 'W' || this.read() != 'A' || this.read() != 'V' || this.read() != 'E') {
                throw new GdxRuntimeException("Invalid wave file header: " + fileName);
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

            this.bitsPerSample = this.read() & 0xff | (this.read() & 0xff) << 8;

            this.skipFully(fmtChunkLength - 16);

            this.dataRemaining = this.seekToChunk('d', 'a', 't', 'a');
        } catch (final Throwable ex) {
            StreamUtils.closeQuietly(this);
            throw new GdxRuntimeException("Error reading WAV file: " + fileName, ex);
        }
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
        return this.bitsPerSample;
    }


    public int getRemainingByteCount() {
        return this.dataRemaining;
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
    public int read(byte[] buffer) {
        if (this.dataRemaining == 0) {
            return -1;
        }
        int offset = 0;
        do {
            int length = 0;
            try {
                length = Math.min(super.read(buffer, offset, buffer.length - offset), this.dataRemaining);
            } catch (final IOException e) {
                throw new TuningForkRuntimeException(e);
            }
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


    @Override
    public void close() {
        try {
            super.close();
        } catch (final IOException e) {
            // ignore but log it
            this.logger.error(this.getClass(), "WavInputStream was not successfully closed: " + e.getMessage());
        } finally {
            this.closed = true;
        }
    }


    @Override
    public boolean isClosed() {
        return this.closed;
    }

}
