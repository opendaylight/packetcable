/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * COPS IPv6 Address
 */
public class COPSIpv6Address extends COPSIpAddress {

    /**
     * Creates an address for a given host
     * @param hostName - the host name
     * @throws UnknownHostException
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv6Address(final String hostName) throws UnknownHostException {
        super(hostName);
    }

    /**
     * Creates an address for a given IP address contained within a byte array
     * @param addr - the host name
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv6Address(final byte[] addr) {
        super(addr);
        if (addr.length != 16) throw new IllegalArgumentException("The address must be 16 bytes");
    }


    @Override
    protected byte[] deriveIpAddress(final String hostName) throws UnknownHostException {
        final InetAddress[] addrs = Inet4Address.getAllByName(hostName);
        for (final InetAddress addr : addrs) {
            if (addr instanceof Inet6Address) {
                return addr.getAddress();
            }
        }
        throw new UnknownHostException("InetAddress could not be found");
    }

    @Override
    public String getIpName() throws UnknownHostException {
        return Inet6Address.getByAddress(_addr).getHostName();
    }
}

