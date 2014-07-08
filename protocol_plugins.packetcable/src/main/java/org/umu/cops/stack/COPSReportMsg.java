/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

/**
 * COPS Report Message (RFC 2748 pag. 25)
 *
 *    The RPT message is used by the PEP to communicate to the PDP its
 *   success or failure in carrying out the PDP's decision, or to report
 *   an accounting related change in state. The Report-Type specifies the
 *   kind of report and the optional ClientSI can carry additional
 *   information per Client-Type.
 *
 *   For every DEC message containing a configuration context that is
 *   received by a PEP, the PEP MUST generate a corresponding Report State
 *   message with the Solicited Message flag set describing its success or
 *   failure in applying the configuration decision. In addition,
 *   outsourcing decisions from the PDP MAY result in a corresponding
 *   solicited Report State from the PEP depending on the context and the
 *   type of client. RPT messages solicited by decisions for a given
 *   Client Handle MUST set the Solicited Message flag and MUST be sent in
 *   the same order as their corresponding Decision messages were
 *   received. There MUST never be more than one Report State message
 *   generated with the Solicited Message flag set per Decision.
 *
 *   The Report State may also be used to provide periodic updates of
 *   client specific information for accounting and state monitoring
 *   purposes depending on the type of the client. In such cases the
 *   accounting report type should be specified utilizing the appropriate
 *   client specific information object.
 *
 *              <Report State> ::== <Common Header>
 *                                  <Client Handle>
 *                                  <Report-Type>
 *                                  [<ClientSI>]
 *                                  [<Integrity>]
 *
 * @version COPSReportMsg.java, v 1.00 2003
 *
 */
public class COPSReportMsg extends COPSMsg {
    /* COPSHeader coming from base class */
    private COPSHandle _clientHandle;
    private COPSReportType _report;
    private Vector _clientSI;
    private COPSIntegrity _integrity;

    public COPSReportMsg() {
        _clientHandle = null;
        _report = null;
        _integrity = null;
        _clientSI = new Vector(20);
    }

    /**
          Parse data and create COPSReportMsg object
     */
    protected COPSReportMsg (byte[] data) throws COPSException {
        _clientHandle = null;
        _report = null;
        _integrity = null;
        parse(data);
    }

    /**
     * Checks the sanity of COPS message and throw an
     * COPSException when data is bad.
     */
    public void checkSanity() throws COPSException {
        if ((_hdr == null) || (_clientHandle == null) || (_report == null))
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_RPT)
            throw new COPSException ("Error Header (no COPS_OP_REQ)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add Report object to the message
     *
     * @param    report              a  COPSReportType
     *
     * @throws   COPSException
     *
     */
    public void add (COPSReportType report) throws COPSException {
        if (report == null)
            throw new COPSException ("Null Handle");

        //Message integrity object should be the very last one
        //If it is already added
        if (_integrity != null)
            throw new COPSException ("No null Handle");

        _report = report;
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
     * Add one or more clientSI objects
     *
     * @param    clientSI            a  COPSClientSI
     *
     * @throws   COPSException
     *
     */
    public void add (COPSClientSI clientSI) throws COPSException {
        if (clientSI == null)
            throw new COPSException ("Null ClientSI");
        _clientSI.add(clientSI);
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
     * Get client Handle
     *
     * @return   a COPSHandle
     *
     */
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }

    /**
     * Get report type
     *
     * @return   a COPSReportType
     *
     */
    public COPSReportType getReport() {
        return _report;
    }

    /**
     * Get clientSI
     *
     * @return   a Vector
     *
     */
    public Vector getClientSI() {
        return _clientSI;
    }

    /**
     * Returns true if it has Integrity object
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
     * Writes data to given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        //checkSanity();
        if (_hdr != null) _hdr.writeData(id);
        if (_clientHandle != null) _clientHandle.writeData(id);
        if (_report != null) _report.writeData(id);

        for (Enumeration e = _clientSI.elements() ; e.hasMoreElements() ;) {
            COPSClientSI clientSI = (COPSClientSI) e.nextElement();
            clientSI.writeData(id);
        }

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
            case COPSObjHeader.COPS_RPT: {
                _report = new COPSReportType(buf);
                _dataStart += _report.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_CSI: {
                COPSClientSI csi = new COPSClientSI(buf);
                _dataStart += csi.getDataLength();
                _clientSI.add(csi);
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
        if (hdr.getOpCode() != COPSHeader.COPS_OP_RPT)
            throw new COPSException ("Null Header");
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
        if (_report != null) len += _report.getDataLength();

        for (Enumeration e = _clientSI.elements() ; e.hasMoreElements() ;) {
            COPSClientSI clientSI = (COPSClientSI) e.nextElement();
            len += clientSI.getDataLength();
        }

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

        if (_report != null)
            _report.dump(os);

        for (Enumeration e = _clientSI.elements() ; e.hasMoreElements() ;) {
            COPSClientSI clientSI = (COPSClientSI) e.nextElement();
            clientSI.dump(os);
        }

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }
}



