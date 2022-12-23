package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.jukebox.song.Song;

public class PlayList {
    protected final Array<Song> songs                  = new Array<>();
    protected int               songIndex;
    protected boolean           playedThrough          = false;
    protected boolean           loop                   = false;
    protected boolean           shuffleAfterPlaytrough = false;


    public void addSong(Song song) {
        this.songs.add(song);
    }


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


    public void reset() {
        this.songIndex = 0;
        this.playedThrough = false;
    }


    public boolean isPlayedThrough() {
        return this.playedThrough;
    }


    public void shuffle() {
        this.songs.shuffle();
    }


    public void setShuffleAfterPlaytrough(boolean value) {
        this.shuffleAfterPlaytrough = value;
    }


    public void setLooping(boolean loop) {
        this.loop = loop;
    }


    public boolean isLoop() {
        return this.loop;
    }

}
