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
 * COPS Keep Alive Message (RFC 2748 pg. 27)
 *
 * The keep-alive message MUST be transmitted by the PEP within the
 * period defined by the minimum of all KA Timer values specified in all
 * received CAT messages for the connection. A KA message MUST be
 * generated randomly between 1/4 and 3/4 of this minimum KA timer
 * interval. When the PDP receives a keep-alive message from a PEP, it
 * MUST echo a keep-alive back to the PEP. This message provides
 * validation for each side that the connection is still functioning
 * even when there is no other messaging.
 *
 * Note: The client-type in the header MUST always be set to 0 as the KA
 * is used for connection verification (not per client session
 * verification).
 *
 * <Keep-Alive>  ::= <Common Header>
 * [<Integrity>]
 * Both client and server MAY assume the TCP connection is insufficient
 * for the client-type with the minimum time value (specified in the CAT
 * message) if no communication activity is detected for a period
 * exceeding the timer period. For the PEP, such detection implies the
 * remote PDP or connection is down and the PEP SHOULD now attempt to
 * use an alternative/backup PDP.
 */
public class COPSKAMsg extends COPSMsg {

    // Optional
    private final COPSIntegrity  _integrity;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message (required)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSKAMsg(final ClientType clientType, final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.KA, clientType), integrity);
    }

    /**
     * Constructor (generally used for sending messages).
     * @param version - the supported PCMM Version
     * @param flag - the flag...
     * @param clientType - the type of client that created the message (required)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSKAMsg(final int version, final Flag flag, final ClientType clientType, final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.KA, clientType), integrity);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSKAMsg(final COPSHeader hdr, final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.KA))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.KA);
        _integrity = integrity;
    }

    // Getter
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected int getDataLength() {
        if (_integrity != null) return _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        else return 0;
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        if (_integrity != null) _integrity.dump(os);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSKAMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSKAMsg copskaMsg = (COPSKAMsg) o;

        return !(_integrity != null ? !_integrity.equals(copskaMsg._integrity) : copskaMsg._integrity != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
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
    public static COPSKAMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSIntegrity integrity = null;

        int dataStart = 0;
        while (dataStart < data.length) {
            final byte[] buf = new byte[data.length - dataStart];
            System.arraycopy(data, dataStart, buf, 0, data.length - dataStart);

            final COPSObjHeaderData objHdrData = COPSObjectParser.parseObjHeader(buf);
            switch (objHdrData.header.getCNum()) {
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSKAMsg(hdrData.header, integrity);
    }

}






