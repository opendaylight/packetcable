/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;

/**
 * COPS IPv4 Last PDP Address
 *
 * @version COPSIpv4LastPdpAddr.java, v 1.00 2003
 *
 */
public class COPSIpv4LastPdpAddr extends COPSIpv4PdpAddress {

    public COPSIpv4LastPdpAddr() {
        super();
        _objHdr.setCNum(COPSObjHeader.COPS_LAST_PDP_ADDR);
    }

    public COPSIpv4LastPdpAddr(byte[] dataPtr) {
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
    };

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
        os.write(new String("Ipv4PdpAddress" + "\n").getBytes());
        os.write(new String("Address: " + _addr.getIpName() + "\n").getBytes());
        os.write(new String("Port: " + _tcpPort + "\n").getBytes());
    }
}
