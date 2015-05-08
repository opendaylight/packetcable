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
 * COPS Client Close Message (RFC 2748 pg. 27)
 *
 * The Client-Close message can be issued by either the PDP or PEP to
 * notify the other that a particular type of client is no longer being
 * supported.
 *
 * <Client-Close>  ::= <Common Header>
 * <Error>
 * [<PDPRedirAddr>]
 * [<Integrity>]
 *
 * The Error object is included to describe the reason for the close
 * (e.g. the requested client-type is not supported by the remote PDP or
 * client failure).
 *
 * A PDP MAY optionally include a PDP Redirect Address object in order
 * to inform the PEP of the alternate PDP it SHOULD use for the client-
 * type specified in the common header.
 */
public class COPSClientCloseMsg extends COPSMsg {

    // Required
    private final COPSError _error;

    // Optional
    private final COPSPdpAddress _redirAddr;
    private final COPSIntegrity _integrity;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message (required)
     * @param error - the error (required)
     * @param redirAddr - the redirect address (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSClientCloseMsg(final ClientType clientType, final COPSError error, final COPSPdpAddress redirAddr,
                              final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.CC, clientType), error, redirAddr, integrity);
    }

    /**
     * Constructor (generally used for sending messages).
     * @param version - the supported PCMM Version
     * @param flag - the flag...
     * @param clientType - the type of client that created the message (required)
     * @param error - the error (required)
     * @param redirAddr - the redirect address (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSClientCloseMsg(final int version, final Flag flag, final ClientType clientType, final COPSError error,
                              final COPSPdpAddress redirAddr, final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.CC, clientType), error, redirAddr, integrity);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header
     * @param error - the error (required)
     * @param redirAddr - the redirect address (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSClientCloseMsg(final COPSHeader hdr, final COPSError error, final COPSPdpAddress redirAddr,
                              final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.CC))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.CAT);
        if (error == null) throw new IllegalArgumentException("Error object must not be null");
        this._error = error;
        this._redirAddr = redirAddr;
        this._integrity = integrity;
    }

    // Getters
    public COPSError getError() {
        return (_error);
    }
    public COPSPdpAddress getRedirAddr() {
        return (_redirAddr);
    }
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _error.writeData(socket);
        if (_redirAddr != null) _redirAddr.writeData(socket);
        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected int getDataLength() {
        int out = _error.getDataLength() + _error.getHeader().getHdrLength();
        if (_redirAddr != null) out += _redirAddr.getDataLength() + _redirAddr.getHeader().getHdrLength();
        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        return out;
    }

    @Override
    protected void dumpBody(OutputStream os) throws IOException {
        _error.dump(os);
        if (_redirAddr != null) _redirAddr.dump(os);
        if (_integrity != null) _integrity.dump(os);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSClientCloseMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSClientCloseMsg closeMsg = (COPSClientCloseMsg) o;

        return _error.equals(closeMsg._error) &&
                !(_integrity != null ? !_integrity.equals(closeMsg._integrity) : closeMsg._integrity != null) &&
                !(_redirAddr != null ? !_redirAddr.equals(closeMsg._redirAddr) : closeMsg._redirAddr != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _error.hashCode();
        result = 31 * result + (_redirAddr != null ? _redirAddr.hashCode() : 0);
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
    public static COPSClientCloseMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSError error = null;
        COPSPdpAddress redirAddr = null;
        COPSIntegrity integrity = null;

        int dataStart = 0;
        while (dataStart < data.length) {
            final byte[] buf = new byte[data.length - dataStart];
            System.arraycopy(data, dataStart, buf, 0, data.length - dataStart);

            final COPSObjHeaderData objHdrData = COPSObjectParser.parseObjHeader(buf);
            switch (objHdrData.header.getCNum()) {
                case ERROR:
                    error = COPSError.parse(objHdrData, buf);
                    break;
                case PDP_REDIR:
                    redirAddr = COPSPdpAddress.parse(objHdrData, buf);
                    break;
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSClientCloseMsg(hdrData.header, error, redirAddr, integrity);
    }

}

