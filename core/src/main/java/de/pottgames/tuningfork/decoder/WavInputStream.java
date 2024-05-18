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

package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * An {@link AudioStream} implementation to read wav files.
 *
 * @author Matthias
 */
public class WavInputStream implements AudioStream {
    private final InputStream      stream;
    private WavDecoder             decoder;
    private final TuningForkLogger logger;
    private final FileHandle       file;
    private final float            duration;
    private boolean                closed = false;


    /**
     * Initializes a {@link WavInputStream} from a {@link FileHandle}.
     *
     * @param file the file handle
     */
    public WavInputStream(FileHandle file) {
        this(file, true);
    }


    /**
     * Initializes a {@link WavInputStream} from a {@link FileHandle}.
     *
     * @param file the file handle
     * @param forStreaming true if this will be used for streaming
     */
    public WavInputStream(FileHandle file, boolean forStreaming) {
        this.stream = file.read();
        this.file = file;
        this.logger = Audio.get().getLogger();
        this.setup(forStreaming);
        this.duration = (float) this.totalSamplesPerChannel() / this.getSampleRate();
    }


    /**
     * Initializes a {@link WavInputStream} from an {@link InputStream}. This stream does not support the reset function. Use
     * {@link #WavInputStream(FileHandle)} instead to get the full functionality.
     *
     * @param stream the input stream
     */
    public WavInputStream(InputStream stream) {
        this(stream, true);
    }


    /**
     * Initializes a {@link WavInputStream} from an {@link InputStream}. This stream does not support the reset function. Use
     * {@link #WavInputStream(FileHandle)} instead to get the full functionality.
     *
     * @param stream the input stream
     * @param forStreaming true if this will be used for streaming
     */
    public WavInputStream(InputStream stream, boolean forStreaming) {
        this.stream = stream;
        this.file = null;
        this.logger = Audio.get().getLogger();
        this.setup(forStreaming);
        this.duration = (float) this.totalSamplesPerChannel() / this.getSampleRate();
    }


    private void setup(boolean forStreaming) {
        this.readRiffChunk();
        final WavFmtChunk fmtChunk = this.readFmtChunk();

        final long bytesRemaining = this.skipToChunk('d', 'a', 't', 'a');
        if (bytesRemaining < 0L) {
            this.throwRuntimeError("Not a valid wav file, audio data not found");
        }

        // FIND DECODER
        final WavDecoderProvider provider = Audio.get().getWavDecoderProvider();
        this.decoder = provider.getDecoder(fmtChunk, forStreaming);
        if (this.decoder == null) {
            this.throwRuntimeError("Unsupported wav file format");
        }
        this.decoder.setup(this.stream, bytesRemaining);
        if (PcmFormat.determineFormat(this.decoder.outputChannels(), this.decoder.outputBitsPerSample(), this.decoder.outputPcmDataType()) == null) {
            this.throwRuntimeError("Unsupported format found in wav file");
        }
    }


    private void readRiffChunk() {
        try {
            // RIFF LITERAL
            final boolean riff = this.stream.read() == 'R' && this.stream.read() == 'I' && this.stream.read() == 'F' && this.stream.read() == 'F';
            if (!riff) {
                this.throwRuntimeError("Not a valid wav file, RIFF header missing");
            }

            // CHUNK SIZE
            final long fileSizeMinus8 = this.read4Bytes();
            if (fileSizeMinus8 < 0) {
                this.throwRuntimeError("Invalid wav file, unexpected end of file");
            }

            // WAVE LITERAL
            final boolean wave = this.stream.read() == 'W' && this.stream.read() == 'A' && this.stream.read() == 'V' && this.stream.read() == 'E';
            if (!wave) {
                this.throwRuntimeError("Not a valid wav file, WAVE literal missing");
            }
        } catch (final IOException e) {
            this.throwRuntimeError("An error occured while reading the wav file", e);
        }
    }


