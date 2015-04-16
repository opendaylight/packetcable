/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;


import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS Keep Alive Timer
 *
 * @version COPSKATimer.java, v 1.00 2003
 *
 */
public class COPSKATimer extends COPSTimer {

    public COPSKATimer() {
        super(new COPSObjHeader(CNum.KA, CType.DEF), (short) 1);
    }

    ///
    public COPSKATimer(short timeVal) {
        super(new COPSObjHeader(CNum.KA, CType.DEF), timeVal);
    }

    /**
     * Method isKATimer
     *
     * @return   a boolean
     *
     */
    public boolean isKATimer() {
        return true;
    }

    protected COPSKATimer(byte[] dataPtr) {
        super (dataPtr);
    }

}

