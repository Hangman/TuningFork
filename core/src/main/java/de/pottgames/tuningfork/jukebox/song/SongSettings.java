package de.pottgames.tuningfork.jukebox.song;

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
     * @param type
     * @param alpha
     *
     * @return the fade volume
     */
    public float fadeVolume(FadeType type, float alpha) {
        if (type == FadeType.IN) {
            return MathUtils.clamp(this.fadeInCurve.apply(alpha) * this.volume, 0f, 1f);
        }
        if (type == FadeType.OUT) {
            return MathUtils.clamp(this.fadeOutCurve.apply(alpha) * this.volume, 0f, 1f);
        }
        return this.volume;
    }


    /**
     * Returns the fade-in duration in seconds.
     *
     * @return fade-in duration in seconds
     */
    public float getFadeInDuration() {
        return this.fadeInDuration;
    }


    /**
     * Returns the fade-out duration in seconds.
     *
     * @return fade-out duration in seconds
     */
    public float getFadeOutDuration() {
        return this.fadeOutDuration;
    }


    /**
     * Returns the fade-in curve.
     *
     * @return fade-in curve
     */
    public Interpolation getFadeInCurve() {
        return this.fadeInCurve;
    }


    /**
     * Returns the fade-out curve.
     *
     * @return fade-out curve
     */
    public Interpolation getFadeOutCurve() {
        return this.fadeOutCurve;
    }


    /**
     * Returns the standard volume.
     *
     * @return volume, ranging from 0-1
     */
    public float getVolume() {
        return this.volume;
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

}