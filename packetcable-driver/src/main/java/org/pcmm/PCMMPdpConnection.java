/*
 @header@
 */

package org.pcmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for managing an provisioning connection at the PDP side.
 */
public class PCMMPdpConnection implements Runnable {


    public final static Logger logger = LoggerFactory.getLogger(PCMMPdpConnection.class);

    /**
    Socket connected to PEP
     */
    private Socket _sock;

    /**
    PEP identifier
    */
    private COPSPepId _pepId;

    /**
     *  Time of the latest keep-alive received
     */
    protected Date _lastRecKa;

    /**
   Maps a Client Handle to a Handler
     */
    protected Map<String, PCMMPdpReqStateMan> _managerMap;

    /**
     *  PDP policy data processor class
     */
    protected PCMMPdpDataProcess _process;

    /**
   Accounting timer value (secs)
     */
    protected short _acctTimer;

    /**
   Keep-alive timer value (secs)
     */
    protected short _kaTimer;

    /**
   COPS error returned by PEP
     */
    protected COPSError _error;

    /**
     * Creates a new PDP connection
     *
     * @param pepId PEP-ID of the connected PEP
     * @param sock Socket connected to PEP
     * @param process Object for processing policy data
     */
    public PCMMPdpConnection(COPSPepId pepId, Socket sock, PCMMPdpDataProcess process) {
        _sock = sock;
        _pepId = pepId;
        _managerMap = new ConcurrentHashMap<>();
        _kaTimer = 120;
        _process = process;
    }

    /**
     * Sets the keep-alive timer value
     * @param kaTimer Keep-alive timer value (secs)
     */
    public void setKaTimer(final short kaTimer) {
        _kaTimer = kaTimer;
    }

    /**
     * Sets the accounting timer value
     * @param acctTimer Accounting timer value (secs)
     */
    public void setAccTimer(final short acctTimer) {
        _acctTimer = acctTimer;
    }

    /**
     * Gets the accounting timer value
     * @return Accounting timer value (secs)
     */
    public short getAcctTimer() {
        return _acctTimer;
    }

    /**
     * Gets the handle map
     * @return   A <tt>Hashtable</tt> holding the handle map
     */
    public Map<String, PCMMPdpReqStateMan> getReqStateMans() {
        // Defensive copy
        return new HashMap<>(_managerMap);
    }

    /**
     * Checks whether the socket to the PEP is closed or not
     * @return   <tt>true</tt> if closed, <tt>false</tt> otherwise
     */
    public boolean isClosed() {
        return _sock.isClosed();
    }

    /**
     * Closes the socket to the PEP
     * @throws IOException
     */
    protected void close()
    throws IOException {
        _sock.close();
    }

    /**
     * Gets the socket to the PEP
     * @return   Socket connected to the PEP
     */
    public Socket getSocket() {
        return _sock;
    }

