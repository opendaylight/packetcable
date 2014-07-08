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
 * COPS Report Type (RFC 2748 pag. 16
 *
 *   The Type of Report on the request state associated with a handle:
 *
 *           C-Num = 12, C-Type = 1
 *
 *               0             1              2             3
 *       +--------------+--------------+--------------+--------------+
 *       |         Report-Type         |        /////////////        |
 *       +--------------+--------------+--------------+--------------+
 *
 *           Report-Type:
 *               1 = Success   : Decision was successful at the PEP
 *               2 = Failure   : Decision could not be completed by PEP
 *               3 = Accounting: Accounting update for an installed state
 *
 *
 * @version COPSReportType.java, v 1.00 2003
 *
 */
public class COPSReportType extends COPSPrObjBase {

    public final static String[] msgMap = {
        "Unknown",
        "Success",
        "Failure",
        "Accounting",
    };

    private COPSObjHeader _objHdr;
    private short _rType;
    private short _reserved;

    public final static short SUCCESS = 1;
    public final static short FAILURE = 2;
    public final static short ACCT = 3;

    public COPSReportType(short rType) {
        _objHdr = new COPSObjHeader();
        _objHdr.setCNum(COPSObjHeader.COPS_RPT);
        _objHdr.setCType((byte) 1);
        _rType = rType;
        _objHdr.setDataLength((short) 4);
    }

    /**
          Parse data and create COPSReportType object
     */
    protected COPSReportType(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        _rType |= ((short) dataPtr[4]) << 8;
        _rType |= ((short) dataPtr[5]) & 0xFF;
        _reserved |= ((short) dataPtr[6]) << 8;
        _reserved |= ((short) dataPtr[7]) & 0xFF;

        _objHdr.setDataLength((short) 4);
    }

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
     * If it is Success, return true
     *
     * @return   a boolean
     *
     */
    public boolean isSuccess() {
        return (_rType == SUCCESS );
    };

    /**
     * If it is Failure, return true
     *
     * @return   a boolean
     *
     */
    public boolean isFailure() {
        return (_rType == FAILURE);
    };

    /**
     * If it is Accounting, return true
     *
     * @return   a boolean
     *
     */
    public boolean isAccounting() {
        return (_rType == ACCT);
    };

    /**
     * Always return true
     *
     * @return   a boolean
     *
     */
    public boolean isReport() {
        return true;
    };

    /**
     * Write data in network byte order on a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        _objHdr.writeData(id);

        byte[] buf = new byte[4];

        buf[0] = (byte) (_rType >> 8);
        buf[1] = (byte) _rType;
        buf[2] = (byte) (_reserved >> 8);
        buf[3] = (byte) _reserved;

        COPSUtil.writeData(id, buf, 4);
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
        os.write(new String("Report: " + msgMap[_rType] + "\n").getBytes());
    }
}





