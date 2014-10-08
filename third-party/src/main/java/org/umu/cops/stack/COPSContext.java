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
 * COPS Context Object
 *
 * @version COPSContext.java, v 1.00 2003
 *
 */
public class COPSContext extends COPSObjBase {

    public final static byte IN_ADMIN = 0x01;
    public final static byte RES_ALLOC = 0x02;
    public final static byte OUT = 0x04;
    public final static byte CONFIG = 0x08;

    private COPSObjHeader _objHdr;
    private short _rType;
    private short _mType;

    ///
    public COPSContext(short rType, short mType ) {
        _objHdr = new COPSObjHeader();
        _objHdr.setCNum(COPSObjHeader.COPS_CONTEXT);
        _objHdr.setCType((byte) 1);
        _rType = rType;
        _mType = mType;
        _objHdr.setDataLength((short) 4);
    }

    /**
          Parse the data and create a Context object
     */
    protected COPSContext(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        _rType |= ((short) dataPtr[4]) << 8;
        _rType |= ((short) dataPtr[5]) & 0xFF;

        _mType |= ((short) dataPtr[6]) << 8;
        _mType |= ((short) dataPtr[7]) & 0xFF;

        _objHdr.setDataLength( (short) 4);
    }

    /**
     * Write object in network byte order to a given network socket
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

        buf[2] = (byte) (_mType >> 8);
        buf[3] = (byte) _mType;

        COPSUtil.writeData(id, buf, 4);
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
     * Returns the detail description of the request type
     *
     * @return   a String
     *
     */
    public String getDescription() {
        String retStr = new String();
        if ((_rType & 0x01) != 0) {
            retStr += (retStr.length() != 0) ? "," : "";
            retStr += "Incoming Message/Admission Control";
        }
        if ((_rType & 0x02) != 0) {
            retStr += (retStr.length() != 0) ? "," : "";
            retStr += "Resource allocation";
        }
        if ((_rType & 0x04) != 0) {
            retStr += (retStr.length() != 0) ? "," : "";
            retStr += "Outgoing message";
        }
        if ((_rType & 0x08) != 0) {
            retStr += (retStr.length() != 0) ? "," : "";
            retStr += "Configuration";
        }
        return retStr;
    }

    /**
     * Method isIncomingMessage
     *
     * @return   a boolean
     *
     */
    public boolean isIncomingMessage() {
        return (_rType & IN_ADMIN) != 0;
    };

    /**
     * Method isAdminControl
     *
     * @return   a boolean
     *
     */
    public boolean isAdminControl() {
        return (_rType & IN_ADMIN) != 0;
    };

    /**
     * Method isResourceAllocationReq
     *
     * @return   a boolean
     *
     */
    public boolean isResourceAllocationReq() {
        return (_rType & RES_ALLOC) != 0;
    };

    /**
     * Method isOutgoingMessage
     *
     * @return   a boolean
     *
     */
    public boolean isOutgoingMessage() {
        return (_rType & OUT) != 0;
    };

    /**
     * Method isConfigRequest
     *
     * @return   a boolean
     *
     */
    public boolean isConfigRequest() {
        return (_rType & CONFIG) != 0;
    };

    /**
     * Method getMessageType
     *
     * @return   a short
     *
     */
    public short getMessageType() {
        return (_mType) ;
    };

    /**
     * Method getRequestType
     *
     * @return   a short
     *
     */
    public short getRequestType() {
        return (_rType);
    };

    /**
     * Method isContext
     *
     * @return   a boolean
     *
     */
    public boolean isContext() {
        return true;
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
        os.write(new String("context: " + getDescription() + "," + _mType + "\n").getBytes());
    }
}


