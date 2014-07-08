/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

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

    private COPSObjHeader _objHdr;
    private COPSData _id;
    private COPSData _padding;

    public COPSHandle() {
        _objHdr = new COPSObjHeader();
        _objHdr.setCNum(COPSObjHeader.COPS_HANDLE);
        _objHdr.setCType((byte) 1);
        _padding = new COPSData();
    }

    /**
          Parse data and create COPSHandle object
     */
    protected COPSHandle(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        //Get the length of data following the obj header
        int dLen = _objHdr.getDataLength() - 4;
        COPSData d = new COPSData (dataPtr, 4, dLen);
        setId(d);
    }

    /**
     * Set handle value
     *
     * @param    id                  a  COPSData
     *
     */
    public void setId(COPSData id) {
        _id = id;
        if ((id.length() % 4) != 0) {
            int padLen = 4 - (_id.length() % 4);
            _padding = getPadding(padLen);
        }
        _objHdr.setDataLength((short) _id.length());
    }

    /**
     * Returns size in number of octects, including header
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        //Add the size of the header also
        int lpadding = 0;
        if (_padding != null) lpadding = _padding.length();
        return ((short) (_objHdr.getDataLength() + lpadding));
    }

    /**
     * Get handle value
     *
     * @return   a COPSData
     *
     */
    public COPSData getId() {
        return _id;
    }

    /**
     * Always return true
     *
     * @return   a boolean
     *
     */
    public boolean isClientHandle() {
        return true;
    }

    /**
     * Write data in network byte order on a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        _objHdr.writeData(id);

        COPSUtil.writeData(id, _id.getData(), _id.length());
        if (_padding != null) {
            COPSUtil.writeData(id, _padding.getData(), _padding.length());
        }
    }

    /**
     * Write an object textual description in the output stream
     *
     * @param    os                  an OutputStream
     *
     * @throws   IOException
     *
     */
    public void dump(OutputStream os) throws IOException {
        _objHdr.dump(os);
        os.write(new String("client-handle: " + _id.str() + "\n").getBytes());
    }
}

