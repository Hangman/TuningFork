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

}
