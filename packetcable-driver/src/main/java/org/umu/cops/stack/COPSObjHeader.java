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
 * COPS Object Header
 *
 * @version COPSObjHeader.java, v 1.00 2003
 *
 */
public class COPSObjHeader extends COPSObjBase {

    public final static byte COPS_HANDLE = 1;
    public final static byte COPS_CONTEXT = 2;
    public final static byte COPS_ININTF = 3;
    public final static byte COPS_OUTINTF = 4;
    public final static byte COPS_REASON_CODE = 5;
    public final static byte COPS_DEC = 6;
    public final static byte COPS_LPDP_DEC = 7;
    public final static byte COPS_ERROR = 8;
    public final static byte COPS_CSI = 9;
    public final static byte COPS_KA = 10;
    public final static byte COPS_PEPID = 11;
    public final static byte COPS_RPT = 12;
    public final static byte COPS_PDP_REDIR = 13;
    public final static byte COPS_LAST_PDP_ADDR = 14;
    public final static byte COPS_ACCT_TIMER = 15;
    public final static byte COPS_MSG_INTEGRITY = 16;

    private transient short _len;
    private transient byte _cNum;

    // TODO - This value may be better as an enumeration
    private transient byte _cType;

    /**
     * Constructor
     * @param cNum - the cNum value
     * @param cType - the cType value
     */
    public COPSObjHeader(final byte cNum, final byte cType) {
        _len = 4;
        _cNum = cNum;
        _cType = cType;
    }

    /**
     * Default constructor
     */
    public COPSObjHeader() {
        _len = 4;
        _cNum = 0;
        _cType = 0;
    }

    protected COPSObjHeader(final byte[] data) {
        parse(data);
    }

    /**
     * Get the data length in number of octets
     * @return   a short
     */
    public short getDataLength() {
        return _len;
    }

    /**
     * Get the class information identifier cNum
     * @return   a byte
     */
    public byte getCNum() {
        return _cNum;
    }

    /**
     * Get the type per cNum
     * @return   a byte
     */
    public byte getCType() {
        return _cType;
    }

    /**
     * Get stringified CNum
     * @return   a String
     */
    public String getStrCNum() {
        switch (getCNum()) {
            case COPS_HANDLE:
                return ("Client-handle");
            case COPS_CONTEXT:
                return ("Context");
            case COPS_ININTF:
                return ("In-Interface");
            case COPS_OUTINTF:
                return ("Out-Interface");
            case COPS_REASON_CODE:
                return ("Reason");
            case COPS_DEC:
                return ("Decision");
            case COPS_LPDP_DEC:
                return ("Local-Decision");
            case COPS_ERROR:
                return ("Error");
            case COPS_CSI:
                return ("Client-SI");
            case COPS_KA:
                return ("KA-timer");
            case COPS_PEPID:
                return ("PEP-id");
            case COPS_RPT:
                return ("Report");
            case COPS_PDP_REDIR:
                return ("Redirect PDP addr");
            case COPS_LAST_PDP_ADDR:
                return ("Last PDP addr");
            case COPS_ACCT_TIMER:
                return ("Account-Timer");
            case COPS_MSG_INTEGRITY:
                return ("Message-Integrity");
            default:
                return ("Unknown");
        }
    }

    /**
     * Set the obj length, the length is the length of the data following
     * the object header.The length of the object header (4 bytes) is added
     * to the length passed.
     * @param    len                 a  short
     */
    public void setDataLength(final short len) {
        //Add the length of the header also
        _len = (short) (len + 4);
    }

    /**
     * Set the class of information cNum
     * @param    cNum                a  byte
     */
    public void setCNum(final byte cNum) {
        _cNum = cNum;
    }

    /**
     * Set the  type defined per cNum
     * @param    cType               a  byte
     */
    public void setCType(final byte cType) {
        _cType = cType;
    }

    /**
     * Writes data to a given network _socket
     * @param    id                  a  Socket
     * @throws   IOException
     */
    public void writeData(final Socket id) throws IOException {
        final byte[] buf = new byte[4];

        buf[0] = (byte) (_len >> 8);
        buf[1] = (byte) _len;
        buf[2] = _cNum;
        buf[3] = _cType;

        COPSUtil.writeData(id, buf, 4);
    }

    /**
     * Method parse
     * @param    data                a  byte[]
     */
    public void parse(final byte[] data) {
        if (data == null || data.length < 4)
            throw new IllegalArgumentException("Data cannot be null or fewer than 2 bytes");

        // TODO - Determine what setting the _len value from the data buffer really means
        _len = 0;
        _len |= ((short) data[0]) << 8;
        _len |= ((short) data[1]) & 0xFF;
        _cNum |= data[2];
        _cType |= data[3];
    }

    /**
     * Write an object textual description in the output stream
     * @param    os                  an OutputStream
     * @throws   IOException
     */
    public void dump(final OutputStream os) throws IOException {
        os.write(("**" + getStrCNum() + "**" + "\n").getBytes());
        os.write(("Length: " + _len + "\n").getBytes());
        os.write(("C-num: " + _cNum + "\n").getBytes());
        os.write(("C-type: " + _cType + "\n").getBytes());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSObjHeader)) {
            return false;
        }
        final COPSObjHeader that = (COPSObjHeader) o;
        return _cNum == that._cNum && _cType == that._cType && _len == that._len;
    }

    @Override
    public int hashCode() {
        int result = (int) _len;
        result = 31 * result + (int) _cNum;
        result = 31 * result + (int) _cType;
        return result;
    }
}

