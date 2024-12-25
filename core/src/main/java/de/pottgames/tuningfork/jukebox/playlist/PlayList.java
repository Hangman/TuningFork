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

package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.jukebox.song.Song;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * A {@link PlayList} is a collection of songs that can be fetched in order.
 *
 * @author Matthias
 */
public class PlayList {
    protected final TuningForkLogger logger;
    protected final Array<Song>      songs                  = new Array<>();
    protected int                    songIndex              = 0;
    protected boolean                playedThrough          = false;
    protected boolean                loop                   = false;
    protected boolean                shuffleAfterPlaytrough = false;


    public PlayList() {
        logger = Audio.get().getLogger();
    }


    /**
     * Adds a song to the end of the list.
     *
     * @param song the song
     */
    public void addSong(Song song) {
        songs.add(song);
    }


    /**
     * Returns the next {@link Song} in the list. Starts over if the last song of the list is fetched.
     *
     * @return the next {@link Song} in the list
     */
    public Song nextSong() {
        if (songs.isEmpty()) {
            logger.warn(this.getClass(), "Requested nextSong on empty PlayList: " + this);
            return null;
        }

        final Song song = songs.get(songIndex);
        songIndex++;
        if (songIndex >= songs.size) {
            songIndex = 0;
            playedThrough = true;
            if (shuffleAfterPlaytrough) {
                shuffle();
            }
        }
        return song;
    }


    /**
     * Resets the index to 0, so the next call to {@link #nextSong()} will return the first song in the list. {@link #isPlayedThrough()} will return false
     * afterwards.
     */
    public void reset() {
        songIndex = 0;
        playedThrough = false;
    }


    /**
     * Returns true if the all {@link Song}s from the list have been fetched.
     *
     * @return true if all songs have been fetched
     */
    public boolean isPlayedThrough() {
        return playedThrough;
    }


    /**
     * Shuffles the internal list of {@link Song}s.
     */
    public void shuffle() {
        songs.shuffle();
    }


    /**
     * If set to true, the internal list of {@link Song}s is shuffled after a complete play through of this {@link PlayList}.
     *
     * @param value if set to true, the playlist will be shuffled after a complete play through
     */
    public void setShuffleAfterPlaytrough(boolean value) {
        shuffleAfterPlaytrough = value;
    }


    /**
     * If set to true, this {@link PlayList} indicates that it should be played on repeat.
     *
     * @param loop true for looped playback
     */
    public void setLooping(boolean loop) {
        this.loop = loop;
    }


    /**
     * Returns true if this {@link PlayList} should be played on repeat.
     *
     * @return true for repeat
     */
    public boolean isLoop() {
        return loop;
    }


    @Override
    public String toString() {
        return "PlayList [songs=" + songs + ", songIndex=" + songIndex + ", playedThrough=" + playedThrough + ", loop=" + loop + ", shuffleAfterPlaytrough="
                + shuffleAfterPlaytrough + "]";
    }

}
