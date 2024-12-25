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
import com.badlogic.gdx.math.MathUtils;
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
 */
public class JukeBox {
    protected final Array<JukeBoxObserver> observer = new Array<>();
    protected PlayList                     currentPlayList;
    protected final PlayListProvider       playListProvider;
    protected Song                         currentSong;
    protected boolean                      stopped  = true;
    protected float                        volume   = 1f;

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
     * @param playListProvider the playlist provider
     */
    public JukeBox(PlayListProvider playListProvider) {
        this.playListProvider = playListProvider;
    }


    /**
     * Updates the JukeBox. This method should be called every frame.
     */
    public void update() {
        if (stopped) {
            handleEvents();
            return;
        }

        SongSource source = null;
        SongSettings settings = null;
        if (currentSong != null) {
            source = currentSong.getSource();
            settings = currentSong.getSettings();
        }

        if (source != null && source.isPlaying()) {
            final float fadeVolume = determineFadeVolume(source, settings);
            if (softStop) {
                softStopFade(source);
            } else {
                source.setVolume(fadeVolume * volume);
            }
        } else {
            resetSoftStop(true);
            if (currentSong != null) {
                this.pushEvent(JukeBoxEventType.SONG_END, currentSong);
            }
            nextSong();
        }

        handleEvents();
    }


    protected void softStopFade(SongSource source) {
        final float secondsSinceSoftStop = (System.currentTimeMillis() - softStopStartTime) / 1000f;
        final float alpha = secondsSinceSoftStop / softStopFadeDuration;
        if (alpha < 1f) {
            final float softFadeVolume = (1f - softStopFadeCurve.apply(alpha)) * softStopFadeStartVolume;
            source.setVolume(softFadeVolume * volume);
        } else {
            stop();
            if (softStopResume) {
                currentPlayList = null;
                play();
            }
        }
    }


