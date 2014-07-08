/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;

// import org.umu.cops.common.COPSDebug;

/**
 * COPS Transceiver
 *
 * @version COPSTransceiver.java, v 1.00 2003
 *
 */
public class COPSTransceiver {

    /**
     * Method sendMsg
     *
     * @param    msg                 a  COPSMsg
     * @param    fd                  a  Socket
     *
     * @throws   IOException
     * @throws   COPSException
     *
     */
    static public void sendMsg(COPSMsg msg, Socket fd) throws IOException, COPSException {
        // COPSDebug.out("COPSTransceiver", "sendMsg ******************************** START" );

        msg.checkSanity();
        msg.writeData(fd);

        // COPSDebug.out("COPSTransceiver", "sendMsg ******************************** END" );
    }

    /**
     * Method receiveMsg
     *
     * @param    fd                  a  Socket
     *
     * @return   a COPSMsg
     *
     * @throws   IOException
     * @throws   COPSException
     *
     */
    static public COPSMsg receiveMsg (Socket fd)  throws IOException, COPSException {
        int nread = 0;
        byte[] hBuf = new byte[8];

        // COPSDebug.out("COPSTransceiver", "receiveMsg ******************************** START" );

        nread = COPSUtil.readData(fd, hBuf, 8);

        if (nread == 0) {
            throw new COPSException("Error reading connection");
        }

        if (nread != 8) {
            throw new COPSException("Bad COPS message");
        }

        COPSHeader hdr = new COPSHeader(hBuf);
        int dataLen = hdr.getMsgLength() - hdr.getHdrLength();
        // COPSDebug.out("COPSTransceiver", "COPS Msg length :[" + dataLen + "]\n" );
        byte[] buf = new byte[dataLen + 1];
        nread = 0;

        nread = COPSUtil.readData(fd, buf, dataLen);
        buf[dataLen] = (byte) '\0';
        // COPSDebug.out("COPSTransceiver", "Data read length:[" + nread + "]\n");

        if (nread != dataLen) {
            throw new COPSException("Bad COPS message");
        }

        COPSMsgParser prser = new COPSMsgParser();
        COPSMsg msg = prser.parse(hdr, buf);

        // COPSDebug.out("COPSTransceiver", "Message received");

        // COPSDebug.out("COPSTransceiver", "receiveMsg ******************************** END" );
        return msg;
    }
}

