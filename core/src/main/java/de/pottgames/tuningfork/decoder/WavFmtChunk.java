package de.pottgames.tuningfork.decoder;

import de.pottgames.tuningfork.DataFormatException;

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
        this.nSamplesPerSec = data[4] | data[5] << 8L | data[6] << 16L | data[7] << 24L;
        this.nAvgBytesPerSec = data[8] | data[9] << 8L | data[10] << 16L | data[11] << 24L;
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
                        final boolean guid1 = data[26] == 0x00;
                        final boolean guid2 = data[27] == 0x00;
                        final boolean guid3 = data[28] == 0x00;
                        final boolean guid4 = data[29] == 0x00;
                        final boolean guid5 = data[30] == 0x10;
                        final boolean guid6 = data[31] == 0x00;
                        final boolean guid7 = data[32] == 0x80;
                        final boolean guid8 = data[33] == 0x00;
                        final boolean guid9 = data[34] == 0x00;
                        final boolean guid10 = data[35] == 0xAA;
                        final boolean guid11 = data[36] == 0x00;
                        final boolean guid12 = data[37] == 0x38;
                        final boolean guid13 = data[38] == 0x9b;
                        final boolean guid14 = data[39] == 0x71;
                        final boolean validGuid = guid1 && guid2 && guid3 && guid4 && guid5 && guid6 && guid7 && guid8 && guid9 && guid10 && guid11 && guid12
                                && guid13 && guid14;
                        if (!validGuid) {
                            throw new DataFormatException("Invalid extension GUID");
                        }
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
