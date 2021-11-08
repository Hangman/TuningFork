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
