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
 * @version COPSHandle.java, v 1.00 2003
 *
 */
public class COPSHandle extends COPSObjBase {

    private final COPSObjHeader _objHdr;
    private final COPSData _id;
    private final COPSData _padding;

    /**
     * Constructor
     * @param id - the identifier (must not be null)
     * @throws java.lang.IllegalArgumentException when the id parameter is null
     */
    public COPSHandle(final COPSData id) {
        if (id == null) throw new IllegalArgumentException("COPSData must not be null");
        _objHdr = new COPSObjHeader(CNum.HANDLE, CType.DEF);
        _id = id;
        if ((_id.length() % 4) != 0) {
            final int padLen = 4 - (_id.length() % 4);
            _padding = getPadding(padLen);
        } else {
            _padding = new COPSData();
        }
        _objHdr.setDataLength((short) _id.length());
    }

    /**
     * Constructor
     * @param dataPtr - the data to parse for setting this object's attributes
     */
    protected COPSHandle(final byte[] dataPtr) {
        if (dataPtr == null || dataPtr.length < 5)
            throw new IllegalArgumentException("Data cannot be null or fewer than 5 bytes");

        _objHdr = COPSObjHeader.parse(dataPtr);

        //Get the length of data following the obj header
        final int dLen = _objHdr.getDataLength() - 4;
        _id = new COPSData(dataPtr, 4, dLen);
        if ((_id.length() % 4) != 0) {
            final int padLen = 4 - (_id.length() % 4);
            _padding = getPadding(padLen);
        } else {
            _padding = new COPSData();
        }
        _objHdr.setDataLength((short) _id.length());
    }

    /**
     * Returns size in number of octects, including header
     * @return   a short
     */
    public short getDataLength() {
        //Add the size of the header also
        final int lpadding;
        if (_padding != null) lpadding = _padding.length();
        else lpadding = 0;
        return ((short) (_objHdr.getDataLength() + lpadding));
    }

    /**
     * Get handle value
     * @return   a COPSData
     */
    public COPSData getId() {
        return _id;
    }

    @Override
    public boolean isClientHandle() {
        return true;
    }

    @Override
    public void writeData(final Socket socket) throws IOException {
        _objHdr.writeData(socket);

        COPSUtil.writeData(socket, _id.getData(), _id.length());
        if (_padding != null) {
            COPSUtil.writeData(socket, _padding.getData(), _padding.length());
        }
    }

    /**
     * Write an object textual description in the output stream
     * @param    os                  an OutputStream
     * @throws   IOException
     */
    public void dump(final OutputStream os) throws IOException {
        _objHdr.dump(os);
        os.write(("client-handle: " + _id.str() + "\n").getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSHandle)) {
            return false;
        }
        final COPSHandle that = (COPSHandle) o;
        return _id.equals(that._id) && _objHdr.equals(that._objHdr);
    }

    @Override
    public int hashCode() {
        int result = _objHdr.hashCode();
        result = 31 * result + _id.hashCode();
        return result;
    }

}

