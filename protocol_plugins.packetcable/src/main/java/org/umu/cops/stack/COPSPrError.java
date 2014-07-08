/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;

/**
 * COPS Provisioning Error
 *
 * @version COPSPrError.java, v 1.00 2003
 *
 */
public class COPSPrError extends COPSPrObjBase {

    protected short _errCode;
    protected short _errSubCode;

    public COPSPrError(short eCode, short eSubCode) {
        _errCode = eCode;
        _errSubCode = eSubCode;
        _len = 8;
    }

    /**
          Parse the data and create a PrGlobalError object
     */
    protected COPSPrError(byte[] dataPtr) {
        _dataRep = null;

        _len |= ((short) dataPtr[0]) << 8;
        _len |= ((short) dataPtr[1]) & 0xFF;

        _sNum |= ((short) dataPtr[2]) << 8;
        _sNum |= ((short) dataPtr[3]) & 0xFF;

        _errCode |= ((short) dataPtr[4]) << 8;
        _errCode |= ((short) dataPtr[5]) & 0xFF;

        _errSubCode |= ((short) dataPtr[6]) << 8;
        _errSubCode |= ((short) dataPtr[7]) & 0xFF;
    }

    /**
     * Method strError
     *
     * @return   a String
     *
     */
    public String strError() {
        return "Error";
    }

    /**
     * Method setData
     *
     * @param    data                a  COPSData
     *
     */
    public void setData(COPSData data) { }     ;

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
     * Returns size in number of octects, including header
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        return 8;
    }

    /**
     * Method getDataRep
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
        _dataRep[4] = (byte) (_errCode >> 8);
        _dataRep[5] = (byte) _errCode;
        _dataRep[6] = (byte) (_errSubCode >> 8);
        _dataRep[7] = (byte) _errSubCode;

        return _dataRep;
    }

}

