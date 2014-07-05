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
 * COPS Client Specific Information Object
 *
 * @version COPSClientSI.java, v 1.00 2003
 *
 */
public class COPSClientSI extends COPSObjBase {
    public final static byte CSI_SIGNALED = 1;
    public final static byte CSI_NAMED = 2;

    private COPSObjHeader _objHdr;
    private COPSData _data;
    private COPSData _padding;

    ///
    public COPSClientSI(byte type) {
        _objHdr = new COPSObjHeader();
        _objHdr.setCNum(COPSObjHeader.COPS_CSI);
        _objHdr.setCType(type);
    }

    public COPSClientSI(byte cnum, byte ctype) {
        _objHdr = new COPSObjHeader();
        _objHdr.setCNum(cnum);
        _objHdr.setCType(ctype);
    }

    /**
     Parse the data and create a ClientSI object
     */
    protected COPSClientSI(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        //Get the length of data following the obj header
        short dLen = (short) (_objHdr.getDataLength() - 4);
        COPSData d = new COPSData(dataPtr, 4, dLen);
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
        _objHdr.setDataLength((short) _data.length());
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
        return (short) (_objHdr.getDataLength() + lpadding);
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
     * Method isClientSI
     *
     * @return   a boolean
     *
     */
    public boolean isClientSI() {
        return true;
    }

    /**
     * Write data on a given network socket
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
        os.write(new String("client-SI: " + _data.str() + "\n").getBytes());
    }
}

