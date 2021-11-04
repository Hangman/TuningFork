package de.pottgames.tuningfork;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;

import com.badlogic.gdx.math.Vector3;

/**
 * The EAX Reverb parameter set is a superset of the standard OpenAL Effects Extension environmental reverb effect. Additional parameters allow for:  Closer
 * control over the tone of the reverb  Reverb directivity, using panning vectors  Reverb granularity, using echo controls The EAX Reverb is natively
 * supported on any devices that support the EAX 3.0 or above standard, including:  SoundBlaster Audigy series soundcards  SoundBlaster X-Fi series soundcards
 * The EAX Reverb will be emulated on devices that only support EAX 2.0. Note: The “Generic Software” device falls into this category as the software mixer
 * supports the EAX 2.0 Reverb effect.
 *
 * @author Matthias
 *
 */
public class EaxReverb extends SoundEffectData {
    /**
     * Range: (0.0–1.0), Default: (1.0)<br>
     * Reverb Modal Density controls the coloration of the late reverb.<br>
     * <br>
     * Lowering the value adds more coloration to the late reverb.
     */
    public float density = 1f;

    /**
     * Range: (0.0–1.0), Default: (1.0)<br>
     * The Reverb Diffusion property controls the echo density in the reverberation decay.<br>
     * <br>
     * It’s set by default to 1.0, which provides the highest density. Reducing diffusion gives the reverberation a more “grainy” character that is especially
     * noticeable with percussive sound sources. If you set a diffusion value of 0.0, the later reverberation sounds like a succession of distinct echoes.
     */
    public float diffusion = 1f;

    /**
     * Range: (0.0–1.0), Default: (0.32)<br>
     * The Reverb Gain property is the master volume control for the reflected sound (both early reflections and reverberation) that the reverb effect adds to
     * all sound sources.<br>
     * <br>
     * It sets the maximum amount of reflections and reverberation added to the final sound mix. The value of the Reverb Gain property ranges from 1.0 (0db)
     * (the maximum amount) to 0.0 (-100db) (no reflected sound at all).
     */
    public float gain = 0.32f;

    /**
     * Range: (0.0–1.0), Default: (0.89)<br>
     * The Reverb Gain HF property further tweaks reflected sound by attenuating it at high frequencies.<br>
     * <br>
     * It controls a low-pass filter that applies globally to the reflected sound of all sound sources feeding the particular instance of the reverb effect. The
     * value of the Reverb Gain HF property ranges from 1.0 (0 dB) (no filter) to 0.0 (-100 dB) (virtually no reflected sound). The field highFrequencyReference
     * sets the frequency at which the value of this property is measured.
     */
    public float gainHf = 0.89f;

    /**
     * Range: (0.0–1.0), Default: (0.0)<br>
     * The Reverb Gain LF property further tweaks reflected sound by attenuating it at low frequencies.<br>
     * <br>
     * It controls a high-pass filter that applies globally to the reflected sound of all sound sources feeding the particular instance of the reverb effect.
     * The value of the Reverb Gain LF property ranges from 1.0 (0db) (no filter) to 0.0 (-100db) (virtually no reflected sound). The field
     * lowFrequencyReference sets the frequency at which the value of this property is measured.
     */
    public float gainLf = 0f;

    /**
     * Range: (0.1–20.0), Default: (1.49)<br>
     * The Decay Time property sets the reverberation decay time.<br>
     * <br>
     * It ranges from 0.1 (typically a small room with very dead surfaces) to 20.0 (typically a large room with very live surfaces).
     */
    public float decayTime = 1.49f;

    /**
     * Range: (0.1–2.0), Default: (0.83)<br>
     * The Decay HF Ratio property sets the spectral quality of the Decay Time parameter.<br>
     * <br>
     * It is the ratio of high-frequency decay time relative to the time set by Decay Time. The Decay HF Ratio value 1.0 is neutral: the decay time is equal for
     * all frequencies. As Decay HF Ratio increases above 1.0, the high-frequency decay time increases so it’s longer than the decay time at low frequencies.
     * You hear a more brilliant reverberation with a longer decay at high frequencies. As the Decay HF Ratio value decreases below 1.0, the high-frequency
     * decay time decreases so it’s shorter than the decay time of the low frequencies. You hear a more natural reverberation.
     */
    public float decayHfRatio = 0.83f;

