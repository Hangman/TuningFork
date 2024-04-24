/**
 * Copyright 2023 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.decoder;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

import java.io.IOException;
import java.io.InputStream;

public class Aiff64BitFloatDecoder implements AiffDecoder {
    private       InputStream stream;
    private       long        inputBytesRemaining;
    private final byte[]      buffer       = new byte[4096];
    private       int         bufferOffset = 0;
    private       int         bufferLength = 0;


    @Override
    public void setup(InputStream stream, long streamLength) {
        this.stream = stream;
        this.inputBytesRemaining = streamLength;
    }


    @Override
    public int read(byte[] output) throws IOException {
        // we don't check if the decoder has been set up properly because this method is crucial for performance

        if (this.inputBytesRemaining <= 0) {
            return -1;
        }

        int writeOffset = 0;

        while (writeOffset < output.length) {
            if (this.bufferOffset >= this.bufferLength - 7) {
                this.bufferLength = this.fillBuffer();
                if (this.bufferLength < 8) {
                    return writeOffset == 0 ? -1 : writeOffset;
                }
            }

            while (this.bufferOffset + 7 < this.bufferLength && writeOffset < output.length - 7) {
                final byte byte1 = this.buffer[this.bufferOffset++];
                final byte byte2 = this.buffer[this.bufferOffset++];
                final byte byte3 = this.buffer[this.bufferOffset++];
                final byte byte4 = this.buffer[this.bufferOffset++];
                final byte byte5 = this.buffer[this.bufferOffset++];
                final byte byte6 = this.buffer[this.bufferOffset++];
                final byte byte7 = this.buffer[this.bufferOffset++];
                final byte byte8 = this.buffer[this.bufferOffset++];
                output[writeOffset++] = byte8;
                output[writeOffset++] = byte7;
                output[writeOffset++] = byte6;
                output[writeOffset++] = byte5;
                output[writeOffset++] = byte4;
                output[writeOffset++] = byte3;
                output[writeOffset++] = byte2;
                output[writeOffset++] = byte1;
                this.inputBytesRemaining -= 8;
                if (this.inputBytesRemaining <= 0) {
                    return writeOffset;
                }
            }
        }

        return writeOffset;
    }


    private int fillBuffer() throws IOException {
        this.bufferOffset = 0;
        return this.stream.read(this.buffer, 0, this.buffer.length);
    }


    @Override
    public int inputBitsPerSample() {
        return 64;
    }


    @Override
    public int outputBitsPerSample() {
        return 64;
    }


    @Override
    public PcmDataType outputPcmDataType() {
        return PcmDataType.FLOAT;
    }


    @Override
    public void close() throws IOException {
        this.stream.close();
    }

}
