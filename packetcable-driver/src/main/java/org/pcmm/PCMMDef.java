/**
 @header@
 */

package org.pcmm;

import org.umu.cops.common.COPS_def;

public class PCMMDef extends COPS_def {

    public static final short C_PCMM = (short) 0x800A;

    /**
     * Get a representative string for an COPS Client Type.
     *
     * @param cType
     *            COPS Client Type
     * @return A representative <tt>String</tt>
     *
     */
    public String strClientType(short cType) {
        switch (cType) {
        case C_PCMM:
            return ("C_PCMM");
        default:
            return super.strClientType(cType);
        }
    }

}
