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

package de.pottgames.tuningfork;

import com.badlogic.gdx.utils.Array;

import de.pottgames.tuningfork.AudioConfig.Spatialization;
import de.pottgames.tuningfork.AudioConfig.Virtualization;

class SoundSourcePool {
    private final Array<BufferedSoundSource> sources         = new Array<>();
    private int                              nextSourceIndex = 0;


    SoundSourcePool(int simultaneousSources) {
        for (int i = 0; i < simultaneousSources; i++) {
            this.sources.add(new BufferedSoundSource());
        }
    }


    BufferedSoundSource findFreeSource(AudioSettings defaultSettings) {
        BufferedSoundSource result = null;
        final int startSourceIndex = this.nextSourceIndex;

        // FIND FREE SOUND SOURCE
        while (result == null) {
            final BufferedSoundSource candidate = this.sources.get(this.nextSourceIndex);
            if (!candidate.obtained && !candidate.isPlaying()) {
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

        // IF NO SOURCE WAS FOUND, CREATE A NEW ONE ON THE FLY
        if (result == null) {
            result = new BufferedSoundSource();
            this.sources.add(result);
        }

        result.reset(defaultSettings);
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


    void setResamplerByIndex(int index) {
        for (final BufferedSoundSource source : this.sources) {
            source.setResamplerByIndex(index);
        }
    }


    void setVirtualization(Virtualization virtualization) {
        for (final BufferedSoundSource source : this.sources) {
            source.setVirtualization(virtualization);
        }
    }


    void setSpatialization(Spatialization spatialization) {
        for (final BufferedSoundSource source : this.sources) {
            source.setSpatialization(spatialization);
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
