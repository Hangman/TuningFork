package de.pottgames.tuningfork.jukebox;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * A class that holds basic meta data to a {@link Song} like title and artist. If you need custom attributes of arbitrary types, use
 * {@link #addAttribute(String, Object)} and {@link #getAttribute(Object)}. It is a regular map of key value pairs.
 *
 * @author Matthias
 *
 */
public class SongMeta {
    protected String                  artist;
    protected String                  title;
    protected String                  album;
    private ObjectMap<Object, Object> attributes;


    public SongMeta setArtist(String artist) {
        this.artist = artist;
        return this;
    }


    public SongMeta setTitle(String title) {
        this.title = title;
        return this;
    }


    public SongMeta setAlbum(String album) {
        this.album = album;
        return this;
    }


    public String getArtist() {
        return this.artist;
    }


    public String getTitle() {
        return this.title;
    }


    public String getAlbum() {
        return this.album;
    }


    public SongMeta addAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new ObjectMap<>();
        }
        this.attributes.put(key, value);
        return this;
    }


    public Object getAttribute(Object key) {
        if (this.attributes == null) {
            return null;
        }
        return this.attributes.get(key);
    }

}