    /**
     * Range: (0.1–2.0), Default: (1.0)<br>
     * The Decay LF Ratio property adjusts the spectral quality of the Decay Time parameter.<br>
     * <br>
     * It is the ratio of low-frequency decay time relative to the time set by Decay Time. The Decay LF Ratio value 1.0 is neutral: the decay time is equal for
     * all frequencies. As Decay LF Ratio increases above 1.0, the low-frequency decay time increases so it’s longer than the decay time at mid frequencies. You
     * hear a more booming reverberation with a longer decay at low frequencies. As the Decay LF Ratio value decreases below 1.0, the low-frequency decay time
     * decreases so it’s shorter than the decay time of the mid frequencies. You hear a more tinny reverberation.
     */
    public float decayLfRatio = 1f;

    /**
     * Range: (0.0–3.16), Default: (0.05)<br>
     * The Reflections Gain property controls the overall amount of initial reflections relative to the Gain property.<br>
     * <br>
     * (The Gain property sets the overall amount of reflected sound: both initial reflections and later reverberation.) The value of Reflections Gain ranges
     * from a maximum of 3.16 (+10 dB) to a minimum of 0.0 (-100 dB) (no initial reflections at all), and is corrected by the value of the Gain property. The
     * Reflections Gain property does not affect the subsequent reverberation decay.<br>
     * <br>
     * You can increase the amount of initial reflections to simulate a more narrow space or closer walls, especially effective if you associate the initial
     * reflections increase with a reduction in reflections delays by lowering the value of the Reflection Delay property. To simulate open or semi-open
     * environments, you can maintain the amount of early reflections while reducing the value of the Late Reverb Gain property, which controls later
     * reflections.
     */
    public float reflectionsGain = 0.05f;

    /**
     * Range: (0.0–0.3), Default: (0.007)<br>
     * The Reflections Delay property is the amount of delay, in seconds, between the arrival time of the direct path from the source to the first reflection
     * from the source.<br>
     * <br>
     * It ranges from 0 to 300 milliseconds. You can reduce or increase Reflections Delay to simulate closer or more distant reflective surfaces and therefore
     * control the perceived size of the room.
     */
    public float reflectionsDelay = 0.007f;

    /**
     * Range: (0.0–1.0), Default: (0,0,0)<br>
     * The Reflections Pan property is a 3D vector that controls the spatial distribution of the cluster of early reflections.<br>
     * <br>
     * The direction of this vector controls the global direction of the reflections, while its magnitude controls how focused the reflections are towards this
     * direction.<br>
     * <br>
     * It is important to note that the direction of the vector is interpreted in the coordinate system of the user, without taking into account the orientation
     * of the virtual listener. For instance, assuming a four-point loudspeaker playback system, setting Reflections Pan to (0., 0., 0.7) means that the
     * reflections are panned to the front speaker pair, whereas as setting of (0., 0., −0.7) pans the reflections towards the rear speakers. These vectors
     * follow the a left-handed co-ordinate system, unlike OpenAL uses a right-handed co-ordinate system.<br>
     * <br>
     * If the magnitude of Reflections Pan is zero (the default setting), the early reflections come evenly from all directions. As the magnitude increases, the
     * reflections become more focused in the direction pointed to by the vector. A magnitude of 1.0 would represent the extreme case, where all reflections
     * come from a single direction.
     */
    public final Vector3  reflectionsPan     = new Vector3(0f, 0f, 0f);
    private final float[] reflectionsPanData = new float[3];

    /**
     * Range: (0.0–10.0), Default: (1.26)<br>
     * The Late Reverb Gain property controls the overall amount of later reverberation relative to the Gain property.<br>
     * <br>
     * (The Gain property sets the overall amount of both initial reflections and later reverberation.) The value of Late Reverb Gain ranges from a maximum of
     * 10.0 (+20 dB) to a minimum of 0.0 (-100 dB) (no late reverberation at all). Note that Late Reverb Gain and Decay Time are independent properties: If you
     * adjust Decay Time without changing Late Reverb Gain, the total intensity (the averaged square of the amplitude) of the late reverberation remains
     * constant.
     */
    public float lateReverbGain = 1.26f;

    /**
     * Range: (0.0–0.1), Default: (0.011)<br>
     * The Late Reverb Delay property defines the begin time, in seconds, of the late reverberation relative to the time of the initial reflection (the first of
     * the early reflections).<br>
     * <br>
     * It ranges from 0 to 100 milliseconds. Reducing or increasing Late Reverb Delay is useful for simulating a smaller or larger room.
     */
    public float lateReverbDelay = 0.011f;

