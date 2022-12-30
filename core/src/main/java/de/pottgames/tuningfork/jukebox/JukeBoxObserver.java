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
