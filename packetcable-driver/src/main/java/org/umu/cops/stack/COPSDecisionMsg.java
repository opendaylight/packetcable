/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

/**
 * COPS Decision Message  (RFC 2748 page. 23)
 *
 * The PDP responds to the REQ with a DEC message that includes the
 * associated client handle and one or more decision objects grouped
 * relative to a Context object and Decision Flags object type pair. If
 * there was a protocol error an error object is returned instead.
 *
 * It is required that the first decision message for a new/updated
 * request will have the solicited message flag set (value = 1) in the
 * COPS header. This avoids the issue of keeping track of which updated
 * request (that is, a request reissued for the same handle) a
 * particular decision corresponds. It is important that, for a given
 * handle, there be at most one outstanding solicited decision per
 * request. This essentially means that the PEP SHOULD NOT issue more
 * than one REQ (for a given handle) before it receives a corresponding
 * DEC with the solicited message flag set. The PDP MUST always issue
 * decisions for requests on a particular handle in the order they
 * arrive and all requests MUST have a corresponding decision.
 *
 * To avoid deadlock, the PEP can always timeout after issuing a request
 * that does not receive a decision. It MUST then delete the timed-out
 * handle, and may try again using a new handle.
 *
 * The format of the Decision message is as follows:
 *
 * <Decision Message> ::= <Common Header>
 * <Client Handle>
 * <Decision(s)> | <Error>
 * [<Integrity>]
 *
 * <Decision(s)> ::= <Decision> | <Decision(s)> <Decision>
 *
 * <Decision> ::= <Context>
 * <Decision: Flags>
 * [<Decision: Stateless Data>]
 * [<Decision: Replacement Data>]
 * [<Decision: ClientSI Data>]
 * [<Decision: Named Data>]
 *
 * The Decision message may include either an Error object or one or
 * more context plus associated decision objects. COPS protocol problems
 * are reported in the Error object (e.g. an error with the format of
 * the original request including malformed request messages, unknown
 * COPS objects in the Request, etc.). The applicable Decision object(s)
 * depend on the context and the type of client. The only ordering
 * requirement for decision objects is that the required Decision Flags
 * object type MUST precede the other Decision object types per context
 * binding.
 */
public class COPSDecisionMsg extends COPSMsg {

    // Required
    private final COPSHandle _clientHandle;

    // Optional
    private final COPSError _error;
    private final Map<COPSContext, Set<COPSDecision>> _decisions;
    private final COPSIntegrity _integrity;

