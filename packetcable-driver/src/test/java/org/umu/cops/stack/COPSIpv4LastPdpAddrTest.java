package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests for the first constructor of the COPSIpv4LastPdpAddr class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSIpv4LastPdpAddrTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullHost() throws Exception {
        new COPSIpv4LastPdpAddr(null, 1234, (short)0);
    }

    @Test(expected = UnknownHostException.class)
    public void invalidHost() throws Exception {
        new COPSIpv4LastPdpAddr("foo", 1234, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPort() throws Exception {
        new COPSIpv4LastPdpAddr("localhost", 0, (short)0);
    }

    @Test
    public void validConstructor1() throws Exception {
        final COPSIpv4LastPdpAddr lastAddr = new COPSIpv4LastPdpAddr("localhost", 1234, (short)0);
        Assert.assertEquals(8, lastAddr.getDataLength());
        Assert.assertEquals(new COPSObjHeader(CNum.LAST_PDP_ADDR, CType.DEF), lastAddr.getHeader());
        Assert.assertEquals(1234, lastAddr.getTcpPort());
        Assert.assertEquals(0, lastAddr.getReserved());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        lastAddr.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Last PDP addr**", lines[0]);
        Assert.assertEquals("C-num: LAST_PDP_ADDR", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Ipv4LastPdpAddress", lines[3]);
        Assert.assertEquals("Address: localhost", lines[4]);
        Assert.assertEquals("Port: 1234", lines[5]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullObjHeader() throws Exception {
        final byte[] addr = InetAddress.getByName("localhost").getAddress();
        new COPSIpv4LastPdpAddr(null, addr, 1234, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() throws Exception {
        final byte[] addr = InetAddress.getByName("localhost").getAddress();
        new COPSIpv4LastPdpAddr(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), addr, 1234, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() throws Exception {
        final byte[] addr = InetAddress.getByName("localhost").getAddress();
        new COPSIpv4LastPdpAddr(new COPSObjHeader(CNum.LAST_PDP_ADDR, CType.STATELESS), addr, 1234, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAddr() throws Exception {
        new COPSIpv4LastPdpAddr(new COPSObjHeader(CNum.LAST_PDP_ADDR, CType.DEF), null, 1234, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroPort() throws Exception {
        final byte[] addr = InetAddress.getByName("localhost").getAddress();
        new COPSIpv4LastPdpAddr(new COPSObjHeader(CNum.LAST_PDP_ADDR, CType.DEF), addr, 0, (short)0);
    }

    @Test
    public void validConstructor2() throws Exception {
        final byte[] addr = InetAddress.getByName("localhost").getAddress();
        final COPSIpv4LastPdpAddr lastAddr = new COPSIpv4LastPdpAddr(new COPSObjHeader(CNum.LAST_PDP_ADDR, CType.DEF),
                addr, 1234, (short)0);
        Assert.assertEquals(8, lastAddr.getDataLength());
        Assert.assertEquals(new COPSObjHeader(CNum.LAST_PDP_ADDR, CType.DEF), lastAddr.getHeader());
        Assert.assertEquals(1234, lastAddr.getTcpPort());
        Assert.assertEquals(0, lastAddr.getReserved());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        lastAddr.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Last PDP addr**", lines[0]);
        Assert.assertEquals("C-num: LAST_PDP_ADDR", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Ipv4LastPdpAddress", lines[3]);
        Assert.assertEquals("Address: localhost", lines[4]);
        Assert.assertEquals("Port: 1234", lines[5]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}
