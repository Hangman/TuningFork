package de.pottgames.tuningfork.decoder;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.decoder.LawDecoder.Encoding;

public class DefaultWavDecoderProvider implements WavDecoderProvider {

    @Override
    public WavDecoder getDecoder(WavFmtChunk fmtChunk, boolean forStreaming) {
        final int inputBitsPerSample = fmtChunk.getwBitsPerSample();
        final int format = fmtChunk.getwFormatTag();
        final int audioFormat = format == WavAudioFormat.WAVE_FORMAT_EXTENSIBLE.getRegNumber() ? fmtChunk.getSubFormatDataCode() : format;
        final int channels = fmtChunk.getnChannels();
        final int blockAlign = fmtChunk.getnBlockAlign();
        final int sampleRate = (int) fmtChunk.getnSamplesPerSec();

        if (inputBitsPerSample <= 0) {
            return null;
        }

        switch (inputBitsPerSample) {
            case 4:
                return getAdpcmDecoder(audioFormat, channels, blockAlign, sampleRate, forStreaming);
            case 8:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, channels, sampleRate, PcmDataType.INTEGER);
                }
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_MULAW.getRegNumber()) {
                    return new LawDecoder(channels, sampleRate, Encoding.U_LAW, false);
                }
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_ALAW.getRegNumber()) {
                    return new LawDecoder(channels, sampleRate, Encoding.A_LAW, false);
                }
                break;
            case 16:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, channels, sampleRate, PcmDataType.INTEGER);
                }
                break;
            case 24:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new Int24To16PcmDecoder(channels, sampleRate);
                }
                break;
            case 32:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_IEEE_FLOAT.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, channels, sampleRate, PcmDataType.FLOAT);
                }
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_PCM.getRegNumber()) {
                    return new Int32To16PcmDecoder(channels, sampleRate);
                }
                break;
            case 64:
                if (audioFormat == WavAudioFormat.WAVE_FORMAT_IEEE_FLOAT.getRegNumber()) {
                    return new PcmDecoder(inputBitsPerSample, channels, sampleRate, PcmDataType.FLOAT);
                }
                break;
        }

        return null;
    }


    protected WavDecoder getAdpcmDecoder(final int audioFormat, final int channels, final int blockAlign, final int sampleRate, boolean forStreaming) {
        if (channels < 1 || channels > 2) {
            return null;
        }

        if (audioFormat == WavAudioFormat.WAVE_FORMAT_DVI_ADPCM.getRegNumber()) {
            if (!forStreaming && Audio.get().isNativeDecodersAvailable()) {
                return new ImaAdpcmRsDecoder(blockAlign, channels, sampleRate);
            }
            return new ImaAdpcmDecoder(blockAlign, channels, sampleRate);
        }
        if (audioFormat == WavAudioFormat.WAVE_FORMAT_ADPCM.getRegNumber()) {
            return new MsAdpcmDecoder(blockAlign, channels, sampleRate);
        }

        return null;
    }

}
