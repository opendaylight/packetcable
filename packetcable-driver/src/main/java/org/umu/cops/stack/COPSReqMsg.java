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
import java.util.Hashtable;
import java.util.Vector;

/**
 * COPS Request Message (RFC 2748 pag. 22)
 *
 *   The PEP establishes a request state client handle for which the
 *   remote PDP may maintain state. The remote PDP then uses this handle
 *   to refer to the exchanged information and decisions communicated over
 *   the TCP connection to a particular PEP for a given client-type.
 *
 *   Once a stateful handle is established for a new request, any
 *   subsequent modifications of the request can be made using the REQ
 *   message specifying the previously installed handle. The PEP is
 *   responsible for notifying the PDP whenever its local state changes so
 *   the PDP's state will be able to accurately mirror the PEP's state.
 *
 *   The format of the Request message is as follows:
 *
 *               <Request Message> ::=  <Common Header>
 *                                      <Client Handle>
 *                                      <Context>
 *                                      [<IN-Int>]
 *                                      [<OUT-Int>]
 *                                      [<ClientSI(s)>]
 *                                      [<LPDPDecision(s)>]
 *                                      [<Integrity>]
 *
 *               <ClientSI(s)> ::= <ClientSI> | <ClientSI(s)> <ClientSI>
 *
 *               <LPDPDecision(s)> ::= <LPDPDecision> |
 *                                     <LPDPDecision(s)> <LPDPDecision>
 *
 *               <LPDPDecision> ::= [<Context>]
 *                                  <LPDPDecision: Flags>
 *                                  [<LPDPDecision: Stateless Data>]
 *                                  [<LPDPDecision: Replacement Data>]
 *                                  [<LPDPDecision: ClientSI Data>]
 *                                  [<LPDPDecision: Named Data>]
 *
 *   The context object is used to determine the context within which all
 *   the other objects are to be interpreted. It also is used to determine
 *   the kind of decision to be returned from the policy server. This
 *   decision might be related to admission control, resource allocation,
 *   object forwarding and substitution, or configuration.
 *
 *   The interface objects are used to determine the corresponding
 *   interface on which a signaling protocol message was received or is
 *   about to be sent. They are typically used if the client is
 *   participating along the path of a signaling protocol or if the client
 *   is requesting configuration data for a particular interface.
 *
 *   ClientSI, the client specific information object, holds the client-
 *   type specific data for which a policy decision needs to be made. In
 *   the case of configuration, the Named ClientSI may include named
 *   information about the module, interface, or functionality to be
 *   configured. The ordering of multiple ClientSIs is not important.
 *
 *   Finally, LPDPDecision object holds information regarding the local
 *   decision made by the LPDP.
 *
 *   Malformed Request messages MUST result in the PDP specifying a
 *   Decision message with the appropriate error code.
 *
 * @version COPSReqMsg.java, v 1.00 2003
 *
 */
public class COPSReqMsg extends COPSMsg {

    /* COPSHeader coming from base class */
    private COPSHandle _clientHandle;
    private COPSContext _context;
    private COPSInterface _inInterface;
    private COPSInterface _outInterface;
    private Vector _clientSIs;
    private Hashtable _decisions;
    private COPSIntegrity _integrity;
    private COPSContext _lpdpContext;

    public COPSReqMsg() {
        _clientHandle = null;
        _context = null;
        _inInterface = null;
        _outInterface = null;
        _clientSIs = new Vector(20);
        _decisions = new Hashtable();
        _integrity = null;
        _lpdpContext = null;
    }

    /**
          Parse data and create COPSReqMsg object
     */
    protected COPSReqMsg(byte[] data) throws COPSException {
        parse(data);
    }

    /**
     * Checks the sanity of COPS message and throw an
     * COPSBadDataException when data is bad.
     */
    public void checkSanity() throws COPSException {
        if ((_hdr == null) || (_clientHandle == null) || (_context == null)) {
            throw new COPSException("Bad message format");
        }
    }

