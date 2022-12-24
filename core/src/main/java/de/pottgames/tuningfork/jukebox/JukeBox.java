package de.pottgames.tuningfork.jukebox;

import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.playlist.PlayListProvider;
import de.pottgames.tuningfork.jukebox.song.Song;
import de.pottgames.tuningfork.jukebox.song.SongSettings;
import de.pottgames.tuningfork.jukebox.song.SongSettings.FadeType;
import de.pottgames.tuningfork.jukebox.song.SongSource;

/**
 * A music player class playing {@link Song}s from a {@link PlayList}. It's called JukeBox to avoid confusion with how libgdx uses the word 'Music'.
 *
 * @author Matthias
 *
 */
public class JukeBox {
    protected final Array<JukeBoxObserver> observer = new Array<>();
    protected PlayList                     currentPlayList;
    protected final PlayListProvider       playListProvider;
    protected Song                         currentSong;
    protected boolean                      stopped  = true;

    protected Song     eventSongStart;
    protected Song     eventSongEnd;
    protected PlayList eventPlayListStart;
    protected PlayList eventPlayListEnd;
    protected boolean  eventJukeBoxEnd;
    protected boolean  eventJukeBoxStart;
    protected boolean  eventJukeBoxPause;


    /**
     * Creates a new JukeBox.
     *
     * @param playListProvider
     */
    public JukeBox(PlayListProvider playListProvider) {
        this.playListProvider = playListProvider;
    }


    /**
     * Updates the JukeBox. This method should be called every frame.
     */
    public void update() {
        if (this.stopped) {
            this.handleEvents();
            return;
        }

        SongSource source = null;
        if (this.currentSong != null) {
            source = this.currentSong.getSource();
        }

        if (source != null && source.isPlaying()) {
            final float playbackPos = source.getPlaybackPosition();
            final SongSettings settings = this.currentSong.getSettings();
            final boolean fadeIn = this.fadeIn(source, settings, playbackPos);
            final boolean fadeOut = this.fadeOut(source, settings, playbackPos, source.getDuration());
            if (!fadeIn && !fadeOut) {
                source.setVolume(settings.getVolume());
            }
        } else {
            this.eventSongEnd = this.currentSong;
            this.nextSong();
        }

        this.handleEvents();
    }


    protected boolean fadeIn(SongSource source, SongSettings settings, float playbackPos) {
        final float fadeDuration = settings.getFadeInDuration();
        if (playbackPos < fadeDuration) {
            final float alpha = playbackPos / fadeDuration;
            final float volume = settings.fadeVolume(FadeType.IN, alpha);
            source.setVolume(volume);
            return true;
        }

        return false;
    }


    protected boolean fadeOut(SongSource source, SongSettings settings, float playbackPos, float songDuration) {
        final float fadeDuration = settings.getFadeOutDuration();
        if (playbackPos > songDuration - fadeDuration) {
            final float alpha = (songDuration - playbackPos) / fadeDuration;
            final float volume = settings.fadeVolume(FadeType.OUT, alpha);
            source.setVolume(volume);
            return true;
        }

        return false;
    }


    /**
     * Starts playback.
     */
    public void play() {
        this.stopped = false;
        this.eventJukeBoxStart = true;
        if (this.currentSong != null) {
            this.currentSong.getSource().play();
        }
    }


    /**
     * Pauses playback. Calling {@link #play()} will resume playback.
     */
    public void pause() {
        this.stopped = true;
        this.eventJukeBoxPause = true;
        if (this.currentSong != null) {
            this.currentSong.getSource().pause();
        }
    }


    /**
     * Stops playback and resets the current PlayList if applicable.
     */
    public void stop() {
        if (!this.stopped) {
            this.eventJukeBoxEnd = true;
            if (this.currentSong != null) {
                this.currentSong.getSource().stop();
            }
            if (this.currentPlayList != null) {
                this.currentPlayList.reset();
            }
        }
        this.currentSong = null;
        this.stopped = true;
    }


    /**
     * Immediately stops playback, removes all observers and sets the JukeBox into a stopped state.
     */
    public void clear() {
        this.stopped = true;
        if (this.currentSong != null) {
            this.currentSong.getSource().stop();
        }
        this.currentPlayList = null;
        this.currentSong = null;
        this.observer.clear();
        this.eventJukeBoxEnd = false;
        this.eventJukeBoxPause = false;
        this.eventJukeBoxStart = false;
        this.eventPlayListEnd = null;
        this.eventPlayListStart = null;
        this.eventSongEnd = null;
        this.eventSongStart = null;
    }


