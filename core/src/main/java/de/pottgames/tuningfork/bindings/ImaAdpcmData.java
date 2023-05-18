package de.pottgames.tuningfork.bindings;

public class ImaAdpcmData {
    public final byte[] pcmData;
    public final int    sampleRate;
    public final int    bitsPerSample;
    public final int    numChannels;


    public ImaAdpcmData(byte[] pcmData, int sampleRate, int bitsPerSample, int numChannels) {
        this.pcmData = pcmData;
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.numChannels = numChannels;
    }

}
