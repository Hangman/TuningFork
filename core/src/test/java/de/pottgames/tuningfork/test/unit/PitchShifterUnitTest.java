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

package de.pottgames.tuningfork.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.pottgames.tuningfork.PitchShifter;

public class PitchShifterUnitTest {

    @Test
    public void correctPitchPoint4() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(0.4f);
        Assertions.assertEquals(12, shifter.coarseTune);
        Assertions.assertEquals(0, shifter.fineTune);
    }


    @Test
    public void correctPitchPoint5() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(0.5f);
        Assertions.assertEquals(12, shifter.coarseTune);
        Assertions.assertEquals(0, shifter.fineTune);
    }


    @Test
    public void correctPitch1() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(1f);
        Assertions.assertEquals(0, shifter.coarseTune);
        Assertions.assertEquals(0, shifter.fineTune);
    }


    @Test
    public void correctPitch1Point5() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(1.5f);
        Assertions.assertEquals(-7, shifter.coarseTune);
        Assertions.assertEquals(-2, shifter.fineTune);
    }


    @Test
    public void correctPitch1Point6() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(1.6f);
        Assertions.assertEquals(-8, shifter.coarseTune);
        Assertions.assertEquals(-14, shifter.fineTune);
    }


    @Test
    public void correctPitch2() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(2f);
        Assertions.assertEquals(-12, shifter.coarseTune);
        Assertions.assertEquals(-0, shifter.fineTune);
    }


    @Test
    public void correctPitch3() {
        final PitchShifter shifter = new PitchShifter();
        shifter.correctPitch(3f);
        Assertions.assertEquals(-12, shifter.coarseTune);
        Assertions.assertEquals(-0, shifter.fineTune);
    }

}
