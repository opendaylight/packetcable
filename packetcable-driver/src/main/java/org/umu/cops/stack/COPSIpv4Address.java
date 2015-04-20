/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * COPS IPv4 Address
 */
public class COPSIpv4Address extends COPSIpAddress {

    /**
     * Creates an address for a given host
     * @param hostName - the host name
     * @throws UnknownHostException
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv4Address(final String hostName) throws UnknownHostException {
        super(hostName);
    }

    /**
     * Creates an address for a given IP address contained within a byte array
     * @param addr - the host name
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv4Address(final byte[] addr) {
        super(addr);
        if (addr.length != 4) throw new IllegalArgumentException("The address must be 4 bytes");
    }

    @Override
    protected byte[] deriveIpAddress(final String hostName) throws UnknownHostException {
        final InetAddress[] addrs = Inet4Address.getAllByName(hostName);
        for (final InetAddress addr : addrs) {
            if (addr instanceof Inet4Address) {
                return addr.getAddress();
            }
        }
        throw new UnknownHostException("InetAddress could not be found");
    }

    @Override
    public String getIpName() throws UnknownHostException {
        return Inet4Address.getByAddress(_addr).getHostName();
    }

}

