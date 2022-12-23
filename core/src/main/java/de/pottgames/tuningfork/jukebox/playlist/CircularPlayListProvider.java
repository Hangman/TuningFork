package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.Array;

public class CircularPlayListProvider implements PlayListProvider {
    private final Array<PlayList> lists = new Array<>();
    private int                   index = 0;


    public CircularPlayListProvider add(PlayList list) {
        this.lists.add(list);
        return this;
    }


    public CircularPlayListProvider addAll(Array<PlayList> lists) {
        this.lists.addAll(lists);
        return this;
    }


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

}
