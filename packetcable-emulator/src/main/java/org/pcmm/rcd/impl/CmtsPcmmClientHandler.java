/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.rcd.impl;

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

    private final static Logger logger = LoggerFactory.getLogger(CmtsPcmmClientHandler.class);

    /**
     * The thread accepting PEP COPS messages
     */
    private transient Thread thread;

    /**
     * The configured gates
     */
    private final Map<Direction, Set<String>> gateConfig;

    /**
     * The connected cable modems and whether or not they are up
     */
    private final Map<String, Boolean> cmStatus;

    /**
     * Constructor when a socket connection has not been established
     * @param host - the host to connect
     * @param port - the port to connect
     * @param gateConfig - the configured gates
     * @param cmStatus - the configured cable modem and their state
     */
    public CmtsPcmmClientHandler(final String host, final int port, final Map<Direction, Set<String>> gateConfig,
                                 final Map<String, Boolean> cmStatus) {
        super(host, port);
        this.gateConfig = Collections.unmodifiableMap(gateConfig);
        this.cmStatus = Collections.unmodifiableMap(cmStatus);
    }

    /**
     * Constructor with a connected socket.
     * @param socket - the socket connection
     * @param gateConfig - the configured gates
     * @param cmStatus - the configured cable modem and their state
     */
    public CmtsPcmmClientHandler(final Socket socket, final Map<Direction, Set<String>> gateConfig,
                                 final Map<String, Boolean> cmStatus) {
        super(socket);
        this.gateConfig = Collections.unmodifiableMap(gateConfig);
        this.cmStatus = Collections.unmodifiableMap(cmStatus);
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
                    short kaTimeVal = kt.getTimerVal();

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
                    final PcmmCmtsConnection conn = new PcmmCmtsConnection(CLIENT_TYPE, getSocket(), gateConfig,
                            cmStatus);
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
