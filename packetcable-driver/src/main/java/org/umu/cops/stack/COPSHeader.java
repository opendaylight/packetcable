/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COPS Header (RFC 2748 pag. 6)
 *
 *   Each COPS message consists of the COPS header followed by a number of
 *   typed objects.
 *
 *             0              1              2              3
 *     +--------------+--------------+--------------+--------------+
 *     |Version| Flags|    Op Code   |       Client-type           |
 *     +--------------+--------------+--------------+--------------+
 *     |                      Message Length                       |
 *     +--------------+--------------+--------------+--------------+
 *
 *     Global note: //// implies field is reserved, set to 0.
 *
 *       The fields in the header are:
 *         Version: 4 bits
 *             COPS version number. Current version is 1.
 *
 *         Flags: 4 bits
 *             Defined flag values (all other flags MUST be set to 0):
 *               0x1 Solicited Message Flag Bit
 *                This flag is set when the message is solicited by
 *                another COPS message. This flag is NOT to be set
 *                (value=0) unless otherwise specified.
 *
 *         Op Code: 8 bits
 *            The COPS operations:
 *              1 = Request                 (REQ)
 *              2 = Decision                (DEC)
 *              3 = Report State            (RPT)
 *              4 = Delete Request State    (DRQ)
 *              5 = Synchronize State Req   (SSQ)
 *              6 = Client-Open             (OPN)
 *              7 = Client-Accept           (CAT)
 *              8 = Client-Close            (CC)
 *              9 = Keep-Alive              (KA)
 *              10= Synchronize Complete    (SSC)
 *
 *       Client-type: 16 bits
 *
 *
 * @version COPSHeader.java, v 1.00 2003
 *
 */
public class COPSHeader {

    /**
     * Map allowing for the quick retrieval of the operation based on the numeric value coming in via the
     * COPS payload.
     */
    final static Map<Integer, OPCode> VAL_TO_OP = new ConcurrentHashMap<>();
    static {
        VAL_TO_OP.put(OPCode.NA.ordinal(), OPCode.NA);
        VAL_TO_OP.put(OPCode.REQ.ordinal(), OPCode.REQ);
        VAL_TO_OP.put(OPCode.DEC.ordinal(), OPCode.DEC);
        VAL_TO_OP.put(OPCode.RPT.ordinal(), OPCode.RPT);
        VAL_TO_OP.put(OPCode.DRQ.ordinal(), OPCode.DRQ);
        VAL_TO_OP.put(OPCode.SSQ.ordinal(), OPCode.SSQ);
        VAL_TO_OP.put(OPCode.OPN.ordinal(), OPCode.OPN);
        VAL_TO_OP.put(OPCode.CAT.ordinal(), OPCode.CAT);
        VAL_TO_OP.put(OPCode.CC.ordinal(), OPCode.CC);
        VAL_TO_OP.put(OPCode.KA.ordinal(), OPCode.KA);
        VAL_TO_OP.put(OPCode.SSC.ordinal(), OPCode.SSC);
    }

    /**
     * Represents the PCMM version number of the message
     * Holds the first nibble of the COPS message
     */
    private final int _pcmmVersion;

    /**
     * Represents the second nibble of the message where solicited decisions will be set to 1 else 0
     * Values 0 = UNSOLICITED | 1 = SOLICITED
     */
    private final Flag _flag;

    /**
     * Represents the type of operation which will be used to determine the type of COPSMsg this header will be a
     * part of.
     * Uses the byte value contained in the second byte of the message and inbound messages should use the constant
     * Map VAL_TO_CT during construction
     */
    private final OPCode _opCode;

    /**
     * Represents client type which there are currently 3 types supported.
     * Uses the 3rd byte of the message and inbound messages should use the constant Map VAL_TO_OP during construction
     */
    private final short _cType;

    /**
     * Easy constructor that implies version 1 and UNSOLICITED flag.
     *
     * User should leverage the main constructor below and set the version and flags.
     *
     * @param opCode - the Operation code denoting the type of message
     * @param clientType - the client type generally denotes if it is an Ipv4 (TYPE_1) else Ipv6
     * @throws java.lang.IllegalArgumentException
     */
    @Deprecated
    public COPSHeader(final OPCode opCode, final short clientType) {
        this(1, Flag.UNSOLICITED, opCode, clientType);
    }

