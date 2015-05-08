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
    // Required
    private final COPSHandle _clientHandle;
    private final COPSReportType _report;
    private final COPSIntegrity _integrity;

    // TODO - Determine why previous implementation had a collection of Client SIs when the specification reads
    // that there may be only one. May need to revert back to a Set<COPSClientSI>
    // Optional
    private final COPSClientSI _clientSI;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message (required)
     * @param clientHandle - the COPS Handle (required)
     * @param report - the report (required)
     * @param clientSI - the client SI (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSReportMsg(final ClientType clientType, final COPSHandle clientHandle, final COPSReportType report,
                         final COPSClientSI clientSI, final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.RPT, clientType), clientHandle, report, clientSI, integrity);
    }

    /**
     * Constructor (generally used for sending messages).
     * @param version - the supported PCMM Version
     * @param flag - the flag...
     * @param clientType - the type of client that created the message (required)
     * @param clientHandle - the COPS Handle (required)
     * @param report - the report (required)
     * @param clientSI - the client SI (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSReportMsg(final int version, final Flag flag, final ClientType clientType, final COPSHandle clientHandle, final COPSReportType report,
                         final COPSClientSI clientSI, final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.RPT, clientType), clientHandle, report, clientSI, integrity);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header
     * @param clientHandle - the COPS Handle (required)
     * @param report - the report (required)
     * @param clientSI - the client SI (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSReportMsg(final COPSHeader hdr, final COPSHandle clientHandle, final COPSReportType report,
                         final COPSClientSI clientSI, final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.RPT))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.RPT);
        if (clientHandle == null) throw new IllegalArgumentException("COPSHandle must not be null");
        if (report == null) throw new IllegalArgumentException("COPSReportType must not be null");

        _clientHandle = clientHandle;
        _report = report;
        _clientSI = clientSI;
        _integrity = integrity;
    }

    // Getters
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }
    public COPSReportType getReport() {
        return _report;
    }
    public COPSClientSI getClientSI() {
        return _clientSI;
    }
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    @Override
    protected int getDataLength() {
        int out = _clientHandle.getDataLength() + _clientHandle.getHeader().getHdrLength();
        out += _report.getDataLength() + _report.getHeader().getHdrLength();
        if (_clientSI != null) out += _clientSI.getDataLength() + _clientSI.getHeader().getHdrLength();
        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        return out;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _clientHandle.writeData(socket);
        _report.writeData(socket);
        if (_clientSI != null) _clientSI.writeData(socket);
        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        _clientHandle.dump(os);
        _report.dump(os);
        if (_clientSI != null) _clientSI.dump(os);
        if (_integrity != null) _integrity.dump(os);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSReportMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSReportMsg that = (COPSReportMsg) o;

        return _clientHandle.equals(that._clientHandle) &&
                !(_clientSI != null ? !_clientSI.equals(that._clientSI) : that._clientSI != null) &&
                !(_integrity != null ? !_integrity.equals(that._integrity) : that._integrity != null) &&
                _report.equals(that._report);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _clientHandle.hashCode();
        result = 31 * result + _report.hashCode();
        result = 31 * result + (_integrity != null ? _integrity.hashCode() : 0);
        result = 31 * result + (_clientSI != null ? _clientSI.hashCode() : 0);
        return result;
    }

    /**
     * Responsible for parsing a byte array to create a COPSDecisionMsg object
     * @param hdrData - the object's header data
     * @param data - the byte array to parse
     * @return - the message object
     * @throws COPSException
     */
    public static COPSReportMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSHandle clientHandle = null;
        COPSReportType report = null;
        COPSIntegrity integrity = null;
        COPSClientSI clientSI = null;

        int dataStart = 0;
        while (dataStart < data.length) {
            final byte[] buf = new byte[data.length - dataStart];
            System.arraycopy(data, dataStart, buf, 0, data.length - dataStart);

            final COPSObjHeaderData objHdrData = COPSObjectParser.parseObjHeader(buf);
            switch (objHdrData.header.getCNum()) {
                case HANDLE:
                    clientHandle = COPSHandle.parse(objHdrData, buf);
                    break;
                case RPT:
                    report = COPSReportType.parse(objHdrData, buf);
                    break;
                case CSI:
                    clientSI = COPSClientSI.parse(objHdrData, buf);
                    break;
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSReportMsg(hdrData.header, clientHandle, report, clientSI, integrity);
    }
}



