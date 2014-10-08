/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

/**
 * Exception class for PDP errors
 *
 * @version COPSPdpException.java, v 2.00 2004
 *
 */

public class COPSPdpException extends Exception {

    private int rc;
    final static int GENERAL_ERROR = 0x00000001;

    /**
    * Creates a <tt>COPSPdpException</tt> with the given message.
    * @param msg    Exception message
    */
    public COPSPdpException(String msg) {
        super(msg);
        rc=0;
    }

    /**
     * Creates a <tt>COPSPdpException</tt> with the given message and return code.
     * @param msg       Exception message
     * @param retCode   Return code
     */
    public COPSPdpException(String msg, int retCode) {
        super(msg);
        rc = retCode;
    }

    /**
     * Gets the return code of the exception
     * @return    Exception's return code
     */
    public int returnCode() {
        return rc;
    }

}
