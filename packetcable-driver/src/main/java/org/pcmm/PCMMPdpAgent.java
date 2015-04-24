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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core PDP agent for provisioning
 */
public class PCMMPdpAgent extends COPSPdpAgent {

    public final static Logger logger = LoggerFactory.getLogger(PCMMPdpAgent.class);

    /** Well-known port for PCMM */
    public static final int WELL_KNOWN_PDP_PORT = 3918;

    private COPSPepId _pepId;
    private String _pepIdString;
    /**
     * PEP host name
     */
    private String psHost;

    /**
     * PEP port
     */
    private int psPort;

    private Socket socket;

    /**
     * Policy data processing object
     */
    private PCMMPdpDataProcess _process;
    private COPSHandle _handle;
//    private short _transactionID;

    /**
     * Temporary until can refactor PdpAgent classes
     */
    @Deprecated
    private final Map<String, PCMMPdpConnection> _connectionMap;

    /**
     * Creates a PDP Agent
     *
     * @param clientType
     *            COPS Client-type
     * @param process
     *            Object to perform policy data processing
     */
    public PCMMPdpAgent(short clientType, PCMMPdpDataProcess process) {
        this(clientType, null, WELL_KNOWN_PDP_PORT, process);
    }

    /**
     * Creates a PDP Agent
     *
     * @param clientType
     *            COPS Client-type
     * @param psHost
     *            Host to connect to
     * @param psPort
     *            Port to connect to
     * @param process
     *            Object to perform policy data processing
     */
    public PCMMPdpAgent(short clientType, String psHost, int psPort, PCMMPdpDataProcess process) {
        super(psPort, clientType, null);
        this._process = process;
        this.psHost = psHost;
        this._connectionMap = new ConcurrentHashMap<>();
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
     * @param psHost
     *            CMTS host name
     * @param psPort
     *            CMTS port
     * @return <tt>true</tt> if PDP accepts the connection; <tt>false</tt>
     *         otherwise
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     * @throws COPSException
     * @throws COPSPdpException
     */
    public boolean connect(String psHost, int psPort) throws IOException, COPSException, COPSPdpException {

        this.psHost = psHost;
        this.psPort = psPort;
        // Create Socket and send OPN
        InetAddress addr = InetAddress.getByName(psHost);
        try {
            socket = new Socket(addr, psPort);
        } catch (IOException e) {
            logger.error("Error creating socket connection", e);
            return (false);
        }
        logger.info("PDP Socket Opened");
        // Loop through for Incoming messages

        // server infinite loop
        // while(true)
        {

            // We're waiting for an message
            try {
                logger.info("PDP  COPSTransceiver.receiveMsg");
                COPSMsg msg = COPSTransceiver.receiveMsg(socket);
                if (msg.getHeader().getOpCode().equals(OPCode.OPN)) {
                    logger.info("PDP msg.getHeader().isAClientOpen");
                    handleClientOpenMsg(socket, msg);
                } else {
                    try {
                        socket.close();
                    } catch (Exception ex) {
                        logger.error("Unexpected exception closing socket", ex);
                    }
                }
            } catch (Exception e) { // COPSException, IOException
                try {
                    socket.close();
                } catch (Exception ex) {
                    logger.error("Unexpected exception closing socket", ex);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a COPS client-open message
     *
     * @param conn
     *            Socket to the PEP
     * @param msg
     *            <tt>COPSMsg</tt> holding the client-open message
     * @throws COPSException
     * @throws IOException
     */
    private void handleClientOpenMsg(final Socket conn, final COPSMsg msg) throws COPSException, IOException {
        final COPSClientOpenMsg cMsg = (COPSClientOpenMsg) msg;
        final COPSPepId pepId = cMsg.getPepId();

        // Validate Client Type
        if (msg.getHeader().getClientType() != getClientType()) {
            // Unsupported client type
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(msg.getHeader().getClientType(),
                    new COPSError(ErrorTypes.UNSUPPORTED_CLIENT_TYPE, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected error writing COPS data", unae);
            }

            throw new COPSException("Unsupported client type");
        }

        // PEPId is mandatory
        if (pepId == null) {
            // Mandatory COPS object missing
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(msg.getHeader().getClientType(),
                    new COPSError(ErrorTypes.MANDATORY_OBJECT_MISSING, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected error writing COPS data", unae);
            }

            throw new COPSException("Mandatory COPS object missing (PEPId)");
        }
        setPepId(pepId);
        // Support
        if ((cMsg.getClientSI() != null) ) {
            final MMVersionInfo _mminfo = new MMVersionInfo(cMsg.getClientSI().getData().getData());
            logger.info("CMTS sent MMVersion info : major:" + _mminfo.getMajorVersionNB() + "  minor:" +
                    _mminfo.getMinorVersionNB());

        } else {
            final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(msg.getHeader().getClientType(),
                    new COPSError(ErrorTypes.UNKNOWN_OBJECT, ErrorTypes.NA), null, null);
            try {
                closeMsg.writeData(conn);
            } catch (IOException unae) {
                logger.error("Unexpected error writing COPS data", unae);
            }

            throw new COPSException("Unsupported objects (PdpAddress, Integrity)");
        }
        /*
        */

        // Connection accepted
        final COPSKATimer katimer = new COPSKATimer(getKaTimer());
        final COPSAcctTimer acctTimer = new COPSAcctTimer(getAcctTimer());
        final COPSClientAcceptMsg acceptMsg;
        if (getAcctTimer() != 0)
            acceptMsg = new COPSClientAcceptMsg(msg.getHeader().getClientType(), katimer, acctTimer, null);
        else
            acceptMsg = new COPSClientAcceptMsg(msg.getHeader().getClientType(), katimer, null, null);

        acceptMsg.writeData(conn);
        // XXX - handleRequestMsg
        try {
            logger.info("PDP COPSTransceiver.receiveMsg");
            COPSMsg rmsg = COPSTransceiver.receiveMsg(socket);
            // Client-Close
            if (rmsg.getHeader().getOpCode().equals(OPCode.CC)) {
                logger.info("Client close description - " + ((COPSClientCloseMsg) rmsg).getError().getDescription());
                // close the socket
                final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(msg.getHeader().getClientType(),
                        new COPSError(ErrorTypes.UNKNOWN_OBJECT, ErrorTypes.NA), null, null);
                try {
                    closeMsg.writeData(conn);
                } catch (IOException unae) {
                    logger.error("Unexpected exception writing COPS data", unae);
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
            throw new COPSException("Error COPSTransceiver.receiveMsg");
        }

        logger.info("PDPCOPSConnection");
        PCMMPdpConnection pdpConn = new PCMMPdpConnection(pepId, conn, _process);
        pdpConn.setKaTimer(getKaTimer());
        if (getAcctTimer() != 0)
            pdpConn.setAccTimer(getAcctTimer());

        // XXX - handleRequestMsg
        // XXX - check handle is valid
        PCMMPdpReqStateMan man = new PCMMPdpReqStateMan(getClientType(), _handle.getId().str());
        pdpConn.getReqStateMans().put(_handle.getId().str(),man);
        man.setDataProcess(_process);
        try {
            man.initRequestState(conn);
        } catch (COPSPdpException unae) {
            logger.error("Error initializing the state manager's request state");
        }
        // XXX - End handleRequestMsg

        logger.info("PDP Thread(pdpConn).start");
        new Thread(pdpConn).start();
        _connectionMap.put(pepId.getData().str(), pdpConn);
    }

    /**
     * @return the _psHost
     */
    public String getPsHost() {
        return psHost;
    }

    /**
     * TODO - make the host immutable
     * @param _psHost
     *            the _psHost to set
     */
    @Deprecated
    public void setPsHost(String _psHost) {
        this.psHost = _psHost;
    }

    /**
     * @return the _psPort
     */
    public int getPsPort() {
        return psPort;
    }

    /**
     * TODO - make the port immutable
     * @param _psPort
     *            the _psPort to set
     */
    @Deprecated
    public void setPsPort(int _psPort) {
        this.psPort = _psPort;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * TODO - Ensure socket is not overly transient
     * @param socket
     *            the socket to set
     */
    @Deprecated
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return the _process
     */
    public PCMMPdpDataProcess getProcess() {
        return _process;
    }

    /**
     * @param _process
     *            the _process to set
     */
    public void setProcess(PCMMPdpDataProcess _process) {
        this._process = _process;
    }

    /**
      * Gets the client handle
      * @return   Client's <tt>COPSHandle</tt>
      */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
      * Gets the PepId
      * @return   <tt>COPSPepId</tt>
      */
    public COPSPepId getPepId() {
        return _pepId;
    }

    public String getPepIdString() {
        return _pepIdString;
    }

    /**
     * Sets the PepId
     * TODO - make PEP ID and the associate string immutable or remove altogether
     * @param   pepId - COPSPepId
     */
    @Deprecated
    public void setPepId(COPSPepId pepId) {
        _pepId = pepId;
        _pepIdString = pepId.getData().str();
     }
    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.rcd.IPCMMClient#isConnected()
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }


}
