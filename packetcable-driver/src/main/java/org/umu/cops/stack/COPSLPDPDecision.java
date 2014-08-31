/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS LPDP Decision Object
 *
 * @version COPSLPDPDecision.java, v 1.00 2003
 *
 */
public class COPSLPDPDecision extends COPSDecision {

    /**
      Constructor to create a Local Decision object.
     */
    public COPSLPDPDecision(byte cType) {
        super (cType);
        _objHdr.setCNum(COPSObjHeader.COPS_LPDP_DEC);
    }

    public COPSLPDPDecision() {
        super ();
        _objHdr.setCNum(COPSObjHeader.COPS_LPDP_DEC);
    }

    /**
     * Method isLocalDecision
     *
     * @return   a boolean
     *
     */
    public boolean isLocalDecision() {
        return true;
    }

    protected COPSLPDPDecision(byte[] data) {
        super (data);
    }

}
