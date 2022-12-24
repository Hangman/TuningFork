package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.jukebox.song.Song;

/**
 * A {@link PlayList} is a collection of songs that can be fetched in order.
 *
 * @author Matthias
 *
 */
public class PlayList {
    protected final Array<Song> songs                  = new Array<>();
    protected int               songIndex;
    protected boolean           playedThrough          = false;
    protected boolean           loop                   = false;
    protected boolean           shuffleAfterPlaytrough = false;


    /**
     * Adds a song to the end of the list.
     *
     * @param song
     */
    public void addSong(Song song) {
        this.songs.add(song);
    }


    /**
     * Returns the next {@link Song} in the list. Starts over if the last song of the list is fetched.
     *
     * @return the next {@link Song} in the list
     */
    public Song nextSong() {
        final Song song = this.songs.get(this.songIndex);
        this.songIndex++;
        if (this.songIndex >= this.songs.size) {
            this.songIndex = 0;
            this.playedThrough = true;
            if (this.shuffleAfterPlaytrough) {
                this.shuffle();
            }
        }
        return song;
    }


    /**
     * Resets the index to 0, so the next call to {@link #nextSong()} will return the first song in the list. {@link #isPlayedThrough()} will return false
     * afterwards.
     */
    public void reset() {
        this.songIndex = 0;
        this.playedThrough = false;
    }


    /**
     * Returns true if the all {@link Song}s from the list have been fetched.
     *
     * @return true if all songs have been fetched
     */
    public boolean isPlayedThrough() {
        return this.playedThrough;
    }


    /**
     * Shuffles the internal list of {@link Song}s.
     */
    public void shuffle() {
        this.songs.shuffle();
    }


    /**
     * If set to true, the internal list of {@link Song}s is shuffled after a complete playthrough of this {@link PlayList}.
     *
     * @param value
     */
    public void setShuffleAfterPlaytrough(boolean value) {
        this.shuffleAfterPlaytrough = value;
    }


    /**
     * If set to true, this {@link PlayList} indicates that it should be played on repeat.
     *
     * @param loop
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
        return this.loop;
    }

}
