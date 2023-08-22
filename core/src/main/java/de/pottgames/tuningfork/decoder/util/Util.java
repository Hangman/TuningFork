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

package de.pottgames.tuningfork.decoder.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

    public static boolean isOdd(int number) {
        return number % 2 != 0;
    }


    public static boolean isOdd(long number) {
        return number % 2 != 0;
    }


    public static boolean isEven(int number) {
        return !Util.isOdd(number);
    }


    public static boolean isEven(long number) {
        return !Util.isOdd(number);
    }


    /**
     * Returns the next higher power of two. If number is negative, 1 is returned.
     *
     * @param number
     *
     * @return the next power of two
     */
    public static int nextPowerOfTwo(int number) {
        if (number <= 0) {
            return 1;
        }
        final int result = Integer.highestOneBit(number);
        if (result == number) {
            return result;
        }
        return result << 1;
    }


    /**
     * Returns the next lower power of two. If the number is negative, 1 is returned.
     *
     * @param number
     *
     * @return the next lower power of two
     */
    public static int lastPowerOfTwo(int number) {
        if (number <= 0) {
            return 1;
        }
        return Integer.highestOneBit(number);
    }


    public static byte[] toByteArray(InputStream stream) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[20000];
        while (true) {
            final int read = stream.read(buffer);
            if (read <= 0) {
                break;
            }
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }


    /**
     * Reads bytes from the provided input stream into the given output buffer, up to the specified limit.
     *
     * This method attempts to read bytes from the input stream into the output buffer until either the specified limit is reached or the end of the input
     * stream is encountered.
     *
     * @param stream The input stream to read from.
     * @param output The byte array to store the read data into.
     * @param limit The maximum number of bytes to read.
     *
     * @return The total number of bytes read from the input stream and stored in the output buffer.
     *
     * @throws IOException If an I/O error occurs while reading from the input stream.
     */
    public static int readAll(InputStream stream, byte[] output, int limit) throws IOException {
        if (limit <= 0) {
            return 0;
        }

        int bytesToRead = limit;
        int offset = 0;

        while (bytesToRead > 0) {
            final int bytesRead = stream.read(output, offset, bytesToRead);
            if (bytesRead == -1) {
                return offset;
            }
            bytesToRead -= bytesRead;
            offset += bytesRead;
        }

        return offset;
    }

}
