package de.pottgames.tuningfork.test;

enum Note {
    A4(440f),
    F4(349.23f),
    E5(659.26f),
    F5(698.46f),
    C5(523.25f),
    AS4(415.3f),
    A5(880f),
    AS5(830.61f),
    G5(783.99f),
    GES5(739.99f),
    B4(466.16f),
    ES5(622.25f),
    D5(587.33f),
    DES5(554.37f),
    H4(493.88f);


    private float frequency;


    Note(float frequency) {
        this.frequency = frequency;
    }


    float getFrequency() {
        return this.frequency;
    }

}
