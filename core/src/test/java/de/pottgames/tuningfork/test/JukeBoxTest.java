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

package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Interpolation;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.jukebox.JukeBox;
import de.pottgames.tuningfork.jukebox.JukeBoxObserver;
import de.pottgames.tuningfork.jukebox.playlist.DefaultPlayListProvider;
import de.pottgames.tuningfork.jukebox.playlist.PlayList;
import de.pottgames.tuningfork.jukebox.song.Song;
import de.pottgames.tuningfork.jukebox.song.SongMeta;
import de.pottgames.tuningfork.jukebox.song.SongSettings;

public class JukeBoxTest extends ApplicationAdapter implements JukeBoxObserver {
    private Audio               audio;
    private StreamedSoundSource rhythm1;
    private SoundBuffer         rhythm2;
    private BufferedSoundSource rhythm2Source;
    private StreamedSoundSource rhythm3;
    private SoundBuffer         rhythm4;
    private BufferedSoundSource rhythm4Source;
    private StreamedSoundSource rhythm5;
    private JukeBox             jukeBox;


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

        // CREATE SONGS
        final SongSettings settings = SongSettings.linear(1f, 2f, 2f);
        final Song song1 = new Song(this.rhythm1, SongSettings.linear(1f, 0.5f, 1f), new SongMeta().setTitle("rhythm1"));
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
        final DefaultPlayListProvider provider = new DefaultPlayListProvider().add(playList).add(playList2);

        this.jukeBox = new JukeBox(provider);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
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


    @Override
    public void onSongStart(Song song) {
        System.out.println(JukeBoxTest.timeStamp() + " Song started: " + song.getMeta().getTitle());
    }


    @Override
    public void onSongEnd(Song song) {
        System.out.println(JukeBoxTest.timeStamp() + " Song ended: " + song.getMeta().getTitle());
    }


    @Override
    public void onPlayListStart(PlayList playList) {
        System.out.println(JukeBoxTest.timeStamp() + " PlayList started: ");
    }


    @Override
    public void onPlayListEnd(PlayList playList) {
        System.out.println(JukeBoxTest.timeStamp() + " PlayList ended: ");
    }


    @Override
    public void onJukeBoxEnd() {
        System.out.println(JukeBoxTest.timeStamp() + " JukeBox ended");
    }


    @Override
    public void onJukeBoxStart() {
        System.out.println(JukeBoxTest.timeStamp() + " JukeBox started");
    }


    @Override
    public void onJukeBoxPause() {
        System.out.println(JukeBoxTest.timeStamp() + " JukeBox paused");
    }


    private static String timeStamp() {
        return "[" + System.currentTimeMillis() + "]";
    }

}
