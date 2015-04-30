/*
 @header@
 */

package org.pcmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for managing an provisioning connection at the PDP side for receiving and brokering out COPS messages.
 */
@ThreadSafe
public class PCMMPdpConnection implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(PCMMPdpConnection.class);

    /**
    Socket connected to PEP
     */
    private final Socket _sock;

    /**
     * PEP identifier
     * TODO - Determine why the original author put this object into this class
     */
    private final COPSPepId _pepId;

    /**
     * Time of the latest keep-alive received
     */
    protected Date _lastRecKa;

    /**
     * Maps a Client Handle to a Handler
     */
    protected final Map<String, PCMMPdpReqStateMan> _managerMap;

    /**
     *  PDP policy data processor class
     */
    protected final PCMMPdpDataProcess _process;

    /**
     * Accounting timer value (secs)
     */
    protected final short _acctTimer;

    /**
     * Keep-alive timer value (secs)
     */
    protected final short _kaTimer;

    /**
     * COPS error returned by PEP on close
     */
    protected transient COPSError _error;

    /**
     * Creates a new PDP connection
     *
     * @param pepId PEP-ID of the connected PEP
     * @param sock Socket connected to PEP
     * @param process Object for processing policy data
     */
    public PCMMPdpConnection(final COPSPepId pepId, final Socket sock, final PCMMPdpDataProcess process,
                             final short kaTimer, final short acctTimer) {
        _pepId = pepId;
        _sock = sock;
        _process = process;
        _kaTimer = kaTimer;
        _acctTimer = acctTimer;
        _managerMap = new ConcurrentHashMap<>();
    }

    public void addStateMan(final String key, final PCMMPdpReqStateMan man) {
        _managerMap.put(key, man);
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
    protected void close() throws IOException {
        if (!_sock.isClosed()) _sock.close();
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
        logger.info("Starting socket listener.");
        Date _lastSendKa = new Date();
        _lastRecKa = new Date();

        // Loop while socket is open
        while (!_sock.isClosed()) {
            try {
                if (_sock.getInputStream().available() != 0) {
                    logger.info("Waiting to process socket messages");
                    processMessage(_sock);
                    logger.info("Message processed");
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
                        final COPSKAMsg msg = new COPSKAMsg(null);
                        logger.info("Sending KA message to CCAP");
                        COPSTransceiver.sendMsg(msg, _sock);
                        logger.info("Sent KA message gto CCAP");
                        _lastSendKa = new Date();
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.info("Shutting down socket connection to CCAP");
                    break;
                }
            } catch (IOException e) {
                logger.error("Exception reading from socket - exiting", e);
                break;
            } catch (COPSException e) {
                logger.error("Exception processing message - continue processing", e);
            }
        }

        try {
            if (! _sock.isClosed())
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
    private void processMessage(final Socket conn) throws COPSException, IOException {
        final COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        logger.info("Processing message received of type - " + msg.getHeader().getOpCode());

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
            case SSQ:
                handleSyncComplete(conn, (COPSSyncStateMsg)msg);
                break;
            default:
                throw new COPSPdpException("Message not expected (" + msg.getHeader().getOpCode() + ").");
        }
    }

    /**
     * Handle Client Close Message, close the passed connection
     * @param    conn                a  Socket
     * @param    cMsg                a  COPSClientCloseMsg
     */
    private void handleClientCloseMsg(final Socket conn, final COPSClientCloseMsg cMsg) {
        _error = cMsg.getError();
        logger.info("Closing client with error - " + _error.getDescription());
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
     * Handle Keep Alive Message
     * @param    conn                a  Socket
     * @param    kaMsg               a  COPSKAMsg
     */
    private void handleKeepAliveMsg(final Socket conn, final COPSKAMsg kaMsg) {
        try {
            // Support
            if (kaMsg.getIntegrity() != null) {
                logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }
            kaMsg.writeData(conn);
        } catch (Exception unae) {
            logger.error("Unexpected exception while writing keep-alive message", unae);
        }
    }

    /**
     * Handle Delete Request Message
     * @param    conn                a  Socket
     * @param    cMsg                a  COPSDeleteMsg
     */
    private void handleDeleteRequestMsg(final Socket conn, final COPSDeleteMsg cMsg) throws COPSPdpException {
        // Support
        if (cMsg.getIntegrity() != null) {
            logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        // Delete clientHandler
        final PCMMPdpReqStateMan man = _managerMap.remove(cMsg.getClientHandle().getId().str());
        if (man == null) {
            logger.warn("Cannot delete request state, no state manger found");
        } else {
            man.processDeleteRequestState(cMsg);
        }
    }

    /**
     * Handle Request Message
     * @param    conn                a  Socket
     * @param    reqMsg              a  COPSReqMsg
     */
    private void handleRequestMsg(final Socket conn, final COPSReqMsg reqMsg) throws COPSPdpException {
        final COPSHeader header = reqMsg.getHeader();
        final short cType = header.getClientType();

        // Support
        if (reqMsg.getIntegrity() != null) {
            logger.error("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        final PCMMPdpReqStateMan man;
        if (_managerMap.get(reqMsg.getClientHandle().getId().str()) == null) {

            man = new PCMMPdpReqStateMan(cType, reqMsg.getClientHandle().getId().str());
            _managerMap.put(reqMsg.getClientHandle().getId().str(), man);
            man.setDataProcess(_process);
            man.initRequestState(_sock);
            logger.info("Created state manager for ID - " + reqMsg.getClientHandle().getId().str());
        } else {
            man = _managerMap.get(reqMsg.getClientHandle().getId().str());
        }

        man.processRequest(reqMsg);
    }

    /**
     * Handle Report Message
     * @param    conn                a  Socket
     * @param    repMsg              a  COPSReportMsg
     */
    private void handleReportMsg(final Socket conn, final COPSReportMsg repMsg) throws COPSPdpException {
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
     * @param    conn                a  Socket
     * @param    cMsg                a  COPSSyncStateMsg
     */
    private void handleSyncComplete(final Socket conn, final COPSSyncStateMsg cMsg) throws COPSPdpException {
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
    protected void syncAllRequestState() throws COPSException {
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
