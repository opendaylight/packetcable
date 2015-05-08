/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COPS Client Specific Information Object (RFC 2748)
 *
 * The various types of this object are required for requests, and used
 * in reports and opens when required. It contains client-type specific
 * information.
 *
 * C-Num = 9,
 *
 * C-Type = 1, Signaled ClientSI.
 *
 * Variable-length field. All objects/attributes specific to a client's
 * signaling protocol or internal state are encapsulated within one or
 * more signaled Client Specific Information Objects. The format of the
 * data encapsulated in the ClientSI object is determined by the
 * client-type.
 *
 * C-Type = 2, Named ClientSI.
 *
 * Variable-length field. Contains named configuration information
 * useful for relaying specific information about the PEP, a request, or
 * configured state to the PDP server.
 */
public class COPSClientSI extends COPSObjBase {

    private final static Map<Integer, CSIType> VAL_TO_CSI = new ConcurrentHashMap<>();
    static {
        VAL_TO_CSI.put(CSIType.NA.ordinal(), CSIType.NA);
        VAL_TO_CSI.put(CSIType.SIGNALED.ordinal(), CSIType.SIGNALED);
        VAL_TO_CSI.put(CSIType.NAMED.ordinal(), CSIType.NAMED);
    }

    /**
     * This value is not being used here but stored only for clarity as it is being mapped directly to the
     * ordinal value of the CType
     */
    private final CSIType _csiType;

    /**
     * The payload data
     */
    private final COPSData _data;

    /**
     * Bytes to add to outbound payload to ensure the length is divisible by 4 bytes
     */
    private final COPSData _padding;

    /**
     * Constructor generally used for sending messages
     * @param csitype - the CSIType
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    public COPSClientSI(final CSIType csitype, final COPSData data) {
        /* The CSIType does not map directly to the CType, therefore the hook to map to a CType below is
           required to ensure the header value outputs the correct value when streamed
         */
        this(new COPSObjHeader(CNum.CSI, COPSObjHeader.VAL_TO_CTYPE.get(csitype.ordinal())), data);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSClientSI(final COPSObjHeader hdr, final COPSData data) {
        super(hdr);
        _csiType = VAL_TO_CSI.get(hdr.getCType().ordinal());

        if (!hdr.getCNum().equals(CNum.CSI))
            throw new IllegalArgumentException("CNum must be equal to " + CNum.CSI);
        if (_csiType == null || _csiType.equals(CSIType.NA))
            throw new IllegalArgumentException("Invalid CSIType");
        if (_csiType.ordinal() != hdr.getCType().ordinal())
            throw new IllegalArgumentException("Error mapping CSIType " + _csiType + " to CType" + hdr.getCType());
        if (data == null) throw new IllegalArgumentException("Data must not be null");

        _data = data;
        if ((_data.length() % 4) != 0) {
            final int padLen = 4 - (_data.length() % 4);
            _padding = COPSObjectParser.getPadding(padLen);
        } else {
            _padding = new COPSData();
        }
    }

    /**
     * Returns the CSIType
     * @return - the type
     */
    public CSIType getCsiType() { return _csiType; }

    /**
     * Returns the data
     * @return - the data
     */
    public COPSData getData() { return _data; }

    @Override
    /* The super says protected but this needs to be public due to usage in COPSDecisionMsgEX.java which is currently
       calling this method. */
    public int getDataLength() {
        return _data.length() + _padding.length();
    }

    @Override
    public void writeBody(final Socket socket) throws IOException {
        COPSUtil.writeData(socket, _data.getData(), _data.length());
        COPSUtil.writeData(socket, _padding.getData(), _padding.length());
    }

    @Override
    public void dumpBody(final OutputStream os) throws IOException {
        os.write(("CSI-type: " + _csiType + "\n").getBytes());
        os.write(("client-SI: " + _data.str() + "\n").getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSClientSI)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSClientSI that = (COPSClientSI) o;
        return _data.equals(that._data) && _padding.equals(that._padding) ||
                COPSUtil.copsDataPaddingEquals(this._data, this._padding, that._data, that._padding);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_data != null ? _data.hashCode() : 0);
        result = 31 * result + (_padding != null ? _padding.hashCode() : 0);
        return result;
    }

    /**
     * Parses bytes to return a COPSClientSI object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSClientSI parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        short dLen = (short) (objHdrData.msgByteCount - 4);
        return new COPSClientSI(objHdrData.header, new COPSData(dataPtr, 4, dLen));
    }

    /**
     * The different CSI types. NA does not exist but is a placeholder for 0 as the ordinal values will be used
     * to determine which type for marshalling
     */
    public enum CSIType {
        NA, SIGNALED, NAMED
    }
}

