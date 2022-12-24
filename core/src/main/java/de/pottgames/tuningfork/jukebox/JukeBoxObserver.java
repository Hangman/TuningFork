package de.pottgames.tuningfork.jukebox;

import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.song.Song;

public interface JukeBoxObserver {

    /**
     * This method is called when a new song is played.
     *
     * @param song
     */
    void onSongStart(Song song);


    /**
     * This method is called when a song finished playing. It will not be called if the {@link JukeBox} has been stopped manually.
     *
     * @param song
     */
    void onSongEnd(Song song);


    /**
     * This method is called when a new {@link PlayList} is used to play songs.
     *
     * @param playList
     */
    void onPlayListStart(PlayList playList);


    /**
     * This method is called when a {@link PlayList} finished playing. It will not be called if the {@link JukeBox} has been stopped manually.
     *
     * @param playList
     */
    void onPlayListEnd(PlayList playList);


    /**
     * This method is called when the {@link JukeBox} begins playback.
     */
    void onJukeBoxStart();


    /**
     * This method is called when the {@link JukeBox} is set to pause.
     */
    void onJukeBoxPause();


    /**
     * This method is called when the {@link JukeBox} stops playing, either by running out of songs/playlists or by being stopped manually.
     */
    void onJukeBoxEnd();

}
