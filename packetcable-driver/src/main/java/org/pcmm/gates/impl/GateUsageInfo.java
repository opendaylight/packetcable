/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IGateUsageInfo;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the IGateSpec interface
 */
public class GateUsageInfo extends PCMMBaseObject implements IGateUsageInfo {

	final long gateUsageInfo;

	private GateUsageInfo(final long gateUsageInfo) {

		super(SNum.GATE_USAGE_INFO, STYPE);
		this.gateUsageInfo = gateUsageInfo;
	}

    @Override
    public long getGateUsageInfo() {
        return gateUsageInfo;
    }



    @Override
    protected byte[] getBytes() {
    	byte[] first = COPSMsgParser.intToBytes((int)gateUsageInfo>>32);
    	byte[] second = COPSMsgParser.intToBytes((int)gateUsageInfo);
    	return new byte[first.length+second.length];
    }

    public static GateUsageInfo parse(final byte[] data) {
        return new GateUsageInfo(
        		(long)(COPSMsgParser.bytesToInt(data[0], data[1],data[2], data[3]))<<32
        		| (COPSMsgParser.bytesToInt(data[4], data[5],data[6], data[7]))& 0xFFFFFFFFL);
    }
}
