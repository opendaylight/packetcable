/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IGateTimeInfo;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the IGateSpec interface
 */
public class GateTimeInfo extends PCMMBaseObject implements IGateTimeInfo {

	final int gateTimeInfo;

	private GateTimeInfo(final int gateTimeInfo) {

		super(SNum.GATE_TIME_INFO, STYPE);
		this.gateTimeInfo = gateTimeInfo;
	}

    @Override
    public int getGateTimeInfo() {
        return gateTimeInfo;
    }


    @Override
    protected byte[] getBytes() {
        return COPSMsgParser.intToBytes(gateTimeInfo);
    }

    public static GateTimeInfo parse(final byte[] data) {
        return new GateTimeInfo((COPSMsgParser.bytesToInt(data[0], data[1],data[2], data[3])));
    }
}
