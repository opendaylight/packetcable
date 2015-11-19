/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

/**
 * Utility for parsing COPS messages obtained from a Socket connection
 */

public class COPSMsgParser {

    public final static Logger logger = LoggerFactory.getLogger(COPSMsgParser.class);

    /**
     * Transforms a COPS message streaming in from a Socket connection into a COPSMsg object
     * @param socket - the socket
     * @return - an implementation of the abstract COPSMsg object
     * @throws IOException
     * @throws COPSException
     */
    public static COPSMsg parseMessage(final Socket socket) throws IOException, COPSException {
        final COPSHeaderData hdrData = readHeader(socket);
        return readBody(socket, hdrData);
    }


    /**
     * Unmarshalls the COPS Header data from the bytes received on the Socket
     * @param socket - the socket
     * @return - the header data
     * @throws IOException
     */
    private static COPSHeaderData readHeader(final Socket socket) throws IOException {
        logger.debug("Reading COPS Header");
        final byte[] data = new byte[8];
        final int bytesRead = readData(socket, data, 8);
        if (bytesRead != 8) throw new IOException("Expected 8 bytes, read in " + bytesRead);

        final byte[] vFlagsNibbles = splitByteToNibbles(data[0]);
        byte version = vFlagsNibbles[0];
        final Flag flag;
        if (vFlagsNibbles[1] == 1) flag = Flag.SOLICITED; else flag = Flag.UNSOLICITED;

        final OPCode opCode;
        if (COPSHeader.VAL_TO_OP.get((int)data[1]) == null) opCode = OPCode.NA;
        else opCode = COPSHeader.VAL_TO_OP.get((int)data[1]);

        short cType = bytesToShort(data[2], data[3]);

        int msgLength = ((short) data[4]) << 24;
        msgLength |= ((short) data[5]) << 16;
        msgLength |= ((short) data[6]) << 8;
        msgLength |= ((short) data[7]) & 0xFF;

        return new COPSHeaderData(new COPSHeader(version, flag, opCode, cType), msgLength);
    }

    /**
     * Takes a short value and splits it into 2 bytes
     * @param val - the value to split
     * @return - a 2 byte array
     */
    public static byte[] shortToBytes(final short val) {
        return Shorts.toByteArray(val);
    }

    /**
     * Takes two bytes and concatenates the two into a short value
     * @param byte1 - the first byte
     * @param byte2 - the training byte
     * @return - the short value
     */
    public static short bytesToShort(final byte byte1, final byte byte2) {
        return Shorts.fromBytes(byte1, byte2);
    }

    public static byte[] intToBytes(final int value) {
        return Ints.toByteArray(value);
    }

    public static int bytesToInt(final byte byte1, final byte byte2, final byte byte3, final byte byte4) {
        return Ints.fromBytes(byte1, byte2, byte3, byte4);
    }

    private static COPSMsg readBody(final Socket socket, final COPSHeaderData hdrData) throws IOException, COPSException {
        logger.debug("Reading COPS Body of type - " + hdrData.header.getOpCode());
        final int expectedBytes = hdrData.msgByteCount - hdrData.header.getHdrLength();
        final byte[] buffer = new byte[expectedBytes];
        final int nread = readData(socket, buffer, expectedBytes);
        if (nread != expectedBytes) {
            throw new COPSException("Bad COPS message");
        }
        return parse(hdrData, buffer);
    }

    /**
     * Reads nchar from a given sockets, blocks on read untill nchar are read of conenction has error
     * bRead returns the bytes read.
     * @param    socket              a  Socket
     * @param    dataRead            a  byte[] - this array should be initialized to the proper size but is then
     *                               populated by reference
     * @param    nchar               an int
     * @return   an int
     * @throws   IOException
     */
    public static int readData(final Socket socket, final byte[] dataRead, final int nchar)  throws IOException {
        final InputStream input = socket.getInputStream();
        int nread = 0;
        int startTime = (int) (new Date().getTime());
        do {
            if (input.available() != 0) {
                nread += input.read(dataRead, nread, nchar-nread);
                startTime = (int) (new Date().getTime());
            } else {
                int nowTime = (int) (new Date().getTime());
                // Read Timeout - TODO - May want to make this configurable
                if ((nowTime - startTime) > 2000)
                    break;
            }
        } while (nread != nchar);
        return nread;
    }
    /**
     * Parse the message with given header , the data is pointing
     * to the data following the header
     * @param    hdrData - contains the header and the
     * @param    data                a  byte[]
     * @return   a COPSMsg
     * @throws   COPSException
     */
    private static COPSMsg parse(final COPSHeaderData hdrData, final byte[] data) throws COPSException {
        final OPCode cCode = hdrData.header.getOpCode();
        switch (cCode) {
            case REQ:
                return COPSReqMsg.parse(hdrData, data);
            case DEC:
                return COPSDecisionMsg.parse(hdrData, data);
            case RPT:
                return COPSReportMsg.parse(hdrData, data);
            case DRQ:
                return COPSDeleteMsg.parse(hdrData, data);
            case OPN:
                return COPSClientOpenMsg.parse(hdrData, data);
            case CAT:
                return COPSClientAcceptMsg.parse(hdrData, data);
            case CC:
                return COPSClientCloseMsg.parse(hdrData, data);
            case KA:
                return COPSKAMsg.parse(hdrData, data);
            case SSQ:
            case SSC:
                return COPSSyncStateMsg.parse(hdrData, data);
            default:
                throw new COPSException("Unsupported client code");
        }
    }

    /**
     * Builds two nibbles represented as bytes from a single byte. Each nibble returned must not be > 15
     * @param b the byte to divide into nibbles
     * @return two bytes where the first represents bits 1-4 and second represents bits 5-8
     */
    public static byte[] splitByteToNibbles(final byte b) {
        return new byte[]{
                //move the four high bits to the right,
                //fill up with zeros
                (byte)(b >>> 4 & 0xF),
                //zero out the four high bits and leave
                //the low bits untouched
                (byte)(b & 0x0F)
        };
    }

    /**
     * Combines two bytes representing a nibble into a single byte
     * @param byte1 - nibble #1 (0-15)
     * @param byte2 - nibble #2 (0-15)
     * @return - one byte representing both nibbles
     * @throws java.lang.IllegalArgumentException if either value is < 0 || > 15
     */
    public static int combineNibbles(final byte byte1, final byte byte2) {
        if (byte1 > 15 || byte1 < 0 || byte2 > 15 || byte2 < 0)
            throw new IllegalArgumentException("Each byte representing a nibble shall not exceed 15 (0xf)");
        return (byte1 << 4) | byte2;
    }

}


