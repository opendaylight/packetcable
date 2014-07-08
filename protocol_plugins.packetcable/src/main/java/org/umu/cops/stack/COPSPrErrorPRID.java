/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Provisioning Error PRID
 *
 * @version COPSPrErrorPRID.java, v 1.00 2003
 *
 */
public class COPSPrErrorPRID extends COPSPrObjBase {

    public COPSPrErrorPRID() {
        _sNum = COPSPrObjBase.PR_IDERR;
        _sType = COPSPrObjBase.PR_BER;
    }

    /**
          Parse the data and create a PrErrorPRID object
     */
    protected COPSPrErrorPRID(byte[] dataPtr) {
        super(dataPtr);
    }

    /**
     * Method isErrorPRID
     *
     * @return   a boolean
     *
     */
    public boolean isErrorPRID() {
        return true;
    }

}
