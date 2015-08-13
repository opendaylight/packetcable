/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.objects;

import org.pcmm.base.impl.PCMMBaseObject;
import org.umu.cops.stack.COPSMsgParser;

/**
 * The Version Info object is used to enable Multimedia applications to adapt their interactions with other devices so
 * that interoperability can be achieved between products supporting different protocol versions. Both the Major
 * Version Number and the Minor Version Number are 2 byte unsigned integers. Both the PDP and the PEP must include this
 * object as specified in Section 6.5.1
 */
public class MMVersionInfo extends PCMMBaseObject {

    public static final short DEFAULT_MAJOR_VERSION_INFO = (short) 5;
    public static final short DEFAULT_MINOR_VERSION_INFO = (short) 0;

    /**
     * The major version number
     */
    private final short majorVersionNB;

    /**
     * The minor version number
     */
    private final short minorVersionNB;

    /**
     * Constructor
     * @param majorVersionNB - the major version number
     * @param minorVersionNB - the minor version number
     */
    public MMVersionInfo(short majorVersionNB, short minorVersionNB) {
        super(SNum.VERSION_INFO, (byte)1);
        this.majorVersionNB = majorVersionNB;
        this.minorVersionNB = minorVersionNB;
    }

    /**
     * @return the majorVersionNB
     */
    public short getMajorVersionNB() {
        return majorVersionNB;
    }

    /**
     * @return the minorVersionNB
     */
    public short getMinorVersionNB() {
        return minorVersionNB;
    }

    @Override
    protected byte[] getBytes() {
        final byte[] majVerBytes = COPSMsgParser.shortToBytes(majorVersionNB);
        final byte[] minVerBytes = COPSMsgParser.shortToBytes(minorVersionNB);
        final byte[] data = new byte[majVerBytes.length + minVerBytes.length];
        System.arraycopy(majVerBytes, 0, data, 0, majVerBytes.length);
        System.arraycopy(minVerBytes, 0, data, majVerBytes.length, minVerBytes.length);
        return data;
    }

    /**
     * Returns an MMVersionInfo object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static MMVersionInfo parse(final byte[] data) {
        return new MMVersionInfo(COPSMsgParser.bytesToShort(data[0], data[1]),
                COPSMsgParser.bytesToShort(data[2], data[3]));
    }

}