    /**
     * Add an IN or OUT interface object
     *
     * @param    inter               a  COPSInterface
     *
     * @throws   COPSException
     *
     */
    public void add (COPSInterface inter) throws COPSException {
        if (!(inter.isInInterface() || inter.isOutInterface()))
            throw new COPSException ("No Interface");

        //Message integrity object should be the very last one
        //If it is already added
        if (_integrity != null)
            throw new COPSException ("Integrity should be the last one");

        if (inter.isInInterface()) {
            if (_inInterface != null)
                throw new COPSException ("Object inInterface exits");

            if (inter.isIpv4Address()) {
                COPSIpv4InInterface inInter = (COPSIpv4InInterface) inter;
                _inInterface = inInter;
            } else {
                COPSIpv6InInterface inInter = (COPSIpv6InInterface) inter;
                _inInterface = inInter;
            }
        } else {
            if (_outInterface != null)
                throw new COPSException ("Object outInterface exits");

            if (inter.isIpv4Address()) {
                COPSIpv4OutInterface outInter = (COPSIpv4OutInterface) inter;
                _outInterface = outInter;
            } else {
                COPSIpv6OutInterface outInter = (COPSIpv6OutInterface) inter;
                _outInterface = outInter;
            }
        }
        setMsgLength();
    }

    /**
     * Add header to the message
     *
     * @param    hdr                 a  COPSHeader
     *
     * @throws   COPSException
     *
     */
    public void add (COPSHeader hdr) throws COPSException {
        if (hdr == null)
            throw new COPSException ("Null Header");
        if (hdr.getOpCode() != COPSHeader.COPS_OP_REQ)
            throw new COPSException ("Error Header (no COPS_OP_REQ)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add Context object to the message
     *
     * @param    context             a  COPSContext
     *
     * @throws   COPSException
     *
     */
    public void add (COPSContext context) throws COPSException {
        if (context == null)
            throw new COPSException ("Null Context");
        _context = context;
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
        _clientSIs.add(clientSI);
        setMsgLength();
    }

    /**
     * Add one or more local decision object for a given decision context
     * the context is optional, if null all decision object are tided to
     * message context
     *
     * @param    decision            a  COPSLPDPDecision
     * @param    context             a  COPSContext
     *
     * @throws   COPSException
     *
     */
    public void addLocalDecision(COPSLPDPDecision decision, COPSContext context) throws COPSException {
        if (!decision.isLocalDecision())
            throw new COPSException ("Local Decision");

        Vector v = (Vector) _decisions.get(context);
        if (decision.isFlagSet()) {
            if (v.size() != 0) {
                //Only one set of decision flags is allowed
                //for each context
                throw new COPSException ("Bad Message format, only one set of decision flags is allowed.");
            }
        } else {
            if (v.size() == 0) {
                //The flags decision must precede any other
                //decision message, since the decision is not
                //flags throw exception
                throw new COPSException ("Bad Message format, flags decision must precede any other decision object.");
            }
        }
        v.add(decision);
        _decisions.put(context,v);

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
        if (_context != null) _context.writeData(id);

        for (Enumeration e = _clientSIs.elements() ; e.hasMoreElements() ;) {
            COPSClientSI clientSI = (COPSClientSI) e.nextElement();
            clientSI.writeData(id);
        }

        //Display any local decisions
        for (Enumeration e = _decisions.keys() ; e.hasMoreElements() ;) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) _decisions.get(context);
            context.writeData(id);

            for (Enumeration ee = v.elements() ; e.hasMoreElements() ;) {
                COPSLPDPDecision decision = (COPSLPDPDecision) ee.nextElement();
                decision.writeData(id);
            }
        }

        if (_integrity != null) _integrity.writeData(id);

    }

    /**
     * Return Header
     *
     * @return   a COPSHeader
     *
     */
    public COPSHeader getHeader() {
        return _hdr;
    }