    /**
     * Should be the main constructor.
     * @param version - PCMM Version
     * @param flag - the header flag
     * @param opCode - the COPS operation code
     * @param clientType - the type of client interfacing
     * @throws java.lang.IllegalArgumentException
     */
    public COPSHeader(final int version, final Flag flag, final OPCode opCode, final short clientType) {
        if(version < 1) throw new IllegalArgumentException("Invalid version number - " + version);
        if(flag == null) throw new IllegalArgumentException("Flag is null");
        if(opCode == null) throw new IllegalArgumentException("OPCode is null");
        _pcmmVersion = version;
        _flag = flag;
        _opCode = opCode;
        _cType = clientType;

        // TODO - Determine why this??? - remove until this makes some sense
//        if (opCode.equals(OPCode.KA)) _cType = ClientType.NA;
//        else _cType = clientType;
    }

    // Getters
    public int getPcmmVersion() { return _pcmmVersion; }
    public Flag getFlag() { return _flag; }

    /**
     * Get header length
     * @return   an int
     */
    public int getHdrLength() {
        return 8;
    }

    /**
     * Get Operation Code
     * @return   a byte
     */
    public OPCode getOpCode() {
        return _opCode;
    }

    /**
     * Get client-type
     * @return   a short
     */
    public short getClientType() {
        return _cType;
    }

    /**
     * Writes object to given network socket in network byte order
     * @param    socket                  a  Socket
     * @throws   IOException
     */
    public void writeData(final Socket socket, final int msgLength) throws IOException {
        byte buf[] = new byte[8];
        buf[0] = (byte) COPSMsgParser.combineNibbles((byte)_pcmmVersion, (byte) _flag.ordinal());
        buf[1] = (byte) _opCode.ordinal();

        final byte[] cTypeBytes = COPSMsgParser.shortToBytes(_cType);
        buf[2] = cTypeBytes[0];
        buf[3] = cTypeBytes[1];
        buf[4] = (byte) (msgLength >> 24);
        buf[5] = (byte) (msgLength >> 16);
        buf[6] = (byte) (msgLength >> 8);
        buf[7] = (byte) msgLength;
        COPSUtil.writeData(socket, buf, 8);
    }

    @Override
    public String toString() {
        return "**MSG HEADER** \n"
                + "Version: " + _pcmmVersion + "\n"
                + "Flags: " + _flag + "\n"
                + "OpCode: " + _opCode + "\n"
                + "Client-type: " + _cType + "\n";
    }

    /**
     * Write an object textual description in the output stream
     * @param    os                  an OutputStream
     * @throws   IOException
     */
    public void dump(OutputStream os) throws IOException {
        os.write(("**MSG HEADER**" + "\n").getBytes());
        os.write(("Version: " + _pcmmVersion + "\n").getBytes());
        os.write(("Flags: " + _flag + "\n").getBytes());
        os.write(("OpCode: " + _opCode + "\n").getBytes());
        os.write(("Client-type: " + _cType + "\n").getBytes());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSHeader)) {
            return false;
        }

        final COPSHeader header = (COPSHeader) o;

        return _pcmmVersion == header._pcmmVersion && _cType == header._cType && _flag == header._flag &&
                _opCode == header._opCode;

    }

    @Override
    public int hashCode() {
        int result = _pcmmVersion;
        result = 31 * result + _flag.hashCode();
        result = 31 * result + _opCode.hashCode();
        result = 31 * result + _cType;
        return result;
    }

    /**
     * Represents the COPS Operation code and byte value corresponds to the item's ordinal value
     *            The COPS operations:
     *              0 = N/A - placeholder for the invalid value of 0
     *              1 = Request                 (REQ)
     *              2 = Decision                (DEC)
     *              3 = Report State            (RPT)
     *              4 = Delete Request State    (DRQ)
     *              5 = Synchronize State Req   (SSQ)
     *              6 = Client-Open             (OPN)
     *              7 = Client-Accept           (CAT)
     *              8 = Client-Close            (CC)
     *              9 = Keep-Alive              (KA)
     *              10= Synchronize Complete    (SSC)
     */
    public enum OPCode {
        NA, REQ, DEC, RPT, DRQ, SSQ, OPN, CAT, CC, KA, SSC
    }

    /**
     * Represents the COPS flags value where the inbound nibble value maps to the ordinal values.
     */
    public enum Flag {
        UNSOLICITED, SOLICITED
    }

}




