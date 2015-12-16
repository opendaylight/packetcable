/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IGateID;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the IGateID interface
 */
public class GateID extends PCMMBaseObject implements IGateID {

    /**
     * The gate ID value
     */
    final int gateId;

    /**
     * Constructor
     * @param gateId - the ID value
     */
    public GateID(final int gateId) {
        super(SNum.GATE_ID, STYPE);
        this.gateId = gateId;
    }

    @Override
    public int getGateID() {
        return gateId;
    }

    @Override
    protected byte[] getBytes() {
        return COPSMsgParser.intToBytes(gateId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GateID)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final GateID gateID = (GateID) o;
        return gateId == gateID.gateId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + gateId;
        return result;
    }

    /**
     * Returns a GateID object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static GateID parse(final byte[] data) {
        return new GateID(COPSMsgParser.bytesToInt(data[0], data[1], data[2], data[3]));
    }
}
