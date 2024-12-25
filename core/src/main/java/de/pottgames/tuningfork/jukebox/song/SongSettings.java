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

package de.pottgames.tuningfork.jukebox.song;

import java.util.Objects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

/**
 * An immutable settings data class for configuring a {@link Song}. It allows settings a general volume and fade-in and fade-out parameters which are used to
 * control the playback of a song.
 *
 * @author Matthias
 *
 */
public class SongSettings {
    /**
     * Does not interpolate and ignores the alpha value, always applies 1.
     */
    public static final Interpolation NO_INTERPOLATION = new NoInterpolation();

    /**
     * No fade-in and no fade-out, general volume of 1.
     */
    public static final SongSettings DEFAULT        = SongSettings.noFade(1f);
    /**
     * Fade-in and fade-out of 1 second, linear interpolation, general volume of 1.
     */
    public static final SongSettings DEFAULT_LINEAR = SongSettings.linear(1f, 1f, 1f);

    private final float         fadeInDuration;
    private final float         fadeOutDuration;
    private final Interpolation fadeInCurve;
    private final Interpolation fadeOutCurve;
    private final float         volume;


    /**
     * Creates a SongSettings instance that has fade-in and fade-out turned off.
     *
     * @param volume the volume this song will be played with, ranging from 0 - 1
     *
     * @return the settings
     */
    public static SongSettings noFade(float volume) {
        return new SongSettings(volume, 0f, SongSettings.NO_INTERPOLATION, 0f, SongSettings.NO_INTERPOLATION);
    }


    /**
     * Creates a SongSettings instance with linear fade-in and fade-out interpolation.
     *
     * @param volume the volume this song will be played with, ranging from 0 - 1
     * @param fadeInDuration fade-in duration in seconds
     * @param fadeOutDuration fade-out duration in seconds
     *
     * @return the settings
     */
    public static SongSettings linear(float volume, float fadeInDuration, float fadeOutDuration) {
        return new SongSettings(volume, fadeInDuration, Interpolation.linear, fadeOutDuration, Interpolation.linear);
    }


    /**
     * Creates a SongSettings instance with the given parameters.
     *
     * @param volume the volume, ranging from 0 - 1. Will be clamped if it exceeds the range.
     * @param fadeInDuration fade-in duration in seconds
     * @param fadeInCurve fade-in curve
     * @param fadeOutDuration fade-out duration in seconds
     * @param fadeOutCurve fade-out curve
     */
    public SongSettings(float volume, float fadeInDuration, Interpolation fadeInCurve, float fadeOutDuration, Interpolation fadeOutCurve) {
        this.volume = MathUtils.clamp(volume, 0f, 1f);
        this.fadeInDuration = fadeInDuration;
        this.fadeInCurve = fadeInCurve != null ? fadeInCurve : SongSettings.NO_INTERPOLATION;
        this.fadeOutDuration = fadeOutDuration;
        this.fadeOutCurve = fadeOutCurve != null ? fadeOutCurve : SongSettings.NO_INTERPOLATION;
    }


    /**
     * Returns the fade volume for a given fade type and alpha value.
     *
     * @param type the fade type
     * @param alpha the alpha value
     *
     * @return the fade volume
     */
    public float fadeVolume(FadeType type, float alpha) {
        if (type == FadeType.IN) {
            return MathUtils.clamp(fadeInCurve.apply(alpha) * volume, 0f, 1f);
        }
        if (type == FadeType.OUT) {
            return MathUtils.clamp(fadeOutCurve.apply(alpha) * volume, 0f, 1f);
        }
        return volume;
    }


    /**
     * Returns the fade-in duration in seconds.
     *
     * @return fade-in duration in seconds
     */
    public float getFadeInDuration() {
        return fadeInDuration;
    }


    /**
     * Returns the fade-out duration in seconds.
     *
     * @return fade-out duration in seconds
     */
    public float getFadeOutDuration() {
        return fadeOutDuration;
    }


    /**
     * Returns the fade-in curve.
     *
     * @return fade-in curve
     */
    public Interpolation getFadeInCurve() {
        return fadeInCurve;
    }


    /**
     * Returns the fade-out curve.
     *
     * @return fade-out curve
     */
    public Interpolation getFadeOutCurve() {
        return fadeOutCurve;
    }


    /**
     * Returns the standard volume.
     *
     * @return volume, ranging from 0-1
     */
    public float getVolume() {
        return volume;
    }


    public enum FadeType {
        IN, OUT;
    }


    /**
     * An {@link Interpolation} implementation that does not interpolate and instead always returns 1.
     *
     * @author Matthias
     *
     */
    static class NoInterpolation extends Interpolation {

        @Override
        public float apply(float a) {
            return 1f;
        }

    }


    @Override
    public int hashCode() {
        return Objects.hash(fadeInCurve, fadeInDuration, fadeOutCurve, fadeOutDuration, volume);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final SongSettings other = (SongSettings) obj;
        return Objects.equals(fadeInCurve, other.fadeInCurve) && Float.floatToIntBits(fadeInDuration) == Float.floatToIntBits(other.fadeInDuration)
                && Objects.equals(fadeOutCurve, other.fadeOutCurve) && Float.floatToIntBits(fadeOutDuration) == Float.floatToIntBits(other.fadeOutDuration)
                && Float.floatToIntBits(volume) == Float.floatToIntBits(other.volume);
    }


    @Override
    public String toString() {
        return "SongSettings [fadeInDuration=" + fadeInDuration + ", fadeOutDuration=" + fadeOutDuration + ", fadeInCurve=" + fadeInCurve + ", fadeOutCurve="
                + fadeOutCurve + ", volume=" + volume + "]";
    }

}
