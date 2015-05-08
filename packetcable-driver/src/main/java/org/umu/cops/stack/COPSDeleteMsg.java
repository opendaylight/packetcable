/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSHeader.ClientType;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * COPS Delete Message (RFC 2748 pag. 24)
 *
 *    When sent from the PEP this message indicates to the remote PDP that
 *    the state identified by the client handle is no longer
 *    available/relevant. This information will then be used by the remote
 *    PDP to initiate the appropriate housekeeping actions. The reason code
 *    object is interpreted with respect to the client-type and signifies
 *    the reason for the removal.
 *
 *    The format of the Delete Request State message is as follows:
 *
 *               <Delete Request>  ::= <Common Header>
 *                                     <Client Handle>
 *                                     <Reason>
 *                                     [<Integrity>]
 *
 *
 * @version COPSDeleteMsg.java, v 1.00 2003
 *
 */
public class COPSDeleteMsg extends COPSMsg {
    // Required
    private final COPSHandle  _clientHandle;
    private final COPSReason _reason;

    // Optional
    private final COPSIntegrity _integrity;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message (required)
     * @param handle - the COPS Handle (required)
     * @param reason - the reason (required)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSDeleteMsg(final ClientType clientType, final COPSHandle handle, final COPSReason reason,
                         final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.DRQ, clientType), handle, reason, integrity);
    }

    /**
     * Constructor (generally used for sending messages).
     * @param version - the supported PCMM Version
     * @param flag - the flag...
     * @param clientType - the type of client that created the message (required)
     * @param handle - the COPS Handle (required)
     * @param reason - the reason (required)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSDeleteMsg(final int version, final Flag flag, final ClientType clientType, final COPSHandle handle,
                         final COPSReason reason, final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.DRQ, clientType), handle, reason, integrity);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header
     * @param handle - the COPS Handle (required)
     * @param reason - the reason (required)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSDeleteMsg(final COPSHeader hdr, final COPSHandle handle, final COPSReason reason,
                         final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.DRQ))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.DRQ);
        if (handle == null) throw new IllegalArgumentException("COPSHandle must not be null");
        if (reason == null) throw new IllegalArgumentException("COPSReason must not be null");

        _clientHandle = handle;
        _reason = reason;
        _integrity = integrity;
    }

    // Getters
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }
    public COPSReason getReason() {
        return _reason;
    }
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    @Override
    protected int getDataLength() {
        int out = _clientHandle.getDataLength() + _clientHandle.getHeader().getHdrLength();
        out += _reason.getDataLength() + _reason.getHeader().getHdrLength();
        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        return out;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _clientHandle.writeData(socket);
        _reason.writeData(socket);
        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        _clientHandle.dump(os);
        _reason.dump(os);
        if (_integrity != null) {
            _integrity.dump(os);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSDeleteMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSDeleteMsg that = (COPSDeleteMsg) o;

        return _clientHandle.equals(that._clientHandle) &&
                !(_integrity != null ? !_integrity.equals(that._integrity) : that._integrity != null) &&
                _reason.equals(that._reason);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _clientHandle.hashCode();
        result = 31 * result + _reason.hashCode();
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
    public static COPSDeleteMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSHandle clientHandle = null;
        COPSReason reason = null;
        COPSIntegrity integrity = null;

        int dataStart = 0;
        while (dataStart < data.length) {
            final byte[] buf = new byte[data.length - dataStart];
            System.arraycopy(data, dataStart, buf, 0, data.length - dataStart);

            final COPSObjHeaderData objHdrData = COPSObjectParser.parseObjHeader(buf);
            switch (objHdrData.header.getCNum()) {
                case HANDLE:
                    clientHandle = COPSHandle.parse(objHdrData, buf);
                    break;
                case REASON_CODE:
                    reason = COPSReason.parse(objHdrData, buf);
                    break;
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSDeleteMsg(hdrData.header, clientHandle, reason, integrity);
    }
}




