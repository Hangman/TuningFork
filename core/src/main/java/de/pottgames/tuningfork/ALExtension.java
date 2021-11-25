package de.pottgames.tuningfork;

enum ALExtension {
    ALC_ENUMERATE_ALL_EXT("ALC_ENUMERATE_ALL_EXT", true),
    ALC_ENUMERATION_EXT("ALC_ENUMERATION_EXT", true),
    ALC_EXT_CAPTURE("ALC_EXT_CAPTURE", true),
    ALC_EXT_DEDICATED("ALC_EXT_DEDICATED", true),
    ALC_EXT_DISCONNECT("ALC_EXT_disconnect", true),
    ALC_EXT_EFX("ALC_EXT_EFX", true),
    ALC_EXT_THREAD_LOCAL_CONTEXT("ALC_EXT_thread_local_context", true),
    ALC_SOFT_DEVICE_CLOCK("ALC_SOFT_device_clock", true),
    ALC_SOFT_HRTF("ALC_SOFT_HRTF", true),
    ALC_SOFT_LOOPBACK("ALC_SOFT_loopback", true),
    ALC_SOFT_OUTPUT_LIMITER("ALC_SOFT_output_limiter", true),
    ALC_SOFT_PAUSE_DEVICE("ALC_SOFT_pause_device", true),
    AL_EXT_ALAW("AL_EXT_ALAW", false),
    AL_EXT_BFORMAT("AL_EXT_BFORMAT", false),
    AL_EXT_DOUBLE("AL_EXT_DOUBLE", false),
    AL_EXT_EXPONENT_DISTANCE("AL_EXT_EXPONENT_DISTANCE", false),
    AL_EXT_FLOAT32("AL_EXT_FLOAT32", false),
    AL_EXT_IMA4("AL_EXT_IMA4", false),
    AL_EXT_LINEAR_DISTANCE("AL_EXT_LINEAR_DISTANCE", false),
    AL_EXT_MCFORMATS("AL_EXT_MCFORMATS", false),
    AL_EXT_MULAW("AL_EXT_MULAW", false),
    AL_EXT_MULAW_BFORMAT("AL_EXT_MULAW_BFORMAT", false),
    AL_EXT_MULAW_MCFORMATS("AL_EXT_MULAW_MCFORMATS", false),
    AL_EXT_OFFSET("AL_EXT_OFFSET", false),
    AL_EXT_SOURCE_DISTANCE_MODEL("AL_EXT_source_distance_model", false),
    AL_EXT_SOURCE_RADIUS("AL_EXT_SOURCE_RADIUS", false),
    AL_EXT_STEREO_ANGLES("AL_EXT_STEREO_ANGLES", false),
    AL_LOKI_QUADRIPHONIC("AL_LOKI_quadriphonic", false),
    AL_SOFT_BLOCK_ALIGNMENT("AL_SOFT_block_alignment", false),
    AL_SOFT_DEFERRED_UPDATES("AL_SOFT_deferred_updates", false),
    AL_SOFT_DIRECT_CHANNELS("AL_SOFT_direct_channels", false),
    AL_SOFTX_EVENTS("AL_SOFTX_events", false),
    AL_SOFTX_FILTER_GAIN_EX("AL_SOFTX_filter_gain_ex", false),
    AL_SOFT_GAIN_CLAMP_EX("AL_SOFT_gain_clamp_ex", false),
    AL_SOFT_LOOP_POINTS("AL_SOFT_loop_points", false),
    AL_SOFTX_MAP_BUFFER("AL_SOFTX_map_buffer", false),
    AL_SOFT_MSADPCM("AL_SOFT_MSADPCM", false),
    AL_SOFT_SOURCE_LATENCY("AL_SOFT_source_latency", false),
    AL_SOFT_SOURCE_LENGTH("AL_SOFT_source_length", false),
    AL_SOFT_SOURCE_RESAMPLER("AL_SOFT_source_resampler", false),
    AL_SOFT_SOURCE_SPATIALIZE("AL_SOFT_source_spatialize", false);


    private final String  alSpecifier;
    private final boolean alc;


    ALExtension(String alSpecifier, boolean alc) {
        this.alSpecifier = alSpecifier;
        this.alc = alc;
    }


    String getAlSpecifier() {
        return this.alSpecifier;
    }


    boolean isAlc() {
        return this.alc;
    }

}
