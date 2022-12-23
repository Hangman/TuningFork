package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.Array;

public class DefaultPlayListProvider implements PlayListProvider {
    private final Array<PlayList> lists = new Array<>();


    public DefaultPlayListProvider add(PlayList list) {
        this.lists.add(list);
        return this;
    }


    public DefaultPlayListProvider addAll(Array<PlayList> lists) {
        this.lists.addAll(lists);
        return this;
    }


    public DefaultPlayListProvider remove(PlayList list) {
        this.lists.removeValue(list, false);
        return this;
    }


    @Override
    public PlayList next() {
        return this.lists.removeIndex(0);
    }


    @Override
    public boolean hasNext() {
        return this.lists.size > 0;
    }

}
