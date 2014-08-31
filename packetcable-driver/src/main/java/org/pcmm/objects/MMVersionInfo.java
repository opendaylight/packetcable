/**
 @header@
 */
package org.pcmm.objects;

import org.pcmm.base.impl.PCMMBaseObject;

/**
 *
 * PCMM MM version info Object
 *
 */
public class MMVersionInfo extends PCMMBaseObject {

    private short majorVersionNB;
    private short minorVersionNB;
    public static final short DEFAULT_MAJOR_VERSION_INFO = (short) 5;
    public static final short DEFAULT_MINOR_VERSION_INFO = (short) 0;

    public MMVersionInfo() {
        this(DEFAULT_MAJOR_VERSION_INFO, DEFAULT_MINOR_VERSION_INFO);
    }

    public MMVersionInfo(short majorVersionNB, short minorVersionNB) {
        super((short) 8, (byte) 1, (byte) 16);
        setShort(this.majorVersionNB = majorVersionNB, (short) 0);
        setShort(this.minorVersionNB = minorVersionNB, (short) 2);
    }

    /**
     * Parse data and create COPSHandle object
     */
    public MMVersionInfo(byte[] dataPtr) {
        super(dataPtr);
        majorVersionNB = getShort((short) 0);
        minorVersionNB = getShort((short) 2);
    }

    /**
     * @return the majorVersionNB
     */
    public short getMajorVersionNB() {
        return majorVersionNB;
    }

    /**
     * @param majorVersionNB
     *            the majorVersionNB to set
     */
    public void setMajorVersionNB(short majorVersionNB) {
        this.majorVersionNB = majorVersionNB;
    }

    /**
     * @return the minorVersionNB
     */
    public short getMinorVersionNB() {
        return minorVersionNB;
    }

    /**
     * @param minorVersionNB
     *            the minorVersionNB to set
     */
    public void setMinorVersionNB(short minorVersionNB) {
        this.minorVersionNB = minorVersionNB;
    }

}
