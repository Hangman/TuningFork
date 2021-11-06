package de.pottgames.tuningfork;

import com.badlogic.gdx.utils.Array;

class SoundSourcePool {
    private final Array<BufferedSoundSource> sources         = new Array<>();
    private int                              nextSourceIndex = 0;
    private final Audio                      audio;


    SoundSourcePool(int simultaneousSources) {
        this.audio = Audio.get();
        for (int i = 0; i < simultaneousSources; i++) {
            this.sources.add(new BufferedSoundSource());
        }
    }


    BufferedSoundSource findFreeSource() {
        BufferedSoundSource result = null;
        final int startSourceIndex = this.nextSourceIndex;

        // FIND FREE SOUND SOURCE
        while (result == null) {
            final BufferedSoundSource candidate = this.sources.get(this.nextSourceIndex);
            if (!candidate.obtained && !candidate.isPlaying()) {
                candidate.reset(this.audio.getDefaultAttenuationFactor(), this.audio.getDefaultAttenuationMinDistance(),
                        this.audio.getDefaultAttenuationMaxDistance());
                result = candidate;
            }
            this.nextSourceIndex++;
            if (this.nextSourceIndex >= this.sources.size) {
                this.nextSourceIndex = 0;
            }
            if (this.nextSourceIndex == startSourceIndex) {
                break;
            }
        }

        return result;
    }


    void resumeAll() {
        for (final BufferedSoundSource source : this.sources) {
            if (source.isPaused()) {
                final boolean obtainedState = source.obtained;
                source.obtained = true;
                source.play();
                source.obtained = obtainedState;
            }
        }
    }


    void pauseAll() {
        for (final BufferedSoundSource source : this.sources) {
            if (source.isPlaying()) {
                final boolean obtainedState = source.obtained;
                source.obtained = true;
                source.pause();
                source.obtained = obtainedState;
            }
        }
    }


    void stopAll() {
        for (final BufferedSoundSource source : this.sources) {
            final boolean obtainedState = source.obtained;
            source.obtained = true;
            source.stop();
            if (!obtainedState) {
                source.setBuffer(null);
            }
            source.obtained = obtainedState;
        }
    }


    void onBufferDisposal(SoundBuffer buffer) {
        for (final BufferedSoundSource source : this.sources) {
            if (source.getBuffer() == buffer) {
                source.obtained = true;
                source.stop();
                source.setBuffer(null);
                source.obtained = false;
            }
        }
    }


    void dispose() {
        this.sources.forEach(BufferedSoundSource::dispose);
        this.sources.clear();
    }

}