    /**
     * Return client Handle
     *
     * @return   a COPSHandle
     *
     */
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }

    /**
     * Return Context
     *
     * @return   a COPSContext
     *
     */
    public COPSContext getContext() {
        return _context;
    }

    /**
     * Returns true if it has In Interface
     *
     * @return   a boolean
     *
     */
    public boolean hasInInterface() {
        return (_inInterface == null);
    }

    /**
     * Should check hasInInterface() before calling
     *
     * @return   a COPSInterface
     *
     */
    public COPSInterface getInInterface() {
        return _inInterface;
    }

    /**
     * Returns true if it has Out interface
     *
     * @return   a boolean
     *
     */
    public boolean hasOutInterface() {
        return (_outInterface == null);
    }

    /**
     * Should check hasOutInterface() before calling
     *
     * @return   a COPSInterface
     *
     */
    public COPSInterface getOutInterface() {
        return _outInterface;
    }

    /**
     * Returns a vector if ClientSI objects
     *
     * @return   a Vector
     *
     */
    public Vector getClientSI() {
        return _clientSIs;
    }

    /**
     * Returns a HashTable of any local decisions
     *
     * @return   a Hashtable
     *
     */
    public Hashtable getLpdpDecisions() {
        return _decisions;
    }

    /**
     * Returns true if it has Integrity object
     *
     * @return   a boolean
     *
     */
    public boolean hasIntegrity() {
        return (_integrity == null);
    }

    /**
     * Get Integrity. Should check hasIntegrity() becfore calling
     *
     * @return   a COPSIntegrity
     *
     */
    public COPSIntegrity getIntegrity() {
        return _integrity;
    }

    /**
     * Parses the data and fills COPSReqMsg with its constituents
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
            case COPSObjHeader.COPS_CONTEXT: {
                if (_context == null) {
                    //Message context
                    _context = new COPSContext(buf);
                    _dataStart += _context.getDataLength();
                } else {
                    //lpdp context
                    _lpdpContext = new COPSContext(buf);
                    _dataStart += _lpdpContext.getDataLength();
                }
            }
            break;
            case COPSObjHeader.COPS_ININTF: {
                if (objHdr.getCType() == 1) {
                    _inInterface = new COPSIpv4InInterface(buf);
                } else {
                    _inInterface = new COPSIpv6InInterface(buf);
                }
                _dataStart += _inInterface.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_OUTINTF: {
                if (objHdr.getCType() == 1) {
                    _outInterface = new COPSIpv4OutInterface(buf);
                } else {
                    _outInterface = new COPSIpv6OutInterface(buf);
                }
                _dataStart += _outInterface.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_LPDP_DEC: {
                COPSLPDPDecision lpdp = new COPSLPDPDecision(buf);
                _dataStart += lpdp.getDataLength();
                addLocalDecision(lpdp, _lpdpContext);
            }
            break;
            case COPSObjHeader.COPS_CSI: {
                COPSClientSI csi = new COPSClientSI (buf);
                _dataStart += csi.getDataLength();
                _clientSIs.add(csi);
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
     * Parses the data and fills that follows the header hdr and fills COPSReqMsg
     *
     * @param    hdr                 a  COPSHeader
     * @param    data                a  byte[]
     *
     * @throws   COPSException
     *
     */
    protected void parse(COPSHeader hdr, byte[] data) throws COPSException {
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

        if (_clientHandle != null)
            len += _clientHandle.getDataLength();

        if (_context != null)
            len += _context.getDataLength();

        for (Enumeration e = _clientSIs.elements() ; e.hasMoreElements() ;) {
            COPSClientSI clientSI = (COPSClientSI) e.nextElement();
            len += clientSI.getDataLength();
        }

        //Display any local decisions
        for (Enumeration e = _decisions.keys() ; e.hasMoreElements() ;) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) _decisions.get(context);
            len += context.getDataLength();

            for (Enumeration ee = v.elements() ; e.hasMoreElements() ;) {
                COPSLPDPDecision decision = (COPSLPDPDecision) ee.nextElement();
                len += decision.getDataLength();
            }
        }

        if (_integrity != null) {
            len += _integrity.getDataLength();
        }

        _hdr.setMsgLength((int) len);

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

        if (_context != null)
            _context.dump(os);

        for (Enumeration e = _clientSIs.elements() ; e.hasMoreElements() ;) {
            COPSClientSI clientSI = (COPSClientSI) e.nextElement();
            clientSI.dump(os);
        }

        //Display any local decisions
        for (Enumeration e = _decisions.keys() ; e.hasMoreElements() ;) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) _decisions.get(context);
            context.dump(os);

            for (Enumeration ee = v.elements() ; e.hasMoreElements() ;) {
                COPSLPDPDecision decision = (COPSLPDPDecision) ee.nextElement();
                decision.dump(os);
            }
        }

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }
}

