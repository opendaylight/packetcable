/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Interface
 *
 * @version COPSInterface.java, v 1.00 2003
 *
 */
abstract class COPSInterface extends COPSObjBase {
    /**
     * Method isIpv4Address
     *
     * @return   a boolean
     *
     */
    protected boolean isIpv4Address() {
        return false;
    };

    /**
     * Method isIpv6Address
     *
     * @return   a boolean
     *
     */
    protected boolean isIpv6Address() {
        return false;
    };

    /**
     * Method isInInterface
     *
     * @return   a boolean
     *
     */
    protected boolean isInInterface() {
        return false;
    };

    /**
     * Method isOutInterface
     *
     * @return   a boolean
     *
     */
    protected boolean isOutInterface() {
        return false;
    };

    /**
     * Method isInterface
     *
     * @return   a boolean
     *
     */
    protected boolean isInterface() {
        return true;
    }
}
