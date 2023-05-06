/**
 * Copyright 2023 Matthias Finke
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
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.decoder.LawDecoder.Encoding;
import de.pottgames.tuningfork.decoder.util.Util;
import de.pottgames.tuningfork.logger.TuningForkLogger;

public class AiffInputStream implements AudioStream {
    private final InputStream      stream;
    private AiffDecoder            decoder;
    private final TuningForkLogger logger;
    private final FileHandle       file;
    private int                    channels          = 0;
    private int                    sampleRate        = 0;
    private long                   totalSampleFrames = 0;
    private int                    inputBitsPerSample;
    private float                  duration          = -1f;
    private boolean                closed            = false;
    private String                 compressionId     = "NONE";


    /**
     * Initializes a {@link AiffInputStream} from a {@link FileHandle}.
     *
     * @param file
     */
    public AiffInputStream(FileHandle file) {
        this.stream = file.read();
        this.file = file;
        this.logger = Audio.get().getLogger();
        try {
            this.setup();
        } catch (final IOException e) {
            this.throwRuntimeError("Error reading aiff file", e);
        }
    }


    /**
     * Initializes a {@link AiffInputStream} from an {@link InputStream}. This stream does not support the reset function. Use
     * {@link #AiffInputStream(FileHandle)} instead to get the full functionality.
     *
     * @param stream
     */
    public AiffInputStream(InputStream stream) {
        this.stream = stream;
        this.file = null;
        this.logger = Audio.get().getLogger();
        try {
            this.setup();
        } catch (final IOException e) {
            this.throwRuntimeError("Error reading aiff file", e);
        }
    }


    private void setup() throws IOException {
        final boolean aifc = this.readFormChunk();
        this.readCommChunk(aifc);

        // SKIP TO SOUND DATA CHUNK
        final long dataChunkSize = this.skipToChunk('S', 'S', 'N', 'D');
        final long offset = this.readUnsignedLong();
        final long blockSize = this.readUnsignedLong();
        if (blockSize > 0) {
            this.logger.warn(this.getClass(), "This aiff file uses block-aligned sound data, which TuningFork does not fully support.");
        }
        this.skip(offset);

        // SETUP DECODER
        int inputBytesPerSample = 0;
        if (this.inputBitsPerSample == 64) {
            inputBytesPerSample = 8;
        } else if (this.inputBitsPerSample > 24) {
            inputBytesPerSample = 4;
        } else if (this.inputBitsPerSample > 16) {
            inputBytesPerSample = 3;
        } else if (this.inputBitsPerSample > 8) {
            inputBytesPerSample = 2;
        } else if (this.inputBitsPerSample <= 8) {
            inputBytesPerSample = 1;
        }
        if ("NONE".equalsIgnoreCase(this.compressionId)) {
            switch (inputBytesPerSample) {
                case 1:
                    this.decoder = new Aiff8BitDecoder(this.inputBitsPerSample);
                    break;
                case 2:
                    this.decoder = new Aiff16BitDecoder(this.inputBitsPerSample);
                    break;
                case 3:
                    this.decoder = new Aiff24BitDecoder(this.inputBitsPerSample);
                    break;
                case 4:
                    this.decoder = new Aiff32BitDecoder(this.inputBitsPerSample);
                    break;
            }
        } else if ("alaw".equalsIgnoreCase(this.compressionId) && inputBytesPerSample == 1) {
            this.decoder = new LawDecoder(this.channels, this.sampleRate, Encoding.A_LAW, true);
        } else if ("ulaw".equalsIgnoreCase(this.compressionId) && inputBytesPerSample == 1) {
            this.decoder = new LawDecoder(this.channels, this.sampleRate, Encoding.U_LAW, true);
        } else if ("FL32".equalsIgnoreCase(this.compressionId) && inputBytesPerSample == 4) {
            this.decoder = new Aiff32BitFloatDecoder();
        } else if ("FL64".equalsIgnoreCase(this.compressionId) && inputBytesPerSample == 8) {
            this.decoder = new Aiff64BitFloatDecoder();
        }

        if (this.decoder == null) {
            this.throwRuntimeError(
                    "Unsupported aiff format: bits per sample (" + this.inputBitsPerSample + ")" + ", compression type (" + this.compressionId + ")");
        }
        this.decoder.setup(this.stream, dataChunkSize - 8 - offset);
    }


    private void readCommChunk(boolean aifc) throws IOException {
        final int commChunkSize = this.skipToChunk('C', 'O', 'M', 'M');
        if (!aifc && commChunkSize != 18 || aifc && commChunkSize < 22) {
            this.throwRuntimeError("Not a valid aiff file, COMM chunk not found");
        }

        this.channels = this.readShort();
        this.totalSampleFrames = this.readUnsignedLong();
        this.inputBitsPerSample = this.readShort();
        this.sampleRate = (int) this.readExtendedPrecision();
        if (aifc) {
            final char char1 = (char) this.stream.read();
            final char char2 = (char) this.stream.read();
            final char char3 = (char) this.stream.read();
            final char char4 = (char) this.stream.read();
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(char1).append(char2).append(char3).append(char4);
            this.compressionId = stringBuilder.toString();

            this.readPString();
        }

        this.duration = (float) this.totalSampleFrames / (float) this.sampleRate;

        this.endChunk(commChunkSize);
    }


    private short readShort() throws IOException {
        final int byte1 = this.stream.read();
        final int byte2 = this.stream.read();
        return (short) (byte1 << 8 | byte2);
    }


    private boolean readFormChunk() throws IOException {
        // FORM CHUNK ID
        final boolean form = this.stream.read() == 'F' && this.stream.read() == 'O' && this.stream.read() == 'R' && this.stream.read() == 'M';
        if (!form) {
            this.throwRuntimeError("Not a valid aiff file, FORM container chunk missing");
        }

        // SKIP CHUNK SIZE
        this.stream.read();
        this.stream.read();
        this.stream.read();
        this.stream.read();

        // FORM TYPE
        final int char1 = this.stream.read();
        final int char2 = this.stream.read();
        final int char3 = this.stream.read();
        final int char4 = this.stream.read();
        final boolean aiff = char1 == 'A' && char2 == 'I' && char3 == 'F' && char4 == 'F';
        final boolean aifc = char1 == 'A' && char2 == 'I' && char3 == 'F' && char4 == 'C';
        if (!aiff && !aifc) {
            this.throwRuntimeError("Not a valid aiff file, FORM type is not AIFF or AIFC");
        }

        return aifc;
    }


    private int skipToChunk(char byte1, char byte2, char byte3, char byte4) throws IOException {
        int chunkSize = -1;
        while (chunkSize < 0) {
            final char read1 = (char) this.stream.read();
            if (read1 != byte1) {
                continue;
            }
            if (read1 < 0) {
                this.throwRuntimeError("Not a valid aiff file, unexpected end of file");
            }
            final char read2 = (char) this.stream.read();
            if (read2 != byte2) {
                continue;
            }
            final char read3 = (char) this.stream.read();
            if (read3 != byte3) {
                continue;
            }
            final char read4 = (char) this.stream.read();
            if (read4 != byte4) {
                continue;
            }
            chunkSize = this.readLong();
        }
        return chunkSize;
    }


    private void skip(long bytes) throws IOException {
        for (int i = 0; i < bytes; i++) {
            this.stream.read();
        }
    }


    public void endChunk(long chunkSize) throws IOException {
        if (Util.isOdd(chunkSize)) {
            this.stream.read();
        }
    }


    @Override
    public int read(byte[] bytes) {
        try {
            return this.decoder.read(bytes);
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    public long totalSamplesPerChannel() {
        return this.totalSampleFrames;
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
        return new AiffInputStream(this.file);
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
        return this.decoder.outputBitsPerSample();
    }


    @Override
    public PcmDataType getPcmDataType() {
        return this.decoder.outputPcmDataType();
    }


    @Override
    public boolean isClosed() {
        return this.closed;
    }


    private int readLong() throws IOException {
        return this.stream.read() << 24 | this.stream.read() << 16 | this.stream.read() << 8 | this.stream.read();
    }


    private long readUnsignedLong() throws IOException {
        return (long) this.stream.read() << 24 | this.stream.read() << 16 | this.stream.read() << 8 | this.stream.read();
    }


    private String readPString() throws IOException {
        final int stringLength = this.stream.read();

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stringLength; i++) {
            builder.append((char) this.stream.read());
        }

        // XXX pad byte should be included even if stringLength is 0 according to the specification,
        // but the Audacity encoder doesn't do this?!
        // may also be a bug in this code I haven't thought about yet
        if (stringLength > 0 && Util.isEven(stringLength)) {
            this.stream.read();
        }

        return builder.toString();
    }


    private float readExtendedPrecision() throws IOException {
        final int byte1 = this.stream.read();
        final int byte2 = this.stream.read();
        final int byte3 = this.stream.read();
        final int byte4 = this.stream.read();
        final int byte5 = this.stream.read();
        final int byte6 = this.stream.read();
        final int byte7 = this.stream.read();
        final int byte8 = this.stream.read();
        final int byte9 = this.stream.read();
        final int byte10 = this.stream.read();
        final boolean sign = byte1 >>> 7 != 0;
        final int exponent = (byte1 & 0b01111111) << 8 | byte2;
        final long fraction = (long) byte3 << 56L | (long) byte4 << 48 | (long) byte5 << 40 | (long) byte6 << 32 | (long) byte7 << 24 | (long) byte8 << 16
                | (long) byte9 << 8 | byte10;

        // conversion to java float
        final int floatExponent = exponent - 16383 + 127;
        final long longFraction = fraction << 1 >>> 64 - 23;
        final int floatFraction = (int) longFraction;
        final int shiftedSignBit = (sign ? 1 : 0) << 31;
        final int shiftedExponent = floatExponent << 23;
        final int rawVal = floatFraction | shiftedExponent | shiftedSignBit;
        return Float.intBitsToFloat(rawVal);
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
    public void close() throws IOException {
        try {
            if (this.decoder != null) {
                this.decoder.close();
            } else {
                this.stream.close();
            }
        } catch (final IOException e) {
            // ignore but log it
            this.logger.error(this.getClass(), "AiffInputStream didn't close successfully: " + e.getMessage());
        } finally {
            this.closed = true;
        }
    }

}
