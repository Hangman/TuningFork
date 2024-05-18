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
     * @param number the number
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
     * @param number the number
     * @return the next lower power of two
     */
    public static int lastPowerOfTwo(int number) {
        if (number <= 0) {
            return 1;
        }
        return Integer.highestOneBit(number);
    }


    /**
     * Fully reads an InputStream and returns its data as an array of bytes.
     *
     * @param stream the InputStream
     * @return a byte array that contains the data of the stream
     */
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
     * Reads bytes from the provided input stream into the given output buffer, up to the specified limit.<br> This
     * method attempts to read bytes from the input stream into the output buffer until either the specified limit is
     * reached or the end of the input stream is encountered.
     *
     * @param stream The input stream to read from.
     * @param output The byte array to store the read data into.
     * @param limit  The maximum number of bytes to read.
     * @return The total number of bytes read from the input stream and stored in the output buffer.
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


    /**
     * Converts a sequence of 8 bytes from the specified source array, starting at the specified offset, into a long
     * value using big-endian byte order.
     *
     * @param source The source byte array.
     * @param offset The starting offset in the source array.
     * @return The long value converted from the specified bytes in big-endian byte order.
     */
    public static long longOfBigEndianBytes(byte[] source, int offset) {
        return (source[offset] & 0xFFL) << 56 | (source[offset + 1] & 0xFFL) << 48 |
               (source[offset + 2] & 0xFFL) << 40 | (source[offset + 3] & 0xFFL) << 32 |
               (source[offset + 4] & 0xFFL) << 24 | (source[offset + 5] & 0xFFL) << 16 |
               (source[offset + 6] & 0xFFL) << 8 | source[offset + 7] & 0xFFL;
    }


    /**
     * Converts a sequence of 8 bytes from the specified source array, starting at the specified offset, into a long
     * value using little-endian byte order.
     *
     * @param source The source byte array.
     * @param offset The starting offset in the source array.
     * @return The long value converted from the specified bytes in little-endian byte order.
     */
    public static long longOfLittleEndianBytes(byte[] source, int offset) {
        return (source[offset + 7] & 0xFFL) << 56 | (source[offset + 6] & 0xFFL) << 48 |
               (source[offset + 5] & 0xFFL) << 40 | (source[offset + 4] & 0xFFL) << 32 |
               (source[offset + 3] & 0xFFL) << 24 | (source[offset + 2] & 0xFFL) << 16 |
               (source[offset + 1] & 0xFFL) << 8 | source[offset] & 0xFFL;
    }


    /**
     * Converts a sequence of 4 bytes from the specified source array, starting at the specified offset, into a long
     * value representing an unsigned integer using big-endian byte order.
     *
     * @param source The source byte array.
     * @param offset The starting offset in the source array.
     * @return The long value representing the unsigned integer from the specified bytes in big-endian order.
     */
    public static long uIntOfBigEndianBytes(byte[] source, int offset) {
        return (source[offset] & 0xFFL) << 24 | (source[offset + 1] & 0xFFL) << 16 | (source[offset + 2] & 0xFFL) << 8 |
               source[offset + 3] & 0xFFL;
    }


    /**
     * Converts a sequence of 4 bytes from the specified source array, starting at the specified offset, into an int
     * value representing a signed integer using big-endian byte order.
     *
     * @param source The source byte array.
     * @param offset The starting offset in the source array.
     * @return The int value representing the signed integer from the specified bytes in big-endian byte order.
     */
    public static int intOfBigEndianBytes(byte[] source, int offset) {
        return (source[offset] & 0xFF) << 24 | (source[offset + 1] & 0xFF) << 16 | (source[offset + 2] & 0xFF) << 8 |
               source[offset + 3] & 0xFF;
    }


    /**
     * Converts a sequence of 4 bytes from the specified source array, starting at the specified offset, into an int
     * value representing a signed integer using little-endian byte order.
     *
     * @param source The source byte array.
     * @param offset The starting offset in the source array.
     * @return The int value representing the signed integer from the specified bytes in little-endian byte order.
     */
    public static int intOfLittleEndianBytes(byte[] source, int offset) {
        return (source[offset + 3] & 0xFF) << 24 | (source[offset + 2] & 0xFF) << 16 |
               (source[offset + 1] & 0xFF) << 8 | source[offset] & 0xFF;
    }


    /**
     * Clamps a value to the given limit.
     *
     * @param value the value
     * @param limit the limit
     * @return the result
     */
    public static int limit(int value, int limit) {
        return Math.min(value, limit);
    }

}
