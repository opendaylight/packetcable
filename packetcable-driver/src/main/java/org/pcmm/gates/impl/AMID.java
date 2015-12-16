/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IAMID;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the IAMID interface
 */
public class AMID extends PCMMBaseObject implements IAMID {

    /**
     * Application Type is a 2-byte unsigned integer value which identifies the type of application that this gate is
     * associated with. The Application Manager MUST include this object in all messages it issues to the Policy Server.
     * The Policy Server MUST include the received AMID in all messages it issues down the CMTS in response to the
     * messages it receives from the Application Manager.
     */
    private final short appType;

    /**
     * The application manager tag
     */
    private final short appMgrTag;

    /**
     * Constructor
     * @param appType - the application type
     * @param appMgrTag - the application manager tag
     */
    public AMID(final short appType, final short appMgrTag) {
        super(SNum.AMID, STYPE);
        this.appType = appType;
        this.appMgrTag = appMgrTag;
    }

    @Override
    public short getApplicationType() {
        return appType;
    }

    @Override
    public short getApplicationMgrTag() {
        return appMgrTag;
    }

    @Override
    protected byte[] getBytes() {
        final byte[] appTypeBytes = COPSMsgParser.shortToBytes(appType);
        final byte[] appMgrBytes = COPSMsgParser.shortToBytes(appMgrTag);
        final byte[] data = new byte[appTypeBytes.length + appMgrBytes.length];
        System.arraycopy(appTypeBytes, 0, data, 0, appTypeBytes.length);
        System.arraycopy(appMgrBytes, 0, data, appTypeBytes.length, appMgrBytes.length);
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AMID)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final AMID amid = (AMID) o;
        return appType == amid.appType && appMgrTag == amid.appMgrTag;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) appType;
        result = 31 * result + (int) appMgrTag;
        return result;
    }

    /**
     * Returns an AMID object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static AMID parse(final byte[] data) {
        return new AMID(COPSMsgParser.bytesToShort(data[0], data[1]), COPSMsgParser.bytesToShort(data[2], data[3]));
    }
}
