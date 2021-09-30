package de.pottgames.tuningfork;

import com.badlogic.gdx.utils.Array;

class SoundSourcePool {
    private final Array<SoundSource> sources         = new Array<>();
    private int                      nextSourceIndex = 0;


    SoundSourcePool(int simultaneousSources) {
        for (int i = 0; i < simultaneousSources; i++) {
            this.sources.add(new SoundSource());
        }
    }


    SoundSource findFreeSource() {
        SoundSource result = null;
        final int startSourceIndex = this.nextSourceIndex;

        // FIND FREE SOUND SOURCE
        while (result == null) {
            final SoundSource candidate = this.sources.get(this.nextSourceIndex);
            if (!candidate.obtained && !candidate.isPlaying()) {
                candidate.reset();
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


    void dispose() {
        this.sources.forEach(SoundSource::dispose);
    }

}
