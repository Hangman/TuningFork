package de.pottgames.tuningfork.jukebox.song;

import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.jukebox.JukeBox;
import de.pottgames.tuningfork.jukebox.playlist.PlayList;

/**
 * An immutable data class containing a {@link SongSource} and a {@link SongSettings} object meant to be added to a {@link PlayList} that can be played via
 * {@link JukeBox}.
 *
 * @author Matthias
 *
 */
public class Song {
    private final SongSource   source;
    private final SongSettings settings;
    private final SongMeta     metaData;


    /**
     * Creates a new Song with default settings. See {@link SongSettings#DEFAULT} for details.
     *
     * @param source
     */
    public Song(SongSource source) {
        this(source, null);
    }


    /**
     * Creates a new Song with the given settings.
     *
     * @param source
     * @param settings if null, default settings will be used, see {@link SongSettings#DEFAULT} for details
     */
    public Song(SongSource source, SongSettings settings) {
        this(source, settings, null);
    }


    /**
     * Creates a new Song with the given settings and meta data.
     *
     * @param source
     * @param settings if null, default settings will be used, see {@link SongSettings#DEFAULT} for details
     * @param metaData may be null
     */
    public Song(SongSource source, SongSettings settings, SongMeta metaData) {
        if (source == null) {
            throw new TuningForkRuntimeException("The source must not be null");
        }
        this.source = source;
        this.metaData = metaData != null ? metaData : new SongMeta();

        if (settings == null) {
            this.settings = SongSettings.DEFAULT;
        } else if (this.source.getDuration() < settings.getFadeInDuration() + settings.getFadeOutDuration()) {
            this.settings = SongSettings.noFade(settings.getVolume());
        } else {
            this.settings = settings;
        }
    }


    /**
     * Returns the {@link SongSource}.
     *
     * @return the source
     */
    public SongSource getSource() {
        return this.source;
    }


    /**
     * Returns the {@link SongSettings}.
     *
     * @return the settings
     */
    public SongSettings getSettings() {
        return this.settings;
    }


    /**
     * Returns the {@link SongMeta}.
     *
     * @return the meta data
     */
    public SongMeta getMeta() {
        return this.metaData;
    }

}