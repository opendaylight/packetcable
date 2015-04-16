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
 * COPS Object Header
 *
 * @version COPSObjHeader.java, v 1.00 2003
 *
 */
public class COPSObjHeader  {

    static Map<Integer, CNum> VAL_TO_CNUM = new ConcurrentHashMap<>();
    static {
        VAL_TO_CNUM.put(CNum.NA.ordinal(), CNum.NA);
        VAL_TO_CNUM.put(CNum.HANDLE.ordinal(), CNum.HANDLE);
        VAL_TO_CNUM.put(CNum.CONTEXT.ordinal(), CNum.CONTEXT);
        VAL_TO_CNUM.put(CNum.ININTF.ordinal(), CNum.ININTF);
        VAL_TO_CNUM.put(CNum.OUTINTF.ordinal(), CNum.OUTINTF);
        VAL_TO_CNUM.put(CNum.REASON_CODE.ordinal(), CNum.REASON_CODE);
        VAL_TO_CNUM.put(CNum.DEC.ordinal(), CNum.DEC);
        VAL_TO_CNUM.put(CNum.LPDP_DEC.ordinal(), CNum.LPDP_DEC);
        VAL_TO_CNUM.put(CNum.ERROR.ordinal(), CNum.ERROR);
        VAL_TO_CNUM.put(CNum.CSI.ordinal(), CNum.CSI);
        VAL_TO_CNUM.put(CNum.KA.ordinal(), CNum.KA);
        VAL_TO_CNUM.put(CNum.PEPID.ordinal(), CNum.PEPID);
        VAL_TO_CNUM.put(CNum.RPT.ordinal(), CNum.RPT);
        VAL_TO_CNUM.put(CNum.PDP_REDIR.ordinal(), CNum.PDP_REDIR);
        VAL_TO_CNUM.put(CNum.LAST_PDP_ADDR.ordinal(), CNum.LAST_PDP_ADDR);
        VAL_TO_CNUM.put(CNum.ACCT_TIMER.ordinal(), CNum.ACCT_TIMER);
        VAL_TO_CNUM.put(CNum.MSG_INTEGRITY.ordinal(), CNum.MSG_INTEGRITY);
    }

    static Map<Integer, CType> VAL_TO_CTYPE = new ConcurrentHashMap<>();
    static {
        VAL_TO_CTYPE.put(CType.NA.ordinal(), CType.NA);
        VAL_TO_CTYPE.put(CType.DEF.ordinal(), CType.DEF);
        VAL_TO_CTYPE.put(CType.STATELESS.ordinal(), CType.STATELESS);
        VAL_TO_CTYPE.put(CType.REPL.ordinal(), CType.REPL);
        VAL_TO_CTYPE.put(CType.CSI.ordinal(), CType.CSI);
        VAL_TO_CTYPE.put(CType.NAMED.ordinal(), CType.NAMED);
    }

    /**
     * Denotes the type of COPSMsg
     */
    private final CNum _cNum;

    /**
     * Subtype or version of the information.
     */
    private final CType _cType;

    /**
     * TODO - remove this attribute as the body of the COPS message should return the body length
     */
    @Deprecated
    private short _len;

    /**
     * Constructor
     * @param cNum - the cNum value
     * @param cType - the cType value
     * @throws java.lang.IllegalArgumentException
     */
    public COPSObjHeader(final CNum cNum, final CType cType) {
        if (cNum == null || cType == null) throw new IllegalArgumentException("CNum and CType must not be null");
        _cNum = cNum;
        _cType = cType;
        _len = 4;
    }

    /**
     * Get the data length in number of octets
     * @return   a short
     */
    public short getHdrLength() {
        return (short)4;
    }

    /**
     * Get the class information identifier cNum
     * @return   a byte
     */

    public CNum getCNum() {
        return _cNum;
    }

    /**
     * Get the type per cNum
     * @return   a byte
     */
    public CType getCType() {
        return _cType;
    }

    /**
     * Get stringified CNum
     * @return   a String
     */
    public String getStrCNum() {
        switch (_cNum) {
            case HANDLE:
                return ("Client-handle");
            case CONTEXT:
                return ("Context");
            case ININTF:
                return ("In-Interface");
            case OUTINTF:
                return ("Out-Interface");
            case REASON_CODE:
                return ("Reason");
            case DEC:
                return ("Decision");
            case LPDP_DEC:
                return ("Local-Decision");
            case ERROR:
                return ("Error");
            case CSI:
                return ("Client-SI");
            case KA:
                return ("KA-timer");
            case PEPID:
                return ("PEP-id");
            case RPT:
                return ("Report");
            case PDP_REDIR:
                return ("Redirect PDP addr");
            case LAST_PDP_ADDR:
                return ("Last PDP addr");
            case ACCT_TIMER:
                return ("Account-Timer");
            case MSG_INTEGRITY:
                return ("Message-Integrity");
            default:
                return ("Unknown");
        }
    }

    /**
     * Set the obj length, the length is the length of the data following
     * the object header.The length of the object header (4 bytes) is added
     * to the length passed.
     *
     * TODO - The data length will be removed from the header in a subsequent patch
     *
     * @param    len                 a  short
     */
    @Deprecated
    public void setDataLength(short len) {
        //Add the length of the header also
        _len = (short) (len + 4);
    }

    /**
     * Get the data length in number of octets
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        return _len;
    }

    /**
     * Writes data to a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        byte[] buf = new byte[4];

        buf[0] = (byte) (_len >> 8);
        buf[1] = (byte) _len;
        buf[2] = (byte) _cNum.ordinal();
        buf[3] = (byte) _cType.ordinal();

        COPSUtil.writeData(id, buf, 4);
    }

    /**
     * Write an object textual description in the output stream
     * @param    os                  an OutputStream
     * @throws   IOException
     */
    public void dump(final OutputStream os) throws IOException {
        os.write(("**" + getStrCNum() + "**" + "\n").getBytes());
        os.write(("C-num: " + _cNum + "\n").getBytes());
        os.write(("C-type: " + _cType + "\n").getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSObjHeader)) {
            return false;
        }

        final COPSObjHeader header = (COPSObjHeader) o;

        return _cNum == header._cNum && _cType == header._cType;

    }

    @Override
    public int hashCode() {
        int result = _cNum.hashCode();
        result = 31 * result + _cType.hashCode();
        return result;
    }

    /**
     * Method parse
     *
     * @param    data                a  byte[]
     *
     */
    public final static COPSObjHeader parse(byte[] data) {
        short len = 0;
        len = 0;
        len |= ((short) data[0]) << 8;
        len |= ((short) data[1]) & 0xFF;

        int cNum = 0;
        cNum |= data[2];

        int cType = 0;
        cType |= data[3];

        final COPSObjHeader hdr = new COPSObjHeader(VAL_TO_CNUM.get(cNum), VAL_TO_CTYPE.get(cType));
        hdr.setDataLength(len);
        return hdr;
    }

    public enum CNum {
        NA,
        HANDLE,
        CONTEXT,
        ININTF,
        OUTINTF,
        REASON_CODE,
        DEC,
        LPDP_DEC,
        ERROR,
        CSI,
        KA,
        PEPID,
        RPT,
        PDP_REDIR,
        LAST_PDP_ADDR,
        ACCT_TIMER,
        MSG_INTEGRITY,
    }

    public enum CType {
        NA,
        DEF,
        STATELESS,
        REPL,
        CSI,
        NAMED
    }

}

