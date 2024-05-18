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

import de.pottgames.tuningfork.jukebox.JukeBox;

/**
 * The {@link JukeBox} fetches {@link PlayList}s from a {@link PlayListProvider}.
 *
 * @author Matthias
 *
 */
public interface PlayListProvider {

    /**
     * Returns the next PlayList. May be null if none is available.
     *
     * @return the {@link PlayList}
     */
    PlayList next();


    /**
     * Returns true if a {@link PlayList} is available via {@link #next()}.
     *
     * @return true if a {@link PlayList} is available, false otherwise
     */
    boolean hasNext();

}