    /**
     * Returns the song that is currently playing. May be null.
     *
     * @return the song
     */
    public Song getCurrentSong() {
        return this.currentSong;
    }


    protected void nextSong() {
        this.currentSong = null;
        if (this.currentPlayList == null) {
            if (this.playListProvider.hasNext()) {
                this.currentPlayList = this.playListProvider.next();
            }
            if (this.currentPlayList != null) {
                this.eventPlayListStart = this.currentPlayList;
            }
        }

        // SET TO NEXT PLAYLIST OR LOOP CURRENT PLAYLIST
        if (this.currentPlayList != null) {
            if (this.currentPlayList.isPlayedThrough()) {
                if (!this.playListProvider.hasNext() && this.currentPlayList.isLoop()) {
                    this.currentPlayList.reset();
                } else {
                    final PlayList lastPlayList = this.currentPlayList;
                    this.currentPlayList = null;
                    if (this.playListProvider.hasNext()) {
                        this.currentPlayList = this.playListProvider.next();
                    }
                    if (this.currentPlayList == null) {
                        this.eventPlayListEnd = lastPlayList;
                        this.eventJukeBoxEnd = true;
                        this.stopped = true;
                        return;
                    }
                    this.eventPlayListEnd = lastPlayList;
                    this.eventPlayListStart = this.currentPlayList;
                }
            }
        }

        // REQUEST NEXT SONG
        if (this.currentPlayList != null) {
            this.currentSong = this.currentPlayList.nextSong();
        }

        // PLAY NEXT SONG AND APPLY FADING
        if (this.currentSong != null) {
            final SongSource source = this.currentSong.getSource();
            this.fadeIn(source, this.currentSong.getSettings(), 0f);
            this.currentSong.getSource().play();
            this.eventSongStart = this.currentSong;
        } else {
            this.stopped = true;
            this.eventJukeBoxEnd = true;
        }
    }


    /**
     * Adds an observer.
     *
     * @param observer
     */
    public void addObserver(JukeBoxObserver observer) {
        this.observer.add(observer);
    }


    /**
     * Removes an observer.
     *
     * @param observer
     */
    public void removeObserver(JukeBoxObserver observer) {
        this.observer.removeValue(observer, true);
    }


    protected void handleEvents() {
        if (this.eventJukeBoxStart) {
            this.notifyJukeBoxStart();
            this.eventJukeBoxStart = false;
        }
        if (this.eventPlayListEnd != null) {
            this.notifyPlayListEnd(this.eventPlayListEnd);
            this.eventPlayListEnd = null;
        }
        if (this.eventPlayListStart != null) {
            this.notifyPlayListStart(this.eventPlayListStart);
            this.eventPlayListStart = null;
        }
        if (this.eventSongEnd != null) {
            this.notifySongEnd(this.eventSongEnd);
            this.eventSongEnd = null;
        }
        if (this.eventSongStart != null) {
            this.notifySongStart(this.eventSongStart);
            this.eventSongStart = null;
        }
        if (this.eventJukeBoxEnd) {
            this.notifyJukeBoxEnd();
            this.eventJukeBoxEnd = false;
        }
        if (this.eventJukeBoxPause) {
            this.notifyJukeBoxPause();
            this.eventJukeBoxPause = false;
        }
    }


    protected void notifySongStart(Song song) {
        for (int i = 0; i < this.observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onSongStart(song);
        }
    }


    protected void notifySongEnd(Song song) {
        for (int i = 0; i < this.observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onSongEnd(song);
        }
    }


    protected void notifyPlayListStart(PlayList playList) {
        for (int i = 0; i < this.observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onPlayListStart(playList);
        }
    }


    protected void notifyPlayListEnd(PlayList playList) {
        for (int i = 0; i < this.observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onPlayListEnd(playList);
        }
    }


    protected void notifyJukeBoxEnd() {
        for (int i = 0; i < this.observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onJukeBoxEnd();
        }
    }


    protected void notifyJukeBoxStart() {
        for (int i = 0; i < this.observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onJukeBoxStart();
        }
    }


    protected void notifyJukeBoxPause() {
        for (int i = 0; i < this.observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onJukeBoxPause();
        }
    }

}
