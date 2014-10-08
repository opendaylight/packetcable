/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;



/**
 * COPS Data
 *
 * @version COPSData.java, v 1.00 2003
 *
 */
public class COPSData {

    private byte[] _dataBuf;
    private int _dLen;

    public COPSData() {
        _dataBuf = null;
        _dLen = 0;
    }

    public COPSData(byte[] dPtr, int offset, int dLen) {
        _dataBuf = new byte[dLen];
        System.arraycopy(dPtr,offset,_dataBuf,0,dLen);
        _dLen = dLen;
    }

    public COPSData(String data) {
        _dLen = data.getBytes().length;
        _dataBuf = new byte[_dLen];
        System.arraycopy(data.getBytes(),0,_dataBuf,0,_dLen);
    }

    /**
     * Method getData
     *
     * @return   a byte[]
     *
     */
    public byte[] getData() {
        return _dataBuf;
    }

    /**
     * Method length
     *
     * @return   an int
     *
     */
    public int length() {
        return _dLen;
    }

    /**
     * Method str
     *
     * @return   a String
     *
     */
    public String str() {
        return new String (_dataBuf);
    }

    public String toString() {
        return str();
    }

    public boolean equals(Object obj) {
        return (((COPSData) obj).toString().equals(str()));
    }
}


