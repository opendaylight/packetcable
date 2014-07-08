/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Provisioning ID
 *
 * @version COPSPrID.java, v 1.00 2003
 *
 */
public class COPSPrID extends COPSPrObjBase {

    public COPSPrID() {
        _sNum = COPSPrObjBase.PR_PRID;
        _sType = COPSPrObjBase.PR_XML;
    }

    ///Parse the data and create a PrID object
    protected COPSPrID(byte[] dataPtr) {
        super(dataPtr);
    }

    /**
     * Method isPRID
     *
     * @return   a boolean
     *
     */
    public boolean isPRID() {
        return true;
    }
}

