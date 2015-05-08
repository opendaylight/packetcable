package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.pcmm.rcd.IPCMMClient;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;

/**
 * Tests the construction of the COPSHeader class
 */
public class COPSHeaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullOPCode1() {
        new COPSHeader(null, IPCMMClient.CLIENT_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroVersion() {
        new COPSHeader(0, Flag.UNSOLICITED, OPCode.CAT, IPCMMClient.CLIENT_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlags() {
        new COPSHeader(1, null, OPCode.CAT, IPCMMClient.CLIENT_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullOPCode2() {
        new COPSHeader(1, Flag.UNSOLICITED, null, IPCMMClient.CLIENT_TYPE);
    }

    @Test
    public void validConstructor1() {
        final COPSHeader header = new COPSHeader(OPCode.CAT, IPCMMClient.CLIENT_TYPE);
        Assert.assertEquals(1, header.getPcmmVersion());
        Assert.assertEquals(Flag.UNSOLICITED, header.getFlag());
        Assert.assertEquals(OPCode.CAT, header.getOpCode());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, header.getClientType());
    }

    @Test
    public void validConstructor2() {
        final COPSHeader header = new COPSHeader(2, Flag.SOLICITED, OPCode.DEC, IPCMMClient.CLIENT_TYPE);
        Assert.assertEquals(2, header.getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, header.getFlag());
        Assert.assertEquals(OPCode.DEC, header.getOpCode());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, header.getClientType());
    }

    // writeData() will be tested implicitly via the COPSMsg tests
}
