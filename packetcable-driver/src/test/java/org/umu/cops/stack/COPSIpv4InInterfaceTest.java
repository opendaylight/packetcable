package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSIpv4InInterface class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSIpv4InInterfaceTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullAddress() {
        new COPSIpv4InInterface(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeader() throws Exception {
        new COPSIpv4InInterface(null, new COPSIpv4Address("localhost"), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAddressWithHeader() {
        new COPSIpv4InInterface(new COPSObjHeader(CNum.ININTF, CType.DEF), null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSIpv4InInterface(new COPSObjHeader(CNum.HANDLE, CType.DEF), null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSIpv4InInterface(new COPSObjHeader(CNum.ININTF, CType.STATELESS), null, 0);
    }

    @Test
    public void valid() throws Exception {
        final COPSIpv4Address address = new COPSIpv4Address("localhost");
        final COPSIpv4InInterface intf = new COPSIpv4InInterface(address, 5);
        Assert.assertEquals(new COPSObjHeader(CNum.ININTF, CType.DEF), intf.getHeader());
        Assert.assertEquals(8, intf.getDataLength());
        Assert.assertEquals(address, intf._addr);
        Assert.assertEquals(5, intf._ifindex);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        intf.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**In-Interface**", lines[0]);
        Assert.assertEquals("C-num: ININTF", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertTrue(lines[3].equals("Address: localhost") || lines[3].equals("Address: 127.0.0.1"));
        Assert.assertEquals("ifindex: 5", lines[4]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}
