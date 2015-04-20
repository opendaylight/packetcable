/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

/**
 * Last PDP Address (RFC 2748)
 *
 * When a PEP sends a Client-Open message for a particular client-type
 * the PEP SHOULD specify the last PDP it has successfully opened
 * (meaning it received a Client-Accept) since the PEP last rebooted.
 * If no PDP was used since the last reboot, the PEP will simply not
 * include this object in the Client-Open message.
 *
 * C-Num = 14,
 *
 * C-Type = 1, IPv4 Address (Same format as PDPRedirAddr)
 *
 * C-Type = 2, IPv6 Address (Same format as PDPRedirAddr)
 */
public class COPSIpv6LastPdpAddr extends COPSIpv6PdpAddress {

    /**
     * Constructor generally used for sending messages
     * @param host - the host name
     * @param tcpPort - the associated port
     * @param reserved - ???
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv6LastPdpAddr(final String host, final int tcpPort, final short reserved) throws UnknownHostException {
        super(new COPSObjHeader(CNum.LAST_PDP_ADDR, CType.STATELESS), host, tcpPort, reserved);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param objHdr - the object header
     * @param addr - the byte array representation of a host
     * @param tcpPort - the associated port
     * @param reserved - ???
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpv6LastPdpAddr(final COPSObjHeader objHdr, final byte[] addr, final int tcpPort, final short reserved) {
        super(objHdr, addr, tcpPort, reserved);
        if (!objHdr.getCNum().equals(CNum.LAST_PDP_ADDR))
            throw new IllegalArgumentException("CNum must be equal to - " + CNum.LAST_PDP_ADDR);
    }

    @Override
    public void dumpBody(OutputStream os) throws IOException {
        os.write(("Ipv6LastPdpAddr" + "\n").getBytes());
        super.dumpBody(os);
    }

}
