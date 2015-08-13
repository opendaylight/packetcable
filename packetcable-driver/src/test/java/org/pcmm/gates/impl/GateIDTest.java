/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
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
