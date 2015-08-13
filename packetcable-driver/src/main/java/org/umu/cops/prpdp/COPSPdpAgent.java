/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.pcmm.objects.MMVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Core PDP agent for managing the connection to one PDP.
 */
public class COPSPdpAgent {

    private static final Logger logger = LoggerFactory.getLogger(COPSPdpAgent.class);

    /** Well-known port for COPS */
    public static final int WELL_KNOWN_PDP_PORT = 3288;
    /** Default keep-alive timer value (secs) */
    public static final short KA_TIMER_VALUE = 30;
    /** Default accounting timer value (secs) */
    public static final short ACCT_TIMER_VALUE = 0;

    /**
     * PDP host address
     */
    protected final String _host;

    /**
     * PDP host port
     */
    protected final int _serverPort;

    /**
     * Client-type of connecting PEP
     */
    protected final short _clientType;

    /**
     * Accounting timer (secs)
     */
    protected final short _acctTimer;

    /**
     * Keep-alive timer (secs)
     */
    protected final short _kaTimer;

    /**
     *  Policy data processing object
     */
    protected final COPSPdpDataProcess _process;

    // Next two attributes are initialized when connected
    /**
     * The Socket connection to the PEP
     */
    protected transient Socket _socket;

    /**
     * The PEP handle
     */
    protected transient COPSHandle _handle;

    // Next three attributes are initialized after the client accepts
    /**
     * Holds the last PEP ID processed
     */
    protected transient COPSPepId _pepId;

    /**
     * the PDP connection connection
     */
    protected transient COPSPdpConnection _pdpConn;

    /**
     * The handle to the tread accepting messages from the PDP
     */
    protected transient Thread _thread;

    /**
     * Creates a PDP Agent
     *
     * @param host  PDP agent host name
     * @param port  Port to listen to
     * @param clientType    COPS Client-type
     * @param process   Object to perform policy data processing
     */
    public COPSPdpAgent(final String host, final int port, final short clientType, final COPSPdpDataProcess process) {
        this._host = host;
        this._serverPort = port;

        this._kaTimer = KA_TIMER_VALUE;
        this._acctTimer = ACCT_TIMER_VALUE;

        this._clientType = clientType;
        this._process = process;
    }

    /**
     * Returns handle after connect() has successfully been executed
     * @return - the handle
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Returns handle after connect() has successfully been executed
     * @return - the handle
     */
    public Socket getSocket() {
        return _socket;
    }

    /**
     * Connects to a PDP
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     * @throws COPSException
     */
    public void connect() throws IOException, COPSException {
        // Create Socket and send OPN
        _socket = new Socket();
        _socket.connect(new InetSocketAddress(InetAddress.getByName(_host), _serverPort));
        logger.info("PDP Socket Opened. Waiting to receive client-open message");
        final COPSMsg msg = COPSTransceiver.receiveMsg(_socket);
        logger.debug("Message received of type - " + msg.getHeader().getOpCode());
        if (msg.getHeader().getOpCode().equals(OPCode.OPN)) {
            handleClientOpenMsg(_socket, msg);
        } else {
            try {
                _socket.close();
            } catch (Exception ex) {
                logger.error("Unexpected error closing socket", ex);
            }
        }
    }

    /**
     * Disconnects a PEP and stops the listener thread
     * @param error COPS Error to be reported as a reason
     */
    public void disconnect(final COPSError error) {
        if (_pdpConn != null) {
            sendCloseMessage(_socket, error.getErrCode(), error.getErrSubCode(), "Disconnecting from PDP requested");
            _pdpConn.close();
        } else {
            logger.warn("Unable to locate PDP connection. Cannot close");
        }
        if (_thread != null) _thread.interrupt();
        else logger.warn("Unable to locate PDP connection thread. Cannot stop it.");

        if (_socket != null && _socket.isConnected())
            try {
                _socket.close();
            } catch (IOException e) {
                logger.error("Error closing socket", e);
            }

        _socket = null;
        _pepId = null;
        _pdpConn = null;
        _thread = null;
    }

    /**
     * Requests a COPS sync for a PEP
     * @throws COPSException
     * @throws COPSPdpException
     */
    public void sync() throws COPSException {
        if (_pdpConn != null) _pdpConn.syncAllRequestState();
        else logger.warn("Unable to sync, not connected to a PEP");
    }

