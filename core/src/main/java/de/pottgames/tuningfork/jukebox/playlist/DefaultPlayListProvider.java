package de.pottgames.tuningfork.jukebox.playlist;

import com.badlogic.gdx.utils.Array;

/**
 * This provider holds a list of {@link PlayList}s and offers them in order. When a {@link PlayList} is retrieved, it is removed from the list. After all
 * {@link PlayList}s have been retrieved, the provider is empty and no longer supplies new {@link PlayList}s.
 *
 * @author Matthias
 *
 */
public class DefaultPlayListProvider implements PlayListProvider {
    private final Array<PlayList> lists = new Array<>();


    /**
     * Adds a {@link PlayList} to the end of the queue.
     *
     * @param list
     *
     * @return the DefaultPlayListProvider for chaining
     */
    public DefaultPlayListProvider add(PlayList list) {
        this.lists.add(list);
        return this;
    }


    /**
     * Adds all {@link PlayList}s of the given array to the end of the queue.
     *
     * @param lists
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
     * @param list
     *
     * @return the DefaultPlayListProvider for chaining
     */
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
