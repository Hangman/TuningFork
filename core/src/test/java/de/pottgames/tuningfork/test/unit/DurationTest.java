/**
 * Copyright 2023 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.test.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;

import de.pottgames.tuningfork.AiffLoader;
import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.AudioConfig;
import de.pottgames.tuningfork.FlacLoader;
import de.pottgames.tuningfork.Mp3Loader;
import de.pottgames.tuningfork.OggLoader;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.StreamedSoundSource;
import de.pottgames.tuningfork.WaveLoader;
import de.pottgames.tuningfork.logger.ConsoleLogger;
import de.pottgames.tuningfork.logger.ConsoleLogger.LogLevel;
import de.pottgames.tuningfork.logger.ErrorLogger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DurationTest {
    private static final float TOLERANCE      = 0.01f;
    private static final float HIGH_TOLERANCE = 0.1f;
    private Audio              audio;
    private ErrorLogger        errorChecker;


    @BeforeAll
    public void setup() {
        Gdx.files = new Lwjgl3Files(); // hack setup gdx because we only need Gdx.files in order to run properly
        this.audio = Audio.init(new AudioConfig().setLogger(new ConsoleLogger(LogLevel.INFO_WARN_ERROR)));
        this.errorChecker = new ErrorLogger(this.getClass(), new ConsoleLogger());
    }


    @Test
    public void test1() {
        this.testWav("numbers.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test2() {
        this.testWav("24bit_stereo.wav", 5.538f, DurationTest.TOLERANCE);
    }


    @Test
    public void test3() {
        this.testWav("32bit_float_numbers.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test4() {
        this.testWav("32bit_stereo.wav", 5.619f, DurationTest.TOLERANCE);
    }


    @Test
    public void test5() {
        this.testWav("64bit_float_numbers.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test6() {
        this.testOgg("carnivalrides.ogg", 25.498f, DurationTest.TOLERANCE);
    }


    @Test
    public void test7() {
        this.testWav("extensible_88200hertz.wav", 6.629f, DurationTest.TOLERANCE);
    }


    @Test
    public void test8() {
        this.testWav("guitar.wav", 9.915f, DurationTest.TOLERANCE);
    }


    @Test
    public void test9() {
        this.testAiff("guitar_32bit_float.aiff", 9.915f, DurationTest.TOLERANCE);
    }


    @Test
    public void test10() {
        this.testWav("ima_adpcm_mono.wav", 5.563f, DurationTest.TOLERANCE);
    }


    @Test
    public void test11() {
        this.testWav("ima_adpcm_stereo.wav", 11.2f, DurationTest.TOLERANCE);
    }


    @Test
    public void test12() {
        this.testAiff("M1F1-int8-AFsp.aif", 2.937f, DurationTest.TOLERANCE);
    }


    @Test
    public void test13() {
        this.testAiff("M1F1-int12-AFsp.aif", 2.937f, DurationTest.TOLERANCE);
    }


    @Test
    public void test14() {
        this.testAiff("M1F1-int32-AFsp.aif", 2.937f, DurationTest.TOLERANCE);
    }


    @Test
    public void test15() {
        this.testWav("ms_adpcm_mono.wav", 4.63f, DurationTest.HIGH_TOLERANCE);
    }


    @Test
    public void test16() {
        this.testWav("ms_adpcm_stereo.wav", 9.003f, DurationTest.HIGH_TOLERANCE);
    }


    @Test
    public void test17() {
        this.testAiff("numbers.aiff", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test18() {
        this.testMp3("numbers.mp3", 10.58f, DurationTest.HIGH_TOLERANCE);
    }


    @Test
    public void test19() {
        this.testFlac("numbers_8bit_mono.flac", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test20() {
        this.testWav("numbers_8bit_mono.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test21() {
        this.testWav("numbers_8bit_mono_8kHz.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test22() {
        this.testWav("numbers_8bit_stereo.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test23() {
        this.testFlac("numbers_16bit_mono.flac", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test24() {
        this.testFlac("numbers_16bit_stereo.flac", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test25() {
        this.testMp3("numbers_stereo.mp3", 10.867f, DurationTest.HIGH_TOLERANCE);
    }


    @Test
    public void test26() {
        this.testOgg("numbers2.ogg", 5.542f, DurationTest.TOLERANCE);
    }


    @Test
    public void test27() {
        this.testOgg("numbers3.ogg", 1080.765f, DurationTest.TOLERANCE);
    }


    @Test
    public void test28() {
        this.testAiff("numbers-alaw.aifc", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test29() {
        this.testWav("numbers-alaw.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test30() {
        this.testAiff("numbers-ulaw.aifc", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test31() {
        this.testWav("numbers-ulaw.wav", 10.508f, DurationTest.TOLERANCE);
    }


    @Test
    public void test32() {
        // Jorbis throws an error when trying to measure the duration of this file, it's okay to return -1 in this case

        final String path = "quadrophonic.ogg";
        final float duration = 0.636f;
        final float tolerance = DurationTest.HIGH_TOLERANCE;

        final FileHandle file = Gdx.files.absolute("src/test/resources/" + path);
        final SoundBuffer buffer = OggLoader.load(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));

        final StreamedSoundSource source = new StreamedSoundSource(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        final float bufferDuration = buffer.getDuration();
        final float streamDuration = source.getDuration();
        final String failMessageBuffer = this.assertionFailureMessage(file.name(), duration, bufferDuration, false);
        final String failMessageStream = this.assertionFailureMessage(file.name(), duration, streamDuration, true);

        final boolean bufferDurationMatches = MathUtils.isEqual(duration, bufferDuration, tolerance);
        final boolean streamDurationMatches = MathUtils.isEqual(duration, streamDuration, tolerance);
        final boolean isStreamDuration = !MathUtils.isEqual(streamDuration, -1f);

        Assertions.assertTrue(bufferDurationMatches, failMessageBuffer);
        Assertions.assertTrue(streamDurationMatches || !isStreamDuration, failMessageStream);

        source.dispose();
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        buffer.dispose();
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
    }


    @Test
    public void test33() {
        this.testWav("quadrophonic.wav", 0.636f, DurationTest.TOLERANCE);
    }


    @Test
    public void test34() {
        this.testWav("rhythm.wav", 3.692f, DurationTest.TOLERANCE);
    }


    @Test
    public void test35() {
        this.testFlac("rhythm2.flac", 16.615f, DurationTest.TOLERANCE);
    }


    @Test
    public void test36() {
        this.testFlac("rhythm3.flac", 16.615f, DurationTest.TOLERANCE);
    }


    @Test
    public void test37() {
        this.testFlac("rhythm4.flac", 16.615f, DurationTest.TOLERANCE);
    }


    @Test
    public void test38() {
        this.testFlac("short.flac", 1.846f, DurationTest.TOLERANCE);
    }


    @Test
    public void test39() {
        this.testWav("test_mono_1s.wav", 1f, DurationTest.TOLERANCE);
    }


    @Test
    public void test40() {
        this.testWav("test_stereo_1s.wav", 1f, DurationTest.TOLERANCE);
    }


    @Test
    public void test41() {
        this.testAiff("wood12.aiff", 0.037f, DurationTest.TOLERANCE);
    }


    @Test
    public void test42() {
        this.testAiff("wood24.aiff", 0.037f, DurationTest.TOLERANCE);
    }


    @Test
    public void test43() {
        this.testAiff("guitar_64bit_float.aiff", 9.915f, DurationTest.TOLERANCE);
    }


    private void testWav(String path, float duration, float tolerance) {
        final FileHandle file = Gdx.files.absolute("src/test/resources/" + path);
        final SoundBuffer buffer = WaveLoader.load(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        this.test(buffer, file, duration, tolerance);
    }


    private void testOgg(String path, float duration, float tolerance) {
        final FileHandle file = Gdx.files.absolute("src/test/resources/" + path);
        final SoundBuffer buffer = OggLoader.load(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        this.test(buffer, file, duration, tolerance);
    }


    private void testMp3(String path, float duration, float tolerance) {
        final FileHandle file = Gdx.files.absolute("src/test/resources/" + path);
        final SoundBuffer buffer = Mp3Loader.load(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        this.test(buffer, file, duration, tolerance);
    }


    private void testFlac(String path, float duration, float tolerance) {
        final FileHandle file = Gdx.files.absolute("src/test/resources/" + path);
        final SoundBuffer buffer = FlacLoader.load(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        this.test(buffer, file, duration, tolerance);
    }


    private void testAiff(String path, float duration, float tolerance) {
        final FileHandle file = Gdx.files.absolute("src/test/resources/" + path);
        final SoundBuffer buffer = AiffLoader.load(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        this.test(buffer, file, duration, tolerance);
    }


    private void test(SoundBuffer buffer, FileHandle file, float duration, float tolerance) {
        final StreamedSoundSource source = new StreamedSoundSource(file);
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        final float bufferDuration = buffer.getDuration();
        final float streamDuration = source.getDuration();
        final String failMessageBuffer = this.assertionFailureMessage(file.name(), duration, bufferDuration, false);
        final String failMessageStream = this.assertionFailureMessage(file.name(), duration, streamDuration, true);

        Assertions.assertTrue(MathUtils.isEqual(duration, bufferDuration, tolerance), failMessageBuffer);
        Assertions.assertTrue(MathUtils.isEqual(duration, streamDuration, tolerance), failMessageStream);

        source.dispose();
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
        buffer.dispose();
        Assertions.assertFalse(this.errorChecker.checkLogError(null));
    }


    private String assertionFailureMessage(String path, float expected, float result, boolean streamed) {
        return path + (streamed ? " streamed" : " buffered") + " failed. expected: " + expected + ", got: " + result;
    }


    @AfterAll
    public void cleanup() {
        this.audio.dispose();
    }

}
