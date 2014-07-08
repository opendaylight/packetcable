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
 * COPS Sync State Message (RFC 2748 pag. 26 and pag. 29
 *
 *   The format of the Synchronize State Query message is as follows:
 *
 *              <Synchronize State> ::= <Common Header>
 *                                      [<Client Handle>]
 *                                      [<Integrity>]
 *
 *   This message indicates that the remote PDP wishes the client (which
 *   appears in the common header) to re-send its state. If the optional
 *   Client Handle is present, only the state associated with this handle
 *   is synchronized. If the PEP does not recognize the requested handle,
 *   it MUST immediately send a DRQ message to the PDP for the handle that
 *   was specified in the SSQ message. If no handle is specified in the
 *    SSQ message, all the active client state MUST be synchronized with
 *   the PDP.
 *
 *   The client performs state synchronization by re-issuing request
 *   queries of the specified client-type for the existing state in the
 *   PEP. When synchronization is complete, the PEP MUST issue a
 *   synchronize state complete message to the PDP.
 *
 *         <Synchronize State Complete>  ::= <Common Header>
 *                                           [<Client Handle>]
 *                                           [<Integrity>]
 *
 *   The Client Handle object only needs to be included if the corresponding
 *   Synchronize State Message originally referenced a specific handle.
 *
 * @version COPSSyncStateMsg.java, v 1.00 2003
 *
 */
public class COPSSyncStateMsg extends COPSMsg {

    /* COPSHeader coming from base class */
    private COPSHandle  _clientHandle;
    private COPSIntegrity  _integrity;

    public COPSSyncStateMsg() {
        _clientHandle = null;
        _integrity = null;
    }

    /**
          Parse data and create COPSSyncStateMsg object
     */
    protected COPSSyncStateMsg(byte[] data) throws COPSException  {
        _clientHandle = null;
        _integrity = null;
        parse(data);
    }

    /**
     * Checks the sanity of COPS message and throw an
     * COPSException when data is bad.
     */
    public void checkSanity() throws COPSException {
        if (_hdr == null) {
            throw new COPSException("Bad message format");
        }
    }

    /**
     * Add message header
     *
     * @param    hdr                 a  COPSHeader
     *
     * @throws   COPSException
     *
     */
    public void add (COPSHeader hdr) throws COPSException {
        if (hdr == null)
            throw new COPSException ("Null Header");
        if ((hdr.getOpCode() != COPSHeader.COPS_OP_SSC) &&
                (hdr.getOpCode() != COPSHeader.COPS_OP_SSQ))
            throw new COPSException ("Error Header (no COPS_OP_SSX)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add client handle to the message
     *
     * @param    handle              a  COPSHandle
     *
     * @throws   COPSException
     *
     */
    public void add (COPSHandle handle) throws COPSException {
        if (handle == null)
            throw new COPSException ("Null Handle");

        //Message integrity object should be the very last one
        //If it is already added
        if (_integrity != null)
            throw new COPSException ("No null Handle");

        _clientHandle = handle;
        setMsgLength();
    }

    /**
     * Add integrity object
     *
     * @param    integrity           a  COPSIntegrity
     *
     * @throws   COPSException
     *
     */
    public void add (COPSIntegrity integrity) throws COPSException {
        if (integrity == null)
            throw new COPSException ("Null Integrity");
        if (!integrity.isMessageIntegrity())
            throw new COPSException ("Error Integrity");
        _integrity = integrity;
        setMsgLength();
    }

    /**
     * If the optional Client Handle is present, only the state associated
      * with this handle is synchronized. If no handle is specified in the
      * SSQ message, all the active client state MUST be synchronized with
      * the PDP.
     *
     * @return   a boolean
     *
     */
    public boolean hasClientHandle() {
        return (_clientHandle != null);
    }

    /**
     * Get client Handle
     *
     * @return   a COPSHandle
     *
     */
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }

    /**
     * Returns true if it has integrity object
     *
     * @return   a boolean
     *
     */
    public boolean hasIntegrity() {
        return (_integrity != null);
    }

    /**
     * Get Integrity. Should check hasIntegrity() before calling
     *
     * @return   a COPSIntegrity
     *
     */
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    /**
     * Writes data to given socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        // checkSanity();
        if (_hdr != null) _hdr.writeData(id);
        if (_clientHandle != null) _clientHandle.writeData(id);
        if (_integrity != null) _integrity.writeData(id);

    }

    /**
     * Parse data
     *
     * @param    data                a  byte[]
     *
     * @throws   COPSException
     *
     */
    protected void parse(byte[] data) throws COPSException {
        super.parseHeader(data);

        while (_dataStart < _dataLength) {
            byte[] buf = new byte[data.length - _dataStart];
            System.arraycopy(data,_dataStart,buf,0,data.length - _dataStart);

            COPSObjHeader objHdr = new COPSObjHeader (buf);
            switch (objHdr.getCNum()) {
            case COPSObjHeader.COPS_HANDLE: {
                _clientHandle = new COPSHandle(buf);
                _dataStart += _clientHandle.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_MSG_INTEGRITY: {
                _integrity = new COPSIntegrity(buf);
                _dataStart += _integrity.getDataLength();
            }
            break;
            default: {
                throw new COPSException("Bad Message format, unknown object type");
            }
            }
        }
        checkSanity();
    }

    /**
     * Parse data
     *
     * @param    hdr                 a  COPSHeader
     * @param    data                a  byte[]
     *
     * @throws   COPSException
     *
     */
    protected void parse(COPSHeader hdr, byte[] data) throws COPSException {

        if ((hdr.getOpCode() != COPSHeader.COPS_OP_SSC) &&
                (hdr.getOpCode() != COPSHeader.COPS_OP_SSQ))
            throw new COPSException ("Error Header (no COPS_OP_SSX)");

        _hdr = hdr;
        parse(data);
        setMsgLength();
    }

    /**
     * Set the message length, base on the set of objects it contains
     *
     * @throws   COPSException
     *
     */
    protected void setMsgLength() throws COPSException {
        short len = 0;
        if (_clientHandle != null) len += _clientHandle.getDataLength();
        if (_integrity != null) len += _integrity.getDataLength();
        _hdr.setMsgLength(len);
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
        _hdr.dump(os);

        if (_clientHandle != null)
            _clientHandle.dump(os);

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }
}



