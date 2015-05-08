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
 * COPS Client Open Message (RFC 2748 page. 26)
 *
 * The Client-Open message can be used by the PEP to specify to the PDP
 * the client-types the PEP can support, the last PDP to which the PEP
 * connected for the given client-type, and/or client specific feature
 * negotiation. A Client-Open message can be sent to the PDP at any time
 * and multiple Client-Open messages for the same client-type are
 * allowed (in case of global state changes).
 *
 * <Client-Open>  ::= <Common Header>
 * <PEPID>
 * [<ClientSI>]
 * [<LastPDPAddr>]
 * [<Integrity>]
 *
 * The PEPID is a symbolic, variable length name that uniquely
 * identifies the specific client to the PDP (see Section 2.2.11).
 *
 * A named ClientSI object can be included for relaying additional
 * global information about the PEP to the PDP when required (as
 * specified in the appropriate extensions document for the client-
 * type).
 *
 * The PEP may also provide a Last PDP Address object in its Client-Open
 * message specifying the last PDP (for the given client-type) for which
 * it is still caching decisions since its last reboot. A PDP can use
 * this information to determine the appropriate synchronization
 * behavior (See section 2.5).
 *
 * If the PDP receives a malformed Client-Open message it MUST generate
 * a Client-Close message specifying the appropriate error code.
 */
public class COPSClientOpenMsg extends COPSMsg {

    private final COPSPepId _pepId;
    private final COPSClientSI _clientSI;
    private final COPSPdpAddress _pdpAddress;
    private final COPSIntegrity _integrity;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message (required)
     * @param pepId - the PEP ID (required)
     * @param clientSI - the COPS Client SI (optional)
     * @param pdpAddress - the COPS PDP Address (optional)
     * @param integrity - the COPS Integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSClientOpenMsg(final ClientType clientType, final COPSPepId pepId, final COPSClientSI clientSI,
                             final COPSPdpAddress pdpAddress, final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.OPN, clientType), pepId, clientSI, pdpAddress, integrity);
    }

    /**
     * Recommended constructor generally for use by a client sending messages.
     * @param version - the supported PCMM Version (required)
     * @param flag - the flag...  (required)
     * @param clientType - the type of client that created the message (required)
     * @param pepId - the PEP ID (required)
     * @param clientSI - the COPS Client SI (optional)
     * @param pdpAddress - the COPS PDP Address (optional)
     * @param integrity - the COPS Integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSClientOpenMsg(final int version, final Flag flag, final ClientType clientType, final COPSPepId pepId,
                             final COPSClientSI clientSI, final COPSPdpAddress pdpAddress,
                             final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.OPN, clientType), pepId, clientSI, pdpAddress, integrity);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header (required)
     * @param pepId - the PEP ID (required)
     * @param clientSI - the COPS Client SI (optional)
     * @param pdpAddress - the COPS PDP Address (optional)
     * @param integrity - the COPS Integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSClientOpenMsg(final COPSHeader hdr, final COPSPepId pepId, final COPSClientSI clientSI,
                             final COPSPdpAddress pdpAddress, final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.OPN))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.OPN);
        if (pepId == null) throw new IllegalArgumentException("Pep ID must not be null");
        // TODO - considering adding some validation on the PDP Address and the client type
        this._pepId = pepId;
        this._clientSI = clientSI;
        this._pdpAddress = pdpAddress;
        this._integrity = integrity;
    }

    // Getters
    public COPSPepId getPepId() {
        return _pepId;
    }
    public COPSClientSI getClientSI() {
        return (_clientSI);
    }
    public COPSPdpAddress getPdpAddress() {
        return _pdpAddress;
    }
    public COPSIntegrity getIntegrity() {
        return _integrity;
    }

    /**
     * Method writeData
     * @param    socket                  a  Socket
     * @throws   IOException
     */
    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _pepId.writeData(socket);
        if (_clientSI != null) _clientSI.writeData(socket);
        if (_pdpAddress != null) _pdpAddress.writeData(socket);
        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected int getDataLength() {
        int out = _pepId.getDataLength() + _pepId.getHeader().getHdrLength();
        if (_clientSI != null) out += _clientSI.getDataLength() + _clientSI.getHeader().getHdrLength();
        if (_pdpAddress != null) out += _pdpAddress.getDataLength() + _pdpAddress.getHeader().getHdrLength();
        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        return out;
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        _pepId.dump(os);
        if (_clientSI != null) _clientSI.dump(os);
        if (_pdpAddress != null) _pdpAddress.dump(os);
        if (_integrity != null)  _integrity.dump(os);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSClientOpenMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSClientOpenMsg that = (COPSClientOpenMsg) o;

        return !(_clientSI != null ? !_clientSI.equals(that._clientSI) : that._clientSI != null) &&
                !(_integrity != null ? !_integrity.equals(that._integrity) : that._integrity != null) &&
                !(_pdpAddress != null ? !_pdpAddress.equals(that._pdpAddress) : that._pdpAddress != null) &&
                _pepId.equals(that._pepId);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _pepId.hashCode();
        result = 31 * result + (_clientSI != null ? _clientSI.hashCode() : 0);
        result = 31 * result + (_pdpAddress != null ? _pdpAddress.hashCode() : 0);
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
    public static COPSClientOpenMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSPepId pepId = null;
        COPSClientSI clientSI = null;
        COPSPdpAddress pdpAddress = null;
        COPSIntegrity integrity = null;

        int dataStart = 0;
        while (dataStart < data.length) {
            final byte[] buf = new byte[data.length - dataStart];
            System.arraycopy(data, dataStart, buf, 0, data.length - dataStart);

            final COPSObjHeaderData objHdrData = COPSObjectParser.parseObjHeader(buf);
            switch (objHdrData.header.getCNum()) {
                case PEPID:
                    pepId = COPSPepId.parse(objHdrData, buf);
                    break;
                case CSI:
                    clientSI = COPSClientSI.parse(objHdrData, buf);
                    break;
                case LAST_PDP_ADDR:
                    pdpAddress = COPSPdpAddress.parse(objHdrData, buf);
                    break;
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSClientOpenMsg(hdrData.header, pepId, clientSI, pdpAddress, integrity);
    }

}



