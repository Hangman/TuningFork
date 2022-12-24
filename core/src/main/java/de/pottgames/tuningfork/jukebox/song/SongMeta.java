package de.pottgames.tuningfork.jukebox.song;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * A class that holds basic meta data to a {@link Song} like title and artist. If you need custom attributes of arbitrary types, use
 * {@link #addAttribute(Object, Object)} and {@link #getAttribute(Object)}. It is a regular map of key-value pairs.
 *
 * @author Matthias
 *
 */
public class SongMeta {
    protected String                  artist;
    protected String                  title;
    protected String                  album;
    private ObjectMap<Object, Object> attributes;


    /**
     * Sets the artist.
     *
     * @param artist
     *
     * @return the SongMeta for chaining
     */
    public SongMeta setArtist(String artist) {
        this.artist = artist;
        return this;
    }


    /**
     * Sets the title.
     *
     * @param title
     *
     * @return the SongMeta for chaining
     */
    public SongMeta setTitle(String title) {
        this.title = title;
        return this;
    }


    /**
     * Sets the album.
     *
     * @param album
     *
     * @return the SongMeta for chaining
     */
    public SongMeta setAlbum(String album) {
        this.album = album;
        return this;
    }


    /**
     * Returns the artist. May be null.
     *
     * @return the artist
     */
    public String getArtist() {
        return this.artist;
    }


    /**
     * Returns the title. May be null.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }


    /**
     * Returns the album. May be null.
     *
     * @return the album
     */
    public String getAlbum() {
        return this.album;
    }


    /**
     * Adds a custom attribute. Attributes are simple key-value pairs.
     *
     * @param key
     * @param value
     *
     * @return the SongMeta for chaining
     */
    public SongMeta addAttribute(Object key, Object value) {
        if (this.attributes == null) {
            this.attributes = new ObjectMap<>();
        }
        this.attributes.put(key, value);
        return this;
    }


    /**
     * Returns the value that is connected to the given key.
     *
     * @param key
     *
     * @return value
     */
    public Object getAttribute(Object key) {
        if (this.attributes == null) {
            return null;
        }
        return this.attributes.get(key);
    }

}
