package de.pottgames.tuningfork.jukebox;

import com.badlogic.gdx.utils.Pool.Poolable;

import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.song.Song;

public class JukeBoxEvent implements Poolable {
    private JukeBoxEventType type;
    private Song             song;
    private PlayList         playList;
    private float            oldVolume = -1f;
    private float            newVolume = -1f;


    @Override
    public void reset() {
        setType(JukeBoxEventType.NONE);
        setSong(null);
        setPlayList(null);
        oldVolume = -1f;
        newVolume = -1f;
    }


    public Song getSong() {
        return song;
    }


    public void setSong(Song song) {
        this.song = song;
    }


    public PlayList getPlayList() {
        return playList;
    }


    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }


    public JukeBoxEventType getType() {
        return type;
    }


    public void setType(JukeBoxEventType type) {
        this.type = type;
        if (this.type == null) {
            this.type = JukeBoxEventType.NONE;
        }
    }


    public float getOldVolume() {
        return oldVolume;
    }


    public void setOldVolume(float oldVolume) {
        this.oldVolume = oldVolume;
    }


    public float getNewVolume() {
        return newVolume;
    }


    public void setNewVolume(float newVolume) {
        this.newVolume = newVolume;
    }


    public enum JukeBoxEventType {
        SONG_START, SONG_END, PLAYLIST_START, PLAYLIST_END, JUKEBOX_START, JUKEBOX_END, JUKEBOX_PAUSE, NONE
    }

}