    /**
     * Constructor for Decision messages containing a COPS Error.
     * As this has been deprecated, the constructor containing the version and Flag should be used going forward.
     * @param clientType - the client type (required)
     * @param clientHandle - the handle (required)
     * @param error - the error (required)
     * @param integrity - the integrity (optional)
     */
    @Deprecated
    public COPSDecisionMsg(final short clientType, final COPSHandle clientHandle,
                           final COPSError error, final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.DEC, clientType), clientHandle, error, null, integrity);
    }

    /**
     * Constructor for Decision messages containing a COPS Error.
     * @param clientType - the client type (required)
     * @param clientHandle - the handle (required)
     * @param error - the error (required)
     * @param integrity - the integrity (optional)
     */
    public COPSDecisionMsg(final int version, final Flag flag, final short clientType, final COPSHandle clientHandle,
                           final COPSError error, final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.DEC, clientType), clientHandle, error, null, integrity);
    }

    /**
     * Constructor for Decision messages containing decisions
     * As this has been deprecated, the constructor containing the version and Flag should be used going forward.
     * @param clientType - the client type (required)
     * @param clientHandle - the handle (required)
     * @param decisions - the decisions (required)
     * @param integrity - the integrity (optional)
     */
    @Deprecated
    public COPSDecisionMsg(final short clientType, final COPSHandle clientHandle,
                           final Map<COPSContext, Set<COPSDecision>> decisions, final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.DEC, clientType), clientHandle, null, decisions, integrity);
    }

    /**
     * Constructor for Decision messages containing decisions
     * @param clientType - the client type (required)
     * @param clientHandle - the handle (required)
     * @param decisions - the decisions (required)
     * @param integrity - the integrity (optional)
     */
    public COPSDecisionMsg(final int version, final Flag flag, final short clientType, final COPSHandle clientHandle,
                           final Map<COPSContext, Set<COPSDecision>> decisions, final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.DEC, clientType), clientHandle, null, decisions, integrity);
    }

    /**
     * Constructor generally designed for Decision messages being parsed from a byte array.
     * @param hdr - the header
     * @param clientHandle - the handle
     * @param error - the error (if null, decisions must not be null or empty)
     * @param decisions - the decisions (must be empty or null if error is not)
     * @param integrity - the integrity (optional)
     */
    protected COPSDecisionMsg(final COPSHeader hdr, final COPSHandle clientHandle,
                           final COPSError error, final Map<COPSContext, Set<COPSDecision>> decisions,
                           final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.DEC))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.DEC);
        if (clientHandle == null) throw new IllegalArgumentException("Client handle must not be null");
        if (error == null && (decisions == null || decisions.isEmpty()))
            throw new IllegalArgumentException("Must contain either an COPSError or at least one decision");
        if (error != null && (decisions != null && !decisions.isEmpty()))
            throw new IllegalArgumentException("Must not contain a COPSError and decisions");

        if(decisions == null) _decisions = Collections.unmodifiableMap(new HashMap<COPSContext, Set<COPSDecision>>());
        else _decisions = Collections.unmodifiableMap(decisions);

        for (Set<COPSDecision> decSet: _decisions.values()) {
            if (decSet == null || decSet.isEmpty())
                throw new IllegalArgumentException("Decisions are empty");
        }

        _clientHandle = clientHandle;
        _error = error;
        _integrity = integrity;

    }

    // Getters
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }
    public COPSError getError() {
        return _error;
    }
    public Map<COPSContext, Set<COPSDecision>> getDecisions() {
        return _decisions;
    }
    public COPSIntegrity getIntegrity() {
        return _integrity;
    }

    @Override
    protected int getDataLength() {
        int out = 0;
        out += _clientHandle.getDataLength() + _clientHandle.getHeader().getHdrLength();
        if (_error != null) out += _error.getDataLength() + _error.getHeader().getHdrLength();

        for (final Map.Entry<COPSContext, Set<COPSDecision>> entry : _decisions.entrySet()) {
            out += entry.getKey().getDataLength() + entry.getKey().getHeader().getHdrLength();
            for (final COPSDecision decision : entry.getValue()) {
                out += decision.getDataLength() + decision.getHeader().getHdrLength();
            }
        }

        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();

        return out;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _clientHandle.writeData(socket);
        if (_error != null) _error.writeData(socket);

        //Display decisions
        //Display any local decisions
        for (final Map.Entry<COPSContext, Set<COPSDecision>> entry : _decisions.entrySet()) {
            entry.getKey().writeData(socket);
            for (final COPSDecision decision : entry.getValue()) {
                decision.writeData(socket);
            }
        }

        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        if (_clientHandle != null)
            _clientHandle.dump(os);
        if (_error != null)
            _error.dump(os);

        //Display any local decisions
        for (final Map.Entry<COPSContext, Set<COPSDecision>> entry : _decisions.entrySet()) {
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSDecisionMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSDecisionMsg that = (COPSDecisionMsg) o;

        for (final Map.Entry<COPSContext, Set<COPSDecision>> entry : this._decisions.entrySet()) {
            final Set<COPSDecision> thatDecisions = that._decisions.get(entry.getKey());
            if (thatDecisions == null) return false;

            for (final COPSDecision thisDecision : entry.getValue()) {
                boolean found = false;
                for (final COPSDecision thatDecision: thatDecisions) {
                    if (thisDecision.equals(thatDecision)) {
                        found = true;
                        break;
                    }
                }
                if (! found) return false;
            }
        }

        return _clientHandle.equals(that._clientHandle) &&
                !(_error != null ? !_error.equals(that._error) : that._error != null) &&
                !(_integrity != null ? !_integrity.equals(that._integrity) : that._integrity != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _clientHandle.hashCode();
        result = 31 * result + (_error != null ? _error.hashCode() : 0);
        result = 31 * result + _decisions.hashCode();
        result = 31 * result + (_integrity != null ? _integrity.hashCode() : 0);
        return result;
    }

    /**
     * Responsible for parsing a byte array to create a COPSDecisionMsg object
     * @param hdrData - the object's header data
     * @param data - the byte array to parse
     * @return - the message object
     * @throws COPSException
     */
    public static COPSDecisionMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSHandle clientHandle = null;
        COPSContext context = null;
        COPSError error = null;
        COPSIntegrity integrity = null;
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();

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
                    if (context == null) {
                        context = COPSContext.parse(objHdrData, buf);
                    } else context = COPSContext.parse(objHdrData, buf);
                    break;
                case ERROR:
                    error = COPSError.parse(objHdrData, buf);
                    break;
                case DEC:
                    if (decisionMap.get(context) != null)
                        decisionMap.get(context).add(COPSDecision.parse(objHdrData, buf));
                    else {
                        final Set<COPSDecision> decisions = new HashSet<>();
                        decisions.add(COPSDecision.parse(objHdrData, buf));
                        decisionMap.put(context, decisions);
                    }
                    break;
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSDecisionMsg(hdrData.header, clientHandle, error, decisionMap, integrity);
    }

}