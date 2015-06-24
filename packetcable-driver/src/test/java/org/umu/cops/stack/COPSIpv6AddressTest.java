package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests for the COPSIpv6Address class.
 */
public class COPSIpv6AddressTest {

    @Test(expected = UnknownHostException.class)
    public void badHost() throws Exception {
        new COPSIpv6Address("foo");
    }

    @Test
    public void localhost() throws Exception {
        final COPSIpv6Address address = new COPSIpv6Address("localhost");
        Assert.assertEquals(16, address.getDataLength());
        Assert.assertArrayEquals(getLocalhostIpv6Address(), address.getAddressBytes());
        Assert.assertNotNull(address.getIpName());
    }

    @Test
    public void addrBytes() throws Exception {
        final byte[] addr = getLocalhostIpv6Address();
        final COPSIpv6Address address = new COPSIpv6Address(addr);
        Assert.assertEquals(16, address.getDataLength());
        Assert.assertArrayEquals(addr, address.getAddressBytes());
        Assert.assertNotNull(address.getIpName());
    }

    private byte[] getLocalhostIpv6Address() throws UnknownHostException {
        final InetAddress[] addrs = Inet4Address.getAllByName("localhost");
        for (final InetAddress addr : addrs) {
            if (addr instanceof Inet6Address) {
                return addr.getAddress();
            }
        }
        throw new UnknownHostException("InetAddress could not be found");
    }
}
