package de.pottgames.tuningfork;

import com.badlogic.gdx.math.Vector3;

/**
 * A sound source is used to represent the position, speed and other attributes of a sound in the virtual audio world. It enables you to play, pause, stop,
 * position sounds and let's you set different effects on it.
 *
 * @author Matthias
 *
 */
public interface SoundSource {

    /**
     * Sets the base volume of this sound source. The final output volume might differ depending on the source's position, speed, etc.
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume.
     */
    void setVolume(float volume);


    /**
     * Sets the pitch of this sound source.
     *
     * @param pitch in the range of 0.5 - 2.0 with 0.5 being the lowest and 2.0 being the highest supported pitch.
     */
    void setPitch(float pitch);


    /**
     * Starts the playback of this sound source.
     */
    void play();


    /**
     * Sets wether the position attribute of this sound source should be handled as relative or absolute values to the listener's position.<br>
     * If set to false, the position is the absolute position in the 3D world.<br>
     * If set to true, the position is relative to the listener's position, meaning a position of x=0,y=0,z=0 is always identical to the listener's position.
     *
     * @param relative true = relative, false = absolute
     */
    void setRelative(boolean relative);


    /**
     * Sets the positions of this sound source in the virtual world.
     *
     * @param position
     */
    void setPosition(Vector3 position);


    /**
     * Sets the positions of this sound source in the virtual world.
     *
     * @param x
     * @param y
     * @param z
     */
    void setPosition(float x, float y, float z);


    /**
     * Sets the speed of this sound source. The speed is <b>not</b> automatically determined by changes to the position, you need to call setSpeed manually.<br>
     * The speed is only used for calculating a Doppler effect. Note that you need to call set speed on the sound listener as well in order to get a proper
     * Doppler effect.
     *
     * @param speed
     */
    void setSpeed(Vector3 speed);


    /**
     * Sets the speed of this sound source. The speed is <b>not</b> automatically determined by changes to the position, you need to call setSpeed manually.<br>
     * The speed is only used for calculating a Doppler effect. Note that you need to call set speed on the sound listener as well in order to get a proper
     * Doppler effect.
     *
     * @param x
     * @param y
     * @param z
     */
    void setSpeed(float x, float y, float z);


    /**
     * Sets wether this sound source should loop. When looping is enabled, the source will immediately play the sound again when it's finished playing.
     *
     * @param looping
     */
    void setLooping(boolean looping);


    /**
     * Returns wether this sound source is currently playing.
     *
     * @return true when this sound source is playing, false otherwise.
     */
    boolean isPlaying();


    /**
     * Pauses the sound playback.
     */
    void pause();


    /**
     * Stops the sound playback and rewinds it.
     */
    void stop();


    /**
     * Returns the duration of the attached sound.
     *
     * @return the duration of the attached sound. -1f if no sound is attached to it.
     */
    float getDuration();


    /**
     * Releases this sound source which makes it available again. Always call this after you're done using it.
     */
    void free();

}
