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

enum Note {
    A4(440f),
    AS4(415.3f),
    A5(880f),
    AS5(830.61f),
    B4(466.16f),
    C5(523.25f),
    CIS5(554.37f),
    D5(587.33f),
    DES5(554.37f),
    DIS5(622.25f),
    ES4(311.13f),
    E5(659.26f),
    ES5(622.25f),
    F4(349.23f),
    F5(698.46f),
    G4(392f),
    GES4(369.99f),
    GIS4(415.3f),
    G5(783.99f),
    GES5(739.99f),
    GIS5(830.61f),
    H4(493.88f),
    SILENCE(1f);


    private final float frequency;


    Note(float frequency) {
        this.frequency = frequency;
    }


    float getFrequency() {
        return this.frequency;
    }

}
