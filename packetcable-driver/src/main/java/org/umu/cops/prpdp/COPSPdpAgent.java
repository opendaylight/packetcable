/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core PDP agent for provisioning
 */
public class COPSPdpAgent extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(COPSPdpAgent.class);

    /** Well-known port for COPS */
    //    public static final int WELL_KNOWN_PDP_PORT = 3288;
    /** Default keep-alive timer value (secs) */
    public static final short KA_TIMER_VALUE = 30;
    /** Default accounting timer value (secs) */
    public static final short ACCT_TIMER_VALUE = 0;

    /**
     * PDP host port
     */
    private int _serverPort;

    /**
     * Client-type of connecting PEP
     */
    private short _clientType;

    /**
     * Accounting timer (secs)
     */
    private short _acctTimer;

    /**
     * Keep-alive timer (secs)
     */
    private short _kaTimer;

    /**
     * The PEP ID
     */
    protected transient COPSPepId _pepId;

    /**
     *   Maps a PEP-ID to a connection
     *   TODO - Refactor COPSPdpConnection to extend PCMMPdpConnection. Until then, the value must remain an Object
     */
    protected Map<String, Object> _connectionMap;
    // map < String(PEPID), COPSPdpConnection > ConnectionMap;

    /**
     *  Policy data processing object
     */
    private COPSPdpDataProcess _process;

    /**
     * Holds all of the threads to manage by PEP ID
     */
    protected final Map<String, Thread> threadMap;

    /**
     * Creates a PDP Agent
     *
     * @param port  Port to listen to
     * @param clientType    COPS Client-type
     * @param process   Object to perform policy data processing
     */
    public COPSPdpAgent(final int port, final short clientType, final COPSPdpDataProcess process) {
        _serverPort = port;

        _kaTimer = KA_TIMER_VALUE;
        _acctTimer = ACCT_TIMER_VALUE;

        _clientType = clientType;
        _connectionMap = new ConcurrentHashMap<>();
        _process = process;
        this.threadMap = new ConcurrentHashMap<>();
    }

    /**
     * Gets the value of the keep-alive timer
     * @return   Keep-alive timer value (secs)
     */
    public short getKaTimer () {
        return _kaTimer;
    }

    /**
     * Gets the accounting timer value
     * @return   Accounting timer value (secs)
     */
    public short getAcctTimer () {
        return _acctTimer;
    }

    /**
     * Gets the client-type
     * @return   The client-type
     */
    public short getClientType() {
        return _clientType;
    }

    /**
     * Disconnects a PEP
     * @param pepID PEP-ID of the PEP to be disconnected
     * @param error COPS Error to be reported as a reason
     * @throws COPSException
     * @throws IOException
     */
    public void disconnect(final String pepID, final COPSError error) throws COPSException, IOException {
        final COPSPdpConnection pdpConn = (COPSPdpConnection)_connectionMap.get(pepID);
        final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(_clientType, error, null, null);
        closeMsg.writeData(pdpConn.getSocket());
        pdpConn.close();
    }

    /**
     * Requests a COPS sync for a PEP
     * @param pepID PEP-ID of the PEP to be synced
     * @throws COPSException
     * @throws COPSPdpException
     */
    public void sync(final String pepID) throws COPSException {
        COPSPdpConnection pdpConn = (COPSPdpConnection) _connectionMap.get(pepID);
        pdpConn.syncAllRequestState();
    }

    /**
     * Removes a PEP from the connection map
     * @param pepID PEP-ID of the PEP to be removed
     */
    public void delete(final String pepID) {
        _connectionMap.remove(pepID);
    }


    /**
     * Runs the PDP process
     */
    public void run() {
        try {
            final ServerSocket serverSocket = new ServerSocket (_serverPort);

            //Loop through for Incoming messages
            // server infinite loop
            while (true) {
                // Wait for an incoming connection from a PEP
                final Socket socket = serverSocket.accept();

                // COPSDebug.out(getClass().getName(),"New connection accepted " +
                //           socket.getInetAddress() +
                //           ":" + socket.getPort());

                // We're waiting for an OPN message
                try {
                    final COPSMsg msg = COPSTransceiver.receiveMsg(socket);
                    logger.info("Message received - " + msg);
                    if (msg.getHeader().getOpCode().equals(OPCode.OPN)) {
                        handleClientOpenMsg(socket, msg);
                    } else {
                        logger.error("Not an open message, closing socket");
                        try {
                            socket.close();
                        } catch (Exception ex) {
                            logger.error("Unexpected exception closing socket", ex);
                        }
                    }
                } catch (Exception e) { // COPSException, IOException
                    // COPSDebug.err(getClass().getName(), COPSDebug.ERROR_EXCEPTION,
                    //    "(" + socket.getInetAddress() + ":" + socket.getPort() + ")", e);
                    try {
                        socket.close();
                    } catch (Exception ex) {
                        logger.error("Unexpected exception closing socket", ex);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error caught while processing socket messages", e);
        }
    }

    /**
     * Handles a COPS client-open message
     * @param    conn Socket to the PEP
     * @param    msg <tt>COPSMsg</tt> holding the client-open message
     * @throws COPSException
     * @throws IOException
     * TODO - Refactor PCMMPdpAgent#handleClientOpenMsg() as it contains much of this same logic
     */
    protected void handleClientOpenMsg(final Socket conn, final COPSMsg msg) throws COPSException, IOException {
        final COPSClientOpenMsg cMsg = (COPSClientOpenMsg) msg;
        _pepId = cMsg.getPepId();

        // Validate Client Type
        if (msg.getHeader().getClientType() != _clientType) {
            // Unsupported client type
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(_clientType,
                    new COPSError(ErrorTypes.UNSUPPORTED_CLIENT_TYPE, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected exception writing data", unae);
            }

            throw new COPSException("Unsupported client type");
        }

        // PEPId is mandatory
        if (_pepId == null) {
            // Mandatory COPS object missing
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(_clientType,
                    new COPSError(ErrorTypes.MANDATORY_OBJECT_MISSING, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected exception writing data", unae);
            }

            throw new COPSException("Mandatory COPS object missing (PEPId)");
        }

        // Support
        if ( (cMsg.getClientSI() != null) || (cMsg.getPdpAddress() != null) || (cMsg.getIntegrity() != null)) {
            // Unsupported objects
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(_clientType,
                    new COPSError(ErrorTypes.UNKNOWN_OBJECT, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Exception writing data", unae);
            }

            throw new COPSException("Unsupported objects (ClientSI, PdpAddress, Integrity)");
        }

        // Connection accepted
        final COPSClientAcceptMsg acceptMsg;
        if (_acctTimer != 0)
            acceptMsg = new COPSClientAcceptMsg(_clientType, new COPSKATimer(_kaTimer),
                new COPSAcctTimer(_acctTimer), null);
        else
            acceptMsg = new COPSClientAcceptMsg(_clientType, new COPSKATimer(_kaTimer),null, null);
        acceptMsg.writeData(conn);

        final COPSPdpConnection pdpConn = new COPSPdpConnection(_pepId, conn,  _process);
        pdpConn.setKaTimer(_kaTimer);
        if (_acctTimer != 0) pdpConn.setAcctTimer(_acctTimer);
        new Thread(pdpConn).start();
        _connectionMap.put(_pepId.getData().str(), pdpConn);
    }

}



