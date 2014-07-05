/**
 @header@
 */


package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

public interface ITrafficProfile extends IPCMMBaseObject {

    static final byte SNUM = 7;

    /**
     * 0x001, 0x011 and 0x111 (Authorized, Reserved, and Committed) are allowed
     *
     * @param en
     */
    void setEnvelop(byte en);

    byte getEnvelop();

}
