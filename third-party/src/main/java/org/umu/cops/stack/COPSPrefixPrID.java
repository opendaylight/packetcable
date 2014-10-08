/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Prefix Provisioning ID
 *
 * @version COPSPrefixPrID.java, v 1.00 2003
 *
 */
public class COPSPrefixPrID extends COPSPrObjBase {

    ///
    public COPSPrefixPrID() {
        _sNum = COPSPrObjBase.PR_PPRID;
        _sType = COPSPrObjBase.PR_XML;
    }

    /**
     * Method isPRIDPrefix
     *
     * @return   a boolean
     *
     */
    public boolean isPRIDPrefix() {
        return true;
    }

    /**
          Parse the data and create a PrefixPrID object
     */
    protected COPSPrefixPrID(byte[] dataPtr) {
        super(dataPtr);
    }
}

