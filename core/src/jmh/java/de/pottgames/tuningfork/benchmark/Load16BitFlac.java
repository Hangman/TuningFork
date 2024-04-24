/**
 * Copyright 2023 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.benchmark;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.FlacLoader;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.logger.MockLogger;
import org.openjdk.jmh.annotations.*;

import java.io.File;

@State(Scope.Thread)
public class Load16BitFlac {
    private Audio       audio;
    private SoundBuffer soundBuffer;


    @Benchmark
    public void load() {
        this.soundBuffer = FlacLoader.load(new File("src/jmh/resources/bench_16bit-flj.flac"));
    }


    @Setup(Level.Iteration)
    public void setup() {
        final AudioConfig config = new AudioConfig();
        config.setLogger(new MockLogger());
        this.audio = Audio.init(config);
    }


    @TearDown(Level.Iteration)
    public void teardown() {
        this.soundBuffer.dispose();
        this.audio.dispose();
    }

}
