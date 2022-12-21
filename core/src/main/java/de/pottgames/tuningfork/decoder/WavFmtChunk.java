package de.pottgames.tuningfork.decoder;

public class WavFmtChunk {
    public final int    length;
    private final int[] data;
    private final int   wFormatTag;
    private final int   nChannels;
    private final long  nSamplesPerSec;
    private final long  nAvgBytesPerSec;
    private final int   nBlockAlign;
    private final int   wBitsPerSample;
    private int         cbSize              = -1;
    private int         wValidBitsPerSample = -1;
    private int         dwChannelMask       = -1;
    private int         subFormatDataCode;


    public WavFmtChunk(int[] data) {
        this.data = data;
        this.length = data.length;

        this.wFormatTag = data[0] | data[1] << 8;
        this.nChannels = data[2] | data[3] << 8;
        this.nSamplesPerSec = data[4] | (long) data[5] << 8L | (long) data[6] << 16L | (long) data[7] << 24L;
        this.nAvgBytesPerSec = data[8] | (long) data[9] << 8L | (long) data[10] << 16L | (long) data[11] << 24L;
        this.nBlockAlign = data[12] | data[13] << 8;
        this.wBitsPerSample = data[14] | data[15] << 8;

        if (data.length > 16) {
            this.cbSize = data[16] | data[17] << 8;
            if (data.length > 18) {
                this.wValidBitsPerSample = data[18] | data[19] << 8;
                if (data.length > 20) {
                    this.dwChannelMask = data[20] | data[21] << 8 | data[22] << 16 | data[23] << 24;
                    if (data.length > 24) {
                        this.subFormatDataCode = data[24] | data[25] << 8;

                        // The remaining 14 bytes contain a fixed string, \x00\x00\x00\x00\x10\x00\x80\x00\x00\xAA\x00\x38\x9B\x71. Encoders don't seem to
                        // follow this specification, hence we don't validate it.
                    }
                }
            }
        }

    }


    /**
     * Returns cbSize or -1 if the field is not present.
     *
     * @return cbSize or -1 if the field is not present
     */
    public int getCbSize() {
        return this.cbSize;
    }


    /**
     * Returns wValidBitsPerSample or -1 if the field is not present.
     *
     * @return wValidBitsPerSample or -1 if the field is not present
     */
    public int getwValidBitsPerSample() {
        return this.wValidBitsPerSample;
    }


    /**
     * Returns dwChannelMask or -1 if the field is not present.
     *
     * @return dwChannelMask or -1 if the field is not present
     */
    public int getDwChannelMask() {
        return this.dwChannelMask;
    }


    /**
     * Returns subFormatDataCode or -1 if the field is not present.
     *
     * @return subFormatDataCode or -1 if the field is not present
     */
    public int getSubFormatDataCode() {
        return this.subFormatDataCode;
    }


    /**
     * Returns wFormatTag.
     *
     * @return wFormatTag
     */
    public int getwFormatTag() {
        return this.wFormatTag;
    }


    /**
     * Returns nChannels.
     *
     * @return nChannels
     */
    public int getnChannels() {
        return this.nChannels;
    }


    /**
     * Returns nSamplesPerSec.
     *
     * @return nSamplesPerSec
     */
    public long getnSamplesPerSec() {
        return this.nSamplesPerSec;
    }


    /**
     * Returns nAvgBytesPerSec.
     *
     * @return nAvgBytesPerSec
     */
    public long getnAvgBytesPerSec() {
        return this.nAvgBytesPerSec;
    }


    /**
     * Returns nBlockAlign.
     *
     * @return nBlockAlign
     */
    public int getnBlockAlign() {
        return this.nBlockAlign;
    }


    /**
     * Returns wBitsPerSample.
     *
     * @return wBitsPerSample
     */
    public int getwBitsPerSample() {
        return this.wBitsPerSample;
    }


    /**
     * Returns a byte (int 0 - 255) of the original chunk data as read from the stream. {@link WavFmtChunk#length} is set to the original data length.
     *
     * @param index
     *
     * @return an int containing the byte of the original data
     */
    public int getRaw(int index) {
        return this.data[index];
    }

}
