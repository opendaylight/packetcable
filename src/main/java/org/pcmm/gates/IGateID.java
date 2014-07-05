/**
 @header@
 */
package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 *
 */
public interface IGateID extends IPCMMBaseObject {
    static final short LENGTH = 8;
    static final byte SNUM = 4;
    static final byte STYPE = 1;

    void setGateID(int gateID);

    int getGateID();
}
