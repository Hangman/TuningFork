package de.pottgames.tuningfork.jukebox;

import com.badlogic.gdx.utils.Pool.Poolable;

import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.song.Song;

public class JukeBoxEvent implements Poolable {
    private JukeBoxEventType type;
    private Song             song;
    private PlayList         playList;


    @Override
    public void reset() {
        this.setType(JukeBoxEventType.NONE);
        this.setSong(null);
        this.setPlayList(null);
    }


    public Song getSong() {
        return this.song;
    }


    public void setSong(Song song) {
        this.song = song;
    }


    public PlayList getPlayList() {
        return this.playList;
    }


    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }


    public JukeBoxEventType getType() {
        return this.type;
    }


    public void setType(JukeBoxEventType type) {
        this.type = type;
        if (this.type == null) {
            this.type = JukeBoxEventType.NONE;
        }
    }


    public enum JukeBoxEventType {
        SONG_START, SONG_END, PLAYLIST_START, PLAYLIST_END, JUKEBOX_START, JUKEBOX_END, JUKEBOX_PAUSE, NONE
    }

}
