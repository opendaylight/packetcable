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
 * COPS Client Close Message
 *
 * @version COPSClientCloseMsg.java, v 1.00 2003
 *
 */
public class COPSClientCloseMsg extends COPSMsg {

    /* COPSHeader coming from base class */
    private COPSError _error;
    private COPSIntegrity _integrity;


    public COPSClientCloseMsg() {
        _error = null;
        _integrity = null;
    }

    protected COPSClientCloseMsg(byte[] data) throws COPSException {
        parse (data);
    }

    /** Checks the sanity of COPS message and throw an
      * COPSBadDataException when data is bad.
      */
    public void checkSanity() throws COPSException {
        if ((_hdr == null) || (_error == null))
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_CC)
            throw new COPSException ("Error Header (no COPS_OP_CC)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add Error object
     *
     * @param    error               a  COPSError
     *
     * @throws   COPSException
     *
     */
    public void add (COPSError error) throws COPSException {
        //Message integrity object should be the very last one
        //If it is already added
        if (_error != null)
            throw new COPSException ("No null Error");
        _error = error;
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
     * Method getError
     *
     * @return   a COPSError
     *
     */
    public COPSError getError() {
        return (_error);
    }

    /**
     * Returns true If it has integrity object
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
     * Write object data to given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        // checkSanity();
        if (_hdr != null) _hdr.writeData(id);
        if (_error != null) _error.writeData(id);
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
        parseHeader(data);

        while (_dataStart < _dataLength) {
            byte[] buf = new byte[data.length - _dataStart];
            System.arraycopy(data,_dataStart,buf,0,data.length - _dataStart);

            COPSObjHeader objHdr = new COPSObjHeader (buf);
            switch (objHdr.getCNum()) {
            case COPSObjHeader.COPS_ERROR: {
                _error = new COPSError(buf);
                _dataStart += _error.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_MSG_INTEGRITY: {
                _integrity = new COPSIntegrity(buf);
                _dataStart += _integrity.getDataLength();
            }
            break;
            default: {
                throw new COPSException("Bad Message format");
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
    protected void parse(COPSHeader hdr, byte[] data) throws COPSException {
        if (hdr.getOpCode() != COPSHeader.COPS_OP_CC)
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
    protected void setMsgLength() throws COPSException {
        int len = 0;
        if (_error != null) len += _error.getDataLength();
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

        if (_error != null)
            _error.dump(os);

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }

}

