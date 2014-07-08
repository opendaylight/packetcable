/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Accounting Timer Object
 *
 * @version COPSAcctTimer.java, v 1.00 2003
 *
 */
public class COPSAcctTimer extends COPSTimer {

    public COPSAcctTimer() {
        super ((short) 1);
        _objHdr.setCNum(COPSObjHeader.COPS_ACCT_TIMER);
        _objHdr.setCType((byte) 1);
    }

    ///
    public COPSAcctTimer(short timeVal) {
        super(timeVal);
        _objHdr.setCNum(COPSObjHeader.COPS_ACCT_TIMER);
        _objHdr.setCType((byte) 1);
    }

    ///
    /**
     * Method isAcctTimer
     *
     * @return   a boolean
     *
     */
    public boolean isAcctTimer() {
        return true;
    }

    ///
    protected COPSAcctTimer(byte[] dataPtr) {
        super (dataPtr);
    }

}

