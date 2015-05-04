package org.umu.cops.ospep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COPSPepConnection represents a PEP-PDP Connection Manager.
 * Responsible for processing messages received from PDP.
 */
public class COPSPepOSConnection implements Runnable {

    public final static Logger logger = LoggerFactory.getLogger(COPSPepOSConnection.class);

    /** Socket connected to PDP */
    protected Socket _sock;

    /** Time to wait responses (milliseconds), default is 10 seconds */
    protected final int _responseTime;

    /** COPS Client-type */
    protected short _clientType;

    /**
        Accounting timer value (secs)
     */
    protected transient short _acctTimer;

    /**
        Keep-alive timer value (secs)
     */
    protected transient short _kaTimer;

    /**
     *  Time of the latest keep-alive received
     */
    protected Date _lastRecKa;

    /**
        Maps a COPS Client Handle to a Request State Manager
     */
    protected final Map<String, COPSPepOSReqStateMan> _managerMap;

    /**
        COPS error returned by PDP
     */
    protected COPSError _error;

    /**
     * Creates a new PEP connection
     * @param clientType    PEP's client-type
     * @param sock          Socket connected to PDP
     */
    public COPSPepOSConnection(final short clientType, final Socket sock) {
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
     * Gets all request state managers
     * @return  A <tt>Hashatable</tt> holding all request state managers
     * TODO - change the return to Map
     */
    protected Hashtable getReqStateMans() {
        return new Hashtable(_managerMap);
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
    protected void close() throws IOException {
        _sock.close();
    }

    /**
     * Sets keep-alive timer
     * @param kaTimer   Keep-alive timer value (secs)
     */
    public void setKaTimer(short kaTimer) {
        _kaTimer = kaTimer;
    }

    /**
     * Sets accounting timer
     * @param acctTimer Accounting timer value (secs)
     */
    public void setAcctTimer(short acctTimer) {
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
                    // Timeout del PDP
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
            logger.error("Unexpected exception closing the socket", e);
        }

        // Notify all Request State Managers
        try {
            notifyCloseAllReqStateMan();
        } catch (COPSPepException e) {
            logger.error("Error closing state managers", e);
        }
    }

    /**
     * Gets a COPS message from the socket and processes it
     * @param conn  Socket connected to the PDP
     * @throws COPSPepException
     * @throws COPSException
     * @throws IOException
     */
    protected void processMessage(Socket conn) throws COPSException, IOException {
        COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        if (msg.getHeader().getOpCode().equals(OPCode.CC)) {
            handleClientCloseMsg(conn, msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.DEC)) {
            handleDecisionMsg(/*OJO conn, */msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.SSQ)) {
            handleSyncStateReqMsg(conn, msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.KA)) {
            handleKeepAliveMsg(conn, msg);
        } else {
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
            if (cMsg.getIntegrity() != null)
                logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());

            conn.close();
        } catch (Exception unae) {
            logger.error("Unexpected exception closing connection", unae);
        }
    }

    /**
     * Gets the COPS error
     * @return  <tt>COPSError</tt> returned by PDP
     */
    protected COPSError getError() {
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

        // Support
        if (cMsg.getIntegrity() != null)
            logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());

        // must we do anything else?
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
     *                  [<ClientSI Decision Data: Outsourcing>]
     * <Decision: Flags> ::= <Command-Code> NULLFlag
     * <Command-Code> ::= NULLDecision | Install | Remove
     * <ClientSI Decision Data> ::= <<Install Decision> | <Remove Decision>>
     * <Install Decision> ::= *(<PRID> <EPD>)
     * <Remove Decision> ::= *(<PRID> | <PPRID>)
     *
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleDecisionMsg(/*OJO Socket conn, */COPSMsg msg) throws COPSPepException {
        COPSDecisionMsg dMsg = (COPSDecisionMsg) msg;
        COPSHandle handle = dMsg.getClientHandle();
        COPSPepOSReqStateMan manager = _managerMap.get(handle.getId().str());
        manager.processDecision(dMsg);
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
    private void handleSyncStateReqMsg(final Socket conn, final COPSMsg msg) throws COPSException {
        final COPSSyncStateMsg cMsg = (COPSSyncStateMsg) msg;
        // COPSHandle handle = cMsg.getClientHandle();
        // COPSHeader header = cMsg.getHeader();

        // Support
        if (cMsg.getIntegrity() != null)
            logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());

        final COPSPepOSReqStateMan manager = _managerMap.get(cMsg.getClientHandle().getId().str());

        if (manager == null)
            logger.warn("Unable to find state manager with ID - " + cMsg.getClientHandle().getId().str());
        else
            manager.processSyncStateRequest(cMsg);
    }

    /**
     * Adds a new request state
     * @param clientHandle  Client's handle
     * @param process       Policy data processing object
     * @param clientSIs     Client data from the outsourcing event
     * @return              The newly created request state manager
     * @throws COPSException
     */
    protected COPSPepOSReqStateMan addRequestState(final String clientHandle, final COPSPepOSDataProcess process,
                                                   final List<COPSClientSI> clientSIs) throws COPSException {
        final COPSPepOSReqStateMan manager = new COPSPepOSReqStateMan(_clientType,
                new COPSHandle(new COPSData(clientHandle)), process, clientSIs);
        if (_managerMap.get(clientHandle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);
        _managerMap.put(clientHandle, manager);
        manager.initRequestState(_sock);
        return manager;
    }

    /**
     * Deletes a request state
     * @param manager   Request state manager
     * @throws COPSPepException
     */
    protected void deleteRequestState(COPSPepOSReqStateMan manager) throws COPSPepException {
        manager.finalizeRequestState();
    }

    private void notifyCloseAllReqStateMan() throws COPSPepException {
        for (final COPSPepOSReqStateMan man : _managerMap.values()) {
                man.processClosedConnection(_error);
        }
    }

    private void notifyNoKAAllReqStateMan() throws COPSPepException {
        for (final COPSPepOSReqStateMan man : _managerMap.values()) {
            man.processNoKAConnection();
        }
    }

    private void notifyAcctAllReqStateMan() throws COPSPepException {
        for (final COPSPepOSReqStateMan man : _managerMap.values()) {
            man.processAcctReport();
        }
    }

}
