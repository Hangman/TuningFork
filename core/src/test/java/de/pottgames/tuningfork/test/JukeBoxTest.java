package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.FlacLoader;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.jukebox.JukeBox;
import de.pottgames.tuningfork.jukebox.PlayList;
import de.pottgames.tuningfork.jukebox.Song;
import de.pottgames.tuningfork.jukebox.SongSettings;

public class JukeBoxTest extends ApplicationAdapter {
    private Audio               audio;
    private StreamedSoundSource rhythm1;
    private SoundBuffer         rhythm2;
    private BufferedSoundSource rhythm2Source;
    private StreamedSoundSource rhythm3;
    private SoundBuffer         rhythm4;
    private BufferedSoundSource rhythm4Source;
    private JukeBox             jukeBox;


    @Override
    public void create() {
        this.audio = Audio.init();

        // LOAD SOUNDS
        this.rhythm1 = new StreamedSoundSource(Gdx.files.internal("rhythm.wav"));
        this.rhythm2 = FlacLoader.load(Gdx.files.internal("rhythm2.flac"));
        this.rhythm3 = new StreamedSoundSource(Gdx.files.internal("rhythm3.flac"));
        this.rhythm4 = FlacLoader.load(Gdx.files.internal("rhythm4.flac"));

        // OBTAIN BUFFERED SOUND SOURCE
        this.rhythm2Source = this.audio.obtainSource(this.rhythm2);
        this.rhythm4Source = this.audio.obtainSource(this.rhythm4);

        // CREATE SONGS
        final SongSettings settings = SongSettings.linear(1f, 2f, 2f);
        final Song song1 = new Song(this.rhythm1, SongSettings.linear(1f, 0.5f, 1f));
        final Song song2 = new Song(this.rhythm2Source, settings);
        final Song song3 = new Song(this.rhythm3, settings);
        final Song song4 = new Song(this.rhythm4Source, settings);

        // CREATE PLAYLIST
        final PlayList playList = new PlayList();
        playList.addSong(song1);
        playList.addSong(song2);
        playList.addSong(song3);
        playList.addSong(song4);
        playList.setLooping(true);

        this.jukeBox = new JukeBox();
        this.jukeBox.queuePlayList(playList);
        this.jukeBox.play();
    }


    @Override
    public void render() {
        this.jukeBox.update();
    }


    @Override
    public void dispose() {

        // always dispose Audio last
        this.audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("JukeBoxTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new JukeBoxTest(), config);
    }
}
