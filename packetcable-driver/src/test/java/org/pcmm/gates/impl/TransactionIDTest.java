/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import org.junit.Assert;
import org.junit.Test;
import org.pcmm.gates.ITransactionID.GateCommandType;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Tests the data holder class TransactionID to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class TransactionIDTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullCommandType() {
        new TransactionID((short)9, null);
    }

    @Test
    public void construction() {
        final TransactionID transID = new TransactionID((short)9, GateCommandType.GATE_CMD_ERR);
        final byte[] dataBytes = transID.getBytes();
        Assert.assertEquals(4, dataBytes.length);
        Assert.assertEquals(9, COPSMsgParser.bytesToShort(dataBytes[0], dataBytes[1]));
        Assert.assertEquals(GateCommandType.GATE_CMD_ERR,
                GateCommandType.valueOf(COPSMsgParser.bytesToShort(dataBytes[2], dataBytes[3])));
    }

    @Test
    public void byteParsing() {
        final TransactionID transID = new TransactionID((short)11, GateCommandType.GATE_DELETE_ACK);
        final TransactionID parsed = TransactionID.parse(transID.getBytes());
        Assert.assertEquals(transID, parsed);
    }

}
