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

package de.pottgames.tuningfork.jukebox;

import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.song.Song;

/**
 * An observer to listen for {@link JukeBox} state changes. Events are emitted in {@link JukeBox#update()}.
 *
 * @author Matthias
 */
public interface JukeBoxObserver {

    /**
     * This method is called when a new song is played.
     *
     * @param song the song
     */
    void onSongStart(Song song);


    /**
     * This method is called when a song finished playing. It will not be called if the {@link JukeBox} has been stopped manually.
     *
     * @param song the song
     */
    void onSongEnd(Song song);


    /**
     * This method is called when a new {@link PlayList} is used to play songs.
     *
     * @param playList the playlist
     */
    void onPlayListStart(PlayList playList);


    /**
     * This method is called when a {@link PlayList} finished playing. It will not be called if the {@link JukeBox} has been stopped manually.
     *
     * @param playList the playlist
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


    /**
     * This method is called when the master volume of the {@link JukeBox} changed by calling {@link JukeBox#setVolume(float)}.
     *
     * @param oldVolume the volume before the change
     * @param newVolume the new volume after the change
     */
    default void onMasterVolumeChanged(float oldVolume, float newVolume) {
        // do nothing by default
    }

}
