package de.pottgames.tuningfork.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.pottgames.tuningfork.PitchShifter;

class PitchShifterUnitTest {

    @Test
    void correctPitchPoint4() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(0.4f);
        Assertions.assertEquals(12, shifter.coarseTune);
        Assertions.assertEquals(0, shifter.fineTune);
    }


    @Test
    void correctPitchPoint5() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(0.5f);
        Assertions.assertEquals(12, shifter.coarseTune);
        Assertions.assertEquals(0, shifter.fineTune);
    }


    @Test
    void correctPitch1() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(1f);
        Assertions.assertEquals(0, shifter.coarseTune);
        Assertions.assertEquals(0, shifter.fineTune);
    }


    @Test
    void correctPitch1Point5() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(1.5f);
        Assertions.assertEquals(-7, shifter.coarseTune);
        Assertions.assertEquals(-2, shifter.fineTune);
    }


    @Test
    void correctPitch1Point6() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(1.6f);
        Assertions.assertEquals(-8, shifter.coarseTune);
        Assertions.assertEquals(-14, shifter.fineTune);
    }


    @Test
    void correctPitch2() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(2f);
        Assertions.assertEquals(-12, shifter.coarseTune);
        Assertions.assertEquals(-0, shifter.fineTune);
    }


    @Test
    void correctPitch3() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(3f);
        Assertions.assertEquals(-12, shifter.coarseTune);
        Assertions.assertEquals(-0, shifter.fineTune);
    }

}
