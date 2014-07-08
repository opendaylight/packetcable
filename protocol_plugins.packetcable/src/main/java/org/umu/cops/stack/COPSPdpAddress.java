/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;

/**
 * COPS PDP Address
 *
 * @version COPSPdpAddress.java, v 1.00 2003
 *
 */
abstract public class COPSPdpAddress extends COPSObjBase {

    /**
     * Method isPdpAddress
     *
     * @return   a boolean
     *
     */
    public boolean isPdpAddress() {
        return true;
    };

    /**
     * Method isIpv4PdpAddress
     *
     * @return   a boolean
     *
     */
    public boolean isIpv4PdpAddress() {
        return false;
    };

    /**
     * Method isIpv6PdpAddress
     *
     * @return   a boolean
     *
     */
    public boolean isIpv6PdpAddress() {
        return false;
    };

    /**
     * Method isLastPdpAddress
     *
     * @return   a boolean
     *
     */
    public boolean isLastPdpAddress() {
        return false;
    };

    /**
     * Method isPdpredirectAddress
     *
     * @return   a boolean
     *
     */
    public boolean isPdpredirectAddress() {
        return false;
    };

    /**
     * Write an object textual description in the output stream
     *
     * @param    os                  an OutputStream
     *
     * @throws   IOException
     *
     */
    abstract public void dump(OutputStream os) throws IOException;
};

