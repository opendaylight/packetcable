/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;

/**
 * COPS IPv6 Output Interface
 *
 * @version COPSIpv6OutInterface.java, v 1.00 2003
 *
 */
public class COPSIpv6OutInterface extends COPSIpv6Interface {
    public COPSIpv6OutInterface() {
        _objHdr = new COPSObjHeader();
        _objHdr.setCNum(COPSObjHeader.COPS_ININTF);
    }

    public COPSIpv6OutInterface(byte[] dataPtr) {
        super(dataPtr);
    }

    /**
     * Method className
     *
     * @return   a String
     *
     */
    public String className() {
        return "COPSIpv6OutInterface";
    }

    /**
     * Method isInInterface
     *
     * @return   a boolean
     *
     */
    public boolean isInInterface() {
        return true;
    }

    /**
     * Writes data to given socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
    }

}
