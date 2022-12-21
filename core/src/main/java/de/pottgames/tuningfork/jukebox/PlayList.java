package de.pottgames.tuningfork.jukebox;

import com.badlogic.gdx.utils.Array;

public class PlayList {
    protected final Array<Song> songs                  = new Array<>();
    protected int               songIndex;
    protected boolean           playedFully;
    protected boolean           loop                   = false;
    protected boolean           shuffleAfterPlaytrough = false;


    public void addSong(Song song) {
        this.songs.add(song);
    }


    Song nextSong() {
        final Song song = this.songs.get(this.songIndex);
        this.songIndex++;
        if (this.songIndex >= this.songs.size) {
            this.songIndex = 0;
            this.playedFully = true;
        }
        return song;
    }


    protected void reset() {
        this.songIndex = 0;
        this.playedFully = false;
        if (this.shuffleAfterPlaytrough) {
            this.shuffle();
        }
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

}
