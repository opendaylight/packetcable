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
 * Client-Accept (CAT)  PDP -> PEP (RFC 2748 pag. 26)
 *
 * The Client-Accept message is used to positively respond to the
 * Client-Open message. This message will return to the PEP a timer
 * object indicating the maximum time interval between keep-alive
 * messages. Optionally, a timer specifying the minimum allowed interval
 * between accounting report messages may be included when applicable.
 *
 * <Client-Accept>  ::= <Common Header>
 * <KA Timer>
 * [<ACCT Timer>]
 * [<Integrity>]
 *
 * If the PDP refuses the client, it will instead issue a Client-Close
 * message.
 *
 * The KA Timer corresponds to maximum acceptable intermediate time
 * between the generation of messages by the PDP and PEP. The timer
 * value is determined by the PDP and is specified in seconds. A timer
 * value of 0 implies no secondary connection verification is necessary.
 *
 * The optional ACCT Timer allows the PDP to indicate to the PEP that
 * periodic accounting reports SHOULD NOT exceed the specified timer
 * interval per client handle. This allows the PDP to control the rate
 * at which accounting reports are sent by the PEP (when applicable).
 *
 * In general, accounting type Report messages are sent to the PDP when
 * determined appropriate by the PEP. The accounting timer merely is
 * used by the PDP to keep the rate of such updates in check (i.e.
 * Preventing the PEP from blasting the PDP with accounting reports).
 * Not including this object implies there are no PDP restrictions on
 * the rate at which accounting updates are generated.
 *
 * If the PEP receives a malformed Client-Accept message it MUST
 * generate a Client-Close message specifying the appropriate error
 * code.
 */
public class COPSClientAcceptMsg extends COPSMsg {

    // Required
    private final COPSKATimer _kaTimer;

    // Optional
    private final COPSAcctTimer _acctTimer;
    private final COPSIntegrity _integrity;

    /**
     * Constructor (generally used for sending messages) which probably should not be used as the PCMM version and
     * Flag values on the header are being hardcoded to 1 and UNSOLICITED respectively. Use the next one below instead
     * @param clientType - the type of client that created the message (required)
     * @param kaTimer - the Keep alive timer (required)
     * @param acctTimer - the account timer (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSClientAcceptMsg(final ClientType clientType, final COPSKATimer kaTimer, final COPSAcctTimer acctTimer,
                               final COPSIntegrity integrity) {
        this(new COPSHeader(OPCode.CAT, clientType), kaTimer, acctTimer, integrity);
    }

    /**
     * Constructor (generally used for sending messages).
     * @param version - the supported PCMM Version
     * @param flag - the flag...
     * @param clientType - the type of client that created the message (required)
     * @param kaTimer - the Keep alive timer (required)
     * @param acctTimer - the account timer (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    public COPSClientAcceptMsg(final int version, final Flag flag, final ClientType clientType,
                               final COPSKATimer kaTimer, final COPSAcctTimer acctTimer, final COPSIntegrity integrity) {
        this(new COPSHeader(version, flag, OPCode.CAT, clientType), kaTimer, acctTimer, integrity);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSHeader information is known.
     * @param hdr - COPS Header
     * @param kaTimer - the Keep alive timer (required)
     * @param acctTimer - the account timer (optional)
     * @param integrity - the integrity (optional)
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSClientAcceptMsg(final COPSHeader hdr, final COPSKATimer kaTimer, final COPSAcctTimer acctTimer,
                               final COPSIntegrity integrity) {
        super(hdr);
        if (!hdr.getOpCode().equals(OPCode.CAT))
            throw new IllegalArgumentException("OPCode must be of type - " + OPCode.CAT);
        if (kaTimer == null) throw new IllegalArgumentException("Keep alive timer must not be null");
        _kaTimer = kaTimer;
        _acctTimer = acctTimer;
        _integrity = integrity;
    }

    // Getters
    public COPSKATimer getKATimer() {
        return _kaTimer;
    }
    public COPSAcctTimer getAcctTimer() {
        return (_acctTimer);
    }
    public COPSIntegrity getIntegrity() {
        return (_integrity);
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _kaTimer.writeData(socket);
        if (_acctTimer != null) _acctTimer.writeData(socket);
        if (_integrity != null) _integrity.writeData(socket);
    }

    @Override
    protected int getDataLength() {
        int out = _kaTimer.getDataLength() + _kaTimer.getHeader().getHdrLength();
        if (_acctTimer != null) out += _acctTimer.getDataLength() + _acctTimer.getHeader().getHdrLength();
        if (_integrity != null) out += _integrity.getDataLength() + _integrity.getHeader().getHdrLength();
        return out;
    }
    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        _kaTimer.dump(os);
        if (_acctTimer != null) _acctTimer.dump(os);
        if (_integrity != null)  _integrity.dump(os);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSClientAcceptMsg)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSClientAcceptMsg acceptMsg = (COPSClientAcceptMsg) o;

        return !(_acctTimer != null ? !_acctTimer.equals(acceptMsg._acctTimer) : acceptMsg._acctTimer != null) &&
                !(_integrity != null ? !_integrity.equals(acceptMsg._integrity) : acceptMsg._integrity != null) &&
                _kaTimer.equals(acceptMsg._kaTimer);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _kaTimer.hashCode();
        result = 31 * result + (_acctTimer != null ? _acctTimer.hashCode() : 0);
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
    public static COPSClientAcceptMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        // Variables for constructor
        COPSKATimer kaTimer = null;
        COPSAcctTimer acctTimer = null;
        COPSIntegrity integrity = null;

        int dataStart = 0;
        while (dataStart < data.length) {
            final byte[] buf = new byte[data.length - dataStart];
            System.arraycopy(data, dataStart, buf, 0, data.length - dataStart);

            final COPSObjHeaderData objHdrData = COPSObjectParser.parseObjHeader(buf);
            switch (objHdrData.header.getCNum()) {
                case KA:
                    kaTimer = COPSKATimer.parse(objHdrData, buf);
                    break;
                case ACCT_TIMER:
                    acctTimer = COPSAcctTimer.parse(objHdrData, buf);
                    break;
                case MSG_INTEGRITY:
                    integrity = COPSIntegrity.parse(objHdrData, buf);
                    break;
                default:
                    throw new COPSException("Bad Message format, unknown object type");
            }
            dataStart += objHdrData.msgByteCount;
        }

        return new COPSClientAcceptMsg(hdrData.header, kaTimer, acctTimer, integrity);
    }

}

