/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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

    public final static byte COPS_OP_REQ = 1;
    public final static byte COPS_OP_DEC = 2;
    public final static byte COPS_OP_RPT = 3;
    public final static byte COPS_OP_DRQ = 4;
    public final static byte COPS_OP_SSQ = 5;
    public final static byte COPS_OP_OPN = 6;
    public final static byte COPS_OP_CAT = 7;
    public final static byte COPS_OP_CC = 8;
    public final static byte COPS_OP_KA = 9;
    public final static byte COPS_OP_SSC = 10;

    public final static byte COPS_FLAG_NULL = 0;
    public final static byte COPS_FLAG_SOLICITED = 1;

    private byte _versionNflg;
    private byte _opCode;
    private short _cType;
    private int _msgLength;

    public COPSHeader() {
        _versionNflg = 0x10;
        _opCode = 0;
        _cType = 0;
        _msgLength = 0;
    }

    public COPSHeader(byte opCode, short clientType) {
        _versionNflg = 0x10;
        _opCode = opCode;
        _cType = clientType;
        _msgLength = 0;
        if (isAKeepAlive()) _cType = 0;
    }

    public COPSHeader(byte opCode) {
        _versionNflg = 0x10;
        _opCode = opCode;
        _cType = 0;
        _msgLength = 0;
        if (isAKeepAlive()) _cType = 0;
    }

    /**
          Parse data and create COPSHeader object
     */
    public COPSHeader(byte[] buf) {
        _versionNflg = (byte) buf[0];
        _opCode = (byte) buf[1];
        _cType |= ((short) buf[2]) << 8;
        _cType |= ((short) buf[3]) & 0xFF;
        _msgLength |= ((short) buf[4]) << 24;
        _msgLength |= ((short) buf[5]) << 16;
        _msgLength |= ((short) buf[6]) << 8;
        _msgLength |= ((short) buf[7]) & 0xFF;
    }

    /**
     * If the operation code corresponds with a message Request, return true
     *
     * @return   a boolean
     *
     */
    public boolean isARequest() {
        return (_opCode == COPS_OP_REQ);
    }

    /**
     * If the operation code corresponds with a message Decision, return true
     *
     * @return   a boolean
     *
     */
    public boolean isADecision() {
        return (_opCode == COPS_OP_DEC);
    }

    /**
     * If the operation code corresponds with a message Report, return true
     *
     * @return   a boolean
     *
     */
    public boolean isAReport() {
        return (_opCode == COPS_OP_RPT);
    }

    /**
     * If the operation code corresponds with a message DeleteRequest, return true
     *
     * @return   a boolean
     *
     */
    public boolean isADeleteReq() {
        return (_opCode == COPS_OP_DRQ);
    }

    /**
     * If the operation code corresponds with a message SyncStateReq, return true
     *
     * @return   a boolean
     *
     */
    public boolean isASyncStateReq() {
        return (_opCode == COPS_OP_SSQ);
    }

    /**
     * If the operation code corresponds with a message ClientOpen, return true
     *
     * @return   a boolean
     *
     */
    public boolean isAClientOpen() {
        return (_opCode == COPS_OP_OPN);
    }

    /**
     * If the operation code corresponds with a message ClientAccept, return true
     *
     * @return   a boolean
     *
     */
    public boolean isAClientAccept() {
        return (_opCode == COPS_OP_CAT);
    }

    /**
     * If operation code corresponds with a message ClientClose, return true
     *
     * @return   a boolean
     *
     */
    public boolean isAClientClose() {
        return (_opCode == COPS_OP_CC);
    }

    /**
     * If the operation code corresponds with a message KeepAlive, return true
     *
     * @return   a boolean
     *
     */
    public boolean isAKeepAlive() {
        return (_opCode == COPS_OP_KA);
    }

    /**
     * If the operation code corresponds with a message SSC, return true
     *
     * @return   a boolean
     *
     */
    public boolean isASyncComplete() {
        return (_opCode == COPS_OP_SSC);
    }

    /**
     * Get message length
     *
     * @return   an int
     *
     */
    public int getMsgLength() {
        return _msgLength;
    }

    /**
     * Get header length
     *
     * @return   an int
     *
     */
    public int getHdrLength() {
        // return (sizeof(u_int32_t) * 2);
        return ( 8 );
    }

    /**
     * Get Operation Code
     *
     * @return   a byte
     *
     */
    public byte getOpCode() {
        return _opCode;
    }

    /**
     * Set the solicitation flag
     *
     * @param    flg                 a  byte
     *
     */
    public void setFlag(byte flg) {
        _versionNflg &= 0x10;
        _versionNflg |= flg;
    }

    /**
     * Returns the flags field
     * @return aByte     Flags field in header
     */
    public byte getFlags() { //OJO
        return (byte) (_versionNflg & 0x0f);
    }

    /**
     * Set the client-type
     *
     * @param    cType               a  short
     *
     */
    public void setClientType(short cType) {
        _cType = cType;
    };

    /**
     * Set the message length
     *
     * @param    len                 an int
     *
     * @throws   COPSException
     *
     */
    public void setMsgLength(int len) throws COPSException {
        if ((len % 4) != 0)
            throw new COPSException ("Message is not aligned on 32 bit intervals");
        _msgLength = len + 8;
    }

    /**
     * Get client-type
     *
     * @return   a short
     *
     */
    public short getClientType() {
        return (_cType);
    };

    /**
     * Always return true
     *
     * @return   a boolean
     *
     */
    public boolean isCOPSHeader() {
        return true;
    };

    /**
     * Writes object to given network socket in network byte order
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        byte buf[] = new byte[8];

        buf[0] = (byte) _versionNflg;
        buf[1] = (byte) _opCode;
        buf[2] = (byte) (_cType >> 8);
        buf[3] = (byte) _cType;
        buf[4] = (byte) (_msgLength >> 24);
        buf[5] = (byte) (_msgLength >> 16);
        buf[6] = (byte) (_msgLength >> 8);
        buf[7] = (byte) _msgLength;

        COPSUtil.writeData(id, buf, 8);
    }

    /**
     * Get an object textual description
     *
     * @return   a String
     *
     */
    public String toString() {
        String str = new String();

        str += "**MSG HEADER** \n";
        str += "Version: " + (_versionNflg >> 4) + "\n";
        str += "Flags: " + (_versionNflg & 0x01) + "\n";
        str += "OpCode: " + _opCode + "\n";
        str += "Client-type: " + _cType + "\n";
        str += "Message-length(bytes): " + _msgLength + "\n";
        return str;
    }

    /**
     * Write an object textual description in the output stream
     *
     * @param    os                  an OutputStream
     *
     * @throws   IOException
     *
     */
    public void dump(OutputStream os) throws IOException {
        os.write(new String("**MSG HEADER**" + "\n").getBytes());
        os.write(new String("Version: " + (_versionNflg >> 4) + "\n").getBytes());
        os.write(new String("Flags: " + (_versionNflg & 0x01) + "\n").getBytes());
        os.write(new String("OpCode: " + _opCode + "\n").getBytes());
        os.write(new String("Client-type: " + _cType + "\n").getBytes());
        os.write(new String("Message-length(bytes): " + _msgLength + "\n").getBytes());
    }
}




