/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.ospep;

import org.umu.cops.stack.COPSException;

/**
 * COPS PEP Exception
 *
 * @version COPSPepException.java, v 2.00 2004
 *
 */
public class COPSPepException extends COPSException {

    public COPSPepException(String s) {
        super(s);
    }

    public COPSPepException(String msg, int retCode) {
        super(msg, retCode);
    }

    public COPSPepException(String msg, Throwable t) {
        super(msg, t);
    }

}
