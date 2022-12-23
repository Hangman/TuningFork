package de.pottgames.tuningfork.jukebox;

import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.song.Song;

public interface JukeBoxObserver {

    void onSongStart(Song song);


    void onSongEnd(Song song);


    void onPlayListStart(PlayList playList);


    void onPlayListEnd(PlayList playList);


    void onJukeBoxStart();


    void onJukeBoxPause();


    void onJukeBoxEnd();

}
