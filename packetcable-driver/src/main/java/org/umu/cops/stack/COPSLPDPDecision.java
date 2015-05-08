/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;


import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS LPDP Decision Object (local decision) (RFC 2748)
 *
 * Decision made by the PEP's local policy decision point (LPDP). May
 * appear in requests. These objects correspond to and are formatted the
 * same as the client specific decision objects defined above.
 *
 * C-Num = 7
 *
 * C-Type = (same C-Type as for Decision objects)
 */
public class COPSLPDPDecision extends COPSDecision {

    /**
     * Constructor generally used for sending messages with a specific, CType, Command and DecisionFlag
     * @param cType - the CType
     * @param cmdCode - the command
     * @param flags - the flags
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    public COPSLPDPDecision(final CType cType, final Command cmdCode, final DecisionFlag flags, final COPSData data) {
        this(new COPSObjHeader(CNum.LPDP_DEC, cType), cmdCode, flags, data);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param cmdCode - the command
     * @param flags - the flags
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSLPDPDecision(final COPSObjHeader hdr, final Command cmdCode, final DecisionFlag flags,
                               final COPSData data) {
        super(hdr, cmdCode, flags, data);

        if (!hdr.getCNum().equals(CNum.LPDP_DEC))
            throw new IllegalArgumentException("Invalid CNum value. Must be " + CNum.LPDP_DEC);
    }

    /**
     * Parses bytes to return a COPSLPDPDecision object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSLPDPDecision parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        final COPSObjHeader tempHdr = new COPSObjHeader(CNum.DEC, objHdrData.header.getCType());
        final COPSObjHeaderData tempObjHdrData = new COPSObjHeaderData(tempHdr, objHdrData.msgByteCount);
        final COPSDecision decision = COPSDecision.parse(tempObjHdrData, dataPtr);
        return new COPSLPDPDecision(objHdrData.header, decision.getCommand(), decision.getFlag(), decision.getData());
    }

}
