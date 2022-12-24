package de.pottgames.tuningfork.jukebox.playlist;

import de.pottgames.tuningfork.jukebox.JukeBox;

/**
 * The {@link JukeBox} fetches {@link PlayList}s from a {@link PlayListProvider}.
 *
 * @author Matthias
 *
 */
public interface PlayListProvider {

    /**
     * Returns the next PlayList. May be null if none is available.
     *
     * @return the {@link PlayList}
     */
    PlayList next();


    /**
     * Returns true if a {@link PlayList} is available via {@link #next()}.
     *
     * @return true if a {@link PlayList} is available, false otherwise
     */
    boolean hasNext();

}
