/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSConnection;
import org.umu.cops.stack.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for managing an provisioning connection at the PDP side.
 */
@ThreadSafe
public class COPSPdpConnection extends COPSConnection {

    public final static Logger logger = LoggerFactory.getLogger(COPSPdpConnection.class);

    /**
     * PEP identifier
     * TODO FIXME - Why is this member never being used?
     */
    private final COPSPepId _pepId;

    /**
     *  Time of the latest keep-alive sent
     * TODO FIXME - Why is this member never being used?
     */
    private volatile Date _lastKa;

    /**
     * Maps a Client Handle to a Handler
     */
    protected final Map<COPSHandle, COPSPdpReqStateMan> _managerMap;

    /**
     *  PDP policy data processor class
     */
    protected final COPSPdpDataProcess _process;

    /**
     * Creates a new PDP connection
     *
     * @param pepId PEP-ID of the connected PEP
     * @param sock  Socket connected to PEP
     * @param process   Object for processing policy data
     */
    public COPSPdpConnection(final COPSPepId pepId, Socket sock, final COPSPdpDataProcess process) {
        this(pepId, sock, process, (short)0, (short)0);
    }

    /**
     * Constructor for this or extended classes
     * @param pepId - PEP-ID of the connected PEP
     * @param sock - Socket connected to PEP
     * @param process - Object for processing policy data
     * @param kaTimer - the Keep-alive timer value
     * @param acctTimer - the accounting timer value
     */
    protected COPSPdpConnection(final COPSPepId pepId, Socket sock, final COPSPdpDataProcess process,
                                final short kaTimer, final short acctTimer) {
        super(sock, kaTimer, acctTimer);
        _pepId = pepId;
        _process = process;
        _lastKa = new Date();
        _managerMap = new ConcurrentHashMap<>();
    }

