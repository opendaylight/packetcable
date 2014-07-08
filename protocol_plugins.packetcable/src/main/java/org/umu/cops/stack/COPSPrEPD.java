/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Provisioning EPD
 *
 * @version COPSPrEPD.java, v 1.00 2003
 *
 */
public class COPSPrEPD extends COPSPrObjBase {

    public COPSPrEPD() {
        _sNum = COPSPrObjBase.PR_EPD;
        _sType = COPSPrObjBase.PR_XML;
    }

    /**
     * Method isEncodedInstanceData
     *
     * @return   a boolean
     *
     */
    public boolean isEncodedInstanceData() {
        return true;
    }

    /**
          Parse the data and create a PrEPD object
     */
    protected COPSPrEPD(byte[] dataPtr) {
        super(dataPtr);
    }


};

