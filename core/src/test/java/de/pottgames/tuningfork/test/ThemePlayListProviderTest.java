package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Interpolation;
import de.pottgames.tuningfork.*;
import de.pottgames.tuningfork.jukebox.JukeBox;
import de.pottgames.tuningfork.jukebox.JukeBoxObserver;
import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.playlist.ThemePlayListProvider;
import de.pottgames.tuningfork.jukebox.song.Song;
import de.pottgames.tuningfork.jukebox.song.SongMeta;
import de.pottgames.tuningfork.jukebox.song.SongSettings;

public class ThemePlayListProviderTest extends ApplicationAdapter implements JukeBoxObserver {
    private Audio                 audio;
    private StreamedSoundSource   rhythm1;
    private SoundBuffer           rhythm2;
    private BufferedSoundSource   rhythm2Source;
    private StreamedSoundSource   rhythm3;
    private SoundBuffer           rhythm4;
    private BufferedSoundSource   rhythm4Source;
    private StreamedSoundSource   rhythm5;
    private JukeBox               jukeBox;
    private ThemePlayListProvider provider;


    @Override
    public void create() {
        this.audio = Audio.init();

        // LOAD SOUNDS
        this.rhythm1 = new StreamedSoundSource(Gdx.files.internal("rhythm.wav"));
        this.rhythm2 = SoundLoader.load(Gdx.files.internal("rhythm2.flac"));
        this.rhythm3 = new StreamedSoundSource(Gdx.files.internal("rhythm3.flac"));
        this.rhythm4 = SoundLoader.load(Gdx.files.internal("rhythm4.flac"));
        this.rhythm5 = new StreamedSoundSource(Gdx.files.internal("short.flac"));

        // OBTAIN BUFFERED SOUND SOURCES
        this.rhythm2Source = this.audio.obtainSource(this.rhythm2);
        this.rhythm4Source = this.audio.obtainSource(this.rhythm4);

        // OPTIONAL: SET RELATIVE
        // if you want to play music globally and not in 3D space, make sure to only provide relative sources
        this.rhythm1.setRelative(true);
        this.rhythm3.setRelative(true);
        this.rhythm5.setRelative(true);
        this.rhythm2Source.setRelative(true);
        this.rhythm4Source.setRelative(true);
        this.rhythm5.setRelative(true);

        // CREATE SONGS
        final SongSettings settings = SongSettings.linear(1f, 2f, 2f);
        final Song song1 =
                new Song(this.rhythm1, SongSettings.linear(1f, 0.5f, 1f), new SongMeta().setTitle("rhythm1"));
        final Song song2 = new Song(this.rhythm2Source, settings, new SongMeta().setTitle("rhythm2"));
        final Song song3 = new Song(this.rhythm3, settings, new SongMeta().setTitle("rhythm3"));
        final Song song4 = new Song(this.rhythm4Source, settings, new SongMeta().setTitle("rhythm4"));
        final Song song5 = new Song(this.rhythm5, settings, new SongMeta().setTitle("rhythm5"));

        // CREATE PLAYLIST 1
        final PlayList playList = new PlayList() {
            @Override
            public String toString() {
                return "PlayList 1";
            }
        };
        playList.addSong(song1);
        playList.addSong(song2);
        playList.addSong(song3);
        playList.addSong(song4);
        playList.setShuffleAfterPlaytrough(true);

        // CREATE PLAYLIST 2
        final PlayList playList2 = new PlayList() {
            @Override
            public String toString() {
                return "PlayList 2";
            }
        };
        playList2.addSong(song5);
        playList2.setLooping(true);

        // CREATE PLAYLIST PROVIDER
        this.provider = new ThemePlayListProvider();
        this.provider.add(playList, 0);
        this.provider.add(playList2, 1);
        this.provider.setTheme(0);

        this.jukeBox = new JukeBox(this.provider);
        this.jukeBox.addObserver(this);
        this.jukeBox.play();
    }


    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            this.jukeBox.play();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            this.jukeBox.pause();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            this.jukeBox.stop();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            this.jukeBox.softStop(Interpolation.linear, 1.5f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            this.provider.setTheme(1 - this.provider.getTheme()); // toggle theme
            this.jukeBox.softStopAndResume(Interpolation.linear, 2f);
        }

        this.jukeBox.update();
    }


    @Override
    public void dispose() {
        this.jukeBox.clear();
        this.rhythm1.dispose();
        this.rhythm2.dispose();
        this.rhythm3.dispose();
        this.rhythm4.dispose();
        this.rhythm5.dispose();
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("ThemePlayListProviderTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new ThemePlayListProviderTest(), config);
    }


    @Override
    public void onSongStart(Song song) {
        System.out.println("Song started: " + song.getMeta().getTitle());
    }


    @Override
    public void onSongEnd(Song song) {
        System.out.println("Song ended: " + song.getMeta().getTitle());
    }


    @Override
    public void onPlayListStart(PlayList playList) {
        System.out.println("PlayList started: " + playList);
    }


    @Override
    public void onPlayListEnd(PlayList playList) {
        System.out.println("PlayList ended: " + playList);
    }


    @Override
    public void onJukeBoxEnd() {
        System.out.println("JukeBox ended");
    }


    @Override
    public void onJukeBoxStart() {
        System.out.println("JukeBox started");
    }


    @Override
    public void onJukeBoxPause() {
        System.out.println("JukeBox paused");
    }

}
