/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

/**
 * COPS Request Message (RFC 2748 page. 22)
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

    // Required Attributes
    private final COPSHandle _clientHandle;
    private final COPSContext _context;

    // Optional Attributes
    private final COPSInterface _inInterface;
    private final COPSInterface _outInterface;
    private final COPSIntegrity _integrity;

    // Collection Attributes (can be empty)
    private final Set<COPSClientSI> _clientSIs;
    private final Map<COPSContext, Set<COPSLPDPDecision>> _decisions;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message  (required)
     * @param handle - the COPS Handle (required)
     * @param context - the COPS Context  (required)
     * @param integrity - the COPS Integrity (optional)
     * @param inInterface - the In Interface (optional)
     * @param outInterface - the Out interface (optional)
     * @param clientSIs - the Client SIs (optional)
     * @param decisions - the Decisions by context (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSReqMsg(final short clientType, final COPSHandle handle, final COPSContext context,
                      final COPSIntegrity integrity, final COPSInterface inInterface, final COPSInterface outInterface,
                      final Set<COPSClientSI> clientSIs, final Map<COPSContext, Set<COPSLPDPDecision>> decisions) {
        this(1, Flag.UNSOLICITED, clientType, handle, context, integrity, inInterface, outInterface,
                clientSIs, decisions);
    }

    /**
     * Recommended constructor generally for use by a client sending messages.
     * @param version - the supported PCMM Version (required)
     * @param flag - the flag...  (required)
     * @param clientType - the type of client that created the message  (required)
     * @param handle - the COPS Handle  (required)
     * @param context - the COPS Context  (required)
     * @param integrity - the COPS Integrity (optional)
     * @param inInterface - the In Interface (optional)
     * @param outInterface - the Out interface (optional)
     * @param clientSIs - the Client SIs (optional)
     * @param decisions - the Decisions by context (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSReqMsg(final int version, final Flag flag, final short clientType, final COPSHandle handle,
                      final COPSContext context, final COPSIntegrity integrity, final COPSInterface inInterface,
                      final COPSInterface outInterface, final Set<COPSClientSI> clientSIs,
                      final Map<COPSContext, Set<COPSLPDPDecision>> decisions) {
        this(new COPSHeader(version, flag, OPCode.REQ, clientType), handle, context, integrity, inInterface,
                outInterface, clientSIs, decisions);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header (required)
     * @param handle - the COPS Handle (required)
     * @param context - the COPS Context (required)
     * @param integrity - the COPS Integrity (optional)
     * @param inInterface - the In Interface (optional)
     * @param outInterface - the Out interface (optional)
     * @param clientSIs - the Client SIs (optional)
     * @param decisions - the Decisions by context (optional)
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSReqMsg(final COPSHeader hdr, final COPSHandle handle, final COPSContext context,
                      final COPSIntegrity integrity, final COPSInterface inInterface, final COPSInterface outInterface,
                      final Set<COPSClientSI> clientSIs, final Map<COPSContext, Set<COPSLPDPDecision>> decisions) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.REQ))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.REQ);
        if (handle == null) throw new IllegalArgumentException("COPSHandle must not be null");
        if (context == null) throw new IllegalArgumentException("COPSContext must not be null");

        _clientHandle = handle;
        _context = context;
        _integrity = integrity;
        _inInterface = inInterface;
        _outInterface = outInterface;

        if (clientSIs == null) _clientSIs = Collections.unmodifiableSet(new HashSet<COPSClientSI>());
        else _clientSIs = Collections.unmodifiableSet(clientSIs);

        if (decisions == null) _decisions = Collections.unmodifiableMap(new HashMap<COPSContext, Set<COPSLPDPDecision>>());
        else _decisions = Collections.unmodifiableMap(decisions);
    }


    // Getters of optional members - all can return null
    public COPSIntegrity getIntegrity() { return _integrity; }
    public COPSInterface getInInterface() { return _inInterface; }
    public COPSInterface getOutInterface() { return _outInterface; }
//    public COPSContext getLpdpContext() { return _lpdpContext; }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _clientHandle.writeData(socket);
        _context.writeData(socket);

        if (_inInterface != null) _inInterface.writeData(socket);
        if (_outInterface != null) _outInterface.writeData(socket);

        for (final COPSClientSI clientSI : _clientSIs) {
            clientSI.writeData(socket);
        }

        //Display any local decisions
        for (final Map.Entry<COPSContext, Set<COPSLPDPDecision>> entry : _decisions.entrySet()) {
            entry.getKey().writeData(socket);
            for (final COPSDecision decision : entry.getValue()) {
                decision.writeData(socket);
            }
        }

        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected int getDataLength() {
        int out = _context.getDataLength() + _context.getHeader().getHdrLength();
        out += _clientHandle.getDataLength() + _clientHandle.getHeader().getHdrLength();
        if (_inInterface != null) out += _inInterface.getDataLength() + _inInterface.getHeader().getHdrLength();
        if (_outInterface != null) out += _outInterface.getDataLength() + _outInterface.getHeader().getHdrLength();

        for (final COPSClientSI clientSI : _clientSIs) {
            out += clientSI.getDataLength() + clientSI.getHeader().getHdrLength();
        }

        for (final Map.Entry<COPSContext, Set<COPSLPDPDecision>> entry : _decisions.entrySet()) {
            out += entry.getKey().getDataLength() + entry.getKey().getHeader().getHdrLength();
            for (final COPSDecision decision : entry.getValue()) {
                out += decision.getDataLength() + decision.getHeader().getHdrLength();
            }
        }

        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        return out;
    }

    /**
     * Return client Handle
     * @return   a COPSHandle
     */
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }

    /**
     * Return Context
     * @return   a COPSContext
     */
    public COPSContext getContext() {
        return _context;
    }

    /**
     * Returns a Set of ClientSI objects
     * @return not null but can be empty
     */
    public Set<COPSClientSI> getClientSI() {
        return _clientSIs;
    }

    /**
     * Returns a Map of COPSDecision objects
     * @return not null but can be empty
     */
    public Map<COPSContext, Set<COPSLPDPDecision>> getDecisions() {
        return _decisions;
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        if (_clientHandle != null)
            _clientHandle.dump(os);

        if (_context != null)
            _context.dump(os);

        for (final COPSClientSI clientSI : _clientSIs) {
            clientSI.dump(os);
        }

        //Display any local decisions
        for (final Map.Entry<COPSContext, Set<COPSLPDPDecision>> entry : _decisions.entrySet()) {
            entry.getKey().dump(os);

            for (final COPSDecision decision : entry.getValue()) {
                decision.dump(os);
            }
        }

        if (_integrity != null) {
            _integrity.dump(os);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSReqMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSReqMsg that = (COPSReqMsg) o;

        if (this._clientSIs.size() != that._clientSIs.size()) return false;
        for (final COPSClientSI thisClientSI : this._clientSIs) {
            boolean found = false;
            for (final COPSClientSI thatClientSI: that._clientSIs) {
                if (thisClientSI.equals(thatClientSI)) {
                    found = true;
                    break;
                }
            }
            if (! found) return false;
        }

        for (final Map.Entry<COPSContext, Set<COPSLPDPDecision>> entry : this._decisions.entrySet()) {
            final Set<COPSLPDPDecision> thatDecisions = that._decisions.get(entry.getKey());
            if (thatDecisions == null) return false;

            for (final COPSLPDPDecision thisDecision : entry.getValue()) {
                boolean found = false;
                for (final COPSLPDPDecision thatDecision: thatDecisions) {
                    if (thisDecision.equals(thatDecision)) {
                        found = true;
                        break;
                    }
                }
                if (! found) return false;
            }
        }

        if (!this._clientHandle.equals(that._clientHandle)) return false;
        if (!this._context.equals(that._context)) return false;

        if (this._integrity == null && that._integrity != null) return false;
        if (this._integrity != null && that._integrity == null) return false;
        if (this._integrity != null && that._integrity != null)
            if (!this._integrity.equals(that._integrity)) return false;

        if (this._inInterface == null && that._inInterface != null) return false;
        if (this._inInterface != null && that._inInterface == null) return false;
        if (this._inInterface != null && that._inInterface != null)
            if (!this._inInterface.equals(that._inInterface)) return false;

        if (this._outInterface == null && that._outInterface != null) return false;
        if (this._outInterface != null && that._outInterface == null) return false;
        if (this._outInterface != null && that._outInterface != null)
            if (!this._outInterface.equals(that._outInterface)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _clientHandle.hashCode();
        result = 31 * result + _context.hashCode();
        result = 31 * result + (_inInterface != null ? _inInterface.hashCode() : 0);
        result = 31 * result + (_outInterface != null ? _outInterface.hashCode() : 0);
        result = 31 * result + _clientSIs.hashCode();
        result = 31 * result + _decisions.hashCode();
        result = 31 * result + (_integrity != null ? _integrity.hashCode() : 0);
        return result;
    }

    /**
     * Responsible for parsing a byte array to create a COPSReqMsg object
     * @param hdrData - the object's header data
     * @param data - the byte array to parse
     * @return - the message object
     * @throws COPSException
     */
    public static COPSReqMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSHandle clientHandle = null;
        COPSContext context = null;
        COPSContext lpdpContext = null;
        COPSIntegrity integrity = null;
        COPSInterface inInterface = null;
        COPSInterface outInterface = null;
        Set<COPSClientSI> clientSIs = new HashSet<>();
        Map<COPSContext, Set<COPSLPDPDecision>> localDecisions = new HashMap<>();

        int dataStart = 0;
        while (dataStart < data.length) {
            final byte[] buf = new byte[data.length - dataStart];
            System.arraycopy(data, dataStart, buf, 0, data.length - dataStart);

            final COPSObjHeaderData objHdrData = COPSObjectParser.parseObjHeader(buf);
            switch (objHdrData.header.getCNum()) {
                case HANDLE:
                    clientHandle = COPSHandle.parse(objHdrData, buf);
                    break;
                case CONTEXT:
                    if (context == null) context = COPSContext.parse(objHdrData, buf);
                    else lpdpContext = COPSContext.parse(objHdrData, buf);
                    break;
                case ININTF:
                    if (objHdrData.header.getCType().equals(CType.DEF)) {
                        inInterface = COPSObjectParser.parseIpv4Interface(objHdrData, buf, true);
                    } else inInterface = COPSObjectParser.parseIpv6Interface(objHdrData, buf, true);
                    break;
                case OUTINTF:
                    if (objHdrData.header.getCType().equals(CType.DEF)) {
                        outInterface = COPSObjectParser.parseIpv4Interface(objHdrData, buf, false);
                    } else  outInterface = COPSObjectParser.parseIpv6Interface(objHdrData, buf, false);
                    break;
                case LPDP_DEC:
                    if (localDecisions.get(lpdpContext) != null)
                        localDecisions.get(lpdpContext).add(COPSLPDPDecision.parse(objHdrData, buf));
                    else {
                        final Set<COPSLPDPDecision> decisions = new HashSet<>();
                        decisions.add(COPSLPDPDecision.parse(objHdrData, buf));
                        localDecisions.put(lpdpContext, decisions);
                    }
                    break;
                case CSI:
                    clientSIs.add(COPSClientSI.parse(objHdrData, buf));
                    break;
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSReqMsg(hdrData.header, clientHandle, context, integrity, inInterface, outInterface,
                clientSIs, localDecisions);
    }

}

