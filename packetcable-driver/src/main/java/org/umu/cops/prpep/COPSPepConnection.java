/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSConnection;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COPSPepConnection represents a PEP-PDP Connection Manager.
 * Responsible for processing messages received from PDP.
 */
@ThreadSafe
public class COPSPepConnection extends COPSConnection {

    private final static Logger logger = LoggerFactory.getLogger(COPSPepConnection.class);

    /** Time to wait responses (milliseconds), default is 10 seconds */
    protected transient int _responseTime;

    /** COPS Client-type */
    protected final short _clientType;

    /**
     Maps a COPS Client Handle to a Request State Manager
     */
    protected final Map<COPSHandle, COPSPepReqStateMan> _managerMap;

    /**
     * Creates a new PEP connection
     * @param clientType    PEP's client-type
     * @param sock          Socket connected to PDP
     */
    public COPSPepConnection(final short clientType, final Socket sock) {
        super(sock, (short)0, (short)0);
        _clientType = clientType;
        _responseTime = 10000;
        _managerMap = new ConcurrentHashMap<>();
    }

    /**
     * Message-processing loop
     */
    public void run () {
        Date lastSendKa = new Date();
        Date lastSendAcc = new Date();
        Date lastRecKa = new Date();
            while (!_sock.isClosed()) {
                try {
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

                        if ((cTime - _startTime) > ((_kaTimer*3/4) * 1000)) {
                            final COPSKAMsg msg = new COPSKAMsg(null);
                            COPSTransceiver.sendMsg(msg, _sock);
                            lastSendKa = new Date();
                        }
                    }

                    // Accounting
                    if (_acctTimer > 0) {
                        int _startTime = (int) (lastSendAcc.getTime());
                        int cTime = (int) (new Date().getTime());

                        if ((cTime - _startTime) > ((_acctTimer*3/4)*1000)) {
                            // Notify all Request State Managers
                            notifyAcctAllReqStateMan();
                            lastSendAcc = new Date();
                        }
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Closing connection");
                        break;
                    } catch (Exception e) {
                        logger.error("Unexpected exception while sleeping. Continue processing messages", e);
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error while processing socket messages. Continue processing", e);
                } catch (Throwable e) {
                    logger.error("Unexpected fatal error while processing COPS messages. Stopping thread", e);
                    break;
                }
            }

        // connection closed by server
        // COPSDebug.out(getClass().getName(),"Connection closed by server");
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
     * @param conn  Socket connected to the PDP
     * @throws COPSException
     * @throws IOException
     */
    protected void processMessage(final Socket conn) throws COPSException, IOException {
        final COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        switch (msg.getHeader().getOpCode()) {
            case CC:
                handleClientCloseMsg(conn, (COPSClientCloseMsg)msg);
                break;
            case DEC:
                handleDecisionMsg((COPSDecisionMsg)msg);
                break;
            case SSQ:
                handleSyncStateReqMsg((COPSSyncStateMsg)msg);
                break;
            case KA:
                handleKeepAliveMsg((COPSKAMsg)msg);
                break;
            default:
                throw new COPSPepException("Message not expected (" + msg.getHeader().getOpCode() + ").");
        }
    }

    /**
     * Handle Keep Alive Message
     * @param    cMsg                a  COPSKAMsg
     */
    private void handleKeepAliveMsg(final COPSKAMsg cMsg) {
        logger.info("Get KAlive Msg");
        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                logger.warn("Unsupported objects (Integrity)");
            }

            // should we do anything else?? ....

        } catch (Exception unae) {
            logger.error("Unexpected exception while writing COPS data", unae);
        }
    }

    /**
     * Method handleDecisionMsg
     * @param    dMsg                 a  COPSDecisionMsg
     */
    protected void handleDecisionMsg(final COPSDecisionMsg dMsg) throws COPSException {
        final COPSHandle handle = dMsg.getClientHandle();
        final Map<COPSContext, Set<COPSDecision>> decisions = dMsg.getDecisions();

        for (final Set<COPSDecision> copsDecisions: decisions.values()) {
            for (final COPSDecision decision : copsDecisions) {
                // Get the associated manager
                final COPSPepReqStateMan manager = _managerMap.get(handle);
                if (manager == null) {
                    logger.warn("Unable to find state manager with key - " + handle);
                    return;
                }

                // Check message type
                // TODO FIXME - Use of manager object could result in a NPE
                if (decision.getFlag().equals(DecisionFlag.REQSTATE)) {
                    if (decision.getCommand().equals(Command.REMOVE))
                        // Delete Request State
                        manager.processDeleteRequestState(dMsg);
                    else
                        // Open new Request State
                        handleOpenNewRequestStateMsg(handle);
                } else
                    // Decision
                    manager.processDecision(dMsg);
            }
        }
    }


    /**
     * Method handleOpenNewRequestStateMsg
     * @param    handle              a  COPSHandle
     */
    private void handleOpenNewRequestStateMsg(final COPSHandle handle) throws COPSPepException {
        final COPSPepReqStateMan manager = _managerMap.get(handle);
        if (manager == null)
            logger.warn("Unable to find state manager with key - " + handle.getId().str());
        else
            manager.processOpenNewRequestState();
    }

    /**
     * Method handleSyncStateReqMsg
     * @param    cMsg                a  COPSSyncStateMsg
     */
    private void handleSyncStateReqMsg(final COPSSyncStateMsg cMsg) throws COPSException {
        if (cMsg.getIntegrity() != null) {
            logger.warn("Unsupported objects (Integrity)");
        }

        final COPSPepReqStateMan manager = _managerMap.get(cMsg.getClientHandle());
        if (manager == null)
            logger.warn("Unable to find state manager with key - " + cMsg.getClientHandle().getId().str());
        else
            manager.processSyncStateRequest(cMsg);
    }

    /**
     * Method createRequestState
     * @param    clientHandle             a  String
     * @param    process                  a  COPSPepDataProcess
     * @return   a COPSPepmanager
     * @throws   COPSException
     * @throws   COPSPepException
     */
    public COPSPepReqStateMan addRequestState(final COPSHandle clientHandle, final COPSPepDataProcess process)
            throws COPSException {
        final COPSPepReqStateMan manager = new COPSPepReqStateMan(_clientType, clientHandle, process, _sock);
        if (_managerMap.get(clientHandle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);

        _managerMap.put(clientHandle, manager);
        logger.info("Added state manager with key - " + clientHandle);
        manager.initRequestState();
        return manager;
    }

    /**
     * Method deleteRequestState
     * @param    manager             a  COPSPepReqStateMan
     * @throws   COPSException
     */
    public void deleteRequestState(COPSPepReqStateMan manager) throws COPSException {
        manager.finalizeRequestState();
    }

    private void notifyCloseAllReqStateMan() throws COPSException {
        for (final COPSPepReqStateMan man: _managerMap.values()) {
            man.processClosedConnection(_error);
        }
    }

    private void notifyNoKAAllReqStateMan() throws COPSException {
        for (final COPSPepReqStateMan man: _managerMap.values()) {
            man.processNoKAConnection();
        }
    }

    private void notifyAcctAllReqStateMan() throws COPSException {
        for (final COPSPepReqStateMan man: _managerMap.values()) {
            man.processAcctReport();
        }
    }

}