    private WavFmtChunk readFmtChunk() {
        try {
            // FMT LITERAL
            final boolean fmt = this.stream.read() == 'f' && this.stream.read() == 'm' && this.stream.read() == 't' && this.stream.read() == ' ';
            if (!fmt) {
                this.throwRuntimeError("Not a valid wav file, FMT  header missing");
            }

            // SUB CHUNK SIZE
            final int chunkSize = (int) this.read4Bytes();
            if (chunkSize < 0) {
                this.throwRuntimeError("Invalid wav file, unexpected end of file");
            }

            final int[] chunkData = new int[chunkSize];
            for (int i = 0; i < chunkSize; i++) {
                chunkData[i] = this.stream.read();
            }

            return new WavFmtChunk(chunkData);
        } catch (final IOException e) {
            this.throwRuntimeError("An error occured while reading the wav file", e);
        }

        return null;
    }


    private long read4Bytes() throws IOException {
        final int byte1 = this.stream.read();
        final int byte2 = this.stream.read();
        final int byte3 = this.stream.read();
        final int byte4 = this.stream.read();
        if (byte1 < 0 || byte2 < 0 || byte3 < 0 || byte4 < 0) {
            return -1L;
        }
        return byte1 | byte2 << 8L | byte3 << 16L | (long) byte4 << 24L;
    }


    private void skipBytes(long n) throws IOException {
        while (n > 0) {
            final long skipped = this.stream.skip(n);
            if (skipped <= 0) {
                this.throwRuntimeError("An error occured while reading wav file");
            }
            n -= skipped;
        }
    }


    private long skipToChunk(char byte1, char byte2, char byte3, char byte4) {
        long chunkSize = -1L;
        try {
            while (true) {
                // READ CHUNK ID
                final int read1 = this.stream.read();
                final int read2 = this.stream.read();
                final int read3 = this.stream.read();
                final int read4 = this.stream.read();
                if (read1 < 0 || read2 < 0 || read3 < 0 || read4 < 0) {
                    return -1L;
                }

                // CHECK IF FOUND
                final boolean foundChunk = read1 == byte1 && read2 == byte2 && read3 == byte3 && read4 == byte4;
                chunkSize = this.read4Bytes();
                if (chunkSize < 0) {
                    return -1L;
                }
                if (foundChunk) {
                    return chunkSize;
                }

                // NOT FOUND, SKIP TO NEXT CHUNK
                this.skipBytes(chunkSize);
            }
        } catch (final IOException e) {
            this.throwRuntimeError("An error occured while reading the wav file", e);
        }
        return chunkSize;
    }


    @Override
    public int read(byte[] bytes) {
        try {
            return this.decoder.read(bytes);
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    @Override
    public float getDuration() {
        return this.duration;
    }


    @Override
    public AudioStream reset() {
        if (this.file == null) {
            throw new TuningForkRuntimeException("This AudioStream doesn't support resetting.");
        }
        StreamUtils.closeQuietly(this);
        return new WavInputStream(this.file);
    }


    public long totalSamplesPerChannel() {
        return this.decoder.outputTotalSamplesPerChannel();
    }


    @Override
    public int getChannels() {
        return this.decoder.outputChannels();
    }


    @Override
    public int getSampleRate() {
        return this.decoder.outputSampleRate();
    }


    @Override
    public int getBitsPerSample() {
        return this.decoder.outputBitsPerSample();
    }


    @Override
    public PcmDataType getPcmDataType() {
        return this.decoder.outputPcmDataType();
    }


    @Override
    public int getBlockAlign() {
        return this.decoder.blockAlign();
    }


    @Override
    public int getBlockSize() {
        return this.decoder.blockSize();
    }


    public long bytesRemaining() {
        return this.decoder.bytesRemaining();
    }


    private void throwRuntimeError(String message) {
        this.throwRuntimeError(message, null);
    }


    private void throwRuntimeError(String message, Exception e) {
        if (e == null) {
            throw new TuningForkRuntimeException(message + ": " + this.file.toString());
        }
        throw new TuningForkRuntimeException(message + ". " + e.getMessage() + ": " + this.file.toString(), e);
    }


    @Override
    public boolean isClosed() {
        return this.closed;
    }


    @Override
    public void close() throws IOException {
        try {
            if (this.decoder != null) {
                this.decoder.close();
            } else {
                this.stream.close();
            }
        } catch (final IOException e) {
            // ignore but log it
            this.logger.error(this.getClass(), "WavInputStream didn't close successfully: " + e.getMessage());
        } finally {
            this.closed = true;
        }
    }

}
