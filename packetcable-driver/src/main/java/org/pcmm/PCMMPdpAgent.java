/**
 @header@
 */

package org.pcmm;

import org.pcmm.objects.MMVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpAgent;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Core PDP agent for provisioning
 */
public class PCMMPdpAgent extends COPSPdpAgent {

    private static final Logger logger = LoggerFactory.getLogger(PCMMPdpAgent.class);

    /** Well-known port for PCMM */
    public static final int WELL_KNOWN_PDP_PORT = 3918;

    /**
     * PEP host name
     */
    private final String psHost;

    /**
     * PEP port
     */
    private final int psPort;

    /**
     * Policy data processing object
     */
    private final PCMMPdpDataProcess _process;

    // Next two attributes are initialized when connected
    /**
     * The Socket connection to the PEP
     */
    private transient Socket socket;
    private transient COPSHandle _handle;

    /**
     * Creates a PDP Agent
     *
     * @param clientType - COPS Client-type
     * @param psHost - Host to connect to
     * @param psPort - Port to connect to
     * @param process - Object to perform policy data processing
     */
    public PCMMPdpAgent(final short clientType, final String psHost, final int psPort,
                        final PCMMPdpDataProcess process) {
        super(psPort, clientType, null);
        this._process = process;
        this.psHost = psHost;
        this.psPort = psPort;
    }

