/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;


import java.util.Arrays;

/**
 * COPS Data
 *
 * // TODO - determine a good description of this class
 *
 * @version COPSData.java, v 1.00 2003
 *
 */
public class COPSData {

    /**
     * TODO - determine a good description for this attribute
     */
    private final byte[] _dataBuf;

    /**
     * TODO - determine a good description for this attribute
     */
    private final int _dLen;

    /**
     * Default constructor
     */
    public COPSData() {
        _dataBuf = null;
        _dLen = 0;
    }

    /**
     * Constructor
     * @param dPtr - the data
     * @param offset - the byte offset
     * @param dLen - the data length
     * @throws java.lang.IllegalArgumentException
     */
    public COPSData(final byte[] dPtr, final int offset, final int dLen) {
        if (dPtr == null) throw new IllegalArgumentException("The data array must not be null");
        if (offset < 0) throw new IllegalArgumentException("The offset must not be < 0");
        if (dLen < 0) throw new IllegalArgumentException("The length < 0");
        if (dLen > dPtr.length - offset)
            throw new IllegalArgumentException("The length must be less than the dPtr size less the offset");

        _dataBuf = new byte[dLen];
        System.arraycopy(dPtr, offset, _dataBuf, 0, dLen);
        _dLen = dLen;
    }

    /**
     * Constructor
     * @param data - the data as a String
     */
    public COPSData(final String data) {
        if (data == null) throw new IllegalArgumentException("Data string must not be null");
        _dLen = data.getBytes().length;
        _dataBuf = new byte[_dLen];
        System.arraycopy(data.getBytes(),0,_dataBuf,0,_dLen);
    }

    /**
     * Method getData
     * @return   a byte[]
     */
    public byte[] getData() {
        return _dataBuf;
    }

    /**
     * Method length
     * @return   an int
     */
    public int length() {
        return _dLen;
    }

    /**
     * Method str
     * @return   a String
     */
    public String str() {
        if (_dataBuf == null) return "";
        else return new String (_dataBuf);
    }

    public String toString() {
        return str();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSData)) {
            return false;
        }
        final COPSData copsData = (COPSData) o;
        return _dLen == copsData._dLen && Arrays.equals(_dataBuf, copsData._dataBuf);
    }

    @Override
    public int hashCode() {
        int result = _dataBuf != null ? Arrays.hashCode(_dataBuf) : 0;
        result = 31 * result + _dLen;
        return result;
    }

}


