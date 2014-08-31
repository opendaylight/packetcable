/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

/**
 * COPS Exception
 *
 * @version COPSException.java, v 1.00 2003
 *
 */
public class COPSException extends Exception {

    private int rc;
    final static int GENERAL_ERROR = 0x00000001;

    public COPSException(String s) {
        super(s);
        rc=0;
    }

    public COPSException(String msg, int retCode) {
        super(msg);
        rc = retCode;
    }

    /**
     * Method returnCode
     *
     * @return   an int
     *
     */
    public int returnCode() {
        return rc;
    }

}
