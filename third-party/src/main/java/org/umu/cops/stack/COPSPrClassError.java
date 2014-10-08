/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Provisioning Class Error
 *
 * @version COPSPrClassError.java, v 1.00 2003
 *
 */
public class COPSPrClassError extends COPSPrError {

    public final static byte C_spaceExhausted = 1;
    public final static byte C_instanceInvalid = 2;
    public final static byte C_attrValueInvalid = 3;
    public final static byte C_attrValueSupLimited = 4;
    public final static byte C_attrEnumSupLimited = 5;
    public final static byte C_attrMaxLengthExceeded = 6;
    public final static byte C_attrRefUnknown = 7;
    public final static byte C_notifyOnly = 8;
    public final static byte C_unknownPrc = 9;
    public final static byte C_tooFewAttrs = 10;
    public final static byte C_invalidAttrType = 11;
    public final static byte C_deletedInRef = 12;
    public final static byte C_specificError = 13;
    public final static byte C_errmax = 14;

    private final static String CerrTable[] = {
        "Reserved",
        "No more instances may currently be installed in the given class",
        "Invalid class instance",
        "Invalid attribute value",
        "The value for attribute not currently supported by the device",
        "The enumeration for attribute not currently supported by the device",
        "Attribute length exceeds device limitations",
        "Unknown attribute reference",
        "Only supported for use by request or report",
        "Class not supported by PEP",
        "Too few attributes",
        "Invalid attribute type",
        "Reference to deleted instance",
        "PRC specific error, check subcode for more details"
    };

    public COPSPrClassError(short eCode, short eSubCode) {
        super (eCode, eSubCode);
        _sNum = COPSPrObjBase.PR_CPERR;
        _sType = COPSPrObjBase.PR_BER;
    }

    /**
          Parse the data and create a PrClassError object
     */
    protected COPSPrClassError(byte[] dataPtr) {
        super(dataPtr);
    }

    /**
     * Method isPRCClassError
     *
     * @return   a boolean
     *
     */
    public boolean isPRCClassError() {
        return true;
    }

    /**
     * Method strError
     *
     * @return   a String
     *
     */
    public String strError() {
        return CerrTable[_errCode];
    };
}

