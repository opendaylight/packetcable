/**
 @header@
 */
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IPCMMError;

/**
 *
 */
public class PCMMError extends PCMMBaseObject implements IPCMMError {
    /**
     *
     */
    public PCMMError() {
        this(LENGTH, STYPE, SNUM);
    }

    public PCMMError(short errorCode, short subErrCode) {
        this();
        setErrorCode(errorCode);
        setErrorSubcode(subErrCode);
    }

    /**
     * @param data
     */
    public PCMMError(byte[] data) {
        super(data);
    }

    /**
     * @param len
     * @param sType
     * @param sNum
     */
    public PCMMError(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCError#setErrorCode(int)
     */
    @Override
    public void setErrorCode(short errorCode) {
        setShort(errorCode, (short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCError#getErrorCode()
     */
    @Override
    public short getErrorCode() {
        return getShort((short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCError#setErrorSubcode(int)
     */
    @Override
    public void setErrorSubcode(short errorSubcode) {
        setShort(errorSubcode, (short) 2);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCError#getErrorCode()
     */
    @Override
    public short getErrorSubcode() {
        return getShort((short) 2);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCError#getDescription()
     */
    @Override
    public String getDescription() {
        String hex = Integer.toHexString(getErrorSubcode() & 0xFFFF);
        return "Error Code: " + getErrorCode() + " Error Subcode : " + hex
               + "  " + Description.valueOf(getErrorCode());
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
