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

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class Aiff16BitDecoder implements AiffDecoder {
    private final int    bitsPerSample;
    private InputStream  stream;
    private long         inputBytesRemaining;
    private final byte[] buffer       = new byte[4096];
    private int          bufferOffset = 0;
    private int          bufferLength = 0;


    public Aiff16BitDecoder(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        inputBytesRemaining = streamLength;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance

        if (inputBytesRemaining <= 0) {
            return -1;
        }

        int writeOffset = 0;

        while (writeOffset < output.length) {
            if (bufferOffset >= bufferLength - 1) {
                bufferLength = fillBuffer();
                if (bufferLength < 2) {
                    return writeOffset == 0 ? -1 : writeOffset;
                }
            }

            while (bufferOffset + 1 < bufferLength && writeOffset < output.length - 1) {
                final byte byte1 = buffer[bufferOffset++];
                final byte byte2 = buffer[bufferOffset++];
                output[writeOffset++] = byte2;
                output[writeOffset++] = byte1;
                inputBytesRemaining -= 2;
                if (inputBytesRemaining <= 0) {
                    return writeOffset;
                }
            }
        }

        return writeOffset;
    }


    private int fillBuffer() throws IOException {
        bufferOffset = 0;
        return stream.read(buffer, 0, buffer.length);
    }


    @Override
    public int inputBitsPerSample() {
        return bitsPerSample;
    }


    @Override
    public int outputBitsPerSample() {
        return 16;
    }


    @Override
    public PcmDataType outputPcmDataType() {
        return PcmDataType.INTEGER;
    }


    @Override
    public void close() throws IOException {
        stream.close();
    }

}
