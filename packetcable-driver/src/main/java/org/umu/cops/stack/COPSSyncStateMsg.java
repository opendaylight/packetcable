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
 * COPS Sync State Message (RFC 2748 pg. 26 and pg. 29
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

    // Optional
    private final COPSHandle  _clientHandle;
    private final COPSIntegrity  _integrity;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message (required)
     * @param handle - the handle (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSSyncStateMsg(final ClientType clientType, final COPSHandle handle, final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.SSQ, clientType), handle, integrity);
    }

    /**
     * Constructor (generally used for sending messages).
     * @param version - the supported PCMM Version
     * @param flag - the flag...
     * @param clientType - the type of client that created the message (required)
     * @param handle - the handle (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSSyncStateMsg(final int version, final Flag flag, final ClientType clientType, final COPSHandle handle,
                            final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.SSQ, clientType), handle, integrity);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header
     * @param handle - the handle (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSSyncStateMsg(final COPSHeader hdr, final COPSHandle handle, final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.SSQ))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.SSQ);
        _clientHandle = handle;
        _integrity = integrity;
    }

    // Getters
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    @Override
    protected int getDataLength() {
        int out = 0;
        if (_clientHandle != null) out += _clientHandle.getDataLength() + _clientHandle.getHeader().getHdrLength();
        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        return out;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        if (_clientHandle != null) _clientHandle.writeData(socket);
        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        if (_clientHandle != null) _clientHandle.dump(os);
        if (_integrity != null) _integrity.dump(os);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSSyncStateMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSSyncStateMsg that = (COPSSyncStateMsg) o;

        return !(_clientHandle != null ? !_clientHandle.equals(that._clientHandle) : that._clientHandle != null) &&
                !(_integrity != null ? !_integrity.equals(that._integrity) : that._integrity != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_clientHandle != null ? _clientHandle.hashCode() : 0);
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
    public static COPSSyncStateMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSHandle clientHandle = null;
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
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSSyncStateMsg(hdrData.header, clientHandle, integrity);
    }

}



