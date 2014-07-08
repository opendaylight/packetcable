/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Provisioning Global Error
 *
 * @version COPSPrGlobalError.java, v 1.00 2003
 *
 */
public class COPSPrGlobalError extends COPSPrError {

    public final static byte G_availMemoryLow = 1;
    public final static byte G_availMemoryExhausted = 2;
    public final static byte G_unknownASN1Tag = 3;
    public final static byte G_maxMsgSizeExceeded = 4;
    public final static byte G_unknownError = 5;
    public final static byte G_maxReqStateOpen = 6;
    public final static byte G_invalidASN1Length = 7;
    public final static byte G_invalidObjPad = 8;
    public final static byte G_unknownPIBData = 9;
    public final static byte G_unknownCOPSPrObj = 10;
    public final static byte G_malformedDec = 11;
    public final static byte G_errmax = 12;


    private final static String GerrTable[] = {
        "Reserved",
        "Available memory low",
        "Available memory exhausted",
        "Unknown ASN.1 tag",
        "Max. message size exceeded",
        "Unknown error",
        "No more Request-states can be created by the PEP",
        "ASN.1 object length was incorrect",
        "Object was not properly padded",
        "Unknown PIB data",
        "Unknown COPS-PR object",
        "Melformed decision"
    };

    ///
    COPSPrGlobalError(short eCode, short eSubCode) {
        super(eCode, eSubCode);
        _sNum = COPSPrObjBase.PR_GPERR;
        _sType = COPSPrObjBase.PR_BER;
    }

    /**
          Parse the data and create a PrGlobalError object
     */
    protected COPSPrGlobalError(byte[] dataPtr) {
        super(dataPtr);
    }


    /**
     * Method isGlobalPrError
     *
     * @return   a boolean
     *
     */
    public boolean isGlobalPrError() {
        return true;
    }

    /**
     * Method strError
     *
     * @return   a String
     *
     */
    public String strError() {
        return GerrTable[_errCode];
    };

};