    /**
     * Main loop
     */
    public void run () {
        Date _lastSendKa = new Date();
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

                    if ((cTime - _startTime) > ((_kaTimer*3/4)*1000)) {
                        // TODO - determine what is the client type to be used here?
                        final COPSKAMsg msg = new COPSKAMsg(null);
                        COPSTransceiver.sendMsg(msg, _sock);
                        _lastSendKa = new Date();
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    logger.error("Unexpected exception while sleeping", e);
                }

            }
        } catch (Exception e) {
            logger.error("Error reading messages from socket", e);
        }

        // connection closed by server
        // COPSDebug.out(getClass().getName(),"Connection closed by client");
        try {
            _sock.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }

        // Notify all Request State Managers
        try {
            notifyCloseAllReqStateMan();
        } catch (COPSPdpException e) {
            logger.error("Error closing state managers", e);
        }
    }

    /**
     * Gets a COPS message from the socket and processes it
     * @param    conn Socket connected to the PEP
     */
    private void processMessage(final Socket conn) throws COPSPdpException, COPSException, IOException {
        final COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        if (msg.getHeader().getOpCode().equals(OPCode.CC)) {
            handleClientCloseMsg(conn, msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.KA)) {
            handleKeepAliveMsg(conn, msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.REQ)) {
            handleRequestMsg(conn, msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.RPT)) {
            handleReportMsg(conn, msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.DRQ)) {
            handleDeleteRequestMsg(conn, msg);
        } else if (msg.getHeader().getOpCode().equals(OPCode.SSQ)) {
            handleSyncComplete(conn, msg);
        } else {
            throw new COPSPdpException("Message not expected (" + msg.getHeader().getOpCode() + ").");
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
     *  <Error>
     *  [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     */
    private void handleClientCloseMsg(Socket conn, COPSMsg msg) {
        COPSClientCloseMsg cMsg = (COPSClientCloseMsg) msg;
        _error = cMsg.getError();
        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }

            conn.close();
        } catch (Exception unae) {
            logger.error("Unexpected exception closing connection", unae);
        }
    }

    /**
     * Gets the occurred COPS Error
     * @return   <tt>COPSError</tt> object
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

        COPSKAMsg kaMsg = (COPSKAMsg) msg;
        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }

            kaMsg.writeData(conn);
        } catch (Exception unae) {
            logger.error("Unexpected exception while writing keep-alive message", unae);
        }
    }

    /**
     * Handle Delete Request Message
     *
     * <Delete Request> ::= <Common Header>
     *                      <Client Handle>
     *                      <Reason>
     *                      [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleDeleteRequestMsg(Socket conn, COPSMsg msg) throws COPSPdpException {
        COPSDeleteMsg cMsg = (COPSDeleteMsg) msg;
        // COPSDebug.out(getClass().getName(),"Removing ClientHandle for " +
        //  conn.getInetAddress() + ":" + conn.getPort() + ":[Reason " + cMsg.getReason().getDescription() + "]");

        // Support
        if (cMsg.getIntegrity() != null) {
            logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        // Delete clientHandler
        if (_managerMap.remove(cMsg.getClientHandle().getId().str()) == null) {
            // TODO - Determine what to do here???
            // COPSDebug.out(getClass().getName(),"Missing for ClientHandle " +
            //  cMsg.getClientHandle().getId().getData());
        }

        final PCMMPdpReqStateMan man = _managerMap.get(cMsg.getClientHandle().getId().str());
        if (man == null) {
            logger.warn("State manager not found");
        } else {
            man.processDeleteRequestState(cMsg);
        }

    }

    /**
     * Handle Request Message
     *
     * <Request> ::= <Common Header>
     *  <Client Handle>
     *  <Context>
     *  *(<Named ClientSI>)
     *  [<Integrity>]
     * <Named ClientSI> ::= <*(<PRID> <EPD>)>
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleRequestMsg(Socket conn, COPSMsg msg) throws COPSPdpException {

        final COPSReqMsg reqMsg = (COPSReqMsg) msg;
//        COPSContext cntxt = reqMsg.getContext();
        final COPSHeader header = reqMsg.getHeader();
        //short reqType = cntxt.getRequestType();
        final short cType = header.getClientType();

        // Support
        if (reqMsg.getIntegrity() != null) {
            logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        PCMMPdpReqStateMan man;
        man = _managerMap.get(reqMsg.getClientHandle().getId().str());
        if (man == null) {

            man = new PCMMPdpReqStateMan(cType, reqMsg.getClientHandle().getId().str());
            _managerMap.put(reqMsg.getClientHandle().getId().str(), man);
            man.setDataProcess(_process);
            man.initRequestState(_sock);

            // COPSDebug.out(getClass().getName(),"createHandler called, clientType=" +
            //    header.getClientType() + " msgType=" +
            //    cntxt.getMessageType() + ", connId=" + conn.toString());
        }

        man.processRequest(reqMsg);
    }

    /**
     * Handle Report Message
     *
     * <Report State> ::= <Common Header>
     *  <Client Handle>
     *  <Report Type>
     *  *(<Named ClientSI>)
     *  [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleReportMsg(Socket conn, COPSMsg msg)
    throws COPSPdpException {
        COPSReportMsg repMsg = (COPSReportMsg) msg;
        // COPSHandle handle = repMsg.getClientHandle();
        // COPSHeader header = repMsg.getHeader();

        // Support
        if (repMsg.getIntegrity() != null) {
            logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        final PCMMPdpReqStateMan man = _managerMap.get(repMsg.getClientHandle().getId().str());
        if (man == null) {
            logger.warn("State manager not found");
        } else {
            man.processReport(repMsg);
        }
    }

    /**
     * Method handleSyncComplete
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleSyncComplete(Socket conn, COPSMsg msg)
    throws COPSPdpException {
        COPSSyncStateMsg cMsg = (COPSSyncStateMsg) msg;
        // COPSHandle handle = cMsg.getClientHandle();
        // COPSHeader header = cMsg.getHeader();

        // Support
        if (cMsg.getIntegrity() != null) {
            logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        final PCMMPdpReqStateMan man = _managerMap.get(cMsg.getClientHandle().getId().str());
        if (man == null) {
            logger.warn("State manager not found");
        } else {
            man.processSyncComplete(cMsg);
        }
    }

    /**
     * Requests a COPS sync from the PEP
     * @throws COPSException
     * @throws COPSPdpException
     */
    protected void syncAllRequestState() throws COPSException, COPSPdpException {
        for (final PCMMPdpReqStateMan man : _managerMap.values()) {
            man.syncRequestState();
        }
    }

    private void notifyCloseAllReqStateMan() throws COPSPdpException {
        for (final PCMMPdpReqStateMan man : _managerMap.values()) {
            man.processClosedConnection(_error);
        }
    }

    private void notifyNoKAAllReqStateMan() throws COPSPdpException {
        for (final PCMMPdpReqStateMan man : _managerMap.values()) {
            man.processNoKAConnection();
        }
    }

}
