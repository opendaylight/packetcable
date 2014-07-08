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
 * COPS Keep Alive Message
 *
 * @version COPSKAMsg.java, v 1.00 2003
 *
 */
public class COPSKAMsg extends COPSMsg {

    /* COPSHeader coming from base class */
    private COPSIntegrity  _integrity;

    public COPSKAMsg() {
        _integrity = null;
    }

    protected COPSKAMsg(byte[] data) throws COPSException {
        _integrity = null;
        parse(data);
    }

    /** Checks the sanity of COPS message and throw an
      * COPSBadDataException when data is bad.
      */
    public void checkSanity() throws COPSException {
        //The client type in the header MUST always be set to 0
        //as KA is used for connection verification.RFC 2748
        if ((_hdr == null) && (_hdr.getClientType() != 0))
            throw new COPSException("Bad message format");
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_KA)
            throw new COPSException ("Error Header (no COPS_OP_KA)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add Integrity objects
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
     * Returns true if it has Integrity object
     *
     * @return   a boolean
     *
     */
    public boolean hasIntegrity() {
        return (_integrity != null);
    };

    /**
     * Should check hasIntegrity() before calling
     *
     * @return   a COPSIntegrity
     *
     */
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    /**
     * Writes data to given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        // checkSanity();
        if (_hdr != null) _hdr.writeData(id);
        if (_integrity != null) _integrity.writeData(id);
    }

    /**
     * Method parse
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
     * Method parse
     *
     * @param    hdr                 a  COPSHeader
     * @param    data                a  byte[]
     *
     * @throws   COPSException
     *
     */
    protected void parse(COPSHeader hdr, byte[] data)     throws COPSException {
        if (hdr.getOpCode() != COPSHeader.COPS_OP_KA)
            throw new COPSException("Error Header");
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
    private void setMsgLength() throws COPSException {
        short len = 0;
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

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }
}






