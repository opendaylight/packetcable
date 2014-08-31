/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */
package org.umu.cops.common;

/**
 * Class containing keywords supported by the client types.
 *
 * @version COPS_def.java, v 2.00 2004
 *
 */
public class COPS_def {

    /** COPS Client Type for RSVP
     */
    public final static short C_RSVP = 1;

    /** COPS Client Type for SIP
     */
    public final static short C_SIP = 100;

    /** COPS Client Type for IPSec
     */
    public final static short C_IPSEC = (short) 0x8001;

    /** Maximum COPS Client Type
     */
    public final static short C_MAX = (short) 0xFFFF;

    /** Get a representative string for an COPS Client Type.
        *
        * @param     cType    COPS Client Type
     * @return A representative <tt>String</tt>
     *
       */
    public String strClientType(short cType) {
        switch (cType) {
        case C_RSVP:
            return ("C_RSVP");
        case C_SIP:
            return ("C_SIP");
        case C_IPSEC:
            return ("C_IPSEC");
        default:
            return "Unknown";
        }
    }
}
