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

package de.pottgames.tuningfork.jukebox;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import de.pottgames.tuningfork.jukebox.JukeBoxEvent.JukeBoxEventType;
import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.playlist.PlayListProvider;
import de.pottgames.tuningfork.jukebox.playlist.ThemePlayListProvider;
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

    protected boolean       softStop       = false;
    private boolean         softStopResume = false;
    protected long          softStopStartTime;
    protected float         softStopFadeDuration;
    protected Interpolation softStopFadeCurve;
    protected float         softStopFadeStartVolume;

    protected Pool<JukeBoxEvent>  eventPool    = new Pool<JukeBoxEvent>(7 * 6) {
                                                   @Override
                                                   protected JukeBoxEvent newObject() {
                                                       return new JukeBoxEvent();
                                                   }
                                               };
    protected Array<JukeBoxEvent> eventHistory = new Array<>();


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
        SongSettings settings = null;
        if (this.currentSong != null) {
            source = this.currentSong.getSource();
            settings = this.currentSong.getSettings();
        }

        if (source != null && source.isPlaying()) {
            final float fadeVolume = this.determineFadeVolume(source, settings);
            if (this.softStop) {
                this.softStopFade(source);
            } else {
                source.setVolume(fadeVolume);
            }
        } else {
            this.resetSoftStop(true);
            if (this.currentSong != null) {
                this.pushEvent(JukeBoxEventType.SONG_END, this.currentSong);
            }
            this.nextSong();
        }

        this.handleEvents();
    }


    protected void softStopFade(SongSource source) {
        final float secondsSinceSoftStop = (System.currentTimeMillis() - this.softStopStartTime) / 1000f;
        final float alpha = secondsSinceSoftStop / this.softStopFadeDuration;
        if (alpha < 1f) {
            final float softFadeVolume = (1f - this.softStopFadeCurve.apply(alpha)) * this.softStopFadeStartVolume;
            source.setVolume(softFadeVolume);
        } else {
            this.stop();
            if (this.softStopResume) {
                this.currentPlayList = null;
                this.play();
            }
        }
    }


    protected float determineFadeVolume(SongSource source, SongSettings settings) {
        final float playbackPos = source.getPlaybackPosition();
        final float fadeIn = this.fadeIn(source, settings, playbackPos);
        final float fadeOut = this.fadeOut(source, settings, playbackPos, source.getDuration());

        if (fadeIn >= 0f) {
            return fadeIn;
        }
        if (fadeOut >= 0f) {
            return fadeOut;
        }

        return settings.getVolume();
    }


    protected float fadeIn(SongSource source, SongSettings settings, float playbackPos) {
        final float fadeDuration = settings.getFadeInDuration();
        if (playbackPos < fadeDuration) {
            final float alpha = playbackPos / fadeDuration;
            final float volume = settings.fadeVolume(FadeType.IN, alpha);
            return volume;
        }
        return -1f;
    }


    protected float fadeOut(SongSource source, SongSettings settings, float playbackPos, float songDuration) {
        final float fadeDuration = settings.getFadeOutDuration();
        if (playbackPos > songDuration - fadeDuration) {
            final float alpha = (songDuration - playbackPos) / fadeDuration;
            final float volume = settings.fadeVolume(FadeType.OUT, alpha);
            return volume;
        }
        return -1f;
    }


    /**
     * Starts playback.
     */
    public void play() {
        this.stopped = false;
        this.pushEvent(JukeBoxEventType.JUKEBOX_START);
        if (this.currentSong != null) {
            this.currentSong.getSource().play();
        }
    }


    /**
     * Pauses playback. Calling {@link #play()} will resume playback.
     */
    public void pause() {
        this.stopped = true;
        this.pushEvent(JukeBoxEventType.JUKEBOX_PAUSE);
        if (this.currentSong != null) {
            this.currentSong.getSource().pause();
        }
    }


    /**
     * Stops playback and resets the current {@link PlayList} if applicable.
     */
    public void stop() {
        if (!this.stopped) {
            this.pushEvent(JukeBoxEventType.JUKEBOX_END);
        }
        if (this.currentSong != null) {
            this.currentSong.getSource().stop();
            this.pushEvent(JukeBoxEventType.SONG_END, this.currentSong);
        }
        if (this.currentPlayList != null) {
            this.currentPlayList.reset();
        }
        this.currentSong = null;
        this.stopped = true;
        this.resetSoftStop(false);
    }


    protected void resetSoftStop(boolean clearResume) {
        this.softStop = false;
        this.softStopFadeCurve = null;
        if (clearResume) {
            this.softStopResume = false;
        }
    }


    /**
     * Fades out the currently playing song and stops the {@link JukeBox} afterwards.<br>
     * <br>
     * There's a couple of reasons why the {@link JukeBox} might stop early:<br>
     * - the rest of the song is shorter than the desired fadeOutDuration<br>
     * - the song duration is not available<br>
     * - fadeOutCurve is null<br>
     * - fadeOutDuration is smaller or equal 0<br>
     *
     * @param fadeOutCurve
     * @param fadeOutDuration fade out duration in seconds
     *
     * @return false if a soft stop couldn't be performed and it is stopped right away
     */
    public boolean softStop(Interpolation fadeOutCurve, float fadeOutDuration) {
        if (this.stopped || this.currentSong == null || fadeOutCurve == null || fadeOutDuration <= 0f) {
            this.stop();
            return false;
        }

        final SongSource source = this.currentSong.getSource();
        final SongSettings settings = this.currentSong.getSettings();
        final float duration = source.getDuration();
        final float position = source.getPlaybackPosition();

        if (duration <= 0f) {
            this.stop();
            return false;
        }

        this.softStop = true;
        this.softStopResume = false;
        this.softStopStartTime = System.currentTimeMillis();
        this.softStopFadeDuration = fadeOutDuration;
        this.softStopFadeCurve = fadeOutCurve;
        if (duration - position < fadeOutDuration) {
            this.softStopFadeDuration = duration - position;
        }
        this.softStopFadeStartVolume = this.determineFadeVolume(source, settings);

        return true;
    }


    /**
     * Soft stops (see {@link #softStop(Interpolation, float)}), ends the current {@link PlayList} and resumes playback afterwards if possible.<br>
     * <br>
     * Since it may not be clear at first glance what the use cases are, here is an example:<br>
     * The World-PlayList is running, but the player gets ambushed, whereupon the World-PlayList should fade-out and the Danger-PlayList should begin
     * playing.<br>
     * This is achievable in 2 steps:<br>
     * 1. change the theme in ThemePlayListProvider {@link ThemePlayListProvider#setTheme(int)}<br>
     * 2. call softStopAndResume on the JukeBox
     *
     * @param fadeOutCurve
     * @param fadeOutDuration
     */
    public void softStopAndResume(Interpolation fadeOutCurve, float fadeOutDuration) {
        final boolean softStopResult = this.softStop(fadeOutCurve, fadeOutDuration);
        if (!softStopResult) {
            this.currentPlayList = null;
            this.play();
            return;
        }
        this.softStopResume = true;
    }


    /**
     * Immediately stops playback, removes all observers and sets the {@link JukeBox} into a stopped state.
     */
    public void clear() {
        this.stopped = true;
        if (this.currentSong != null) {
            this.currentSong.getSource().stop();
        }
        this.currentPlayList = null;
        this.currentSong = null;
        this.observer.clear();
        for (final JukeBoxEvent event : this.eventHistory) {
            this.eventPool.free(event);
        }
        this.eventHistory.clear();
    }


    /**
     * Returns the {@link Song} that is currently playing. May be null.
     *
     * @return the song
     */
    public Song getCurrentSong() {
        return this.currentSong;
    }


    /**
     * Returns true if this {@link JukeBox} is playing at the moment.
     *
     * @return true if playing
     */
    public boolean isPlaying() {
        return !this.stopped;
    }


    protected void nextSong() {
        this.currentSong = null;
        if (this.currentPlayList == null) {
            if (this.playListProvider.hasNext()) {
                this.currentPlayList = this.playListProvider.next();
            }
            if (this.currentPlayList != null) {
                this.pushEvent(JukeBoxEventType.PLAYLIST_START, this.currentPlayList);
            }
        }

        // SET TO NEXT PLAYLIST OR LOOP CURRENT PLAYLIST
        if (this.currentPlayList != null && this.currentPlayList.isPlayedThrough()) {
            if (!this.playListProvider.hasNext() && this.currentPlayList.isLoop()) {
                this.currentPlayList.reset();
            } else {
                final PlayList lastPlayList = this.currentPlayList;
                this.currentPlayList = this.playListProvider.hasNext() ? this.playListProvider.next() : null;
                if (this.currentPlayList == null) {
                    this.pushEvent(JukeBoxEventType.PLAYLIST_END, lastPlayList);
                    this.pushEvent(JukeBoxEventType.JUKEBOX_END);
                    this.stopped = true;
                    return;
                }
                this.pushEvent(JukeBoxEventType.PLAYLIST_END, lastPlayList);
                this.pushEvent(JukeBoxEventType.PLAYLIST_START, this.currentPlayList);
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
            this.pushEvent(JukeBoxEventType.SONG_START, this.currentSong);
        } else {
            this.stopped = true;
            this.pushEvent(JukeBoxEventType.JUKEBOX_END);
        }
    }


    /**
     * Adds an observer. See {@link JukeBoxObserver} for details.
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


    protected void pushEvent(JukeBoxEventType type) {
        final JukeBoxEvent event = this.eventPool.obtain();
        event.setType(type);
        this.eventHistory.add(event);
    }


    protected void pushEvent(JukeBoxEventType type, Song song) {
        if (song == null) {
            throw new RuntimeException("song is null");
        }
        final JukeBoxEvent event = this.eventPool.obtain();
        event.setType(type);
        event.setSong(song);
        this.eventHistory.add(event);
    }


    protected void pushEvent(JukeBoxEventType type, PlayList playList) {
        final JukeBoxEvent event = this.eventPool.obtain();
        event.setType(type);
        event.setPlayList(playList);
        this.eventHistory.add(event);
    }


    protected void handleEvents() {
        for (final JukeBoxEvent event : this.eventHistory) {
            switch (event.getType()) {
                case JUKEBOX_END:
                    this.notifyJukeBoxEnd();
                    break;
                case JUKEBOX_PAUSE:
                    this.notifyJukeBoxPause();
                    break;
                case JUKEBOX_START:
                    this.notifyJukeBoxStart();
                    break;
                case PLAYLIST_END:
                    this.notifyPlayListEnd(event.getPlayList());
                    break;
                case PLAYLIST_START:
                    this.notifyPlayListStart(event.getPlayList());
                    break;
                case SONG_END:
                    this.notifySongEnd(event.getSong());
                    break;
                case SONG_START:
                    this.notifySongStart(event.getSong());
                    break;
                case NONE:
                    break;
            }
            this.eventPool.free(event);
        }
        this.eventHistory.clear();
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
