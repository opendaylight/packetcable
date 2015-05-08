/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COPS Reason Object (RFC 2748 page. 12)
 *
 *   This object specifies the reason why the request state was deleted.
 *   It appears in the delete request (DRQ) message. The Reason Sub-code
 *   field is reserved for more detailed client-specific reason codes
 *   defined in the corresponding documents.
 *
 *           C-Num = 5, C-Type = 1
 *
 *               0             1              2             3
 *       +--------------+--------------+--------------+--------------+
 *       |         Reason-Code         |       Reason Sub-code       |
 *       +--------------+--------------+--------------+--------------+
 *
 *           Reason Code:
 *               1 = Unspecified
 *               2 = Management
 *               3 = Preempted (Another request state takes precedence)
 *               4 = Tear (Used to communicate a signaled state removal)
 *               5 = Timeout (Local state has timed-out)
 *               6 = Route Change (Change invalidates request state)
 *               7 = Insufficient Resources (No local resource available)
 *               8 = PDP's Directive (PDP decision caused the delete)
 *                9 = Unsupported decision (PDP decision not supported)
 *               10= Synchronize Handle Unknown
 *               11= Transient Handle (stateless event)
 *               12= Malformed Decision (could not recover)
 *               13= Unknown COPS Object from PDP:
 *                   Sub-code (octet 2) contains unknown object's C-Num
 *                   and (octet 3) contains unknown object's C-Type.
 *
 * @version COPSReason.java, v 1.00 2003
 *
 */
public class COPSReason extends COPSObjBase {

    private final static Map<Integer, ReasonCode> VAL_TO_REASON = new ConcurrentHashMap<>();
    static {
        VAL_TO_REASON.put(ReasonCode.NA.ordinal(), ReasonCode.NA);
        VAL_TO_REASON.put(ReasonCode.UNSPECIFIED.ordinal(), ReasonCode.UNSPECIFIED);
        VAL_TO_REASON.put(ReasonCode.MANAGEMENT.ordinal(), ReasonCode.MANAGEMENT);
        VAL_TO_REASON.put(ReasonCode.PREEMPTED.ordinal(), ReasonCode.PREEMPTED);
        VAL_TO_REASON.put(ReasonCode.TEAR.ordinal(), ReasonCode.TEAR);
        VAL_TO_REASON.put(ReasonCode.TIMEOUT.ordinal(), ReasonCode.TIMEOUT);
        VAL_TO_REASON.put(ReasonCode.ROUTE_CHANGE.ordinal(), ReasonCode.ROUTE_CHANGE);
        VAL_TO_REASON.put(ReasonCode.INSUFF_RESOURCES.ordinal(), ReasonCode.INSUFF_RESOURCES);
        VAL_TO_REASON.put(ReasonCode.PDP_DIRECTIVE.ordinal(), ReasonCode.PDP_DIRECTIVE);
        VAL_TO_REASON.put(ReasonCode.UNSUPPORT_DEC.ordinal(), ReasonCode.UNSUPPORT_DEC);
        VAL_TO_REASON.put(ReasonCode.SYNC_HANDLE.ordinal(), ReasonCode.SYNC_HANDLE);
        VAL_TO_REASON.put(ReasonCode.TRANS_HANDLE.ordinal(), ReasonCode.TRANS_HANDLE);
        VAL_TO_REASON.put(ReasonCode.MALFORMED_DEC.ordinal(), ReasonCode.MALFORMED_DEC);
        VAL_TO_REASON.put(ReasonCode.UNKNOWN_COPS_OBJ.ordinal(), ReasonCode.UNKNOWN_COPS_OBJ);
    }

