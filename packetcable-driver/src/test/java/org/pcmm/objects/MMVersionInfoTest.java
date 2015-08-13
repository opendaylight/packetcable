/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.objects;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Tests the data holder class MMVersionInfo to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class MMVersionInfoTest {

    @Test
    public void construction() {
        final MMVersionInfo verInfo = new MMVersionInfo((short)5, (short)6);
        final byte[] dataBytes = verInfo.getBytes();
        Assert.assertEquals(4, dataBytes.length);
        Assert.assertEquals(5, COPSMsgParser.bytesToShort(dataBytes[0], dataBytes[1]));
        Assert.assertEquals(6, COPSMsgParser.bytesToShort(dataBytes[2], dataBytes[3]));
    }

    @Test
    public void byteParsing() {
        final MMVersionInfo verInfo = new MMVersionInfo((short)7, (short)8);
        final MMVersionInfo parsed = MMVersionInfo.parse(verInfo.getBytes());
        Assert.assertEquals(verInfo, parsed);
    }

}
