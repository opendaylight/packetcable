/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Keep Alive Timer
 *
 * @version COPSKATimer.java, v 1.00 2003
 *
 */
public class COPSKATimer extends COPSTimer {

    public COPSKATimer() {
        super ((short) 1);
        _objHdr.setCNum(COPSObjHeader.COPS_KA);
        _objHdr.setCType((byte) 1);
    }

    ///
    public COPSKATimer(short timeVal) {
        super(timeVal);
        _objHdr.setCNum(COPSObjHeader.COPS_KA);
        _objHdr.setCType((byte) 1);
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

