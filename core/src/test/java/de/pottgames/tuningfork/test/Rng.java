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

package de.pottgames.tuningfork.test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Rng {

    private Rng() {
        // hide public constructor
    }


    public static float get(float min, float max) {
        return ThreadLocalRandom.current().nextFloat() * (max - min) + min;
    }


    public static float get(float min, float max, Random random) {
        return random.nextFloat() * (max - min) + min;
    }


    public static int get(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }


    public static int get(int min, int max, Random random) {
        return random.nextInt(max - min + 1) + min;
    }


    public static long get(long min, long max) {
        return ThreadLocalRandom.current().nextLong(max - min + 1) + min;
    }

}
