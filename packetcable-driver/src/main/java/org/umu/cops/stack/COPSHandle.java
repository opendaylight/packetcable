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

/**
 * COPS Handle Object (RFC 2748 pag. 9)
 *
 *   The Handle Object encapsulates a unique value that identifies an
 *   installed state. This identification is used by most COPS operations.
 *
 *           C-Num = 1
 *
 *           C-Type = 1, Client Handle.
 *
 *   Variable-length field, no implied format other than it is unique from
 *   other client handles from the same PEP (a.k.a. COPS TCP connection)
 *   for a particular client-type. It is always initially chosen by the
 *   PEP and then deleted by the PEP when no longer applicable. The client
 *   handle is used to refer to a request state initiated by a particular
 *   PEP and installed at the PDP for a client-type. A PEP will specify a
 *   client handle in its Request messages, Report messages and Delete
 *   messages sent to the PDP. In all cases, <b>the client handle is used to
 *   uniquely identify a particular PEP's request for a client-type</b>.
 *
 *   The client handle value is set by the PEP and is opaque to the PDP.
 *   The PDP simply performs a byte-wise comparison on the value in this
 *   object with respect to the handle object values of other currently
 *   installed requests.
 *
 */
public class COPSHandle extends COPSObjBase {

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
     * @param id - the identifier (must not be null)
     * @throws java.lang.IllegalArgumentException when the id parameter is null
     */
    public COPSHandle(final COPSData id) {
        this(new COPSObjHeader(CNum.HANDLE, CType.DEF), id);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param objHdr - the object header
     * @param data - the ID
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSHandle(final COPSObjHeader objHdr, final COPSData data) {
        super(objHdr);
        if (!objHdr.getCNum().equals(CNum.HANDLE))
            throw new IllegalArgumentException("CNum on header must be of type HANDLE");
        if (!objHdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("Invalid CType value. Must be " + CType.DEF);
        if (data == null) throw new IllegalArgumentException("COPSData must not be null");

        _data = data;

        if ((_data.length() % 4) != 0) {
            final int padLen = 4 - (_data.length() % 4);
            _padding = COPSObjectParser.getPadding(padLen);
        } else {
            _padding = new COPSData();
        }
    }

    @Override
    public int getDataLength() {
        return _data.length() + _padding.length();
    }

    /**
     * Get handle value
     * @return   a COPSData
     */
    public COPSData getId() {
        return _data;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        COPSUtil.writeData(socket, _data.getData(), _data.length());
        COPSUtil.writeData(socket, _padding.getData(), _padding.length());
    }

    @Override
    public void dumpBody(final OutputStream os) throws IOException {
        os.write(("client-handle: " + _data.str() + "\n").getBytes());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSHandle)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSHandle that = (COPSHandle) o;

        return _data.equals(that._data) && _padding.equals(that._padding) ||
                COPSUtil.copsDataPaddingEquals(this._data, this._padding, that._data, that._padding);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _data.hashCode();
        result = 31 * result + _padding.hashCode();
        return result;
    }

    /**
     * Parses bytes to return a COPSHandle object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSHandle parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        if (dataPtr == null || dataPtr.length < 5)
            throw new IllegalArgumentException("Data cannot be null or fewer than 5 bytes");

        //Get the length of data following the obj header
        final COPSData id = new COPSData(dataPtr, 4, objHdrData.msgByteCount - objHdrData.header.getHdrLength());
        return new COPSHandle(objHdrData.header, id);
    }

}

