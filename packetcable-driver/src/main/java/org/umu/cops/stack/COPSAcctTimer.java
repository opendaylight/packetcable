/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;


import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS Accounting Timer Object
 *
 * @version COPSAcctTimer.java, v 1.00 2003
 *
 */
public class COPSAcctTimer extends COPSTimer {

    public COPSAcctTimer() {
        super(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), (short) 1);
    }

    ///
    public COPSAcctTimer(short timeVal) {
        super(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), timeVal);
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

