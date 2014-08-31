/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

/**
 * COPS PEP Exception
 *
 * @version COPSPepException.java, v 2.00 2004
 *
 */
public class COPSPepException extends Exception {

    private int rc;
    final static int GENERAL_ERROR = 0x00000001;

    public COPSPepException(String s) {
        super(s);
        rc=0;
    }

    public COPSPepException(String msg, int retCode) {
        super(msg);
        rc = retCode;
    }

    /**
     * Return error code
     *
     * @return    error code
     *
     */
    public int returnCode() {
        return rc;
    }

}
