/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.umu.cops.stack.COPSException;

/**
 * Exception class for PDP errors
 *
 * @version COPSPdpException.java, v 2.00 2004
 *
 */

public class COPSPdpException extends COPSException {

    /**
    * Creates a <tt>COPSPdpException</tt> with the given message.
    * @param msg    Exception message
    */
    public COPSPdpException(String msg) {
        super(msg);
    }

    /**
     * Creates a <tt>COPSPdpException</tt> with the given message and return code.
     * @param msg       Exception message
     * @param retCode   Return code
     */
    public COPSPdpException(String msg, int retCode) {
        super(msg, retCode);
    }

    /**
     * Creates a <tt>COPSPdpException</tt> with the given message and throwable.
     * @param msg       Exception message
     * @param t         the Throwable
     */
    public COPSPdpException(String msg, Throwable t) {
        super(msg, t);
    }

}
