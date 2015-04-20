/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;

/**
 * Utilities for sending and receiving COPS messages
 *
 * @version COPSTransceiver.java, v 1.00 2003
 *
 */
public class COPSTransceiver {

    /**
     * Method sendMsg
     * @param    msg                 a  COPSMsg
     * @param    fd                  a  Socket
     * @throws   IOException
     * @throws   COPSException
     */
    static public void sendMsg(final COPSMsg msg, final Socket fd) throws IOException, COPSException {
        msg.writeData(fd);
    }

    /**
     * Parses a COPS message coming in via the socket
     * @param    socket - the socket from which the message will arrive
     * @return   a COPSMsg object
     * @throws   IOException
     * @throws   COPSException
     */
    static public COPSMsg receiveMsg(final Socket socket) throws IOException, COPSException {
        return COPSMsgParser.parseMessage(socket);
    }
}