    private final static Map<ReasonCode, String> REASON_TO_STRING = new ConcurrentHashMap<>();
    static {
        REASON_TO_STRING.put(ReasonCode.NA, "Unknown.");
        REASON_TO_STRING.put(ReasonCode.UNSPECIFIED, "Unspecified.");
        REASON_TO_STRING.put(ReasonCode.MANAGEMENT, "Management.");
        REASON_TO_STRING.put(ReasonCode.PREEMPTED, "Preempted (Another request state takes precedence).");
        REASON_TO_STRING.put(ReasonCode.TEAR, "Tear (Used to communicate a signaled state removal).");
        REASON_TO_STRING.put(ReasonCode.TIMEOUT, "Timeout ( Local state has timed-out).");
        REASON_TO_STRING.put(ReasonCode.ROUTE_CHANGE, "Route change (Change invalidates request state).");
        REASON_TO_STRING.put(ReasonCode.INSUFF_RESOURCES, "Insufficient Resources.");
        REASON_TO_STRING.put(ReasonCode.PDP_DIRECTIVE, "PDP's Directive.");
        REASON_TO_STRING.put(ReasonCode.UNSUPPORT_DEC, "Unsupported decision.");
        REASON_TO_STRING.put(ReasonCode.SYNC_HANDLE, "Synchronize handle unknown.");
        REASON_TO_STRING.put(ReasonCode.TRANS_HANDLE, "Transient handle.");
        REASON_TO_STRING.put(ReasonCode.MALFORMED_DEC, "Malformed decision.");
        REASON_TO_STRING.put(ReasonCode.UNKNOWN_COPS_OBJ, "Unknown COPS object from PDP.");
    }

    /**
     * The reason
     */
    private final ReasonCode _reasonCode;

    /**
     * Reserved for more detailed client-specific reasons
     */
    private final ReasonCode _reasonSubCode;

    /**
     * Constructor generally used for sending messages
     * @param reasonCode - the reason code
     * @param subCode - more detailed reasons
     * @throws java.lang.IllegalArgumentException
     */
    public COPSReason(final ReasonCode reasonCode, final ReasonCode subCode) {
        this(new COPSObjHeader(CNum.REASON_CODE, CType.DEF), reasonCode, subCode);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param reasonCode - the reason code
     * @param subCode - the type of message
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSReason(final COPSObjHeader hdr, final ReasonCode reasonCode, final ReasonCode subCode) {
        super(hdr);
        if (!hdr.getCNum().equals(CNum.REASON_CODE))
            throw new IllegalArgumentException("Must have a CNum value of " + CNum.REASON_CODE);
        if (!hdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("Must have a CType value of " + CType.DEF);
        if (reasonCode == null || subCode == null) throw new IllegalArgumentException("Error codes must not be null");
        if (reasonCode.equals(ReasonCode.NA))
            throw new IllegalArgumentException("Error code must not be of type " + ReasonCode.NA);

        _reasonCode = reasonCode;
        _reasonSubCode = subCode;
    }

    public ReasonCode getReasonCode() { return _reasonCode; }
    public ReasonCode getReasonSubCode() { return _reasonSubCode; }

    @Override
    protected int getDataLength() {
        return 4;
    }

    /**
     * Get Reason description
     * @return   a String
     */
    public String getDescription() {
        return (REASON_TO_STRING.get(_reasonCode) + ":");
    }

    @Override
    protected void writeBody(final Socket id) throws IOException {
        final byte[] buf = new byte[4];
        buf[0] = (byte) (_reasonCode.ordinal() >> 8);
        buf[1] = (byte) _reasonCode.ordinal();
        buf[2] = (byte) (_reasonSubCode.ordinal() >> 8);
        buf[3] = (byte) _reasonSubCode.ordinal();
        COPSUtil.writeData(id, buf, 4);
    }

    @Override
    public void dumpBody(final OutputStream os) throws IOException {
        os.write(("Reason Code: " + _reasonCode + "\n").getBytes());
        os.write(("Reason Sub Code: " + _reasonSubCode + "\n").getBytes());
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSReason parse(final COPSObjHeaderData objHdrData, byte[] dataPtr) {
        short reasonCode = 0;
        reasonCode |= ((short) dataPtr[4]) << 8;
        reasonCode |= ((short) dataPtr[5]) & 0xFF;

        short reasonSubCode = 0;
        reasonSubCode |= ((short) dataPtr[6]) << 8;
        reasonSubCode |= ((short) dataPtr[7]) & 0xFF;

        return new COPSReason(objHdrData.header, VAL_TO_REASON.get((int)reasonCode),
                VAL_TO_REASON.get((int)reasonSubCode));
    }

    /**
     * All of the supported reason codes
     */
    public enum ReasonCode {
        NA, UNSPECIFIED, MANAGEMENT, PREEMPTED, TEAR, TIMEOUT, ROUTE_CHANGE, INSUFF_RESOURCES, PDP_DIRECTIVE,
        UNSUPPORT_DEC, SYNC_HANDLE, TRANS_HANDLE, MALFORMED_DEC, UNKNOWN_COPS_OBJ
    }

}


