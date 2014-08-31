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
 * COPS PEP Identification Object
 *
 * @version COPSPepId.java, v 1.00 2003
 *
 */
public class COPSPepId extends COPSObjBase {

    COPSObjHeader _objHdr;
    COPSData _data;
    COPSData _padding;

    public COPSPepId() {
        _objHdr = new COPSObjHeader();
        _objHdr.setCNum(COPSObjHeader.COPS_PEPID);
        _objHdr.setCType((byte) 1);
    }

    protected COPSPepId(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        //Get the length of data following the obj header
        short dLen = (short) (_objHdr.getDataLength() - 4);
        COPSData d = new COPSData (dataPtr, 4, dLen);
        setData(d);
    }

    /**
     * Method setData
     *
     * @param    data                a  COPSData
     *
     */
    public void setData(COPSData data) {
        _data = data;
        if (_data.length() % 4 != 0) {
            int padLen = 4 - _data.length() % 4;
            _padding = getPadding(padLen);
        }
        _objHdr.setDataLength((short)_data.length());
    }

    /**
     * Returns size in number of octects, including header
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        //Add the size of the header also
        int lpadding = 0;
        if (_padding != null) lpadding = _padding.length();
        return ((short) (_objHdr.getDataLength() + lpadding));
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
     * Method isPepId
     *
     * @return   a boolean
     *
     */
    public boolean isPepId() {
        return true;
    };

    /**
     * Write data to given netwrok socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        _objHdr.writeData(id);
        COPSUtil.writeData(id, _data.getData(), _data.length());
        if (_padding != null) {
            COPSUtil.writeData(id, _padding.getData(), _padding.length());
        }
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
        _objHdr.dump(os);
        os.write(new String("PEPID: " + _data.str() + "\n").getBytes());
    }
}



