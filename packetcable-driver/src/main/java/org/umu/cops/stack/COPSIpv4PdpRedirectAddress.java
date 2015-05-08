package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

/**
 * PDP Redirect Address (RFC 2748 pg. 15)
 *
 * A PDP when closing a PEP session for a particular client-type may
 optionally use this object to redirect the PEP to the specified PDP
 server address and TCP port number:
 *
 * C-Num = 13,
 *
 * C-Type = 1, IPv4 Address (Same format as PDPRedirAddr)
 *
 * C-Type = 2, IPv6 Address (Same format as PDPRedirAddr)
 */
public class COPSIpv4PdpRedirectAddress extends COPSIpv4PdpAddress {

    /**
     * Constructor generally used for sending messages
     * @param host - the host name
     * @param port - the associated port
     * @param reserved - ???
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv4PdpRedirectAddress(final String host, final int port, final short reserved) throws UnknownHostException {
        super(new COPSObjHeader(CNum.PDP_REDIR, CType.DEF), host, port, reserved);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param objHdr - the object header
     * @param addr - the byte array representation of a host
     * @param tcpPort - the associated port
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpv4PdpRedirectAddress(final COPSObjHeader objHdr, final byte[] addr, final int tcpPort,
                                  final short reserved) {
        super(objHdr, addr, tcpPort, reserved);
        if (!objHdr.getCNum().equals(CNum.PDP_REDIR))
            throw new IllegalArgumentException("CNum must be equal to - " + CNum.PDP_REDIR);
    }

    @Override
    public void dumpBody(OutputStream os) throws IOException {
        os.write(("Ipv4PdpRedirectAddress" + "\n").getBytes());
        super.dumpBody(os);
    }
}
