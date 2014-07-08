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
 * COPS Timer Object
 *
 * @version COPSTimer.java, v 1.00 2003
 *
 */
public class COPSTimer extends COPSObjBase {

    protected COPSObjHeader _objHdr;
    private short _reserved;
    private short _timerValue;

    /**
     * Returns size in number of octects, including header
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        //Add the size of the header also
        return (_objHdr.getDataLength());
    }

    /**
     * Method getTimerVal
     *
     * @return   a short
     *
     */
    public short getTimerVal() {
        return _timerValue;
    };

    /**
     * Method isTimer
     *
     * @return   a boolean
     *
     */
    public boolean isTimer() {
        return true;
    };

    /**
     * Method isKATimer
     *
     * @return   a boolean
     *
     */
    public boolean isKATimer() {
        return false;
    };

    /**
     * Method isAcctTimer
     *
     * @return   a boolean
     *
     */
    public boolean isAcctTimer() {
        return false;
    };

    /**
     * Write data to given socket in Network byte order
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        _objHdr.writeData(id);

        byte[] buf = new byte[4];

        buf[0] = (byte) (_reserved >> 8);
        buf[1] = (byte) _reserved;
        buf[2] = (byte) (_timerValue >> 8);
        buf[3] = (byte) _timerValue;
        COPSUtil.writeData(id, buf, 4);
    }

    protected COPSTimer(short timeVal) {
        _objHdr = new COPSObjHeader();
        //Time range is 1 - 65535 seconds
        _timerValue = timeVal;
        // _objHdr.setDataLength(sizeof(u_int32_t));
        _objHdr.setDataLength((short) 4);
    }

    /**
     * Receive data that is in netwrok byte order and fill in the obj.
     */
    protected COPSTimer(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        _reserved |= ((short) dataPtr[4]) << 8;
        _reserved |= ((short) dataPtr[5]) & 0xFF;
        _timerValue |= ((short) dataPtr[6]) << 8;
        _timerValue |= ((short) dataPtr[7]) & 0xFF;

        // _objHdr.setDataLength(sizeof(u_int32_t));
        _objHdr.setDataLength((short) 4);
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
        os.write(new String("Timer val: " + _timerValue + "\n").getBytes());
    }

}

