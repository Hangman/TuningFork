package de.pottgames.tuningfork.jukebox.song;

import de.pottgames.tuningfork.SoundSource;

/**
 * Identifies objects to be used as a source of a {@link Song}.
 *
 * @author Matthias
 *
 */
public abstract class SongSource extends SoundSource {

    protected SongSource() {
    }


    /**
     * Returns the duration in seconds. Might return -1 if the duration is not available.
     *
     * @return the duration in seconds
     */
    public abstract float getDuration();


    /**
     * Returns the playback position in seconds.
     *
     * @return the playback position in seconds
     */
    public abstract float getPlaybackPosition();

}
