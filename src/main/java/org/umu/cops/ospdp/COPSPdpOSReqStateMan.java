package org.umu.cops.ospdp;

import java.net.Socket;
import java.util.Vector;

import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDeleteMsg;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSReportMsg;
import org.umu.cops.stack.COPSReportType;
import org.umu.cops.stack.COPSReqMsg;
import org.umu.cops.stack.COPSSyncStateMsg;

/**
 * State manager class for outsourcing requests, at the PDP side.
 */
public class COPSPdpOSReqStateMan {
    /**
     * Request State created
     */
    public final static short ST_CREATE = 1;
    /**
     * Request received
     */
    public final static short ST_INIT = 2;
    /**
     * Decisions sent
     */
    public final static short ST_DECS = 3;
    /**
     * Report received
     */
    public final static short ST_REPORT = 4;
    /**
     * Request state finalized
     */
    public final static short ST_FINAL = 5;
    /**
     * New request state solicited
     */
    public final static short ST_NEW = 6;
    /**
     * Delete request state solicited
     */
    public final static short ST_DEL = 7;
    /**
     * SYNC request sent
     */
    public final static short ST_SYNC = 8;
    /**
     * SYNC completed
     */
    public final static short ST_SYNCALL = 9;
    /**
     * Close connection received
     */
    public final static short ST_CCONN = 10;
    /**
     * Keep-alive timeout
     */
    public final static short ST_NOKA = 11;
    /**
     * Accounting timeout
     */
    public final static short ST_ACCT = 12;

    /**
     * COPS client-type that identifies the policy client
     */
    protected short _clientType;

    /**
     *  COPS client handle used to uniquely identify a particular
     *  PEP's request for a client-type
     */
    protected COPSHandle _handle;

    /**
     * Object for performing policy data processing
     */
    protected COPSPdpOSDataProcess _process;

    /**
     *  Current state of the request being managed
     */
    protected short _status;

    /** COPS message transceiver used to send COPS messages */
    protected COPSPdpOSMsgSender _sender;

    /**
     * Creates a request state manager
     * @param clientType    Client-type
     * @param clientHandle  Client handle
     */
    public COPSPdpOSReqStateMan(short clientType, String clientHandle) {
        // COPS Handle
        _handle = new COPSHandle();
        COPSData id = new COPSData(clientHandle);
        _handle.setId(id);
        // client-type
        _clientType = clientType;

        _status = ST_CREATE;
    }

    /**
     * Gets the client handle
     * @return   Client's <tt>COPSHandle</tt>
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Gets the client-type
     * @return   Client-type value
     */
    public short getClientType() {
        return _clientType;
    }

    /**
     * Gets the status of the request
     * @return      Request state value
     */
    public short getStatus() {
        return _status;
    }

    /**
     * Gets the policy data processing object
     * @return   Policy data processing object
     */
    public COPSPdpOSDataProcess getDataProcess() {
        return _process;
    }

    /**
     * Sets the policy data processing object
     * @param   process Policy data processing object
     */
    public void setDataProcess(COPSPdpOSDataProcess process) {
        _process = process;
    }

    /**
     * Called when COPS sync is completed
     * @param    repMsg              COPS sync message
     * @throws   COPSPdpException
     */
    protected void processSyncComplete(COPSSyncStateMsg repMsg)
    throws COPSPdpException {

        _status = ST_SYNCALL;

        // maybe we should notifySyncComplete ...
    }

    /**
     * Initializes a new request state over a socket
     * @param sock  Socket to the PEP
     * @throws COPSPdpException
     */
    protected void initRequestState(Socket sock)
    throws COPSPdpException {
        // Inits an object for sending COPS messages to the PDP
        _sender = new COPSPdpOSMsgSender(_clientType, _handle, sock);

        // Initial state
        _status = ST_INIT;
    }

    /**
     * Processes a COPS request
     * @param msg   COPS request received from the PEP
     * @throws COPSPdpException
     */
    protected void processRequest(COPSReqMsg msg) throws COPSPdpException {
        Vector clientSIs = msg.getClientSI();

        //** Here we must retrieve a decision depending on the
        //** supplied ClientSIs
        /*Vector removeDecs = new Vector();
        Vector installDecs = new Vector();*/
        _process.setClientData(this, clientSIs);

        Vector removeDecs = _process.getRemovePolicy(this);
        Vector installDecs = _process.getInstallPolicy(this);

        //** We create a SOLICITED decision
        //**
        _sender.sendSolicitedDecision(removeDecs, installDecs);
        _status = ST_DECS;
    }

    /**
     * Processes a report
     * @param msg   Report message from the PEP
     * @throws COPSPdpException
     */
    protected void processReport(COPSReportMsg msg) throws COPSPdpException {
        //** Analyze the report
        //**

        /*
         * <Report State> ::= <Common Header>
         *                      <Client Handle>
         *                      <Report Type>
         *                      *(<Named ClientSI>)
         *                      [<Integrity>]
         * <Named ClientSI: Report> ::= <[<GPERR>] *(<report>)>
         * <report> ::= <ErrorPRID> <CPERR> *(<PRID><EPD>)
         *
         * Important, <Named ClientSI> is not parsed
        */

        // COPSHeader hdrmsg = msg.getHeader();
        // COPSHandle handlemsg = msg.getClientHandle();

        // Report Type
        COPSReportType rtypemsg = msg.getReport();

        // Named ClientSI
        Vector clientSIs = msg.getClientSI();

        //** We should act here in accordance with
        //** the received report
        if (rtypemsg.isSuccess()) {
            _status = ST_REPORT;
            _process.successReport(this, clientSIs);
        } else if (rtypemsg.isFailure()) {
            _status = ST_REPORT;
            _process.failReport(this, clientSIs);
        } else if (rtypemsg.isAccounting()) {
            _status = ST_ACCT;
            _process.acctReport(this, clientSIs);
        }
    }

    /**
     * Called when connection is closed
     * @param error Reason
     * @throws COPSPdpException
     */
    protected void processClosedConnection(COPSError error)
    throws COPSPdpException {
        if (_process != null)
            _process.notifyClosedConnection(this, error);

        _status = ST_CCONN;
    }

    /**
     * Called when no keep-alive is received
     * @throws COPSPdpException
     */
    protected void processNoKAConnection()
    throws COPSPdpException {
        if (_process != null)
            _process.notifyNoKAliveReceived(this);

        _status = ST_NOKA;
    }

    /**
     * Deletes the request state
     * @throws COPSPdpException
     */
    protected void finalizeRequestState()
    throws COPSPdpException {
        _sender.sendDeleteRequestState();
        _status = ST_FINAL;
    }

    /**
     * Asks for a COPS sync
     * @throws COPSPdpException
     */
    protected void syncRequestState()
    throws COPSPdpException {
        _sender.sendSyncRequestState();
        _status = ST_SYNC;
    }

    /**
     * Opens a new request state
     * @throws COPSPdpException
     */
    protected void openNewRequestState()//FIXME: unused?
    throws COPSPdpException {
        _sender.sendOpenNewRequestState();
        _status = ST_NEW;
    }

    /**
     * Processes a COPS delete message
     * @param dMsg  <tt>COPSDeleteMsg</tt> received from the PEP
     * @throws COPSPdpException
     */
    protected void processDeleteRequestState(COPSDeleteMsg dMsg)
    throws COPSPdpException {
        if (_process != null)
            _process.closeRequestState(this);

        _status = ST_DEL;
    }

}
