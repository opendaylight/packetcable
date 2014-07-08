/**
 @header@
 */
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITrafficProfile;

/**
 *
 */
public class DOCSISServiceClassNameTrafficProfile extends PCMMBaseObject
            implements ITrafficProfile {

    public static final byte STYPE = 2;
    public static final short LENGTH = 12;

    /**
     *
     */
    public DOCSISServiceClassNameTrafficProfile() {
        this(LENGTH, STYPE, SNUM);
    }

    /**
     * @param data
     */
    public DOCSISServiceClassNameTrafficProfile(byte[] data) {
        super(data);
    }

    /**
     * @param len
     * @param sType
     * @param sNum
     */
    public DOCSISServiceClassNameTrafficProfile(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
        setEnvelop((byte) 0x7);
    }

    /**
     * @return the serviceClassName
     */
    public String getServiceClassName() {
        return new String(getBytes((short) 4, (short) 4));
    }

    /**
     * @param serviceClassName
     *            the serviceClassName to set
     */
    public void setServiceClassName(String serviceClassName) {
        setBytes(serviceClassName.getBytes(), (short) 4);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ITrafficProfile#getEnvelop()
     */
    @Override
    public byte getEnvelop() {
        return getBytes((short) 0, (short) 1)[0];
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ITrafficProfile#setEnvelop(byte)
     */
    @Override
    public void setEnvelop(byte en) {
        setBytes(new byte[] { en }, (short) 0);
    }
}