    /**
     * Handles a COPS client-open message and sets the _pepId, _handle, _pdpConn, & _thread objects in the process
     * as well as starts the PDP connection thread for receiving other COPS messages from the PDP
     * @param    conn Socket to the PEP
     * @param    msg <tt>COPSMsg</tt> holding the client-open message
     * @throws COPSException
     * @throws IOException
     */
    protected void handleClientOpenMsg(final Socket conn, final COPSMsg msg) throws COPSException, IOException {
        logger.info("Processing client open message");

        if (_pepId != null) {
            throw new COPSException("Connection already opened");
        }

        final COPSClientOpenMsg cMsg = (COPSClientOpenMsg) msg;
        _pepId = cMsg.getPepId();

        // Validate Client Type
        if (msg.getHeader().getClientType() != _clientType) {
            sendCloseMessage(conn, ErrorTypes.UNSUPPORTED_CLIENT_TYPE, ErrorTypes.NA,
                    "Unsupported client type");
        }

        // PEPId is mandatory
        if (_pepId == null) {
            sendCloseMessage(conn, ErrorTypes.MANDATORY_OBJECT_MISSING, ErrorTypes.NA,
                    "Mandatory COPS object missing (PEPId)");
        }

        // TODO - Determine if I should be checking for the PDPAddress and Integrity objects on the message too???
        // Support
/*
        if ( (cMsg.getClientSI() != null) || (cMsg.getPdpAddress() != null) || (cMsg.getIntegrity() != null)) {
            sendCloseMessage(conn, ErrorTypes.UNSUPPORTED_CLIENT_TYPE,
                    "Unsupported objects (ClientSI, PdpAddress, Integrity)");
        }
*/
        // Support
        if ((cMsg.getClientSI() == null) ) {
            sendCloseMessage(conn, ErrorTypes.UNKNOWN_OBJECT, ErrorTypes.NA,
                    "Unsupported objects (PdpAddress, Integrity)");
        } else {
            final MMVersionInfo _mminfo = MMVersionInfo.parse(cMsg.getClientSI().getData().getData());
            logger.debug("CMTS sent MMVersion info : major:" + _mminfo.getMajorVersionNB() + "  minor:" +
                    _mminfo.getMinorVersionNB());
        }

        acceptConnection(conn);

        _handle = handleAcceptResponse(conn);
        if (_handle != null) {
            // Connection accepted
            _pdpConn = setputPdpConnection(conn, _handle);
            _thread = new Thread(_pdpConn, "PDP Agent for PEP ID " + _pepId.getData().str());
            _thread.start();
        } else {
            throw new COPSException("Unable to connect to PDP");
        }
    }

    /**
     * Creates and sends a client close message
     * @param conn - the socket connection
     * @param errorType - the error type to send
     * @param msg - the error message to log
     */
    private void sendCloseMessage(final Socket conn, final ErrorTypes errorType, final ErrorTypes errorSubType,
                                  final String msg) {
        final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(_clientType,
                new COPSError(errorType, errorSubType), null, null);
        try {
            logger.info("Sending client-close message. Reason: " + msg);
            closeMsg.writeData(conn);
        } catch (IOException unae) {
            logger.error("Exception writing data", unae);
        }
    }

    /**
     * Sends a client-accept message back to the PDP
     * @param conn - the socket connection to the PDP
     * @throws IOException
     */
    private void acceptConnection(final Socket conn) throws IOException {
        final COPSClientAcceptMsg acceptMsg;
        if (_acctTimer != 0)
            acceptMsg = new COPSClientAcceptMsg(_clientType, new COPSKATimer(_kaTimer),
                    new COPSAcctTimer(_acctTimer), null);
        else
            acceptMsg = new COPSClientAcceptMsg(_clientType, new COPSKATimer(_kaTimer) ,null, null);
        acceptMsg.writeData(conn);
    }

    /**
     * Waits for the response back from the PDP and handles it appropriately. When successful, the handle to the
     * client is returned.
     * @param conn - the socket connection to the PDP
     * @return - the handle or null if not successful
     */
    private COPSHandle handleAcceptResponse(final Socket conn) {
        try {
            logger.debug("handleClientOpenMsg() - Waiting to receive message");
            final COPSMsg rmsg = COPSTransceiver.receiveMsg(conn);
            logger.debug("Received message of type - " + rmsg.getHeader().getOpCode());
            // Client-Close
            if (rmsg.getHeader().getOpCode().equals(OPCode.CC)) {
                logger.info("Received client-close message");
                sendCloseMessage(conn, ErrorTypes.SHUTTING_DOWN, ErrorTypes.NA, "Received client-close message");
                return null;
            } else {
                // Request
                if (rmsg.getHeader().getOpCode().equals(OPCode.REQ)) {
                    final COPSReqMsg rMsg = (COPSReqMsg) rmsg;
                    return rMsg.getClientHandle();
                } else {
                    sendCloseMessage(conn, ErrorTypes.UNKNOWN_OBJECT, ErrorTypes.NA, "Received unknown object");
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("Error COPSTransceiver.receiveMsg", e);
            return null;
        }
    }

    /**
     * Creates the PDP connection object
     * @param conn - the socket connection to the PDP
     * @param handle - the client's handle
     * @return - the PDP connection object
     */
    protected COPSPdpConnection setputPdpConnection(final Socket conn, final COPSHandle handle) {
        logger.debug("PDPCOPSConnection");
        final COPSPdpConnection pdpConn = new COPSPdpConnection(_pepId, conn, _process, _kaTimer, _acctTimer);

        // XXX - handleRequestMsg
        // XXX - check handle is valid
        final COPSPdpReqStateMan man = new COPSPdpReqStateMan(_clientType, handle, _process, conn);
        pdpConn.addStateMan(handle, man);
        // XXX - End handleRequestMsg

        logger.info("Starting PDP connection thread to - " + _host);
        return pdpConn;
    }

}



