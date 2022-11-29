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


    public static EaxReverb generic() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.8913f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.0500f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb paddedCell() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.1715f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0010f;
        result.gainLf = 1.0000f;
        result.decayTime = 0.1700f;
        result.decayHfRatio = 0.1000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2500f;
        result.reflectionsDelay = 0.0010f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2691f;
        result.lateReverbDelay = 0.0020f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb room() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.5929f;
        result.gainLf = 1.0000f;
        result.decayTime = 0.4000f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.1503f;
        result.reflectionsDelay = 0.0020f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.0629f;
        result.lateReverbDelay = 0.0030f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb bathroom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.1715f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.2512f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.5400f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.6531f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 3.2734f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb livingRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.9766f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0010f;
        result.gainLf = 1.0000f;
        result.decayTime = 0.5000f;
        result.decayHfRatio = 0.1000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2051f;
        result.reflectionsDelay = 0.0030f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.2805f;
        result.lateReverbDelay = 0.0040f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb stoneRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 1.0000f;
        result.decayTime = 2.3100f;
        result.decayHfRatio = 0.6400f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.4411f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1003f;
        result.lateReverbDelay = 0.0170f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb auditorium() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.5781f;
        result.gainLf = 1.0000f;
        result.decayTime = 4.3200f;
        result.decayHfRatio = 0.5900f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.4032f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7170f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb concertHall() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 1.0000f;
        result.decayTime = 3.9200f;
        result.decayHfRatio = 0.7000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2427f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.9977f;
        result.lateReverbDelay = 0.0290f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb cave() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 1.0000f;
        result.gainLf = 1.0000f;
        result.decayTime = 2.9100f;
        result.decayHfRatio = 1.3000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.5000f;
        result.reflectionsDelay = 0.0150f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7063f;
        result.lateReverbDelay = 0.0220f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb arena() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.4477f;
        result.gainLf = 1.0000f;
        result.decayTime = 7.2400f;
        result.decayHfRatio = 0.3300f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2612f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.0186f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb hangar() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.3162f;
        result.gainLf = 1.0000f;
        result.decayTime = 10.0500f;
        result.decayHfRatio = 0.2300f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.5000f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2560f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb carpetedHallway() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0100f;
        result.gainLf = 1.0000f;
        result.decayTime = 0.3000f;
        result.decayHfRatio = 0.1000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.1215f;
        result.reflectionsDelay = 0.0020f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1531f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb hallway() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.5900f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2458f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.6615f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb stoneCorridor() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.7612f;
        result.gainLf = 1.0000f;
        result.decayTime = 2.7000f;
        result.decayHfRatio = 0.7900f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2472f;
        result.reflectionsDelay = 0.0130f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.5758f;
        result.lateReverbDelay = 0.0200f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb alley() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.3000f;
        result.gain = 0.3162f;
        result.gainHf = 0.7328f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.8600f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2500f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.9954f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1250f;
        result.echoDepth = 0.9500f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb forest() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.3000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0224f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.5400f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.0525f;
        result.reflectionsDelay = 0.1620f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7682f;
        result.lateReverbDelay = 0.0880f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1250f;
        result.echoDepth = 1.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb city() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.5000f;
        result.gain = 0.3162f;
        result.gainHf = 0.3981f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.6700f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.0730f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1427f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb mountains() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.2700f;
        result.gain = 0.3162f;
        result.gainHf = 0.0562f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.2100f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.0407f;
        result.reflectionsDelay = 0.3000f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1919f;
        result.lateReverbDelay = 0.1000f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 1.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb quarry() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.3162f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.0000f;
        result.reflectionsDelay = 0.0610f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.7783f;
        result.lateReverbDelay = 0.0250f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1250f;
        result.echoDepth = 0.7000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb plain() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.2100f;
        result.gain = 0.3162f;
        result.gainHf = 0.1000f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.5000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.0585f;
        result.reflectionsDelay = 0.1790f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1089f;
        result.lateReverbDelay = 0.1000f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 1.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb parkingLot() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 1.0000f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.6500f;
        result.decayHfRatio = 1.5000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.2082f;
        result.reflectionsDelay = 0.0080f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.2652f;
        result.lateReverbDelay = 0.0120f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb sewerpipe() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3071f;
        result.diffusion = 0.8000f;
        result.gain = 0.3162f;
        result.gainHf = 0.3162f;
        result.gainLf = 1.0000f;
        result.decayTime = 2.8100f;
        result.decayHfRatio = 0.1400f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 1.6387f;
        result.reflectionsDelay = 0.0140f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 3.2471f;
        result.lateReverbDelay = 0.0210f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb underwater() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0100f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.4900f;
        result.decayHfRatio = 0.1000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.5963f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 7.0795f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 1.1800f;
        result.modulationDepth = 0.3480f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb drugged() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 0.5000f;
        result.gain = 0.3162f;
        result.gainHf = 1.0000f;
        result.gainLf = 1.0000f;
        result.decayTime = 8.3900f;
        result.decayHfRatio = 1.3900f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.8760f;
        result.reflectionsDelay = 0.0020f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 3.1081f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 1.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb dizzy() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 0.6000f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 1.0000f;
        result.decayTime = 17.2300f;
        result.decayHfRatio = 0.5600f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.1392f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.4937f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 1.0000f;
        result.modulationTime = 0.8100f;
        result.modulationDepth = 0.3100f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb psychotic() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.0625f;
        result.diffusion = 0.5000f;
        result.gain = 0.3162f;
        result.gainHf = 0.8404f;
        result.gainLf = 1.0000f;
        result.decayTime = 7.5600f;
        result.decayHfRatio = 0.9100f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 0.4864f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 2.4378f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 4.0000f;
        result.modulationDepth = 1.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb castleSmallRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8900f;
        result.gain = 0.3162f;
        result.gainHf = 0.3981f;
        result.gainLf = 0.1000f;
        result.decayTime = 1.2200f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 0.3100f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0220f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.9953f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1380f;
        result.echoDepth = 0.0800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb castleShortPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8900f;
        result.gain = 0.3162f;
        result.gainHf = 0.3162f;
        result.gainLf = 0.1000f;
        result.decayTime = 2.3200f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 0.3100f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0230f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1380f;
        result.echoDepth = 0.0800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb castleMediumRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.9300f;
        result.gain = 0.3162f;
        result.gainHf = 0.2818f;
        result.gainLf = 0.1000f;
        result.decayTime = 2.0400f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 0.4600f;
        result.reflectionsGain = 0.6310f;
        result.reflectionsDelay = 0.0220f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.5849f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1550f;
        result.echoDepth = 0.0300f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb castleLargeRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.2818f;
        result.gainLf = 0.1259f;
        result.decayTime = 2.5300f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 0.5000f;
        result.reflectionsGain = 0.4467f;
        result.reflectionsDelay = 0.0340f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0160f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1850f;
        result.echoDepth = 0.0700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb castleLongPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8900f;
        result.gain = 0.3162f;
        result.gainHf = 0.3981f;
        result.gainLf = 0.1000f;
        result.decayTime = 3.4200f;
        result.decayHfRatio = 0.8300f;
        result.decayLfRatio = 0.3100f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0230f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1380f;
        result.echoDepth = 0.0800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb castleHall() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8100f;
        result.gain = 0.3162f;
        result.gainHf = 0.2818f;
        result.gainLf = 0.1778f;
        result.decayTime = 3.1400f;
        result.decayHfRatio = 0.7900f;
        result.decayLfRatio = 0.6200f;
        result.reflectionsGain = 0.1778f;
        result.reflectionsDelay = 0.0560f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0240f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb castleCupboard() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8900f;
        result.gain = 0.3162f;
        result.gainHf = 0.2818f;
        result.gainLf = 0.1000f;
        result.decayTime = 0.6700f;
        result.decayHfRatio = 0.8700f;
        result.decayLfRatio = 0.3100f;
        result.reflectionsGain = 1.4125f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 3.5481f;
        result.lateReverbDelay = 0.0070f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1380f;
        result.echoDepth = 0.0800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb castleCourtyard() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.4200f;
        result.gain = 0.3162f;
        result.gainHf = 0.4467f;
        result.gainLf = 0.1995f;
        result.decayTime = 2.1300f;
        result.decayHfRatio = 0.6100f;
        result.decayLfRatio = 0.2300f;
        result.reflectionsGain = 0.2239f;
        result.reflectionsDelay = 0.1600f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7079f;
        result.lateReverbDelay = 0.0360f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.3700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb castleAlcove() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8900f;
        result.gain = 0.3162f;
        result.gainHf = 0.5012f;
        result.gainLf = 0.1000f;
        result.decayTime = 1.6400f;
        result.decayHfRatio = 0.8700f;
        result.decayLfRatio = 0.3100f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0340f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1380f;
        result.echoDepth = 0.0800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5168.6001f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factorySmallRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.5012f;
        result.decayTime = 1.7200f;
        result.decayHfRatio = 0.6500f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 0.7079f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.7783f;
        result.lateReverbDelay = 0.0240f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1190f;
        result.echoDepth = 0.0700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryShortPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 0.6400f;
        result.gain = 0.2512f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.5012f;
        result.decayTime = 2.5300f;
        result.decayHfRatio = 0.6500f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0380f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1350f;
        result.echoDepth = 0.2300f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryMediumRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 0.8200f;
        result.gain = 0.2512f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.5012f;
        result.decayTime = 2.7600f;
        result.decayHfRatio = 0.6500f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 0.2818f;
        result.reflectionsDelay = 0.0220f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0230f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1740f;
        result.echoDepth = 0.0700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryLargeRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 0.7500f;
        result.gain = 0.2512f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.6310f;
        result.decayTime = 4.2400f;
        result.decayHfRatio = 0.5100f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 0.1778f;
        result.reflectionsDelay = 0.0390f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0230f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2310f;
        result.echoDepth = 0.0700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryLongPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 0.6400f;
        result.gain = 0.2512f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.5012f;
        result.decayTime = 4.0600f;
        result.decayHfRatio = 0.6500f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0370f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1350f;
        result.echoDepth = 0.2300f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryHall() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 0.7500f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.6310f;
        result.decayTime = 7.4300f;
        result.decayHfRatio = 0.5100f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 0.0631f;
        result.reflectionsDelay = 0.0730f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0270f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryCupboard() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3071f;
        result.diffusion = 0.6300f;
        result.gain = 0.2512f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.5012f;
        result.decayTime = 0.4900f;
        result.decayHfRatio = 0.6500f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 1.2589f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.9953f;
        result.lateReverbDelay = 0.0320f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1070f;
        result.echoDepth = 0.0700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryCourtyard() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3071f;
        result.diffusion = 0.5700f;
        result.gain = 0.3162f;
        result.gainHf = 0.3162f;
        result.gainLf = 0.6310f;
        result.decayTime = 2.3200f;
        result.decayHfRatio = 0.2900f;
        result.decayLfRatio = 0.5600f;
        result.reflectionsGain = 0.2239f;
        result.reflectionsDelay = 0.1400f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.3981f;
        result.lateReverbDelay = 0.0390f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.2900f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb factoryAlcove() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 0.5900f;
        result.gain = 0.2512f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.5012f;
        result.decayTime = 3.1400f;
        result.decayHfRatio = 0.6500f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 1.4125f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.0000f;
        result.lateReverbDelay = 0.0380f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1140f;
        result.echoDepth = 0.1000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3762.6001f;
        result.lfReference = 362.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceSmallRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8400f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.2818f;
        result.decayTime = 1.5100f;
        result.decayHfRatio = 1.5300f;
        result.decayLfRatio = 0.2700f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1640f;
        result.echoDepth = 0.1400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceShortPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7500f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.2818f;
        result.decayTime = 1.7900f;
        result.decayHfRatio = 1.4600f;
        result.decayLfRatio = 0.2800f;
        result.reflectionsGain = 0.5012f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0190f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1770f;
        result.echoDepth = 0.0900f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceMediumRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8700f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.4467f;
        result.decayTime = 2.2200f;
        result.decayHfRatio = 1.5300f;
        result.decayLfRatio = 0.3200f;
        result.reflectionsGain = 0.3981f;
        result.reflectionsDelay = 0.0390f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0270f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1860f;
        result.echoDepth = 0.1200f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceLargeRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8100f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.4467f;
        result.decayTime = 3.1400f;
        result.decayHfRatio = 1.5300f;
        result.decayLfRatio = 0.3200f;
        result.reflectionsGain = 0.2512f;
        result.reflectionsDelay = 0.0390f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.0000f;
        result.lateReverbDelay = 0.0270f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2140f;
        result.echoDepth = 0.1100f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceLongPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7700f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.3981f;
        result.decayTime = 3.0100f;
        result.decayHfRatio = 1.4600f;
        result.decayLfRatio = 0.2800f;
        result.reflectionsGain = 0.7943f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0250f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1860f;
        result.echoDepth = 0.0400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceHall() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7600f;
        result.gain = 0.3162f;
        result.gainHf = 0.4467f;
        result.gainLf = 0.5623f;
        result.decayTime = 5.4900f;
        result.decayHfRatio = 1.5300f;
        result.decayLfRatio = 0.3800f;
        result.reflectionsGain = 0.1122f;
        result.reflectionsDelay = 0.0540f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.6310f;
        result.lateReverbDelay = 0.0520f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2260f;
        result.echoDepth = 0.1100f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceCupboard() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8300f;
        result.gain = 0.3162f;
        result.gainHf = 0.5012f;
        result.gainLf = 0.2239f;
        result.decayTime = 0.7600f;
        result.decayHfRatio = 1.5300f;
        result.decayLfRatio = 0.2600f;
        result.reflectionsGain = 1.1220f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.9953f;
        result.lateReverbDelay = 0.0160f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1430f;
        result.echoDepth = 0.0800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceCourtyard() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.5900f;
        result.gain = 0.3162f;
        result.gainHf = 0.2818f;
        result.gainLf = 0.3162f;
        result.decayTime = 2.0400f;
        result.decayHfRatio = 1.2000f;
        result.decayLfRatio = 0.3800f;
        result.reflectionsGain = 0.3162f;
        result.reflectionsDelay = 0.1730f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.3162f;
        result.lateReverbDelay = 0.0430f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2350f;
        result.echoDepth = 0.4800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb icepalaceAlcove() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8400f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.2818f;
        result.decayTime = 2.7600f;
        result.decayHfRatio = 1.4600f;
        result.decayLfRatio = 0.2800f;
        result.reflectionsGain = 1.1220f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1610f;
        result.echoDepth = 0.0900f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 12428.5000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spacestationSmallRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.2109f;
        result.diffusion = 0.7000f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.8913f;
        result.decayTime = 1.7200f;
        result.decayHfRatio = 0.8200f;
        result.decayLfRatio = 0.5500f;
        result.reflectionsGain = 0.7943f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0130f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1880f;
        result.echoDepth = 0.2600f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spaceStationShortPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.2109f;
        result.diffusion = 0.8700f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.5700f;
        result.decayHfRatio = 0.5000f;
        result.decayLfRatio = 0.5500f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0160f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1720f;
        result.echoDepth = 0.2000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spaceStationMediumRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.2109f;
        result.diffusion = 0.7500f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.0100f;
        result.decayHfRatio = 0.5000f;
        result.decayLfRatio = 0.5500f;
        result.reflectionsGain = 0.3981f;
        result.reflectionsDelay = 0.0340f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0350f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2090f;
        result.echoDepth = 0.3100f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spaceStationLargeRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 0.8100f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.8900f;
        result.decayHfRatio = 0.3800f;
        result.decayLfRatio = 0.6100f;
        result.reflectionsGain = 0.3162f;
        result.reflectionsDelay = 0.0560f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0350f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2330f;
        result.echoDepth = 0.2800f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spaceStationLongPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 0.8913f;
        result.decayTime = 4.6200f;
        result.decayHfRatio = 0.6200f;
        result.decayLfRatio = 0.5500f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0310f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.2300f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spaceStationHall() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 0.8700f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 0.8913f;
        result.decayTime = 7.1100f;
        result.decayHfRatio = 0.3800f;
        result.decayLfRatio = 0.6100f;
        result.reflectionsGain = 0.1778f;
        result.reflectionsDelay = 0.1000f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.6310f;
        result.lateReverbDelay = 0.0470f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.2500f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spaceStationCupboard() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.1715f;
        result.diffusion = 0.5600f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.8913f;
        result.decayTime = 0.7900f;
        result.decayHfRatio = 0.8100f;
        result.decayLfRatio = 0.5500f;
        result.reflectionsGain = 1.4125f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.7783f;
        result.lateReverbDelay = 0.0180f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1810f;
        result.echoDepth = 0.3100f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb spaceStationAlcove() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.2109f;
        result.diffusion = 0.7800f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.8913f;
        result.decayTime = 1.1600f;
        result.decayHfRatio = 0.8100f;
        result.decayLfRatio = 0.5500f;
        result.reflectionsGain = 1.4125f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.0000f;
        result.lateReverbDelay = 0.0180f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1920f;
        result.echoDepth = 0.2100f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 3316.1001f;
        result.lfReference = 458.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenSmallRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1122f;
        result.gainLf = 0.3162f;
        result.decayTime = 0.7900f;
        result.decayHfRatio = 0.3200f;
        result.decayLfRatio = 0.8700f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0320f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0290f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenShortPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1259f;
        result.gainLf = 0.3162f;
        result.decayTime = 1.7500f;
        result.decayHfRatio = 0.5000f;
        result.decayLfRatio = 0.8700f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.6310f;
        result.lateReverbDelay = 0.0240f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenMediumRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1000f;
        result.gainLf = 0.2818f;
        result.decayTime = 1.4700f;
        result.decayHfRatio = 0.4200f;
        result.decayLfRatio = 0.8200f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0490f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0290f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenLargeRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0891f;
        result.gainLf = 0.2818f;
        result.decayTime = 2.6500f;
        result.decayHfRatio = 0.3300f;
        result.decayLfRatio = 0.8200f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0660f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7943f;
        result.lateReverbDelay = 0.0490f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenLongPassage() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1000f;
        result.gainLf = 0.3162f;
        result.decayTime = 1.9900f;
        result.decayHfRatio = 0.4000f;
        result.decayLfRatio = 0.7900f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.4467f;
        result.lateReverbDelay = 0.0360f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenHall() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0794f;
        result.gainLf = 0.2818f;
        result.decayTime = 3.4500f;
        result.decayHfRatio = 0.3000f;
        result.decayLfRatio = 0.8200f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0880f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7943f;
        result.lateReverbDelay = 0.0630f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenCupboard() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1413f;
        result.gainLf = 0.3162f;
        result.decayTime = 0.5600f;
        result.decayHfRatio = 0.4600f;
        result.decayLfRatio = 0.9100f;
        result.reflectionsGain = 1.1220f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0280f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenCourtyard() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.6500f;
        result.gain = 0.3162f;
        result.gainHf = 0.0794f;
        result.gainLf = 0.3162f;
        result.decayTime = 1.7900f;
        result.decayHfRatio = 0.3500f;
        result.decayLfRatio = 0.7900f;
        result.reflectionsGain = 0.5623f;
        result.reflectionsDelay = 0.1230f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1000f;
        result.lateReverbDelay = 0.0320f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb woodenAlcove() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1259f;
        result.gainLf = 0.3162f;
        result.decayTime = 1.2200f;
        result.decayHfRatio = 0.6200f;
        result.decayLfRatio = 0.9100f;
        result.reflectionsGain = 1.1220f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7079f;
        result.lateReverbDelay = 0.0240f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4705.0000f;
        result.lfReference = 99.6000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb sportEmptystadium() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.4467f;
        result.gainLf = 0.7943f;
        result.decayTime = 6.2600f;
        result.decayHfRatio = 0.5100f;
        result.decayLfRatio = 1.1000f;
        result.reflectionsGain = 0.0631f;
        result.reflectionsDelay = 0.1830f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.3981f;
        result.lateReverbDelay = 0.0380f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb sportSquashCourt() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7500f;
        result.gain = 0.3162f;
        result.gainHf = 0.3162f;
        result.gainLf = 0.7943f;
        result.decayTime = 2.2200f;
        result.decayHfRatio = 0.9100f;
        result.decayLfRatio = 1.1600f;
        result.reflectionsGain = 0.4467f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7943f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1260f;
        result.echoDepth = 0.1900f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 7176.8999f;
        result.lfReference = 211.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb sportSmallSwimmingpool() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7000f;
        result.gain = 0.3162f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.8913f;
        result.decayTime = 2.7600f;
        result.decayHfRatio = 1.2500f;
        result.decayLfRatio = 1.1400f;
        result.reflectionsGain = 0.6310f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7943f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1790f;
        result.echoDepth = 0.1500f;
        result.modulationTime = 0.8950f;
        result.modulationDepth = 0.1900f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb sportLargeSwimmingpool() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.7943f;
        result.gainLf = 1.0000f;
        result.decayTime = 5.4900f;
        result.decayHfRatio = 1.3100f;
        result.decayLfRatio = 1.1400f;
        result.reflectionsGain = 0.4467f;
        result.reflectionsDelay = 0.0390f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.5012f;
        result.lateReverbDelay = 0.0490f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2220f;
        result.echoDepth = 0.5500f;
        result.modulationTime = 1.1590f;
        result.modulationDepth = 0.2100f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb sportGymnasium() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8100f;
        result.gain = 0.3162f;
        result.gainHf = 0.4467f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.1400f;
        result.decayHfRatio = 1.0600f;
        result.decayLfRatio = 1.3500f;
        result.reflectionsGain = 0.3981f;
        result.reflectionsDelay = 0.0290f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.5623f;
        result.lateReverbDelay = 0.0450f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1460f;
        result.echoDepth = 0.1400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 7176.8999f;
        result.lfReference = 211.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb sportFullStadium() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0708f;
        result.gainLf = 0.7943f;
        result.decayTime = 5.2500f;
        result.decayHfRatio = 0.1700f;
        result.decayLfRatio = 0.8000f;
        result.reflectionsGain = 0.1000f;
        result.reflectionsDelay = 0.1880f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.2818f;
        result.lateReverbDelay = 0.0380f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb sportStadiumTannoy() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7800f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.5012f;
        result.decayTime = 2.5300f;
        result.decayHfRatio = 0.8800f;
        result.decayLfRatio = 0.6800f;
        result.reflectionsGain = 0.2818f;
        result.reflectionsDelay = 0.2300f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.5012f;
        result.lateReverbDelay = 0.0630f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.2000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb prefabWorkshop() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1413f;
        result.gainLf = 0.3981f;
        result.decayTime = 0.7600f;
        result.decayHfRatio = 1.0000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0120f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb prefabSchoolRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4022f;
        result.diffusion = 0.6900f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 0.5012f;
        result.decayTime = 0.9800f;
        result.decayHfRatio = 0.4500f;
        result.decayLfRatio = 0.1800f;
        result.reflectionsGain = 1.4125f;
        result.reflectionsDelay = 0.0170f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0150f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.0950f;
        result.echoDepth = 0.1400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 7176.8999f;
        result.lfReference = 211.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb prefabPractiseRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4022f;
        result.diffusion = 0.8700f;
        result.gain = 0.3162f;
        result.gainHf = 0.3981f;
        result.gainLf = 0.5012f;
        result.decayTime = 1.1200f;
        result.decayHfRatio = 0.5600f;
        result.decayLfRatio = 0.1800f;
        result.reflectionsGain = 1.2589f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0110f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.0950f;
        result.echoDepth = 0.1400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 7176.8999f;
        result.lfReference = 211.2000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb prefabOuthouse() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.1122f;
        result.gainLf = 0.1585f;
        result.decayTime = 1.3800f;
        result.decayHfRatio = 0.3800f;
        result.decayLfRatio = 0.3500f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0240f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.6310f;
        result.lateReverbDelay = 0.0440f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1210f;
        result.echoDepth = 0.1700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 107.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb prefabCaravan() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0891f;
        result.gainLf = 0.1259f;
        result.decayTime = 0.4300f;
        result.decayHfRatio = 1.5000f;
        result.decayLfRatio = 1.0000f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0120f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.9953f;
        result.lateReverbDelay = 0.0120f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb domeTomb() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7900f;
        result.gain = 0.3162f;
        result.gainHf = 0.3548f;
        result.gainLf = 0.2239f;
        result.decayTime = 4.1800f;
        result.decayHfRatio = 0.2100f;
        result.decayLfRatio = 0.1000f;
        result.reflectionsGain = 0.3868f;
        result.reflectionsDelay = 0.0300f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.6788f;
        result.lateReverbDelay = 0.0220f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1770f;
        result.echoDepth = 0.1900f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 20.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb pipeSmall() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.3548f;
        result.gainLf = 0.2239f;
        result.decayTime = 5.0400f;
        result.decayHfRatio = 0.1000f;
        result.decayLfRatio = 0.1000f;
        result.reflectionsGain = 0.5012f;
        result.reflectionsDelay = 0.0320f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 2.5119f;
        result.lateReverbDelay = 0.0150f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 20.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb domeSaintPauls() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8700f;
        result.gain = 0.3162f;
        result.gainHf = 0.3548f;
        result.gainLf = 0.2239f;
        result.decayTime = 10.4800f;
        result.decayHfRatio = 0.1900f;
        result.decayLfRatio = 0.1000f;
        result.reflectionsGain = 0.1778f;
        result.reflectionsDelay = 0.0900f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0420f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.1200f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 20.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb pipeLongThin() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.2560f;
        result.diffusion = 0.9100f;
        result.gain = 0.3162f;
        result.gainHf = 0.4467f;
        result.gainLf = 0.2818f;
        result.decayTime = 9.2100f;
        result.decayHfRatio = 0.1800f;
        result.decayLfRatio = 0.1000f;
        result.reflectionsGain = 0.7079f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7079f;
        result.lateReverbDelay = 0.0220f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 20.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb pipeLarge() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.3548f;
        result.gainLf = 0.2239f;
        result.decayTime = 8.4500f;
        result.decayHfRatio = 0.1000f;
        result.decayLfRatio = 0.1000f;
        result.reflectionsGain = 0.3981f;
        result.reflectionsDelay = 0.0460f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.5849f;
        result.lateReverbDelay = 0.0320f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 20.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb pipeResonant() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.1373f;
        result.diffusion = 0.9100f;
        result.gain = 0.3162f;
        result.gainHf = 0.4467f;
        result.gainLf = 0.2818f;
        result.decayTime = 6.8100f;
        result.decayHfRatio = 0.1800f;
        result.decayLfRatio = 0.1000f;
        result.reflectionsGain = 0.7079f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.0000f;
        result.lateReverbDelay = 0.0220f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 20.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb outdoorsBackyard() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.4500f;
        result.gain = 0.3162f;
        result.gainHf = 0.2512f;
        result.gainLf = 0.5012f;
        result.decayTime = 1.1200f;
        result.decayHfRatio = 0.3400f;
        result.decayLfRatio = 0.4600f;
        result.reflectionsGain = 0.4467f;
        result.reflectionsDelay = 0.0690f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7079f;
        result.lateReverbDelay = 0.0230f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2180f;
        result.echoDepth = 0.3400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4399.1001f;
        result.lfReference = 242.9000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb outdoorsRollingPlains() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.0112f;
        result.gainLf = 0.6310f;
        result.decayTime = 2.1300f;
        result.decayHfRatio = 0.2100f;
        result.decayLfRatio = 0.4600f;
        result.reflectionsGain = 0.1778f;
        result.reflectionsDelay = 0.3000f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.4467f;
        result.lateReverbDelay = 0.0190f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 1.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4399.1001f;
        result.lfReference = 242.9000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb outdoorsDeepCanyon() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7400f;
        result.gain = 0.3162f;
        result.gainHf = 0.1778f;
        result.gainLf = 0.6310f;
        result.decayTime = 3.8900f;
        result.decayHfRatio = 0.2100f;
        result.decayLfRatio = 0.4600f;
        result.reflectionsGain = 0.3162f;
        result.reflectionsDelay = 0.2230f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.3548f;
        result.lateReverbDelay = 0.0190f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 1.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4399.1001f;
        result.lfReference = 242.9000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb outdoorsCreek() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.3500f;
        result.gain = 0.3162f;
        result.gainHf = 0.1778f;
        result.gainLf = 0.5012f;
        result.decayTime = 2.1300f;
        result.decayHfRatio = 0.2100f;
        result.decayLfRatio = 0.4600f;
        result.reflectionsGain = 0.3981f;
        result.reflectionsDelay = 0.1150f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1995f;
        result.lateReverbDelay = 0.0310f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2180f;
        result.echoDepth = 0.3400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 4399.1001f;
        result.lfReference = 242.9000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb outdoorsValley() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.2800f;
        result.gain = 0.3162f;
        result.gainHf = 0.0282f;
        result.gainLf = 0.1585f;
        result.decayTime = 2.8800f;
        result.decayHfRatio = 0.2600f;
        result.decayLfRatio = 0.3500f;
        result.reflectionsGain = 0.1413f;
        result.reflectionsDelay = 0.2630f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.3981f;
        result.lateReverbDelay = 0.1000f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.3400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 107.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb moodHeaven() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.9400f;
        result.gain = 0.3162f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.4467f;
        result.decayTime = 5.0400f;
        result.decayHfRatio = 1.1200f;
        result.decayLfRatio = 0.5600f;
        result.reflectionsGain = 0.2427f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0290f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0800f;
        result.modulationTime = 2.7420f;
        result.modulationDepth = 0.0500f;
        result.airAbsorptionGainHf = 0.9977f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb moodHell() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.5700f;
        result.gain = 0.3162f;
        result.gainHf = 0.3548f;
        result.gainLf = 0.4467f;
        result.decayTime = 3.5700f;
        result.decayHfRatio = 0.4900f;
        result.decayLfRatio = 2.0000f;
        result.reflectionsGain = 0.0000f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1100f;
        result.echoDepth = 0.0400f;
        result.modulationTime = 2.1090f;
        result.modulationDepth = 0.5200f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 139.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb moodMemory() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8500f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 0.3548f;
        result.decayTime = 4.0600f;
        result.decayHfRatio = 0.8200f;
        result.decayLfRatio = 0.5600f;
        result.reflectionsGain = 0.0398f;
        result.reflectionsDelay = 0.0000f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.1220f;
        result.lateReverbDelay = 0.0000f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.4740f;
        result.modulationDepth = 0.4500f;
        result.airAbsorptionGainHf = 0.9886f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb drivingCommentator() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 0.5012f;
        result.decayTime = 2.4200f;
        result.decayHfRatio = 0.8800f;
        result.decayLfRatio = 0.6800f;
        result.reflectionsGain = 0.1995f;
        result.reflectionsDelay = 0.0930f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.2512f;
        result.lateReverbDelay = 0.0170f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 1.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9886f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb drivingPitGarage() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.4287f;
        result.diffusion = 0.5900f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.5623f;
        result.decayTime = 1.7200f;
        result.decayHfRatio = 0.9300f;
        result.decayLfRatio = 0.8700f;
        result.reflectionsGain = 0.5623f;
        result.reflectionsDelay = 0.0000f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0160f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.1100f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb drivingInCarRacer() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.0832f;
        result.diffusion = 0.8000f;
        result.gain = 0.3162f;
        result.gainHf = 1.0000f;
        result.gainLf = 0.7943f;
        result.decayTime = 0.1700f;
        result.decayHfRatio = 2.0000f;
        result.decayLfRatio = 0.4100f;
        result.reflectionsGain = 1.7783f;
        result.reflectionsDelay = 0.0070f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7079f;
        result.lateReverbDelay = 0.0150f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 10268.2002f;
        result.lfReference = 251.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb drivingInCarSports() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.0832f;
        result.diffusion = 0.8000f;
        result.gain = 0.3162f;
        result.gainHf = 0.6310f;
        result.gainLf = 1.0000f;
        result.decayTime = 0.1700f;
        result.decayHfRatio = 0.7500f;
        result.decayLfRatio = 0.4100f;
        result.reflectionsGain = 1.0000f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.5623f;
        result.lateReverbDelay = 0.0000f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 10268.2002f;
        result.lfReference = 251.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb drivingInCarLuxury() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.2560f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.1000f;
        result.gainLf = 0.5012f;
        result.decayTime = 0.1300f;
        result.decayHfRatio = 0.4100f;
        result.decayLfRatio = 0.4600f;
        result.reflectionsGain = 0.7943f;
        result.reflectionsDelay = 0.0100f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.5849f;
        result.lateReverbDelay = 0.0100f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 10268.2002f;
        result.lfReference = 251.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb drivingFullGrandStand() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 0.2818f;
        result.gainLf = 0.6310f;
        result.decayTime = 3.0100f;
        result.decayHfRatio = 1.3700f;
        result.decayLfRatio = 1.2800f;
        result.reflectionsGain = 0.3548f;
        result.reflectionsDelay = 0.0900f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1778f;
        result.lateReverbDelay = 0.0490f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 10420.2002f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb drivingEmptyGrandStand() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 1.0000f;
        result.gain = 0.3162f;
        result.gainHf = 1.0000f;
        result.gainLf = 0.7943f;
        result.decayTime = 4.6200f;
        result.decayHfRatio = 1.7500f;
        result.decayLfRatio = 1.4000f;
        result.reflectionsGain = 0.2082f;
        result.reflectionsDelay = 0.0900f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.2512f;
        result.lateReverbDelay = 0.0490f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 10420.2002f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb drivingTunnel() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8100f;
        result.gain = 0.3162f;
        result.gainHf = 0.3981f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.4200f;
        result.decayHfRatio = 0.9400f;
        result.decayLfRatio = 1.3100f;
        result.reflectionsGain = 0.7079f;
        result.reflectionsDelay = 0.0510f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7079f;
        result.lateReverbDelay = 0.0470f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2140f;
        result.echoDepth = 0.0500f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 155.3000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb cityStreets() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7800f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.8913f;
        result.decayTime = 1.7900f;
        result.decayHfRatio = 1.1200f;
        result.decayLfRatio = 0.9100f;
        result.reflectionsGain = 0.2818f;
        result.reflectionsDelay = 0.0460f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.1995f;
        result.lateReverbDelay = 0.0280f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.2000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb citySubway() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7400f;
        result.gain = 0.3162f;
        result.gainHf = 0.7079f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.0100f;
        result.decayHfRatio = 1.2300f;
        result.decayLfRatio = 0.9100f;
        result.reflectionsGain = 0.7079f;
        result.reflectionsDelay = 0.0460f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0280f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1250f;
        result.echoDepth = 0.2100f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb cityMuseum() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.1778f;
        result.gainLf = 0.1778f;
        result.decayTime = 3.2800f;
        result.decayHfRatio = 1.4000f;
        result.decayLfRatio = 0.5700f;
        result.reflectionsGain = 0.2512f;
        result.reflectionsDelay = 0.0390f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0340f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1300f;
        result.echoDepth = 0.1700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 107.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb cityLibrary() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.2818f;
        result.gainLf = 0.0891f;
        result.decayTime = 2.7600f;
        result.decayHfRatio = 0.8900f;
        result.decayLfRatio = 0.4100f;
        result.reflectionsGain = 0.3548f;
        result.reflectionsDelay = 0.0290f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0200f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1300f;
        result.echoDepth = 0.1700f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 2854.3999f;
        result.lfReference = 107.5000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    public static EaxReverb cityUnderpass() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8200f;
        result.gain = 0.3162f;
        result.gainHf = 0.4467f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.5700f;
        result.decayHfRatio = 1.1200f;
        result.decayLfRatio = 0.9100f;
        result.reflectionsGain = 0.3981f;
        result.reflectionsDelay = 0.0590f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.8913f;
        result.lateReverbDelay = 0.0370f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.1400f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9920f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb cityAbandoned() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.6900f;
        result.gain = 0.3162f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.8913f;
        result.decayTime = 3.2800f;
        result.decayHfRatio = 1.1700f;
        result.decayLfRatio = 0.9100f;
        result.reflectionsGain = 0.4467f;
        result.reflectionsDelay = 0.0440f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.2818f;
        result.lateReverbDelay = 0.0240f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.2000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9966f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb dustyRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 0.3645f;
        result.diffusion = 0.5600f;
        result.gain = 0.3162f;
        result.gainHf = 0.7943f;
        result.gainLf = 0.7079f;
        result.decayTime = 1.7900f;
        result.decayHfRatio = 0.3800f;
        result.decayLfRatio = 0.2100f;
        result.reflectionsGain = 0.5012f;
        result.reflectionsDelay = 0.0020f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.2589f;
        result.lateReverbDelay = 0.0060f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2020f;
        result.echoDepth = 0.0500f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.0000f;
        result.airAbsorptionGainHf = 0.9886f;
        result.hfReference = 13046.0000f;
        result.lfReference = 163.3000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb chapel() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.8400f;
        result.gain = 0.3162f;
        result.gainHf = 0.5623f;
        result.gainLf = 1.0000f;
        result.decayTime = 4.6200f;
        result.decayHfRatio = 0.6400f;
        result.decayLfRatio = 1.2300f;
        result.reflectionsGain = 0.4467f;
        result.reflectionsDelay = 0.0320f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 0.7943f;
        result.lateReverbDelay = 0.0490f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.2500f;
        result.echoDepth = 0.0000f;
        result.modulationTime = 0.2500f;
        result.modulationDepth = 0.1100f;
        result.airAbsorptionGainHf = 0.9943f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = true;
        return result;
    }


    public static EaxReverb smallWaterRoom() {
        final EaxReverb result = new EaxReverb();
        result.density = 1.0000f;
        result.diffusion = 0.7000f;
        result.gain = 0.3162f;
        result.gainHf = 0.4477f;
        result.gainLf = 1.0000f;
        result.decayTime = 1.5100f;
        result.decayHfRatio = 1.2500f;
        result.decayLfRatio = 1.1400f;
        result.reflectionsGain = 0.8913f;
        result.reflectionsDelay = 0.0200f;
        result.reflectionsPan.set(0f, 0f, 0f);
        result.lateReverbGain = 1.4125f;
        result.lateReverbDelay = 0.0300f;
        result.lateReverbPan.set(0f, 0f, 0f);
        result.echoTime = 0.1790f;
        result.echoDepth = 0.1500f;
        result.modulationTime = 0.8950f;
        result.modulationDepth = 0.1900f;
        result.airAbsorptionGainHf = 0.9920f;
        result.hfReference = 5000.0000f;
        result.lfReference = 250.0000f;
        result.roomRolloffFactor = 0.0000f;
        result.decayHfLimit = false;
        return result;
    }


    @Override
    protected void apply(int effectId) {
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
