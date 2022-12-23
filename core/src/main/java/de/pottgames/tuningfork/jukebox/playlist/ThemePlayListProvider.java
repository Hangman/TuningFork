package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.IntMap;

public class ThemePlayListProvider implements PlayListProvider {
    private IntMap<PlayList> lists = new IntMap<>();
    private int              theme;


    public ThemePlayListProvider add(PlayList list, int theme) {
        this.lists.put(theme, list);
        return this;
    }


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