    /**
     * Range: (0.0–1.0), Default: (0,0,0)<br>
     * The Late Reverb Pan property is a 3D vector that controls the spatial distribution of the late reverb.<br>
     * <br>
     * The direction of this vector controls the global direction of the reverb, while its magnitude controls how focused the reverb are towards this direction.
     * The details under reflectionsPan, above, also apply to Late Reverb Pan.
     */
    public final Vector3  lateReverbPan     = new Vector3(0f, 0f, 0f);
    private final float[] lateReverbPanData = new float[3];

    /**
     * Range: (0.075–0.25), Default: (0.25)<br>
     * Echo Depth introduces a cyclic echo in the reverberation decay, which will be noticeable with transient or percussive sounds.<br>
     * <br>
     * A larger value of Echo Depth will make this effect more prominent. Echo Time controls the rate at which the cyclic echo repeats itself along the
     * reverberation decay. For example, the default setting for Echo Time is 250 ms. causing the echo to occur 4 times per second. Therefore, if you were to
     * clap your hands in this type of environment, you will hear four repetitions of clap per second.<br>
     * <br>
     * Together with Reverb Diffusion, Echo Depth will control how long the echo effect will persist along the reverberation decay. In a more diffuse
     * environment, echoes will wash out more quickly after the direct sound. In an environment that is less diffuse, you will be able to hear a larger number
     * of repetitions of the echo, which will wash out later in the reverberation decay. If Diffusion is set to 0.0 and Echo Depth is set to 1.0, the echo will
     * persist distinctly until the end of the reverberation decay.
     */
    public float echoTime = 0.25f;

    /**
     * Range: (0.0–1.0), Default: (0.0)<br>
     * Echo Depth introduces a cyclic echo in the reverberation decay, which will be noticeable with transient or percussive sounds.<br>
     * <br>
     * A larger value of Echo Depth will make this effect more prominent. Echo Time controls the rate at which the cyclic echo repeats itself along the
     * reverberation decay. For example, the default setting for Echo Time is 250 ms. causing the echo to occur 4 times per second. Therefore, if you were to
     * clap your hands in this type of environment, you will hear four repetitions of clap per second.<br>
     * <br>
     * Together with Reverb Diffusion, Echo Depth will control how long the echo effect will persist along the reverberation decay. In a more diffuse
     * environment, echoes will wash out more quickly after the direct sound. In an environment that is less diffuse, you will be able to hear a larger number
     * of repetitions of the echo, which will wash out later in the reverberation decay. If Diffusion is set to 0.0 and Echo Depth is set to 1.0, the echo will
     * persist distinctly until the end of the reverberation decay.
     */
    public float echoDepth = 0f;

    /**
     * Range: (0.004–4.0), Default: (0.25)<br>
     * Using modulationTime and modulationDepth, you can create a pitch modulation in the reverberant sound.<br>
     * <br>
     * This will be most noticeable applied to sources that have tonal color or pitch. You can use this to make some trippy effects! Modulation Time controls
     * the speed of the vibrato (rate of periodic changes in pitch).
     */
    public float modulationTime = 0.25f;

    /**
     * Range: (0.0–1.0), Default: (0.0)<br>
     * Using modulationTime and modulationDepth, you can create a pitch modulation in the reverberant sound.<br>
     * <br>
     * This will be most noticeable applied to sources that have tonal color or pitch. You can use this to make some trippy effects! Modulation Depth controls
     * the amount of pitch change. Low values of Diffusion will contribute to reinforcing the perceived effect by reducing the mixing of overlapping reflections
     * in the reverberation decay.
     */
    public float modulationDepth = 0f;

    /**
     * Range: (0.892–1.0), Default: (0.994)<br>
     * The Air Absorption Gain HF property controls the distance-dependent attenuation at high frequencies caused by the propagation medium.<br>
     * <br>
     * It applies to reflected sound only. You can use Air Absorption Gain HF to simulate sound transmission through foggy air, dry air, smoky atmosphere, and
     * so on. The default value is 0.994 (-0.05 dB) per meter, which roughly corresponds to typical condition of atmospheric humidity, temperature, and so on.
     * Lowering the value simulates a more absorbent medium (more humidity in the air, for example); raising the value simulates a less absorbent medium (dry
     * desert air, for example).
     */
    public float airAbsorptionGainHf = 0.994f;

    /**
     * Range: (1000.0–20000.0), Default: (5000.0)<br>
     * The properties HF Reference and LF Reference determine respectively the frequencies at which the high-frequency effects and the low-frequency effects
     * created by EAX Reverb properties are measured, for example Decay HF Ratio and Decay LF Ratio.<br>
     * <br>
     * Note that it is necessary to maintain a factor of at least 10 between these two reference frequencies so that low frequency and high frequency properties
     * can be accurately controlled and will produce independent effects. In other words, the LF Reference value should be less than 1/10 of the HF Reference
     * value.
     */
    public float hfReference = 5000f;

