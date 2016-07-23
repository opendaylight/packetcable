/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.pcmm.gates.impl;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Tests the data holder class GateId to ensure both construction and byte parsing result in correct object creation.
 */
public class GateIDTest {

    @Test
    public void construction() {
        final GateID gateID = new GateID(9);

        // Check the object's bytes
        final byte[] dataBytes = gateID.getBytes();
        Assert.assertEquals(9, gateID.getGateID());
        Assert.assertEquals(4, dataBytes.length);
        Assert.assertEquals(9, COPSMsgParser.bytesToInt(dataBytes[0], dataBytes[1], dataBytes[2], dataBytes[3]));

        // Check the byte parsing
        final GateID parsed = GateID.parse(dataBytes);
        Assert.assertEquals(gateID, parsed);
    }

    @Test
    public void byteParsing() {
        final GateID gateID = new GateID(10);
        final GateID parsed = GateID.parse(gateID.getBytes());
        Assert.assertEquals(gateID, parsed);
    }

}
