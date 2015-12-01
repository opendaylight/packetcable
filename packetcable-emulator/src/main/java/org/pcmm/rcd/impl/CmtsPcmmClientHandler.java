/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.messages.impl.MessageFactory;
import org.pcmm.rcd.IPCMMServer.IPCMMClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpep.COPSPepException;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * This class was created by moving an anonymous inner class from CMTS.java and is responsible for creating a persistent
 * connection with a PEP.
 */
public class CmtsPcmmClientHandler extends AbstractPCMMClient implements IPCMMClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(CmtsPcmmClientHandler.class);

    /**
     * The thread accepting PEP COPS messages
     */
    private transient Thread thread;

    /**
     * Emulator configuration
     */
    private final CMTSConfig config;

    /**
     * Constructor when a socket connection has not been established
     * @param host - the host to connect
     * @param port - the port to connect
     * @param config - emulator configuration
     */
    public CmtsPcmmClientHandler(final String host, final int port, final CMTSConfig config) {
        super(host, port);
        this.config = checkNotNull(config);
    }

    /**
     * Constructor with a connected socket.
     * @param socket - the socket connection
     * @param config - emulator configuration
     */
    public CmtsPcmmClientHandler(final Socket socket, final CMTSConfig config) {
        super(socket);
        this.config = checkNotNull(config);
    }

    public void stop() {
        if (thread != null && thread.isAlive())
            thread.interrupt();
    }

    @Override
    public void run() {
        try {
            logger.info("Send OPN message to the PS");
            sendRequest(MessageFactory.getInstance().create(OPCode.OPN, new Properties()));

            // wait for CAT
            final COPSMsg recvMsg = readMessage();

            switch (recvMsg.getHeader().getOpCode()) {
                case CC:
                    final COPSClientCloseMsg closeMsg = (COPSClientCloseMsg) recvMsg;
                    logger.info("PS requested Client-Close" + closeMsg.getError().getDescription());
                    // send a CC message and close the socket
                    disconnect();
                    break;
                case CAT:
                    logger.info("received Client-Accept from PS");
                    final COPSClientAcceptMsg acceptMsg = (COPSClientAcceptMsg) recvMsg;
                    // Support
                    if (acceptMsg.getIntegrity() != null) {
                        throw new COPSPepException("Unsupported object (Integrity)");
                    }

                    // Mandatory KATimer
                    final COPSKATimer kt = acceptMsg.getKATimer();
                    if (kt == null)
                        throw new COPSPepException("Mandatory COPS object missing (KA Timer)");
                    final short kaTimeVal = kt.getTimerVal();

                    // ACTimer
                    final COPSAcctTimer at = acceptMsg.getAcctTimer();
                    short acctTimer = 0;
                    if (at != null)
                        acctTimer = at.getTimerVal();

                    logger.info("Send a REQ message to the PS");
                    final Properties prop = new Properties();
                    final COPSMsg reqMsg = MessageFactory.getInstance().create(OPCode.REQ, prop);
                    final COPSHandle handle = ((COPSReqMsg) reqMsg).getClientHandle();
                    sendRequest(reqMsg);

                    // Create the connection manager
                    final PcmmCmtsConnection conn = new PcmmCmtsConnection(CLIENT_TYPE, getSocket(), config);
                    conn.addRequestState(handle, new CmtsDataProcessor());
                    conn.setKaTimer(kaTimeVal);
                    conn.setAcctTimer(acctTimer);

                    logger.info(getClass().getName() + " Thread(conn).start");
                    thread = new Thread(conn);
                    thread.start();
                    break;
                default:
                    throw new COPSPepException("Message not expected. Closing connection for " + getSocket().toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void task(Callable<?> c) {
        // TODO Auto-generated method stub

    }

    @Override
    public void shouldWait(int t) {
        // TODO Auto-generated method stub

    }

    @Override
    public void done() {
        // TODO Auto-generated method stub

    }

}
