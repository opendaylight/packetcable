/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Tests the data holder class AMID to ensure both construction and byte parsing result in correct object creation.
 */
public class AMIDTest {

    @Test
    public void construction() {
        final AMID amid = new AMID((short)5, (short)6);
        final byte[] dataBytes = amid.getBytes();
        Assert.assertEquals((short)5, amid.getApplicationType());
        Assert.assertEquals((short)6, amid.getApplicationMgrTag());
        Assert.assertEquals(4, dataBytes.length);
        Assert.assertEquals(5, COPSMsgParser.bytesToShort(dataBytes[0], dataBytes[1]));
        Assert.assertEquals(6, COPSMsgParser.bytesToShort(dataBytes[2], dataBytes[3]));
    }

    @Test
    public void byteParsing() {
        final AMID amid = new AMID((short)7, (short)8);
        final AMID parsed = AMID.parse(amid.getBytes());
        Assert.assertEquals(amid, parsed);
    }

}
