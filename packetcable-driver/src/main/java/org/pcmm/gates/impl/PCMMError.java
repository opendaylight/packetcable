/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IPCMMError;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the IPCMMError interface
 */
public class PCMMError extends PCMMBaseObject implements IPCMMError {

    /**
     * The error code (cannot be NA)
     */
    private final ErrorCode errorCode;

    /**
     * The error sub-code (defaults to NA)
     */
    private final ErrorCode subErrCode;

    /**
     * Constructor without a sub-code which will then be set to NA
     * @param errorCode - the error code (required and not NA)
     */
    public PCMMError(final ErrorCode errorCode) {
        this(errorCode, null);
    }

    /**
     * Constructor with a sub-code
     * @param errorCode - the error code (required and not NA)
     * @param subErrCode - the sub-code (defaults to NA when null)
     */
    public PCMMError(final ErrorCode errorCode, final ErrorCode subErrCode) {
        super(SNum.PCMM_ERROR, STYPE);
        if (errorCode == null || errorCode.equals(ErrorCode.NA))
            throw new IllegalArgumentException("ErrorCode is required and must not be NA");
        this.errorCode = errorCode;
        if (subErrCode == null) this.subErrCode = ErrorCode.NA;
        else this.subErrCode = subErrCode;
    }

    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public ErrorCode getErrorSubcode() {
        return subErrCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCError#getDescription()
     */
    @Override
    public String getDescription() {
        String hex = Integer.toHexString(subErrCode.getCode() & 0xFFFF);
        return "Error Code: " + errorCode.getCode() + " Error Subcode : " + hex + "  " + errorCode.getDescription();
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    protected byte[] getBytes() {
        final byte[] errorCodeBytes = COPSMsgParser.shortToBytes(errorCode.getCode());
        final byte[] subErrCodeBytes = COPSMsgParser.shortToBytes(subErrCode.getCode());
        final byte[] data = new byte[errorCodeBytes.length + subErrCodeBytes.length];
        System.arraycopy(errorCodeBytes, 0, data, 0, errorCodeBytes.length);
        System.arraycopy(subErrCodeBytes, 0, data, errorCodeBytes.length, subErrCodeBytes.length);
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PCMMError)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final PCMMError pcmmError = (PCMMError) o;
        return errorCode == pcmmError.errorCode && subErrCode == pcmmError.subErrCode;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + errorCode.hashCode();
        result = 31 * result + subErrCode.hashCode();
        return result;
    }

    /**
     * Returns a PCMMError object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static PCMMError parse(final byte[] data) {
        return new PCMMError(ErrorCode.valueOf(COPSMsgParser.bytesToShort(data[0], data[1])),
                ErrorCode.valueOf((COPSMsgParser.bytesToShort(data[2], data[3]))));

    }

}