    /**
     * XXX -tek- This is the retooled connect. Not sure if the while forever
     * loop is needed. Socket accept --> handleClientOpenMsg --> pdpConn.run()
     *
     * Below is new Thread(pdpConn).start(); Does that do it?
     *
     */
    /**
     * Connects to a PDP
     *
     * @return <tt>true</tt> if PDP accepts the connection; <tt>false</tt>
     *         otherwise
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     * @throws COPSException
     */
    public boolean connect() throws IOException, COPSException {
        // Create Socket and send OPN
        final InetAddress addr = InetAddress.getByName(psHost);
        socket = new Socket(addr, psPort);
        logger.debug("{} {}", getClass().getName(), "PDP Socket Opened");

        // We're waiting for an message
        try {
            logger.debug("Waiting to receiveMsg");
            final COPSMsg msg = COPSTransceiver.receiveMsg(socket);
            logger.debug("Message received of type - " + msg.getHeader().getOpCode());
            if (msg.getHeader().getOpCode().equals(OPCode.OPN)) {
                handleClientOpenMsg(socket, msg);
            } else {
                try {
                    socket.close();
                } catch (Exception ex) {
                    logger.error("Unexpected error closing socket", ex);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error handing client open message", e);
            try {
                socket.close();
            } catch (Exception ex) {
                logger.error("Unexpected error closing socket", ex);
            }
            return true;
        }

        return false;
    }

    // TODO - remove and let super handle after DataProcess & PdpConnection classes are properly refactored
    @Override
    public void disconnect (final String pepID, final COPSError error) throws COPSException, IOException {
        final PCMMPdpConnection pdpConn = (PCMMPdpConnection) _connectionMap.get(pepID);
        if (pdpConn != null) {
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(getClientType(), error, null, null);
            closeMsg.writeData(pdpConn.getSocket());
            pdpConn.close();
        }

        final Thread thread = threadMap.remove(pepID);
        if (thread != null) thread.interrupt();
    }

    // TODO - this method should be broken apart into smaller pieces.
    @Override
    protected void handleClientOpenMsg(final Socket conn, final COPSMsg msg) throws COPSException, IOException {
        logger.info("Processing client open message");
        final COPSClientOpenMsg cMsg = (COPSClientOpenMsg) msg;
        _pepId = cMsg.getPepId();

        // Validate Client Type
        if (msg.getHeader().getClientType() != getClientType()) {
            // Unsupported client type
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(getClientType(),
                    new COPSError(ErrorTypes.UNSUPPORTED_CLIENT_TYPE, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected error writing data", unae);
            }

            throw new COPSException("Unsupported client type");
        }

        // PEPId is mandatory
        if (_pepId == null) {
            // Mandatory COPS object missing
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(getClientType(),
                    new COPSError(ErrorTypes.MANDATORY_OBJECT_MISSING, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected error closing socket", unae);
            }

            throw new COPSException("Mandatory COPS object missing (PEPId)");
        }

        // Support
        if ((cMsg.getClientSI() != null) ) {
            final MMVersionInfo _mminfo = new MMVersionInfo(cMsg.getClientSI().getData().getData());
            logger.debug("CMTS sent MMVersion info : major:" + _mminfo.getMajorVersionNB() + "  minor:" +
                    _mminfo.getMinorVersionNB());

        } else {
            // Unsupported objects
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(getClientType(),
                    new COPSError(ErrorTypes.UNKNOWN_OBJECT, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected error writing data", unae);
            }

            throw new COPSException("Unsupported objects (PdpAddress, Integrity)");
        }
        /*
        */

        // Connection accepted
        final COPSClientAcceptMsg acceptMsg;
        if (getAcctTimer() != 0)
            acceptMsg = new COPSClientAcceptMsg(getClientType(), new COPSKATimer(getKaTimer()),
                    new COPSAcctTimer(getAcctTimer()), null);
        else
            acceptMsg = new COPSClientAcceptMsg(getClientType(), new COPSKATimer(getKaTimer()), null, null);
        acceptMsg.writeData(conn);
        // XXX - handleRequestMsg
        try {
            logger.debug("handleClientOpenMsg() - Waiting to receive message");
            final COPSMsg rmsg = COPSTransceiver.receiveMsg(socket);
            logger.debug("Received message of type - " + rmsg.getHeader().getOpCode());
            // Client-Close
            if (rmsg.getHeader().getOpCode().equals(OPCode.CC)) {
                System.out.println(((COPSClientCloseMsg) rmsg)
                        .getError().getDescription());
                // close the socket
                final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(getClientType(),
                        new COPSError(ErrorTypes.UNKNOWN_OBJECT, ErrorTypes.NA), null, null);
                try {
                    closeMsg.writeData(conn);
                } catch (IOException unae) {
                    logger.error("Unexpected error writing data", unae);
                }
                throw new COPSException("CMTS requetsed Client-Close");
            } else {
                // Request
                if (rmsg.getHeader().getOpCode().equals(OPCode.REQ)) {
                    COPSReqMsg rMsg = (COPSReqMsg) rmsg;
                    _handle = rMsg.getClientHandle();
                } else
                    throw new COPSException("Can't understand request");
            }
        } catch (Exception e) { // COPSException, IOException
            throw new COPSException("Error COPSTransceiver.receiveMsg", e);
        }

        logger.debug("PDPCOPSConnection");
        final PCMMPdpConnection pdpConn = new PCMMPdpConnection(_pepId, conn, _process, getKaTimer(), getAcctTimer());

        // XXX - handleRequestMsg
        // XXX - check handle is valid
        final PCMMPdpReqStateMan man = new PCMMPdpReqStateMan(getClientType(), _handle, _process);
        pdpConn.addStateMan(_handle.getId().str(), man);
        try {
            man.initRequestState(conn);
        } catch (COPSPdpException unae) {
            logger.error("Unexpected error initializing state", unae);
        }
        // XXX - End handleRequestMsg

        logger.info("Starting PDP connection thread to - " + psHost);

        // TODO - store the thread reference so it is possible to manage.
        final Thread thread = new Thread(pdpConn, "Agent for - " + psHost);
        thread.start();
        threadMap.put(_pepId.getData().str(), thread);
        _connectionMap.put(_pepId.getData().str(), pdpConn);
    }

    public Socket getSocket() {
        return socket;
    }

    public COPSHandle getClientHandle() {
        return _handle;
    }

    public String getPepIdString() {
        return _pepId.getData().str();
    }

}

