/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IGateState;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the IGateID interface
 */
public class GateState extends PCMMBaseObject implements IGateState {

    /**
     * The Gate State value (unsigned 32 bit integer)
     */
    final GateStateType gateState;

    /**
     * The Gate State value (unsigned 32 bit integer)
     */
    final GateStateReasonType gateStateReason;

    /**
     * Constructor
     * @param gateId - the ID value
     */
    public GateState(final GateStateType gateState, final GateStateReasonType gateStateReason) {
        super(SNum.GATE_STATE, STYPE);
        this.gateState = gateState;
        this.gateStateReason = gateStateReason;
    }

    @Override
    public GateStateType getGateState() {
        return gateState;
    }

	@Override
	public GateStateReasonType getGateStateReason() {
		return gateStateReason;
	}

    @Override
    protected byte[] getBytes() {
        return COPSMsgParser.shortToBytes(gateState.getValue());
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
        final GateState gateSTATE = (GateState) o;
        return gateState == gateSTATE.gateState;
    }

    //@Override
    //public int hashCode() {
    //    int result = super.hashCode();
    //    result = 31 * result + gateState.;
    //    return result;
   // }

    /**
     * Returns a GateState object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static GateState parse(final byte[] data) {
        return new GateState(GateStateType.valueOf(COPSMsgParser.bytesToShort(data[0], data[1])),
        		GateStateReasonType.valueOf(COPSMsgParser.bytesToShort(data[2], data[3])));
    }


}