    protected float determineFadeVolume(SongSource source, SongSettings settings) {
        final float playbackPos = source.getPlaybackPosition();
        final float fadeIn = fadeIn(source, settings, playbackPos);
        final float fadeOut = fadeOut(source, settings, playbackPos, source.getDuration());

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
            return settings.fadeVolume(FadeType.IN, alpha);
        }
        return -1f;
    }


    protected float fadeOut(SongSource source, SongSettings settings, float playbackPos, float songDuration) {
        final float fadeDuration = settings.getFadeOutDuration();
        if (playbackPos > songDuration - fadeDuration) {
            final float alpha = (songDuration - playbackPos) / fadeDuration;
            return settings.fadeVolume(FadeType.OUT, alpha);
        }
        return -1f;
    }


    /**
     * Sets the master volume of the Jukebox.<br>
     * The change isn't applied immediately, it will be applied in the next call to {@link JukeBox#update()}.<br>
     * The final volume equation looks like:<br>
     * <code>
     * master volume = JukeBox.getVolume() * SongSettings.fadeVolume()
     * </code>
     *
     * @param volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume, values outside of the range will be clamped
     */
    public void setVolume(float volume) {
        this.volume = MathUtils.clamp(volume, 0f, 1f);
    }


    /**
     * Returns the JukeBox's master volume.<br>
     * The final volume equation looks like:<br>
     * <code>
     * master volume = JukeBox.getVolume() * SongSettings.fadeVolume()
     * </code>
     *
     * @return the master volume in the range of 0.0 - 1.0 with 0 being silent and 1 being the maximum volume
     */
    public float getVolume() {
        return volume;
    }


    /**
     * Starts playback.
     */
    public void play() {
        stopped = false;
        this.pushEvent(JukeBoxEventType.JUKEBOX_START);
        if (currentSong != null) {
            currentSong.getSource().play();
        }
    }


    /**
     * Pauses playback. Calling {@link #play()} will resume playback.
     */
    public void pause() {
        stopped = true;
        this.pushEvent(JukeBoxEventType.JUKEBOX_PAUSE);
        if (currentSong != null) {
            currentSong.getSource().pause();
        }
    }


    /**
     * Stops playback and resets the current {@link PlayList} if applicable.
     */
    public void stop() {
        if (!stopped) {
            this.pushEvent(JukeBoxEventType.JUKEBOX_END);
        }
        if (currentSong != null) {
            currentSong.getSource().stop();
            this.pushEvent(JukeBoxEventType.SONG_END, currentSong);
        }
        if (currentPlayList != null) {
            currentPlayList.reset();
        }
        currentSong = null;
        stopped = true;
        resetSoftStop(false);
    }


    protected void resetSoftStop(boolean clearResume) {
        softStop = false;
        softStopFadeCurve = null;
        if (clearResume) {
            softStopResume = false;
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
     * @param fadeOutCurve the interpolation used to fade-out
     * @param fadeOutDuration fade out duration in seconds
     *
     * @return false if a soft stop couldn't be performed and it is stopped right away
     */
    public boolean softStop(Interpolation fadeOutCurve, float fadeOutDuration) {
        if (stopped || currentSong == null || fadeOutCurve == null || fadeOutDuration <= 0f) {
            stop();
            return false;
        }

        final SongSource source = currentSong.getSource();
        final SongSettings settings = currentSong.getSettings();
        final float duration = source.getDuration();
        final float position = source.getPlaybackPosition();

        if (duration <= 0f) {
            stop();
            return false;
        }

        softStop = true;
        softStopResume = false;
        softStopStartTime = System.currentTimeMillis();
        softStopFadeDuration = fadeOutDuration;
        softStopFadeCurve = fadeOutCurve;
        if (duration - position < fadeOutDuration) {
            softStopFadeDuration = duration - position;
        }
        softStopFadeStartVolume = determineFadeVolume(source, settings);

        return true;
    }


    /**
     * Soft stops (see {@link #softStop(Interpolation, float)}), ends the current {@link PlayList} and resumes playback afterward if possible.<br>
     * <br>
     * Since it may not be clear at first glance what the use cases are, here is an example:<br>
     * The World-PlayList is running, but the player gets ambushed, whereupon the World-PlayList should fade-out and the Danger-PlayList should begin
     * playing.<br>
     * This is achievable in 2 steps:<br>
     * 1. change the theme in ThemePlayListProvider {@link ThemePlayListProvider#setTheme(int)}<br>
     * 2. call softStopAndResume on the JukeBox
     *
     * @param fadeOutCurve the interpolation used to fade-out
     * @param fadeOutDuration the fade-out duration in seconds
     */
    public void softStopAndResume(Interpolation fadeOutCurve, float fadeOutDuration) {
        final boolean softStopResult = softStop(fadeOutCurve, fadeOutDuration);
        if (!softStopResult) {
            currentPlayList = null;
            play();
            return;
        }
        softStopResume = true;
    }


    /**
     * Immediately stops playback, removes all observers and sets the {@link JukeBox} into a stopped state.
     */
    public void clear() {
        stopped = true;
        if (currentSong != null) {
            currentSong.getSource().stop();
        }
        currentPlayList = null;
        currentSong = null;
        observer.clear();
        for (final JukeBoxEvent event : eventHistory) {
            eventPool.free(event);
        }
        eventHistory.clear();
    }


    /**
     * Returns the {@link Song} that is currently playing. May be null.
     *
     * @return the song
     */
    public Song getCurrentSong() {
        return currentSong;
    }


    /**
     * Returns true if this {@link JukeBox} is playing at the moment.
     *
     * @return true if playing
     */
    public boolean isPlaying() {
        return !stopped;
    }


    protected void nextSong() {
        currentSong = null;
        if (currentPlayList == null) {
            if (playListProvider.hasNext()) {
                currentPlayList = playListProvider.next();
            }
            if (currentPlayList != null) {
                this.pushEvent(JukeBoxEventType.PLAYLIST_START, currentPlayList);
            }
        }

        // SET TO NEXT PLAYLIST OR LOOP CURRENT PLAYLIST
        if (currentPlayList != null && currentPlayList.isPlayedThrough()) {
            if (!playListProvider.hasNext() && currentPlayList.isLoop()) {
                currentPlayList.reset();
            } else {
                final PlayList lastPlayList = currentPlayList;
                currentPlayList = playListProvider.hasNext() ? playListProvider.next() : null;
                if (currentPlayList == null) {
                    this.pushEvent(JukeBoxEventType.PLAYLIST_END, lastPlayList);
                    this.pushEvent(JukeBoxEventType.JUKEBOX_END);
                    stopped = true;
                    return;
                }
                this.pushEvent(JukeBoxEventType.PLAYLIST_END, lastPlayList);
                this.pushEvent(JukeBoxEventType.PLAYLIST_START, currentPlayList);
            }
        }

        // REQUEST NEXT SONG
        if (currentPlayList != null) {
            currentSong = currentPlayList.nextSong();
        }

        // PLAY NEXT SONG AND APPLY FADING
        if (currentSong != null) {
            final SongSource source = currentSong.getSource();
            fadeIn(source, currentSong.getSettings(), 0f);
            currentSong.getSource().play();
            this.pushEvent(JukeBoxEventType.SONG_START, currentSong);
        } else {
            stopped = true;
            this.pushEvent(JukeBoxEventType.JUKEBOX_END);
        }
    }


    /**
     * Adds an observer. See {@link JukeBoxObserver} for details.
     *
     * @param observer the jukebox observer
     */
    public void addObserver(JukeBoxObserver observer) {
        this.observer.add(observer);
    }


    /**
     * Removes an observer.
     *
     * @param observer the jukebox observer
     */
    public void removeObserver(JukeBoxObserver observer) {
        this.observer.removeValue(observer, true);
    }


    protected void pushEvent(JukeBoxEventType type) {
        final JukeBoxEvent event = eventPool.obtain();
        event.setType(type);
        eventHistory.add(event);
    }


    protected void pushEvent(JukeBoxEventType type, Song song) {
        if (song == null) {
            throw new RuntimeException("song is null");
        }
        final JukeBoxEvent event = eventPool.obtain();
        event.setType(type);
        event.setSong(song);
        eventHistory.add(event);
    }


    protected void pushEvent(JukeBoxEventType type, PlayList playList) {
        final JukeBoxEvent event = eventPool.obtain();
        event.setType(type);
        event.setPlayList(playList);
        eventHistory.add(event);
    }


    protected void handleEvents() {
        for (final JukeBoxEvent event : eventHistory) {
            switch (event.getType()) {
                case JUKEBOX_END:
                    notifyJukeBoxEnd();
                    break;
                case JUKEBOX_PAUSE:
                    notifyJukeBoxPause();
                    break;
                case JUKEBOX_START:
                    notifyJukeBoxStart();
                    break;
                case PLAYLIST_END:
                    notifyPlayListEnd(event.getPlayList());
                    break;
                case PLAYLIST_START:
                    notifyPlayListStart(event.getPlayList());
                    break;
                case SONG_END:
                    notifySongEnd(event.getSong());
                    break;
                case SONG_START:
                    notifySongStart(event.getSong());
                    break;
                case NONE:
                    break;
            }
            eventPool.free(event);
        }
        eventHistory.clear();
    }


    protected void notifySongStart(Song song) {
        for (int i = 0; i < observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onSongStart(song);
        }
    }


    protected void notifySongEnd(Song song) {
        for (int i = 0; i < observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onSongEnd(song);
        }
    }


    protected void notifyPlayListStart(PlayList playList) {
        for (int i = 0; i < observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onPlayListStart(playList);
        }
    }


    protected void notifyPlayListEnd(PlayList playList) {
        for (int i = 0; i < observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onPlayListEnd(playList);
        }
    }


    protected void notifyJukeBoxEnd() {
        for (int i = 0; i < observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onJukeBoxEnd();
        }
    }


    protected void notifyJukeBoxStart() {
        for (int i = 0; i < observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onJukeBoxStart();
        }
    }


    protected void notifyJukeBoxPause() {
        for (int i = 0; i < observer.size; i++) {
            final JukeBoxObserver observer = this.observer.get(i);
            observer.onJukeBoxPause();
        }
    }

}
