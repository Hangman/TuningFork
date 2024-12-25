/**
 * Copyright 2024 Matthias Finke
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.pottgames.tuningfork.decoder;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.PcmFormat.PcmDataType;
import de.pottgames.tuningfork.TuningForkException;
import de.pottgames.tuningfork.TuningForkRuntimeException;
import de.pottgames.tuningfork.decoder.util.Util;
import de.pottgames.tuningfork.logger.TuningForkLogger;

/**
 * An {@link AudioStream} implementation to read qoa files.
 *
 * @author Matthias
 */
public class QoaInputStream implements AudioStream {
    private final InputStream      stream;
    private QoaDecoder             decoder;
    private final TuningForkLogger logger;
    private final FileHandle       file;
    private final float            duration;
    private boolean                closed = false;


    /**
     * Initializes a {@link QoaInputStream} from a {@link FileHandle}.
     *
     * @param file the file handle
     */
    public QoaInputStream(FileHandle file) {
        this(file, true);
    }


    /**
     * Initializes a {@link QoaInputStream} from a {@link FileHandle}.
     *
     * @param file the file handle
     * @param forStreaming true if this will be used for streaming
     */
    public QoaInputStream(FileHandle file, boolean forStreaming) {
        stream = file.read();
        this.file = file;
        logger = Audio.get().getLogger();
        setup(forStreaming);
        duration = (float) totalSamplesPerChannel() / getSampleRate();
    }


    /**
     * Initializes a {@link QoaInputStream} from an {@link InputStream}. This stream does not support the reset function. Use
     * {@link #QoaInputStream(FileHandle)} instead to get the full functionality.
     *
     * @param stream the input stream
     */
    public QoaInputStream(InputStream stream) {
        this(stream, true);
    }


    /**
     * Initializes a {@link QoaInputStream} from an {@link InputStream}. This stream does not support the reset function. Use
     * {@link #QoaInputStream(FileHandle)} instead to get the full functionality.
     *
     * @param stream the input stream
     * @param forStreaming true if this will be used for streaming
     */
    public QoaInputStream(InputStream stream, boolean forStreaming) {
        this.stream = stream;
        file = null;
        logger = Audio.get().getLogger();
        setup(forStreaming);
        duration = (float) totalSamplesPerChannel() / getSampleRate();
    }


    private void setup(boolean forStreaming) {
        final byte[] header = new byte[8];
        try {
            if (Util.readAll(stream, header, header.length) < 8) {
                this.throwRuntimeError("Not a valid QOA file, header too short");
            }
        } catch (final IOException e) {
            this.throwRuntimeError("Error reading QOA file", e);
        }
        if (header[0] != 'q' || header[1] != 'o' || header[2] != 'a' || header[3] != 'f') {
            this.throwRuntimeError("Not a valid QOA file, header missing qoaf identifier");
        }
        final long totalSamples = Util.uIntOfBigEndianBytes(header, 4);
        if (totalSamples == 0) {
            this.throwRuntimeError("Invalid static QOA file, streamed QOA isn't supported by TuningFork");
        }
        try {
            decoder = new QoaDecoder(stream, totalSamples);
        } catch (final IOException | TuningForkException e) {
            this.throwRuntimeError("Error creating QOA decoder", e);
        }
    }


    @Override
    public int read(byte[] bytes) {
        try {
            return decoder.read(bytes);
        } catch (final IOException | TuningForkException e) {
            throw new TuningForkRuntimeException(e);
        }
    }


    @Override
    public float getDuration() {
        return duration;
    }


    @Override
    public AudioStream reset() {
        if (file == null) {
            this.throwRuntimeError("This AudioStream doesn't support resetting.");
        }
        StreamUtils.closeQuietly(this);
        return new QoaInputStream(file);
    }


    public long totalSamplesPerChannel() {
        return decoder.outputTotalSamplesPerChannel();
    }


    @Override
    public int getChannels() {
        return decoder.outputChannels();
    }


    @Override
    public int getSampleRate() {
        return decoder.outputSampleRate();
    }


    @Override
    public int getBitsPerSample() {
        return decoder.outputBitsPerSample();
    }


    @Override
    public PcmDataType getPcmDataType() {
        return decoder.outputPcmDataType();
    }


    private void throwRuntimeError(String message) {
        this.throwRuntimeError(message, null);
    }


    private void throwRuntimeError(String message, Exception e) {
        if (e == null) {
            throw new TuningForkRuntimeException(message + ": " + file.toString());
        }
        throw new TuningForkRuntimeException(message + ". " + e.getMessage() + ": " + file.toString(), e);
    }


    @Override
    public boolean isClosed() {
        return closed;
    }


    @Override
    public void close() throws IOException {
        try {
            if (decoder != null) {
                decoder.close();
            } else {
                stream.close();
            }
        } catch (final IOException e) {
            // ignore but log it
            logger.error(this.getClass(), "QoaInputStream didn't close successfully: " + e.getMessage());
        } finally {
            closed = true;
        }
    }

}
