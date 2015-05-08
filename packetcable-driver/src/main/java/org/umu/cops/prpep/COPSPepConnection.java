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

    public final static Logger logger = LoggerFactory.getLogger(COPSPepConnection.class);

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
    protected final Map<String, COPSPepReqStateMan> _managerMap;

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
    protected byte processMessage(final Socket conn) throws COPSPepException, COPSException, IOException {
        final COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        switch (msg.getHeader().getOpCode()) {
            case CC:
                handleClientCloseMsg(conn, msg);
                return (byte)OPCode.CC.ordinal();
            case DEC:
                handleDecisionMsg(conn, msg);
                return (byte)OPCode.DEC.ordinal();
            case SSQ:
                handleSyncStateReqMsg(conn, msg);
                return (byte)OPCode.SSQ.ordinal();
            case KA:
                handleKeepAliveMsg(conn, msg);
                return (byte)OPCode.KA.ordinal();
            default:
                throw new COPSPepException("Message not expected (" + msg.getHeader().getOpCode() + ").");
        }
    }

    /**
     * Handle Client Close Message, close the passed connection
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     *
     * <Client-Close> ::= <Common Header>
     *                      <Error>
     *                      [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     */
    private void handleClientCloseMsg(Socket conn, COPSMsg msg) {
        COPSClientCloseMsg cMsg = (COPSClientCloseMsg) msg;
        _error = cMsg.getError();

        // COPSDebug.out(getClass().getName(),"Got close request, closing connection " +
        //  conn.getInetAddress() + ":" + conn.getPort() + ":[Error " + _error.getDescription() + "]");

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
     *
     * @return   a COPSError
     *
     */
    protected COPSError getError()  {
        return _error;
    }

    /**
     * Handle Keep Alive Message
     *
     * <Keep-Alive> ::= <Common Header>
     *                  [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleKeepAliveMsg(Socket conn, COPSMsg msg) {
        COPSKAMsg cMsg = (COPSKAMsg) msg;

        // COPSDebug.out(getClass().getName(),"Get KAlive Msg");

        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }

            // should we do anything else?? ....

        } catch (Exception unae) {
            logger.error("Unexpected exception while writing COPS data", unae);
        }
    }

    /**
     * Method handleDecisionMsg
     *
     * <Decision Message> ::= <Common Header: Flag SOLICITED>
     *                          <Client Handle>
     *                          *(<Decision>) | <Error>
     *                          [<Integrity>]
     * <Decision> ::= <Context>
     *                  <Decision: Flags>
     *                  [<Named Decision Data: Provisioning>]
     * <Decision: Flags> ::= <Command-Code> NULLFlag
     * <Command-Code> ::= NULLDecision | Install | Remove
     * <Named Decision Data> ::= <<Install Decision> | <Remove Decision>>
     * <Install Decision> ::= *(<PRID> <EPD>)
     * <Remove Decision> ::= *(<PRID> | <PPRID>)
     *
     * Very important, this is actually being treated like this:
     * <Install Decision> ::= <PRID> | <EPD>
     * <Remove Decision> ::= <PRID> | <PPRID>
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleDecisionMsg(final Socket conn, final COPSMsg msg) throws COPSPepException {
        final COPSDecisionMsg dMsg = (COPSDecisionMsg) msg;
        final COPSHandle handle = dMsg.getClientHandle();
        final Map<COPSContext, Set<COPSDecision>> decisions = dMsg.getDecisions();

        for (final Set<COPSDecision> copsDecisions: decisions.values()) {
            for (final COPSDecision decision : copsDecisions) {
                // Get the associated manager
                final COPSPepReqStateMan manager = _managerMap.get(handle.getId().str());
                if (manager == null)
                    logger.warn("Unable to find state manager with key - " + handle.getId().str());

                // Check message type
                // TODO FIXME - Use of manager object could result in a NPE
                if (decision.getFlag().equals(DecisionFlag.REQSTATE)) {
                    if (decision.getCommand().equals(Command.REMOVE))
                        // Delete Request State
                        manager.processDeleteRequestState(dMsg);
                    else
                        // Open new Request State
                        handleOpenNewRequestStateMsg(conn, handle);
                } else
                    // Decision
                    manager.processDecision(dMsg);
            }
        }
    }


    /**
     * Method handleOpenNewRequestStateMsg
     *
     * @param    conn                a  Socket
     * @param    handle              a  COPSHandle
     *
     */
    private void handleOpenNewRequestStateMsg(Socket conn, COPSHandle handle) throws COPSPepException {

        COPSPepReqStateMan manager = _managerMap.get(handle.getId().str());
        if (manager == null)
            logger.warn("Unable to find state manager with key - " + handle.getId().str());

        // TODO FIXME - Use of manager object could result in a NPE
        manager.processOpenNewRequestState();
    }

    /**
     * Method handleSyncStateReqMsg
     *
     *              <Synchronize State> ::= <Common Header>
     *                                      [<Client Handle>]
     *                                      [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleSyncStateReqMsg(Socket conn, COPSMsg msg) throws COPSPepException {
        COPSSyncStateMsg cMsg = (COPSSyncStateMsg) msg;

        // Support
        if (cMsg.getIntegrity() != null) {
            logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        COPSPepReqStateMan manager = _managerMap.get(cMsg.getClientHandle().getId().str());
        if (manager == null) {
            logger.warn("Unable to find state manager with key - " + cMsg.getClientHandle().getId().str());
        } else {
            manager.processSyncStateRequest(cMsg);
        }
    }

    /**
     * Method createRequestState
     *
     * @param    clientHandle             a  String
     * @param    process                  a  COPSPepDataProcess
     *
     * @return   a COPSPepmanager
     *
     * @throws   COPSException
     * @throws   COPSPepException
     *
     */
    protected COPSPepReqStateMan addRequestState(String clientHandle, COPSPepDataProcess process) throws COPSException,
            COPSPepException {
        COPSPepReqStateMan manager = new COPSPepReqStateMan(_clientType, clientHandle);
        if (_managerMap.get(clientHandle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);

        manager.setDataProcess(process);
        _managerMap.put(clientHandle,manager);
        manager.initRequestState(_sock);
        return manager;
    }

    /**
     * Method deleteRequestState
     *
     * @param    manager             a  COPSPepReqStateMan
     *
     * @throws   COPSException
     * @throws   COPSPepException
     *
     */
    protected void deleteRequestState(COPSPepReqStateMan manager) throws COPSException, COPSPepException {
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