    /**
     * Main loop
     */
    public void run () {
        Date lastSendKa = new Date();
        Date lastRecKa = new Date();
        try {
            while (!_sock.isClosed()) {
                if (_sock.getInputStream().available() != 0) {
                    processMessage(_sock);
                    lastRecKa = new Date();
                }

                // Keep Alive
                if (_kaTimer > 0) {
                    // Timeout at PDP
                    int _startTime = (int) (lastRecKa.getTime());
                    int cTime = (int) (new Date().getTime());

                    if ((cTime - _startTime) > _kaTimer*1000) {
                        _sock.close();
                        // Notify all Request State Managers
                        notifyNoKAAllReqStateMan();
                    }

                    // Send to PEP
                    _startTime = (int) (lastSendKa.getTime());
                    cTime = (int) (new Date().getTime());

                    if ((cTime - _startTime) > ((_kaTimer*3/4)*1000)) {
                        // TODO - what should the real clientType be here???
                        final COPSKAMsg msg = new COPSKAMsg(null);
                        COPSTransceiver.sendMsg(msg, _sock);
                        lastSendKa = new Date();
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    logger.error("Exception thrown while sleeping", e);
                }

            }
        } catch (Exception e) {
            logger.error("Error while processing socket messages", e);
        }

        // connection closed by server
        try {
            _sock.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }

        // Notify all Request State Managers
        try {
            notifyCloseAllReqStateMan();
        } catch (COPSException e) {
            logger.error("Error closing state managers");
        }
    }

    /**
     * Gets a COPS message from the socket and processes it
     * @param    conn Socket connected to the PEP
     */
    private void processMessage(final Socket conn) throws COPSException, IOException {
        final COPSMsg msg = COPSTransceiver.receiveMsg(conn);
        switch (msg.getHeader().getOpCode()) {
            case CC:
                handleClientCloseMsg(conn, (COPSClientCloseMsg)msg);
                break;
            case KA:
                handleKeepAliveMsg(conn, (COPSKAMsg)msg);
                break;
            case REQ:
                handleRequestMsg(conn, (COPSReqMsg)msg);
                break;
            case RPT:
                handleReportMsg(conn, (COPSReportMsg)msg);
                break;
            case DRQ:
                handleDeleteRequestMsg(conn, (COPSDeleteMsg)msg);
                break;
            case SSC:
                handleSyncComplete(conn, (COPSSyncStateMsg)msg);
                break;
            default:
                throw new COPSPdpException("Message not expected (" + msg.getHeader().getOpCode() + ").");
        }
    }

    /**
     * Handle Keep Alive Message
     * @param    conn                a  Socket
     * @param    kaMsg               a  COPSKAMsg
     */
    private void handleKeepAliveMsg(final Socket conn, final COPSKAMsg kaMsg) {
        try {
            // Support
            if (kaMsg.getIntegrity() != null) {
                logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }
            kaMsg.writeData(conn);
            _lastKa = new Date();
        } catch (Exception unae) {
            logger.error("Unexpected exception while writing COPS data", unae);
        }
    }

    /**
     * Handle Delete Request Message
     * @param    conn                a  Socket
     * @param    cMsg                a  COPSDeleteMsg
     */
    private void handleDeleteRequestMsg(final Socket conn, final COPSDeleteMsg cMsg) throws COPSException {
        // Support
        if (cMsg.getIntegrity() != null) {
            logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        final COPSPdpReqStateMan man = _managerMap.remove(cMsg.getClientHandle());
        if (man == null) {
            logger.warn("No state manager found with ID - " + cMsg.getClientHandle().getId().str());
        } else {
            man.processDeleteRequestState(cMsg);
        }
    }

    /**
     * Handle Request Message
     * @param    conn                a  Socket
     * @param    reqMsg              a  COPSReqMsg
     */
    protected void handleRequestMsg(final Socket conn, final COPSReqMsg reqMsg) throws COPSException {
        final COPSHeader header = reqMsg.getHeader();

        // Support
        if (reqMsg.getIntegrity() != null) {
            logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        final COPSPdpReqStateMan man;
        if (_managerMap.get(reqMsg.getClientHandle()) == null) {

            man = createStateManager(reqMsg);
            _managerMap.put(reqMsg.getClientHandle(), man);
            man.initRequestState(_sock);

            logger.info("createHandler called, clientType=" + header.getClientType() + " msgType=" + ", connId=" +
                    conn.toString());
        } else {
            man = _managerMap.get(reqMsg.getClientHandle());
        }
        man.processRequest(reqMsg);
    }

    /**
     * Returns an instance of a COPSPdpReqStateMan
     * @param reqMsg - the request on which to create the state manager
     * @return - the state manager
     */
    protected COPSPdpReqStateMan createStateManager(final COPSReqMsg reqMsg) {
        return new COPSPdpReqStateMan(reqMsg.getHeader().getClientType(), reqMsg.getClientHandle(), _process);
    }

    /**
     * Handle Report Message
     * @param    conn                a  Socket
     * @param    repMsg              a  COPSReportMsg
     */
    private void handleReportMsg(final Socket conn, final COPSReportMsg repMsg) throws COPSException {
        // Support
        if (repMsg.getIntegrity() != null) {
            logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        final COPSPdpReqStateMan man = _managerMap.get(repMsg.getClientHandle());
        if (man == null) {
            logger.warn("No state manager found with ID - " + repMsg.getClientHandle().getId().str());
        } else {
            man.processReport(repMsg);
        }
    }

    /**
     * Method handleSyncComplete
     * @param    conn                a  Socket
     * @param    cMsg                a  COPSSyncStateMsg
     */
    private void handleSyncComplete(final Socket conn, final COPSSyncStateMsg cMsg) throws COPSException {
        // Support
        if (cMsg.getIntegrity() != null) {
            logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        final COPSPdpReqStateMan man = _managerMap.get(cMsg.getClientHandle());
        if (man == null) {
            logger.warn("No state manager found with ID - " + cMsg.getClientHandle().getId().str());
        } else {
            man.processSyncComplete(cMsg);
        }
    }

    /**
     * Requests a COPS sync from the PEP
     * @throws COPSException
     * @throws COPSPdpException
     */
    public void syncAllRequestState() throws COPSException {
        for (final COPSPdpReqStateMan man : _managerMap.values()) {
            man.syncRequestState();
        }
    }

    private void notifyCloseAllReqStateMan() throws COPSException {
        for (final COPSPdpReqStateMan man : _managerMap.values()) {
            man.processClosedConnection(_error);
        }
    }

    private void notifyNoKAAllReqStateMan() throws COPSException {
        for (final COPSPdpReqStateMan man : _managerMap.values()) {
            man.processNoKAConnection();
        }
    }

}

