/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;

/**
 * COPS IPv6 Last PDP Address
 *
 * @version COPSIpv6LastPdpAddr.java, v 1.00 2003
 *
 */
public class COPSIpv6LastPdpAddr extends COPSIpv6PdpAddress {

    public COPSIpv6LastPdpAddr() {
        super();
        _objHdr.setCNum(COPSObjHeader.COPS_LAST_PDP_ADDR);
    }

    public COPSIpv6LastPdpAddr(byte[] dataPtr) {
        super(dataPtr);
    }

    /**
     * Method isLastPdpAddress
     *
     * @return   a boolean
     *
     */
    public boolean isLastPdpAddress() {
        return true;
    }

    /**
     * Write an object textual description in the output stream
     *
     * @param    os                  an OutputStream
     *
     * @throws   IOException
     *
     */
    public void dump(OutputStream os) throws IOException {
        _objHdr.dump(os);
        os.write(new String("Ipv6PdpAddress" + "\n").getBytes());
        os.write(new String("Address: " + _addr.getIpName() + "\n").getBytes());
        os.write(new String("Port: " + _tcpPort + "\n").getBytes());
    }
};
