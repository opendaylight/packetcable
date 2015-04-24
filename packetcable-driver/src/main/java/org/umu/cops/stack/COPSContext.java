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
 * COPS Context Object (RFC 2748)
 *
 * Specifies the type of event(s) that triggered the query. Required for
 * request messages. Admission control, resource allocation, and
 * forwarding requests are all amenable to client-types that outsource
 * their decision making facility to the PDP. For applicable client-
 * types a PEP can also make a request to receive named configuration
 * information from the PDP. This named configuration data may be in a
 * form useful for setting system attributes on a PEP, or it may be in
 * the form of policy rules that are to be directly verified by the PEP.
 *
 * Multiple flags can be set for the same request. This is only allowed,
 * however, if the set of client specific information in the combined
 * request is identical to the client specific information that would be
 * specified if individual requests were made for each specified flag.
 *
 * C-num = 2, C-Type = 1
 *
 * R-Type (Request Type Flag)
 *
 * 0x01 = Incoming-Message/Admission Control request
 * 0x02 = Resource-Allocation request
 * 0x04 = Outgoing-Message request
 * 0x08 = Configuration request
 *
 * M-Type (Message Type)
 *
 * Client Specific 16 bit values of protocol message types
 */
public class COPSContext extends COPSObjBase {

    /**
     * A Map containing each RType by the byte value
     */
    public final static Map<Integer, RType> VAL_TO_RTYPE = new ConcurrentHashMap<>();
    static {
        VAL_TO_RTYPE.put(1, RType.IN_ADMIN);
        VAL_TO_RTYPE.put(2, RType.RES_ALLOC);
        VAL_TO_RTYPE.put(4, RType.OUT);
        VAL_TO_RTYPE.put(8, RType.CONFIG);
    }

    /**
     * A Map containing the byte value by RType
     */
    private final static Map<RType, Integer> RTYPE_TO_VAL = new ConcurrentHashMap<>();
    static {
        for (final Map.Entry<Integer, RType> entry : VAL_TO_RTYPE.entrySet()) {
            RTYPE_TO_VAL.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * The request type
     */
    private final RType _rType;

    /**
     * The message type
     * Cannot find a list of types in order to make this an enumeration
     */
    private short _mType;

    /**
     * Constructor generally used for sending messages
     * @param rType - the type of request
     * @param mType - the type of message
     * @throws java.lang.IllegalArgumentException
     */
    public COPSContext(final RType rType, final short mType ) {
        this(new COPSObjHeader(CNum.CONTEXT, CType.DEF), rType, mType);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param rType - the type of request
     * @param mType - the type of message
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSContext(final COPSObjHeader objHdr, final RType rType, final short mType ) {
        super(objHdr);

        if (!objHdr.getCNum().equals(CNum.CONTEXT))
            throw new IllegalArgumentException("CNum must be equal to " + CNum.CONTEXT);
        if (!objHdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("Invalid CType value. Must be " + CType.DEF);
        if (rType == null) throw new IllegalArgumentException("Must have a valid RType");

        _rType = rType;
        _mType = mType;
    }

    @Override
    public void writeBody(final Socket socket) throws IOException {
        byte[] buf = new byte[4];

        final int rType = RTYPE_TO_VAL.get(_rType);
        buf[0] = (byte)((byte)rType >> 8);
        buf[1] = (byte)rType;

        buf[2] = (byte)(_mType >> 8);
        buf[3] = (byte)_mType;

        COPSUtil.writeData(socket, buf, 4);
    }

    @Override
    public int getDataLength() {
        return 4;
    }

    /**
     * Returns the detail description of the request type
     * @return   a String
     */
    public String getDescription() {
        switch (_rType) {
            case IN_ADMIN: return "Incoming Message/Admission Control";
            case RES_ALLOC: return "Resource allocation";
            case OUT: return "Outgoing message";
            case CONFIG: return "Configuration";
            default: return "";
        }
    }

    @Override
    public void dumpBody(final OutputStream os) throws IOException {
        os.write(("context: " + getDescription() + "," + _mType + "\n").getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSContext)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSContext that = (COPSContext) o;

        return _mType == that._mType && _rType == that._rType;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _rType.hashCode();
        result = 31 * result + (int) _mType;
        return result;
    }

    /**
     * Parses bytes to return a COPSContext object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSContext parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        short rType = 0;
        rType |= ((short) dataPtr[4]) << 8;
        rType |= ((short) dataPtr[5]) & 0xFF;

        short mType = 0;
        mType |= ((short) dataPtr[6]) << 8;
        mType |= ((short) dataPtr[7]) & 0xFF;

        return new COPSContext(objHdrData.header, VAL_TO_RTYPE.get((int)rType), mType);
    }

    /**
     * The request type
     */
    public enum RType {
        IN_ADMIN, RES_ALLOC, OUT, CONFIG,
    }

}


