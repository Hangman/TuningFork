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

/**
 * This provider keeps a list of {@link PlayList}s and iterates through them in order. After a complete run, it starts again with the first PlayList in the
 * list.
 *
 * @author Matthias
 */
public class CircularPlayListProvider implements PlayListProvider {
    private final Array<PlayList> lists = new Array<>();
    private int                   index = 0;


    /**
     * Adds a {@link PlayList} to the end of the internal list.
     *
     * @param list the playlist
     *
     * @return the CircularPlayListProvider for chaining
     */
    public CircularPlayListProvider add(PlayList list) {
        this.lists.add(list);
        return this;
    }


    /**
     * Adds all {@link PlayList}s from the given array to the end of the internal list.
     *
     * @param lists an array of playlists
     *
     * @return the CircularPlayListProvider for chaining
     */
    public CircularPlayListProvider addAll(Array<PlayList> lists) {
        this.lists.addAll(lists);
        return this;
    }


    /**
     * Removes a {@link PlayList} from the internal list.
     *
     * @param list the playlist
     *
     * @return the CircularPlayListProvider for chaining
     */
    public CircularPlayListProvider remove(PlayList list) {
        this.lists.removeValue(list, false);
        return this;
    }


    @Override
    public PlayList next() {
        final PlayList result = this.lists.get(this.index);
        this.index++;
        if (this.index >= this.lists.size) {
            this.index = 0;
        }
        return result;
    }


    @Override
    public boolean hasNext() {
        return this.lists.size > 0;
    }


    @Override
    public String toString() {
        return "CircularPlayListProvider [lists=" + this.lists + ", index=" + this.index + "]";
    }

}
