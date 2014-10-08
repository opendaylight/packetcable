/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * COPS Provisioning Object Base
 *
 * @version COPSPrObjBase.java, v 1.01 2003
 *
 */
public class COPSPrObjBase {
    public final static byte PR_PRID = 1;
    public final static byte PR_PPRID = 2;
    public final static byte PR_EPD = 3;
    public final static byte PR_GPERR = 4;
    public final static byte PR_CPERR = 5;
    public final static byte PR_IDERR = 6;

    public final static byte PR_BER = 1;
    public final static byte PR_XML = 2;

    protected short _len;
    protected byte _sNum;
    protected byte _sType;
    protected COPSData _data;
    protected COPSData _padding;

    protected byte[] _dataRep;
    ///
    protected COPSPrObjBase() {
        _dataRep = null;
    }

    public COPSPrObjBase(byte[] dataPtr) {
        _dataRep = null;

        _len |= ((short) dataPtr[0]) << 8;
        _len |= ((short) dataPtr[1]) & 0xFF;

        _sNum |= ((short) dataPtr[2]) << 8;
        _sNum |= ((short) dataPtr[3]) & 0xFF;

        //Get the length of data following the obj header
        short dLen = (short) (_len - 4);
        COPSData d = new COPSData(dataPtr, 4, dLen);
        setData(d);
    }

    /**
     * Add padding in the data, if the Provisioning data does
     * not fall on 32-bit boundary
     *
     * @param    len                 an int
     *
     * @return   a COPSData
     *
     */
    public COPSData getPadding(int len) {
        byte[] padBuf = new byte[len];
        Arrays.fill(padBuf, (byte) 0);
        COPSData d = new COPSData(padBuf, 0, len);
        return d;
    }

    /**
     * Method isPRID
     *
     * @return   a boolean
     *
     */
    public boolean isPRID() {
        return false;
    };

    /**
     * Method isPRIDPrefix
     *
     * @return   a boolean
     *
     */
    public boolean isPRIDPrefix() {
        return false;
    };

    /**
     * Method isEncodedInstanceData
     *
     * @return   a boolean
     *
     */
    public boolean isEncodedInstanceData() {
        return false;
    };

    /**
     * Method isGlobalPrError
     *
     * @return   a boolean
     *
     */
    public boolean isGlobalPrError() {
        return false;
    };

    /**
     * Method isPRCClassError
     *
     * @return   a boolean
     *
     */
    public boolean isPRCClassError() {
        return false;
    };

    /**
     * Method isErrorPRID
     *
     * @return   a boolean
     *
     */
    public boolean isErrorPRID() {
        return false;
    };

    /**
     * Method setData
     *
     * @param    data                a  COPSData
     *
     */
    public void setData(COPSData data) {
        _data = data;
        if (_data.length() % 4 != 0) {
            int padLen = 4 - (_data.length() % 4);
            _padding = getPadding(padLen);
        }
        _len = (short) (_data.length() + 4);
    }

    /**
     * Get the class information identifier cNum
     *
     * @return   a byte
     *
     */
    public byte getSNum() {
        return _sNum;
    };

    /**
     * Get the type per sNum
     *
     * @return   a byte
     *
     */
    public byte getSType() {
        return _sType;
    };

    /**
     * Get stringified CNum
     *
     * @return   a String
     *
     */
    public String getStrSNum() {
        switch (getSNum()) {
        case PR_PRID:
            return ("PRID");
        case PR_PPRID:
            return ("PRID Prefix");
        case PR_EPD:
            return ("EPD");
        case PR_GPERR:
            return ("GPERR");
        case PR_CPERR:
            return ("CPERR");
        case PR_IDERR:
            return ("IDERR");
        default:
            return ("Unknown");
        }
    }

    /**
     * Returns size in number of octects, including header
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        //Add the size of the header also
        //Header length contains the header+data length
        int lpadding = 0;
        if (_padding != null) lpadding = _padding.length();
        return ( (short) (_len + lpadding));
    }

    /**
     * Method getData
     *
     * @return   a COPSData
     *
     */
    public COPSData getData() {
        return _data;
    };

    /**
     * Write data on a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        byte[] dataRep = getDataRep();
        COPSUtil.writeData(id, dataRep, dataRep.length);
    }

    /**
     * Get the binary data contained in the object
     *
     * @return   a byte[]
     *
     */
    public byte[] getDataRep() {
        _dataRep = new byte[getDataLength()];

        _dataRep[0] = (byte) (_len >> 8);
        _dataRep[1] = (byte) _len;
        _dataRep[2] = (byte) (_sNum >> 8);
        _dataRep[3] = (byte) _sNum;

        System.arraycopy(_data.getData(), 0, _dataRep, 4, _data.length());

        if (_padding != null) {
            System.arraycopy(_padding.getData(), 0, _dataRep, 4 + _data.length(), _padding.length());
        }

        return _dataRep;
    }

}


