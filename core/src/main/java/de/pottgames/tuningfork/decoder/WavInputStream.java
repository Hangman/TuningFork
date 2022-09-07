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
    private final InputStream      stream;
    private int                    audioFormat;
    private WavDecoder             decoder;
    private final TuningForkLogger logger;
    private final String           fileName;
    private int                    channels;
    private int                    sampleRate;
    private int                    bitsPerSample;
    private long                   totalSamples;
    private boolean                closed = false;


    public WavInputStream(InputStream input) {
        this.stream = input;
        this.fileName = null;
        this.logger = Audio.get().getLogger();
        try {
            this.setup();
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    public WavInputStream(FileHandle file) {
        this.stream = file.read();
        this.fileName = file.toString();
        this.logger = Audio.get().getLogger();
        try {
            this.setup();
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    private void setup() throws IOException {
        this.readRiffChunk();
        this.readFmtChunk();
        final long bytesRemaining = this.skipToChunk('d', 'a', 't', 'a');
        if (bytesRemaining < 0L) {
            this.throwRuntimeError("Not a valid wav file, audio data not found");
        }
        this.totalSamples = bytesRemaining * 8L / this.bitsPerSample / this.channels;

        // FIND DECODER
        final WavDecoderProvider provider = Audio.get().getWavDecoderProvider();
        this.decoder = provider.getDecoder(this.bitsPerSample, this.audioFormat);
        if (this.decoder == null) {
            this.throwRuntimeError("Unsupported wav file format");
        }
        this.decoder.setup(this.stream, bytesRemaining);
        this.bitsPerSample = this.decoder.outputBitsPerSample();
        if (PcmFormat.determineFormat(this.channels, this.bitsPerSample, this.decoder.getPcmDataType()) == null) {
            this.throwRuntimeError("Unsupported format found in wav file");
        }
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
        this.audioFormat = this.stream.read() | this.stream.read() << 8;
        chunkSize -= 2L;

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
        chunkSize -= 2L;

        if (this.audioFormat == WavAudioFormat.WAVE_FORMAT_EXTENSIBLE.getRegNumber()) {
            final int cbSize = this.stream.read() | this.stream.read() << 8;
            if (cbSize != 22) {
                this.throwRuntimeError("Invalid wav file, EXTENSIBLE format is used, cbSize must be 22");
            }
            chunkSize -= 2L;

            // SKIP VALID BITS PER SAMPLE
            this.stream.read();
            this.stream.read();
            chunkSize -= 2L;

            // SKIP CHANNEL MASK
            this.stream.read();
            this.stream.read();
            this.stream.read();
            this.stream.read();
            chunkSize -= 4L;

            // AUDIO FORMAT
            this.audioFormat = this.stream.read() | this.stream.read() << 8;
            chunkSize -= 2L;

            // FIXED GUID STRING
            final boolean guid1 = this.stream.read() == 0x00;
            final boolean guid2 = this.stream.read() == 0x00;
            final boolean guid3 = this.stream.read() == 0x00;
            final boolean guid4 = this.stream.read() == 0x00;
            final boolean guid5 = this.stream.read() == 0x10;
            final boolean guid6 = this.stream.read() == 0x00;
            final boolean guid7 = this.stream.read() == 0x80;
            final boolean guid8 = this.stream.read() == 0x00;
            final boolean guid9 = this.stream.read() == 0x00;
            final boolean guid10 = this.stream.read() == 0xAA;
            final boolean guid11 = this.stream.read() == 0x00;
            final boolean guid12 = this.stream.read() == 0x38;
            final boolean guid13 = this.stream.read() == 0x9b;
            final boolean guid14 = this.stream.read() == 0x71;
            final boolean valid = guid1 && guid2 && guid3 && guid4 && guid5 && guid6 && guid7 && guid8 && guid9 && guid10 && guid11 && guid12 && guid13
                    && guid14;
            if (!valid) {
                this.throwRuntimeError("Invalid wav file, EXTENSIBLE format header is incorrect");
            }
            chunkSize -= 14L;

            this.logger.debug(this.getClass(), (this.fileName != null ? this.fileName : this.stream.toString())
                    + " uses the EXTENSIBLE format which is only partly supported by TuningFork.");
        }

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
        try {
            return this.decoder.read(bytes);
        } catch (final Exception e) {
            this.throwRuntimeError("An error occured while reading wav file", e);
        }
        return -1;
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
        return this.decoder.getPcmDataType();
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
