package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.IntMap;

/**
 * This provider offers {@link PlayList}s based on the currently set theme. {@link PlayList}s are not consumed/removed when fetched.<br>
 * Theme - PlayList is a 1:1 relation, you can only connect one PlayList to one theme.
 *
 * @author Matthias
 *
 */
public class ThemePlayListProvider implements PlayListProvider {
    private IntMap<PlayList> lists = new IntMap<>();
    private int              theme;


    /**
     * Adds a theme and the corresponding {@link PlayList}, potentially replacing a previously set {@link PlayList} for that theme.
     *
     * @param list
     * @param theme a custom id you can choose freely
     *
     * @return the ThemePlayListProvider for chaining
     */
    public ThemePlayListProvider add(PlayList list, int theme) {
        this.lists.put(theme, list);
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
        this.lists.remove(theme);
        return this;
    }


    @Override
    public PlayList next() {
        return this.lists.get(this.theme);
    }


    @Override
    public boolean hasNext() {
        final PlayList list = this.lists.get(this.theme);
        return list != null;
    }

}
