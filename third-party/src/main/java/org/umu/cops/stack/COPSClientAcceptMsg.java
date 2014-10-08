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
 *     COPS Client Accept Message
 *
 * @version COPSClientAcceptMsg.java, v 1.00 2003
 *
 */
public class COPSClientAcceptMsg extends COPSMsg {

    /* COPSHeader coming from base class */
    private COPSKATimer _kaTimer;
    private COPSAcctTimer _acctTimer;
    private COPSIntegrity _integrity;

    ///Constructor
    public COPSClientAcceptMsg() {
        _kaTimer = null;
        _acctTimer = null;
        _integrity = null;
    }

    ///Create object from data
    protected COPSClientAcceptMsg(byte[] data) throws COPSException {
        parse(data);
    }

    /** Checks the sanity of COPS message and throw an
      * COPSBadDataException when data is bad.
      */
    public void checkSanity() throws COPSException {
        if ((_hdr == null) || (_kaTimer == null))
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_CAT)
            throw new COPSException ("Error Header (no COPS_OP_CAT)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add Timer object to the message
     *
     * @param    timer               a  COPSTimer
     *
     * @throws   COPSException
     *
     */
    public void add (COPSTimer timer) throws COPSException {
        if (timer.isKATimer()) {
            _kaTimer = (COPSKATimer) timer;
        } else {
            _acctTimer = (COPSAcctTimer) timer;
        }
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
     * Method getKATimer
     *
     * @return   a COPSKATimer
     *
     */
    public COPSKATimer getKATimer() {
        return _kaTimer;
    };

    /**
     * Returns true if has a account timer object
     *
     * @return   a boolean
     *
     */
    public boolean hasAcctTimer() {
        return (_acctTimer != null);
    };

    /**
     * Should check hasAcctTimer() before calling
     *
     * @return   a COPSAcctTimer
     *
     */
    public COPSAcctTimer getAcctTimer() {
        return (_acctTimer);
    }

    /**
     * Returns true if has a Integrity object
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
     * Writes data to a given socket id
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        // checkSanity();
        if (_hdr != null) _hdr.writeData(id);
        if (_kaTimer != null) _kaTimer.writeData(id);
        if (_acctTimer != null) _acctTimer.writeData(id);
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
            case COPSObjHeader.COPS_KA: {
                _kaTimer = new COPSKATimer(buf);
                _dataStart += _kaTimer.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_ACCT_TIMER: {
                _acctTimer = new COPSAcctTimer(buf);
                _dataStart += _acctTimer.getDataLength();
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_CAT)
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
        short len = 0;
        if (_kaTimer != null) len += _kaTimer.getDataLength();
        if (_acctTimer != null) len += _acctTimer.getDataLength();
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

        if (_kaTimer != null)
            _kaTimer.dump(os);

        if (_acctTimer != null)
            _acctTimer.dump(os);

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }
}

