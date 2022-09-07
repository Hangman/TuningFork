package de.pottgames.tuningfork.decoder;

import de.pottgames.tuningfork.PcmFormat.PcmDataType;

public class DefaultWavDecoderProvider implements WavDecoderProvider {

    @Override
    public WavDecoder getDecoder(int inputBitsPerSample, int channels, int audioFormat, int blockAlign) {
        if (inputBitsPerSample <= 0) {
            return null;
        }

        switch (inputBitsPerSample) {
            case 4:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_DVI_ADPCM.getRegNumber()) {
                    if (channels == 1 || channels == 2) {
                        return new ImaAdpcmDecoder(blockAlign, channels);
                    }
                }
                break;
            case 8:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, PcmDataType.INTEGER);
                }
                break;
            case 16:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, PcmDataType.INTEGER);
                }
                break;
            case 24:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new Int24To16PcmDecoder();
                }
                break;
            case 32:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_IEEE_FLOAT.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, PcmDataType.FLOAT);
                }
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new Int32To16PcmDecoder();
                }
                break;
            case 64:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_IEEE_FLOAT.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, PcmDataType.FLOAT);
                }
                break;
        }

        return null;
    }

}