    /**
     * Range: (20.0–1000.0), Default: (250.0)<br>
     * The properties HF Reference and LF Reference determine respectively the frequencies at which the high-frequency effects and the low-frequency effects
     * created by EAX Reverb properties are measured, for example Decay HF Ratio and Decay LF Ratio.<br>
     * <br>
     * Note that it is necessary to maintain a factor of at least 10 between these two reference frequencies so that low frequency and high frequency properties
     * can be accurately controlled and will produce independent effects. In other words, the LF Reference value should be less than 1/10 of the HF Reference
     * value.
     */
    public float lfReference = 250f;

    /**
     * Range: (0.0–10.0), Default: (0.0)<br>
     * The Room Rolloff Factor property is one of two methods available to attenuate the reflected sound (containing both reflections and reverberation)
     * according to source-listener distance.<br>
     * <br>
     * It’s defined the same way as OpenAL’s Rolloff Factor, but operates on reverb sound instead of direct-path sound. Setting the Room Rolloff Factor value to
     * 1.0 specifies that the reflected sound will decay by 6 dB every time the distance doubles. Any value other than 1.0 is equivalent to a scaling factor
     * applied to the quantity specified by ((Source listener distance) - (Reference Distance)). Reference Distance is an OpenAL source parameter that specifies
     * the inner border for distance rolloff effects: if the source comes closer to the listener than the reference distance, the direct-path sound isn’t
     * increased as the source comes closer to the listener, and neither is the reflected sound.<br>
     * <br>
     * The default value of Room Rolloff Factor is 0.0 because, by default, the Effects Extension reverb effect naturally manages the reflected sound level
     * automatically for each sound source to simulate the natural rolloff of reflected sound vs. distance in typical rooms. (Note that this isn’t the case if
     * the source property flag AL_AUXILIARY_SEND_FILTER_GAIN_AUTO is set to AL_FALSE) You can use Room Rolloff Factor as an option to automatic control so you
     * can exaggerate or replace the default automatically-controlled rolloff.
     */
    public float roomRolloffFactor = 0f;

    /**
     * Default: (True)<br>
     * When this flag is set, the high-frequency decay time automatically stays below a limit value that’s derived from the setting of the property Air
     * Absorption HF.<br>
     * <br>
     * This limit applies regardless of the setting of the property Decay HF Ratio, and the limit doesn’t affect the value of Decay HF Ratio. This limit, when
     * on, maintains a natural sounding reverberation decay by allowing you to increase the value of Decay Time without the risk of getting an unnaturally long
     * decay time at high frequencies. If this flag is set to AL_FALSE, high-frequency decay time isn’t automatically limited.
     */
    public boolean decayHfLimit = true;


    @Override
    void apply(int effectId) {
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_DENSITY, this.density);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_DIFFUSION, this.diffusion);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_GAIN, this.gain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_GAINHF, this.gainHf);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_GAINLF, this.gainLf);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_DECAY_TIME, this.decayTime);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, this.decayHfRatio);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_DECAY_LFRATIO, this.decayLfRatio);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, this.reflectionsGain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_REFLECTIONS_DELAY, this.reflectionsDelay);
        this.reflectionsPanData[0] = this.reflectionsPan.x;
        this.reflectionsPanData[1] = this.reflectionsPan.y;
        this.reflectionsPanData[2] = this.reflectionsPan.z;
        EXTEfx.alEffectfv(effectId, EXTEfx.AL_EAXREVERB_REFLECTIONS_PAN, this.reflectionsPanData);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, this.lateReverbGain);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, this.lateReverbDelay);
        this.lateReverbPanData[0] = this.lateReverbPan.x;
        this.lateReverbPanData[1] = this.lateReverbPan.y;
        this.lateReverbPanData[2] = this.lateReverbPan.z;
        EXTEfx.alEffectfv(effectId, EXTEfx.AL_EAXREVERB_REFLECTIONS_PAN, this.lateReverbPanData);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_ECHO_TIME, this.echoTime);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_ECHO_DEPTH, this.echoDepth);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_MODULATION_TIME, this.modulationTime);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_MODULATION_DEPTH, this.modulationDepth);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, this.airAbsorptionGainHf);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_HFREFERENCE, this.hfReference);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_LFREFERENCE, this.lfReference);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, this.roomRolloffFactor);
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EAXREVERB_DECAY_HFLIMIT, this.decayHfLimit ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

}
