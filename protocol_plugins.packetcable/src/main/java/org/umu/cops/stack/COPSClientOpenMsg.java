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
 * COPS Client Open Message
 *
 * @version COPSClientOpenMsg.java, v 1.00 2003
 *
 */
public class COPSClientOpenMsg extends COPSMsg {

    private COPSPepId _pepId;
    private COPSClientSI _clientSI;
    private COPSPdpAddress _pdpAddress;
    private COPSIntegrity _integrity;

    public COPSClientOpenMsg() {
        _pepId = null;
        _clientSI = null;
        _pdpAddress = null;
        _integrity = null;
        _hdr = null;
    }

    protected COPSClientOpenMsg(byte[] data) throws COPSException {
        _pepId = null;
        _clientSI = null;
        _pdpAddress = null;
        _integrity = null;
        _hdr = null;
        parse(data);
    }

    /**
     * Method writeData
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        // checkSanity();
        if (_hdr != null)_hdr.writeData(id);
        if (_pepId != null) _pepId.writeData(id);
        if (_clientSI != null) _clientSI.writeData(id);
        if (_pdpAddress != null) _pdpAddress.writeData(id);
        if (_integrity != null) _integrity.writeData(id);
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_OPN)
            throw new COPSException ("Error Header (no COPS_OP_OPN)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add PEP Identification Object
     *
     * @param    pepid               a  COPSPepId
     *
     * @throws   COPSException
     *
     */
    public void add (COPSPepId pepid) throws COPSException {
        if (pepid == null)
            throw new COPSException ("Null COPSPepId");
        if (!pepid.isPepId())
            throw new COPSException ("Error COPSPepId");
        _pepId = pepid;
        setMsgLength();
    }

    /**
     * Add Client Specific Information Object
     *
     * @param    clientSI            a  COPSClientSI
     *
     * @throws   COPSException
     *
     */
    public void add (COPSClientSI clientSI) throws COPSException {
        if (clientSI == null)
            throw new COPSException ("Null COPSClientSI");
        if (!clientSI.isClientSI())
            throw new COPSException ("Error COPSClientSI");
        _clientSI = clientSI;
        setMsgLength();
    }

    /**
     * Add PDP Address
     *
     * @param    pdpAddr             a  COPSPdpAddress
     *
     * @throws   COPSException
     *
     */
    public void add (COPSPdpAddress pdpAddr) throws COPSException {
        if (pdpAddr == null)
            throw new COPSException ("Null COPSPdpAddress");
        if (!pdpAddr.isLastPdpAddress())
            throw new COPSException ("Error COPSPdpAddress");
        _pdpAddress = pdpAddr;
        setMsgLength();
    }

    /**
     * Add Integrity object
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

    /** Checks the sanity of COPS message and throw an
          * COPSBadDataException when data is bad.
    */
    public void checkSanity() throws COPSException {
        if ((_hdr == null) || (_pepId == null))
            throw new COPSException("Bad message format");
    }

    /**
     * Method getPepId
     *
     * @return   a COPSPepId
     *
     */
    public COPSPepId getPepId() {
        return _pepId;
    }

    /**
     * Method hasClientSI
     *
     * @return   a boolean
     *
     */
    public boolean hasClientSI() {
        return (_clientSI != null);
    }

    /**
     * Method getClientSI
     *
     * @return   a COPSClientSI
     *
     */
    public COPSClientSI getClientSI() {
        return (_clientSI);
    }

    /**
     * Method hasPdpAddress
     *
     * @return   a boolean
     *
     */
    public boolean hasPdpAddress() {
        return (_pdpAddress != null);
    }

    /**
     * Method getPdpAddress
     *
     * @return   a COPSPdpAddress
     *
     */
    public COPSPdpAddress getPdpAddress() {
        return _pdpAddress;
    }

    /**
     * Method hasIntegrity
     *
     * @return   a boolean
     *
     */
    public boolean hasIntegrity() {
        return (_integrity != null);
    }

    /**
     * Method getIntegrity
     *
     * @return   a COPSIntegrity
     *
     */
    public COPSIntegrity getIntegrity() {
        return _integrity;
    }

    /**
     * Set the message length, base on the set of objects it contains
     *
     * @throws   COPSException
     *
     */
    private void setMsgLength() throws COPSException {
        short len = 0;
        if (_pepId != null) len += _pepId.getDataLength();
        if (_clientSI != null) len += _clientSI.getDataLength();
        if (_pdpAddress != null) len += _pdpAddress.getDataLength();
        if (_integrity != null) len += _integrity.getDataLength();
        _hdr.setMsgLength(len);
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
            case COPSObjHeader.COPS_PEPID: {
                _pepId = new COPSPepId(buf);
                _dataStart += _pepId.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_LAST_PDP_ADDR: {
                if (objHdr.getCType() == 1) {
                    _pdpAddress = new COPSIpv4LastPdpAddr(buf);
                } else if (objHdr.getCType() == 2) {
                    _pdpAddress = new COPSIpv6LastPdpAddr(buf);
                }
                _dataStart += _pdpAddress.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_CSI: {
                _clientSI = new COPSClientSI(buf);
                _dataStart += _clientSI.getDataLength();
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_OPN)
            throw new COPSException("Error Header");
        _hdr = hdr;
        parse(data);
        setMsgLength();
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

        if (_pepId != null)
            _pepId.dump(os);

        if (_clientSI != null)
            _clientSI.dump(os);

        if (_pdpAddress != null)
            _pdpAddress.dump(os);

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }
}



