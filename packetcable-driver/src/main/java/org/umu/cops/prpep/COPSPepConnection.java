/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSHeader.OPCode;

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
public class COPSPepConnection implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(COPSPepConnection.class);

    /** Socket connected to PDP */
    protected Socket _sock;

    /** Time to wait responses (milliseconds), default is 10 seconds */
    protected int _responseTime;

    /** COPS Client-type */
    protected short _clientType;

    /**
        Accounting timer value (secs)
     */
    protected short _acctTimer;

    /**
        Keep-alive timer value (secs)
     */
    protected short _kaTimer;

    /**
     *  Time of the latest keep-alive received
     */
    protected Date _lastRecKa;

    /**
        Maps a COPS Client Handle to a Request State Manager
     */
    protected final Map<COPSHandle, COPSPepReqStateMan> _managerMap;

    /**
        COPS error returned by PDP
     */
    protected COPSError _error;

    /**
     * Creates a new PEP connection
     * @param clientType    PEP's client-type
     * @param sock          Socket connected to PDP
     */
    public COPSPepConnection(final short clientType, final Socket sock) {
        _clientType = clientType;
        _sock = sock;

        // Timers
        _acctTimer = 0;
        _kaTimer = 0;
        _responseTime = 10000;
        _managerMap = new ConcurrentHashMap<>();
    }

    /**
     * Gets the response time
     * @return  Response time value (msecs)
     */
    public int getResponseTime() {
        return _responseTime;
    }

    /**
     * Gets the socket connected to the PDP
     * @return  Socket connected to PDP
     */
    public Socket getSocket() {
        return _sock;
    }

    /**
     * Gets keep-alive timer
     * @return  Keep-alive timer value (secs)
     */
    public short getKaTimer () {
        return _kaTimer;
    }

    /**
     * Gets accounting timer
     * @return  Accounting timer value (secs)
     */
    public short getAcctTimer () {
        return _acctTimer;
    }

    /**
     * Checks whether the socket to the PDP is closed or not
     * @return  <tt>true</tt> if the socket is closed, <tt>false</tt> otherwise
     */
    public boolean isClosed() {
        return _sock.isClosed();
    }

    /**
     * Closes the socket
     *
     * @throws java.io.IOException
     */
    protected void close()
    throws IOException {
        _sock.close();
    }

    /**
     * Sets response time
     * @param respTime  Response time value (msecs)
     */
    public void setResponseTime(int respTime) {
        _responseTime = respTime;
    }

    /**
     * Sets keep-alive timer
     * @param kaTimer   Keep-alive timer value (secs)
     */
    public void setKaTimer (short kaTimer) {
        _kaTimer = kaTimer;
    }

    /**
     * Sets accounting timer
     * @param acctTimer Accounting timer value (secs)
     */
    public void setAcctTimer (short acctTimer) {
        _acctTimer = acctTimer;
    }

    /**
     * Message-processing loop
     */
    public void run () {
        Date _lastSendKa = new Date();
        Date _lastSendAcc = new Date();
        _lastRecKa = new Date();
        try {
            while (!_sock.isClosed()) {
                if (_sock.getInputStream().available() != 0) {
                    processMessage(_sock);
                    _lastRecKa = new Date();
                }

                // Keep Alive
                if (_kaTimer > 0) {
                    // Timeout at PDP
                    int _startTime = (int) (_lastRecKa.getTime());
                    int cTime = (int) (new Date().getTime());

                    if ((cTime - _startTime) > _kaTimer*1000) {
                        _sock.close();
                        // Notify all Request State Managers
                        notifyNoKAAllReqStateMan();
                    }

                    // Send to PEP
                    _startTime = (int) (_lastSendKa.getTime());
                    cTime = (int) (new Date().getTime());

                    if ((cTime - _startTime) > ((_kaTimer*3/4) * 1000)) {
                        final COPSKAMsg msg = new COPSKAMsg(null);
                        COPSTransceiver.sendMsg(msg, _sock);
                        _lastSendKa = new Date();
                    }
                }

                // Accounting
                if (_acctTimer > 0) {
                    int _startTime = (int) (_lastSendAcc.getTime());
                    int cTime = (int) (new Date().getTime());

                    if ((cTime - _startTime) > ((_acctTimer*3/4)*1000)) {
                        // Notify all Request State Managers
                        notifyAcctAllReqStateMan();
                        _lastSendAcc = new Date();
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
        // COPSDebug.out(getClass().getName(),"Connection closed by server");
        try {
            _sock.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }

        // Notify all Request State Managers
        try {
            notifyCloseAllReqStateMan();
        } catch (COPSPepException e) {
            logger.error("Error closing state managers");
        }
    }

    /**
     * Gets a COPS message from the socket and processes it
     * @param conn  Socket connected to the PDP
     * @return COPS message type
     * @throws COPSPepException
     * @throws COPSException
     * @throws IOException
     */
    protected byte processMessage(final Socket conn) throws COPSException, IOException {
        final COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        switch (msg.getHeader().getOpCode()) {
            case CC:
                handleClientCloseMsg(conn, (COPSClientCloseMsg)msg);
                return (byte)OPCode.CC.ordinal();
            case DEC:
                handleDecisionMsg((COPSDecisionMsg)msg);
                return (byte)OPCode.DEC.ordinal();
            case SSQ:
                handleSyncStateReqMsg((COPSSyncStateMsg)msg);
                return (byte)OPCode.SSQ.ordinal();
            case KA:
                handleKeepAliveMsg((COPSKAMsg)msg);
                return (byte)OPCode.KA.ordinal();
            default:
                throw new COPSPepException("Message not expected (" + msg.getHeader().getOpCode() + ").");
        }
    }

    /**
     * Handle Client Close Message, close the passed connection
     * @param    conn                a  Socket
     * @param    cMsg                a  COPSClientCloseMsg
     */
    private void handleClientCloseMsg(final Socket conn, final COPSClientCloseMsg cMsg) {
        _error = cMsg.getError();
        logger.info("Got close request, closing connection "
                + conn.getInetAddress() + ":" + conn.getPort() + ":[Error " + _error.getDescription() + "]");
        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }
            conn.close();
        } catch (Exception unae) {
            logger.error("Unexpected exception closing connection", unae);
        }
    }

    /**
     * Method getError
     * @return   a COPSError
     */
    protected COPSError getError()  {
        return _error;
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
    private void handleDecisionMsg(final COPSDecisionMsg dMsg) throws COPSException {
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
                    manager.processDecision(dMsg, _sock);
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
    private void handleSyncStateReqMsg(final COPSSyncStateMsg cMsg) throws COPSPepException {
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
    protected COPSPepReqStateMan addRequestState(final COPSHandle clientHandle, final COPSPepDataProcess process)
            throws COPSException {
        final COPSPepReqStateMan manager = new COPSPepReqStateMan(_clientType, clientHandle, process);
        if (_managerMap.get(clientHandle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);

        _managerMap.put(clientHandle, manager);
        logger.info("Added state manager with key - " + clientHandle);
        manager.initRequestState(_sock);
        return manager;
    }

    /**
     * Method deleteRequestState
     * @param    manager             a  COPSPepReqStateMan
     * @throws   COPSException
     * @throws   COPSPepException
     */
    protected void deleteRequestState(COPSPepReqStateMan manager) throws COPSException {
        manager.finalizeRequestState();
    }

    private void notifyCloseAllReqStateMan() throws COPSPepException {
        for (final COPSPepReqStateMan man: _managerMap.values()) {
            man.processClosedConnection(_error);
        }
    }

    private void notifyNoKAAllReqStateMan() throws COPSPepException {
        for (final COPSPepReqStateMan man: _managerMap.values()) {
            man.processNoKAConnection();
        }
    }

    private void notifyAcctAllReqStateMan() throws COPSPepException {
        for (final COPSPepReqStateMan man: _managerMap.values()) {
            man.processAcctReport();
        }
    }

}

