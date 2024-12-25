package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.AudioConfig.Virtualization;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundLoader;
import de.pottgames.tuningfork.StreamedSoundSource;

public class DirectChannelRemixTest extends ApplicationAdapter {
    private static final Virtualization[] virtualizations     = { Virtualization.OFF_DROP_CHANNELS, Virtualization.OFF_REMIX_CHANNELS, Virtualization.ON };
    private int                           virtualizationIndex = -1;
    private Audio                         audio;
    private SoundBuffer                   sound;
    private StreamedSoundSource           streamedSource;
    private boolean                       streamed            = false;


    @Override
    public void create() {
        final AudioConfig config = new AudioConfig();
        config.setVirtualization(Virtualization.ON);
        audio = Audio.init(config);
        sound = SoundLoader.load(Gdx.files.internal("quadrophonic.ogg"));
        streamedSource = new StreamedSoundSource(Gdx.files.internal("quadrophonic.ogg"));
        System.out.println("Press space to switch through the different virtualization settings.");
    }


    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            virtualizationIndex++;
            if (virtualizationIndex >= DirectChannelRemixTest.virtualizations.length) {
                virtualizationIndex = 0;
                streamed = !streamed;
            }
            final Virtualization virtualization = DirectChannelRemixTest.virtualizations[virtualizationIndex];
            System.out.println("virtualization: " + virtualization);
            audio.setDefaultVirtualization(virtualization);
            if (streamed) {
                System.out.println("playing via StreamedSoundSource");
                streamedSource.play();
            } else {
                System.out.println("playing via SoundBuffer");
                sound.play();
            }
        }
    }


    @Override
    public void dispose() {
        sound.dispose();
        audio.dispose();
    }


    public static void main(String[] args) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DirectChannelRemixTest");
        config.setWindowedMode(1000, 800);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new DirectChannelRemixTest(), config);
    }

}
