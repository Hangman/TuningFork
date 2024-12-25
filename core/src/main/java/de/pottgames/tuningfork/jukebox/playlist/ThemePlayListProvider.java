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

import com.badlogic.gdx.utils.IntMap;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * This provider offers {@link PlayList}s based on the currently set theme. {@link PlayList}s are not consumed/removed when fetched.<br>
 * Theme - PlayList is a 1:1 relation, you can only connect one PlayList to one theme.
 *
 * @author Matthias
 */
public class ThemePlayListProvider implements PlayListProvider {
    private final TuningForkLogger logger;
    private IntMap<PlayList>       lists = new IntMap<>();
    protected int                  theme;


    public ThemePlayListProvider() {
        logger = Audio.get().getLogger();
    }


    /**
     * Sets the theme.
     *
     * @param theme the theme identifier
     *
     * @return the ThemePlayListProvider for chaining
     */
    public ThemePlayListProvider setTheme(int theme) {
        if (lists.get(theme) == null) {
            logger.warn(this.getClass(), "There is no corresponding playlist for the selected theme: " + theme);
        }
        this.theme = theme;
        return this;
    }


    /**
     * Returns the theme.
     *
     * @return the theme
     */
    public int getTheme() {
        return theme;
    }


    /**
     * Adds a theme and the corresponding {@link PlayList}, potentially replacing a previously set {@link PlayList} for that theme.
     *
     * @param list the playlist
     * @param theme a custom id you can choose freely
     *
     * @return the ThemePlayListProvider for chaining
     */
    public ThemePlayListProvider add(PlayList list, int theme) {
        lists.put(theme, list);
        return this;
    }


    /**
     * Removes a {@link PlayList} and the theme from the internal map.
     *
     * @param theme a custom id you can choose freely
     *
     * @return the ThemePlayListProvider for chaining
     */
    public ThemePlayListProvider remove(int theme) {
        lists.remove(theme);
        return this;
    }


    @Override
    public PlayList next() {
        return lists.get(theme);
    }


    @Override
    public boolean hasNext() {
        final PlayList list = lists.get(theme);
        return list != null;
    }


    @Override
    public String toString() {
        return "ThemePlayListProvider [lists=" + lists + ", theme=" + theme + "]";
    }

}
