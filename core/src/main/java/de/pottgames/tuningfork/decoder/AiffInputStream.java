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
     * @param file the file
     */
    public AiffInputStream(FileHandle file) {
        stream = file.read();
        this.file = file;
        logger = Audio.get().getLogger();
        try {
            setup();
        } catch (final IOException e) {
            this.throwRuntimeError("Error reading aiff file", e);
        }
    }


    /**
     * Initializes a {@link AiffInputStream} from an {@link InputStream}. This stream does not support the reset function. Use
     * {@link #AiffInputStream(FileHandle)} instead to get the full functionality.
     *
     * @param stream the input stream
     */
    public AiffInputStream(InputStream stream) {
        this.stream = stream;
        file = null;
        logger = Audio.get().getLogger();
        try {
            setup();
        } catch (final IOException e) {
            this.throwRuntimeError("Error reading aiff file", e);
        }
    }


    private void setup() throws IOException {
        final boolean aifc = readFormChunk();
        readCommChunk(aifc);

        // SKIP TO SOUND DATA CHUNK
        final long dataChunkSize = skipToChunk('S', 'S', 'N', 'D');
        final long offset = readUnsignedLong();
        final long blockSize = readUnsignedLong();
        if (blockSize > 0) {
            logger.warn(this.getClass(), "This aiff file uses block-aligned sound data, which TuningFork does not fully support.");
        }
        skip(offset);

        // SETUP DECODER
        int inputBytesPerSample = 0;
        if (inputBitsPerSample == 64) {
            inputBytesPerSample = 8;
        } else if (inputBitsPerSample > 24) {
            inputBytesPerSample = 4;
        } else if (inputBitsPerSample > 16) {
            inputBytesPerSample = 3;
        } else if (inputBitsPerSample > 8) {
            inputBytesPerSample = 2;
        } else {
            inputBytesPerSample = 1;
        }
        if ("NONE".equalsIgnoreCase(compressionId)) {
            switch (inputBytesPerSample) {
                case 1:
                    decoder = new Aiff8BitDecoder(inputBitsPerSample);
                    break;
                case 2:
                    decoder = new Aiff16BitDecoder(inputBitsPerSample);
                    break;
                case 3:
                    decoder = new Aiff24BitDecoder(inputBitsPerSample);
                    break;
                case 4:
                    decoder = new Aiff32BitDecoder(inputBitsPerSample);
                    break;
            }
        } else if ("alaw".equalsIgnoreCase(compressionId) && inputBytesPerSample == 1) {
            decoder = new LawDecoder(channels, sampleRate, Encoding.A_LAW, true);
        } else if ("ulaw".equalsIgnoreCase(compressionId) && inputBytesPerSample == 1) {
            decoder = new LawDecoder(channels, sampleRate, Encoding.U_LAW, true);
        } else if ("FL32".equalsIgnoreCase(compressionId) && inputBytesPerSample == 4) {
            decoder = new Aiff32BitFloatDecoder();
        } else if ("FL64".equalsIgnoreCase(compressionId) && inputBytesPerSample == 8) {
            decoder = new Aiff64BitFloatDecoder();
        }

        if (decoder == null) {
            this.throwRuntimeError("Unsupported aiff format: bits per sample (" + inputBitsPerSample + ")" + ", compression type (" + compressionId + ")");
        }
        decoder.setup(stream, dataChunkSize - 8 - offset);
    }


    private void readCommChunk(boolean aifc) throws IOException {
        final int commChunkSize = skipToChunk('C', 'O', 'M', 'M');
        if (!aifc && commChunkSize != 18 || aifc && commChunkSize < 22) {
            this.throwRuntimeError("Not a valid aiff file, COMM chunk not found");
        }

        channels = readShort();
        totalSampleFrames = readUnsignedLong();
        inputBitsPerSample = readShort();
        sampleRate = (int) readExtendedPrecision();
        if (aifc) {
            final char char1 = (char) stream.read();
            final char char2 = (char) stream.read();
            final char char3 = (char) stream.read();
            final char char4 = (char) stream.read();
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(char1).append(char2).append(char3).append(char4);
            compressionId = stringBuilder.toString();

            readPString();
        }

        duration = (float) totalSampleFrames / (float) sampleRate;

        endChunk(commChunkSize);
    }


    private short readShort() throws IOException {
        final int byte1 = stream.read();
        final int byte2 = stream.read();
        return (short) (byte1 << 8 | byte2);
    }


    private boolean readFormChunk() throws IOException {
        // FORM CHUNK ID
        final boolean form = stream.read() == 'F' && stream.read() == 'O' && stream.read() == 'R' && stream.read() == 'M';
        if (!form) {
            this.throwRuntimeError("Not a valid aiff file, FORM container chunk missing");
        }

        // SKIP CHUNK SIZE
        stream.read();
        stream.read();
        stream.read();
        stream.read();

        // FORM TYPE
        final int char1 = stream.read();
        final int char2 = stream.read();
        final int char3 = stream.read();
        final int char4 = stream.read();
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
            final int read1 = stream.read();
            if ((char) read1 != byte1) {
                continue;
            }
            if (read1 < 0) {
                this.throwRuntimeError("Not a valid aiff file, unexpected end of file");
            }
            final char read2 = (char) stream.read();
            if (read2 != byte2) {
                continue;
            }
            final char read3 = (char) stream.read();
            if (read3 != byte3) {
                continue;
            }
            final char read4 = (char) stream.read();
            if (read4 != byte4) {
                continue;
            }
            chunkSize = readLong();
        }
        return chunkSize;
    }


    private void skip(long bytes) throws IOException {
        for (int i = 0; i < bytes; i++) {
            stream.read();
        }
    }


    public void endChunk(long chunkSize) throws IOException {
        if (Util.isOdd(chunkSize)) {
            stream.read();
        }
    }


    @Override
    public int read(byte[] bytes) {
        try {
            return decoder.read(bytes);
        } catch (final IOException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    public long totalSamplesPerChannel() {
        return totalSampleFrames;
    }


    @Override
    public float getDuration() {
        return duration;
    }


    @Override
    public AudioStream reset() {
        if (file == null) {
            throw new TuningForkRuntimeException("This AudioStream doesn't support resetting.");
        }
        StreamUtils.closeQuietly(this);
        return new AiffInputStream(file);
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
        return decoder.outputBitsPerSample();
    }


    @Override
    public PcmDataType getPcmDataType() {
        return decoder.outputPcmDataType();
    }


    @Override
    public boolean isClosed() {
        return closed;
    }


    private int readLong() throws IOException {
        return stream.read() << 24 | stream.read() << 16 | stream.read() << 8 | stream.read();
    }


    private long readUnsignedLong() throws IOException {
        return (long) stream.read() << 24 | stream.read() << 16 | stream.read() << 8 | stream.read();
    }


    private String readPString() throws IOException {
        final int stringLength = stream.read();

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stringLength; i++) {
            builder.append((char) stream.read());
        }

        // XXX pad byte should be included even if stringLength is 0 according to the specification,
        // but the Audacity encoder doesn't do this?!
        // may also be a bug in this code I haven't thought about yet
        if (stringLength > 0 && Util.isEven(stringLength)) {
            stream.read();
        }

        return builder.toString();
    }


    private float readExtendedPrecision() throws IOException {
        final int byte1 = stream.read();
        final int byte2 = stream.read();
        final int byte3 = stream.read();
        final int byte4 = stream.read();
        final int byte5 = stream.read();
        final int byte6 = stream.read();
        final int byte7 = stream.read();
        final int byte8 = stream.read();
        final int byte9 = stream.read();
        final int byte10 = stream.read();
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
            throw new TuningForkRuntimeException(message + ": " + file.toString());
        }
        throw new TuningForkRuntimeException(message + ". " + e.getMessage() + ": " + file.toString(), e);
    }


    @Override
    public void close() throws IOException {
        try {
            if (decoder != null) {
                decoder.close();
            } else {
                stream.close();
            }
        } catch (final IOException e) {
            // ignore but log it
            logger.error(this.getClass(), "AiffInputStream didn't close successfully: " + e.getMessage());
        } finally {
            closed = true;
        }
    }

}
