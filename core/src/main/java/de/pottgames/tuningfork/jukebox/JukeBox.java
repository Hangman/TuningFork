package de.pottgames.tuningfork.jukebox;

import de.pottgames.tuningfork.jukebox.SongSettings.FadeType;

public class JukeBox {
    protected PlayList currentPlayList;
    protected PlayList nextPlayList;
    protected Song     currentSong;
    protected boolean  stopped = true;


    public void update() {
        if (this.stopped) {
            return;
        }

        if (this.currentSong != null) {
            final SongSource source = this.currentSong.getSource();
            if (source.isPlaying()) {
                final float playbackPos = source.getPlaybackPosition();
                final SongSettings settings = this.currentSong.getSettings();
                final boolean fadeIn = this.fadeIn(source, settings, playbackPos);
                final boolean fadeOut = this.fadeOut(source, settings, playbackPos, source.getDuration());
                if (!fadeIn && !fadeOut) {
                    source.setVolume(settings.getVolume());
                }
            } else {
                this.nextSong();
            }
        } else {
            this.nextSong();
        }
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


    public void queuePlayList(PlayList list) {
        this.nextPlayList = list;
    }


    public void play() {
        this.stopped = false;
    }


    protected void nextSong() {
        this.currentSong = null;
        if (this.currentPlayList == null) {
            this.currentPlayList = this.nextPlayList;
        }

        // SET TO NEXT PLAYLIST OR LOOP CURRENT PLAYLIST
        if (this.currentPlayList != null) {
            if (this.currentPlayList.playedFully && this.currentPlayList.loop) {
                if (this.currentPlayList.loop && this.nextPlayList == null) {
                    this.currentPlayList.reset();
                } else {
                    this.currentPlayList = this.nextPlayList;
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
        } else {
            this.stopped = true;
        }
    }

}
