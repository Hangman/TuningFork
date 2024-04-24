package de.pottgames.tuningfork.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import de.pottgames.tuningfork.*;
import de.pottgames.tuningfork.AudioConfig.Virtualization;

public class DirectChannelRemixTest extends ApplicationAdapter {
    private static final Virtualization[]    virtualizations     =
            {Virtualization.OFF_DROP_CHANNELS, Virtualization.OFF_REMIX_CHANNELS, Virtualization.ON};
    private              int                 virtualizationIndex = -1;
    private              Audio               audio;
    private              SoundBuffer         sound;
    private              StreamedSoundSource streamedSource;
    private              boolean             streamed            = false;


    @Override
    public void create() {
        final AudioConfig config = new AudioConfig();
        config.setVirtualization(Virtualization.ON);
        this.audio = Audio.init(config);
        this.sound = SoundLoader.load(Gdx.files.internal("quadrophonic.ogg"));
        this.streamedSource = new StreamedSoundSource(Gdx.files.internal("quadrophonic.ogg"));
        System.out.println("Press space to switch through the different virtualization settings.");
    }


    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            this.virtualizationIndex++;
            if (this.virtualizationIndex >= DirectChannelRemixTest.virtualizations.length) {
                this.virtualizationIndex = 0;
                this.streamed = !this.streamed;
            }
            final Virtualization virtualization = DirectChannelRemixTest.virtualizations[this.virtualizationIndex];
            System.out.println("virtualization: " + virtualization);
            this.audio.setDefaultVirtualization(virtualization);
            if (this.streamed) {
                System.out.println("playing via StreamedSoundSource");
                this.streamedSource.play();
            } else {
                System.out.println("playing via SoundBuffer");
                this.sound.play();
            }
        }
    }


    @Override
    public void dispose() {
        this.sound.dispose();
        this.audio.dispose();
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
