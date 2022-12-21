package de.pottgames.tuningfork.jukebox;

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


    public abstract float getDuration();


    public abstract float getPlaybackPosition();

}
