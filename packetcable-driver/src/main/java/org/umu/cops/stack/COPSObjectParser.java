package org.umu.cops.stack;

import java.util.Arrays;

/**
 * Static utilitarian methods for parsing COPS objects contained within COPS messages.
 */
public class COPSObjectParser {

    /**
     * Parses the header information for a COPSObjBase object.
     * @param data - the data to parse
     * @return - the header
     */
    public static COPSObjHeaderData parseObjHeader(final byte[] data) {
        if (data == null || data.length < 4)
            throw new IllegalArgumentException("Data cannot be null or fewer than 4 bytes");

        // TODO - Determine what setting the _len value from the data buffer really means
        int len = 0;
        len |= ((short) data[0]) << 8;
        len |= ((short) data[1]) & 0xFF;

        int cNum = 0;
        cNum |= data[2];

        int cType = 0;
        cType |= data[3];

        return new COPSObjHeaderData(
                new COPSObjHeader(COPSObjHeader.VAL_TO_CNUM.get(cNum), COPSObjHeader.VAL_TO_CTYPE.get(cType)), len);
    }

    /**
     * Parses bytes to return a COPSInterface object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSInterface parseIpv4Interface(final COPSObjHeaderData objHdrData, final byte[] dataPtr,
                                                   final boolean isIn) {
        if (isIn) return COPSIpv4InInterface.parse(objHdrData, dataPtr);
        else return COPSIpv4OutInterface.parse(objHdrData, dataPtr);
    }

    /**
     * Parses bytes to return a COPSIpv6Interface object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSIpv6Interface parseIpv6Interface(final COPSObjHeaderData objHdrData, final byte[] dataPtr,
                                                       final boolean isIn) {
        if (isIn) return COPSIpv6InInterface.parse(objHdrData, dataPtr);
        else return COPSIpv6OutInterface.parse(objHdrData, dataPtr);
    }

    /**
     * Add padding in the data, if the data does not fall on 32-bit boundary
     * @param    len                 an int
     * @return   a COPSData
     */
    public static COPSData getPadding(final int len) {
        byte[] padBuf = new byte[len];
        Arrays.fill(padBuf, (byte) 0);
        return new COPSData(padBuf, 0, len);
    }

}
