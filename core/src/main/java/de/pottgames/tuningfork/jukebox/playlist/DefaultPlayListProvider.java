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
 * This provider holds a list of {@link PlayList}s and offers them in order. When a {@link PlayList} is retrieved, it is removed from the list. After all
 * {@link PlayList}s have been retrieved, the provider is empty and no longer supplies new {@link PlayList}s.
 *
 * @author Matthias
 */
public class DefaultPlayListProvider implements PlayListProvider {
    private final Array<PlayList> lists = new Array<>();


    /**
     * Adds a {@link PlayList} to the end of the queue.
     *
     * @param list the playlist
     *
     * @return the DefaultPlayListProvider for chaining
     */
    public DefaultPlayListProvider add(PlayList list) {
        lists.add(list);
        return this;
    }


    /**
     * Adds all {@link PlayList}s of the given array to the end of the queue.
     *
     * @param lists an array of playlists
     *
     * @return the DefaultPlayListProvider for chaining
     */
    public DefaultPlayListProvider addAll(Array<PlayList> lists) {
        this.lists.addAll(lists);
        return this;
    }


    /**
     * Removes a {@link PlayList} from the queue.
     *
     * @param list the playlist
     *
     * @return the DefaultPlayListProvider for chaining
     */
    public DefaultPlayListProvider remove(PlayList list) {
        lists.removeValue(list, false);
        return this;
    }


    @Override
    public PlayList next() {
        return lists.removeIndex(0);
    }


    @Override
    public boolean hasNext() {
        return lists.size > 0;
    }


    @Override
    public String toString() {
        return "DefaultPlayListProvider [lists=" + lists + "]";
    }

}
