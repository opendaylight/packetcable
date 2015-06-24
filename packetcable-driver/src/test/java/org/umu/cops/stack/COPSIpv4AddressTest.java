package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests for the COPSIpv4Address class.
 */
public class COPSIpv4AddressTest {

    @Test(expected = UnknownHostException.class)
    public void badHost() throws Exception {
        new COPSIpv4Address("foo");
    }

    @Test
    public void localhost() throws Exception {
        final COPSIpv4Address address = new COPSIpv4Address("localhost");
        Assert.assertEquals(4, address.getDataLength());
        Assert.assertArrayEquals(InetAddress.getByName("localhost").getAddress(), address.getAddressBytes());
        Assert.assertTrue(address.getIpName().equals("localhost") || address.getIpName().equals("127.0.0.1"));
    }

    @Test
    public void addrBytes() throws Exception {
        final byte[] addr = InetAddress.getByName("localhost").getAddress();
        final COPSIpv4Address address = new COPSIpv4Address(addr);
        Assert.assertEquals(4, address.getDataLength());
        Assert.assertArrayEquals(addr, address.getAddressBytes());
        Assert.assertTrue(address.getIpName().equals("localhost") || address.getIpName().equals("127.0.0.1"));
    }

}
