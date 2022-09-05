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

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.PcmFormat;
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.logger.TuningForkLogger;

public class WavInputStream implements AudioStream {
    private static final int       FORMAT_PCM        = 0x1;
    private static final int       FORMAT_FLOAT      = 0x3;
    private static final int       FORMAT_EXTENSIBLE = 0xfffe;
    private final InputStream      stream;
    private final TuningForkLogger logger;
    private final String           fileName;
    private int                    channels;
    private int                    sampleRate;
    private int                    bitsPerSample;
    private PcmDataType            pcmDataType;
    private long                   bytesRemaining;
    private long                   totalSamples;
    private boolean                closed            = false;


    public WavInputStream(InputStream input) {
        this.stream = input;
        this.fileName = null;
        this.logger = Audio.get().getLogger();
        try {
            this.readHeader();
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    public WavInputStream(FileHandle file) {
        this.stream = file.read();
        this.fileName = file.toString();
        this.logger = Audio.get().getLogger();
        try {
            this.readHeader();
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    private void readHeader() throws IOException {
        this.readRiffChunk();
        this.readFmtChunk();
        this.bytesRemaining = this.skipToChunk('d', 'a', 't', 'a');
        if (this.bytesRemaining < 0L) {
            this.throwRuntimeError("Not a valid wav file, audio data not found");
        }
        this.totalSamples = this.bytesRemaining * 8L / this.bitsPerSample / this.channels;
    }


    private void readRiffChunk() throws IOException {
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
    }


    private void readFmtChunk() throws IOException {
        // FMT LITERAL
        final boolean fmt = this.stream.read() == 'f' && this.stream.read() == 'm' && this.stream.read() == 't' && this.stream.read() == ' ';
        if (!fmt) {
            this.throwRuntimeError("Not a valid wav file, FMT  header missing");
        }

        // SUB CHUNK SIZE
        long chunkSize = this.read4Bytes();
        if (chunkSize < 0) {
            this.throwRuntimeError("Invalid wav file, unexpected end of file");
        }

        // AUDIO FORMAT
        final int audioFormat = this.stream.read() | this.stream.read() << 8;
        if (audioFormat != WavInputStream.FORMAT_PCM && audioFormat != WavInputStream.FORMAT_EXTENSIBLE && audioFormat != WavInputStream.FORMAT_FLOAT) {
            this.throwRuntimeError("Only uncompressed (PCM) wav files are supported, this file seems to hold compressed data");
        }
        chunkSize -= 2L;
        if (audioFormat == WavInputStream.FORMAT_FLOAT) {
            this.pcmDataType = PcmDataType.FLOAT;
        } else {
            this.pcmDataType = PcmDataType.INTEGER;
        }

        // NUMBER OF CHANNELS
        this.channels = this.stream.read() | this.stream.read() << 8;
        if (!PcmFormat.isSupportedChannelCount(this.channels)) {
            this.throwRuntimeError(
                    "Unsupported number of channels in wav file: " + this.channels + ", TuningFork only supports (" + PcmFormat.CHANNELS_STRING + ") channels");
        }
        chunkSize -= 2L;

        // SAMPLE RATE
        this.sampleRate = (int) this.read4Bytes();
        if (this.sampleRate < 0) {
            this.throwRuntimeError("Invalid wav file, unexpected end of file");
        }
        chunkSize -= 4;

        // BYTE RATE & BLOCK ALIGN
        this.skipBytes(6L);
        chunkSize -= 6L;

        // BITS PER SAMPLE
        this.bitsPerSample = this.stream.read() | this.stream.read() << 8;
        if (!PcmFormat.isSupportedBitRate(this.bitsPerSample)) {
            this.throwRuntimeError("Unsupported bits per sample in wav file: " + this.bitsPerSample + ", TuningFork only supports ("
                    + PcmFormat.BITS_PER_SAMPLE_STRING + ") bitrates");
        }
        if (PcmFormat.determineFormat(this.channels, this.bitsPerSample, this.pcmDataType) == null) {
            this.throwRuntimeError("Unsupported format (supported by TuningFork: " + PcmFormat.NAMES_STRING + ") in wav file");
        }
        chunkSize -= 2L;

        // SKIP TO END OF CHUNK
        this.skipBytes(chunkSize);
    }


    private long read4Bytes() throws IOException {
        final int byte1 = this.stream.read();
        final int byte2 = this.stream.read();
        final int byte3 = this.stream.read();
        final int byte4 = this.stream.read();
        if (byte1 < 0 || byte2 < 0 || byte3 < 0 || byte4 < 0) {
            return -1L;
        }
        return byte1 | byte2 << 8L | byte3 << 16L | byte4 << 24L;
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


    private long skipToChunk(char byte1, char byte2, char byte3, char byte4) throws IOException {
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
            final long chunkSize = this.read4Bytes();
            if (chunkSize < 0) {
                return -1L;
            }
            if (foundChunk) {
                return chunkSize;
            }

            // NOT FOUND, SKIP TO NEXT CHUNK
            this.skipBytes(chunkSize);
        }
    }


    @Override
    public int read(byte[] bytes) {
        if (this.bytesRemaining == 0) {
            return -1;
        }

        int bytesToRead = bytes.length;
        int offset = 0;

        while (bytesToRead > 0 && this.bytesRemaining > 0) {
            int bytesRead = 0;
            try {
                bytesRead = this.stream.read(bytes, offset, (int) Math.min(bytesToRead, this.bytesRemaining));
            } catch (final IOException e) {
                this.throwRuntimeError("An error occured while reading wav file", e);
            }
            if (bytesRead == -1) {
                if (offset > 0) {
                    return offset;
                }
                return -1;
            }
            this.bytesRemaining -= bytesRead;
            bytesToRead -= bytesRead;
            offset += bytesRead;
        }

        return offset;
    }


    public long totalSamples() {
        return this.totalSamples;
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


    @Override
    public PcmDataType getPcmDataType() {
        return this.pcmDataType;
    }


    private void throwRuntimeError(String message) {
        this.throwRuntimeError(message, null);
    }


    private void throwRuntimeError(String message, Exception e) {
        if (e == null) {
            if (this.fileName != null) {
                throw new TuningForkRuntimeException(message + ": " + this.fileName);
            }
            throw new TuningForkRuntimeException(message + ": " + this.stream.toString());
        }
        if (this.fileName != null) {
            throw new TuningForkRuntimeException(message + ": " + this.fileName, e);
        }
        throw new TuningForkRuntimeException(message + ": " + this.stream.toString(), e);
    }


    @Override
    public boolean isClosed() {
        return this.closed;
    }


    @Override
    public void close() throws IOException {
        try {
            this.stream.close();
        } catch (final IOException e) {
            // ignore but log it
            this.logger.error(this.getClass(), "WavInputStream didn't close successfully: " + e.getMessage());
        } finally {
            this.closed = true;
        }
    }

}
