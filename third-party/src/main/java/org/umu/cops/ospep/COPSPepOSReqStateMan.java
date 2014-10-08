package org.umu.cops.ospep;

import java.net.Socket;
import java.util.Vector;

import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSSyncStateMsg;

/**
 * State manager class for outsourcing requests, at the PEP side.
 */
public class COPSPepOSReqStateMan {
    /**
     * Request State created
     */
    public final static short ST_CREATE = 1;
    /**
     * Request sent
     */
    public final static short ST_INIT = 2;
    /**
     * Decisions received
     */
    public final static short ST_DECS = 3;
    /**
     * Report sent
     */
    public final static short ST_REPORT = 4;
    /**
     * Request State finalized
     */
    public final static short ST_FINAL = 5;
    /**
     * New Request State solicited
     */
    public final static short ST_NEW = 6;
    /**
     * Delete Request State solicited
     */
    public final static short ST_DEL = 7;
    /**
     * SYNC request received
     */
    public final static short ST_SYNC = 8;
    /**
     * Sync completed
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
        Object for performing policy data processing
     */
    protected COPSPepOSDataProcess _process;

    /**
     * ClientSI data from signaling.
     */
    protected Vector _clientSIs;

    /**
     *  Current state of the request being managed
     */
    protected short _status;

    /**
        COPS message transceiver used to send COPS messages
     */
    protected COPSPepOSMsgSender _sender;

    /**
     * Sync state
     */
    protected boolean _syncState;

    /**
     * Creates a state request manager
     * @param    clientType Client-type
     * @param   clientHandle    Client's <tt>COPSHandle</tt>
     */
    public COPSPepOSReqStateMan(short clientType, String clientHandle) {
        // COPS Handle
        _handle = new COPSHandle();
        COPSData id = new COPSData(clientHandle);
        _handle.setId(id);
        // client-type
        _clientType = clientType;
        _syncState = true;
        _status = ST_CREATE;
        _clientSIs = null;
    }

    /**
     * Gets the client handle
     * @return  Client's <tt>COPSHandle</tt>
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Sets the client SI data.
     * @param someClientSIs Client SI data built by the event listener
     */
    public void setClientSI(Vector someClientSIs) {
        _clientSIs = someClientSIs;
    }

    /**
     * Gets the client-type
     * @return  Client-type value
     */
    public short getClientType() {
        return _clientType;
    }

    /**
     * Gets the request status
     * @return  Request status value
     */
    public short getStatus() {
        return _status;
    }

    /**
     * Gets the policy data processing object
     *
     * @return   Policy data processing object
     *
     */
    public COPSPepOSDataProcess getDataProcess() {
        return _process;
    }

    /**
     * Sets the policy data processing object
     *
     * @param   process   Policy data processing object
     *
     */
    public void setDataProcess(COPSPepOSDataProcess process) {
        _process = process;
    }

    /**
     * Initializes a new request state over a socket
     * @param sock  Socket to the PDP
     * @throws COPSPepException
     */
    protected void initRequestState(Socket sock) throws COPSPepException {
        // Inits an object for sending COPS messages to the PDP
        _sender = new COPSPepOSMsgSender(_clientType, _handle, sock);

        // If an object exists for retrieving the PEP features,
        // use it for retrieving them.
        /*      Hashtable clientSIs;
                if (_process != null)
                    clientSIs = _process.getClientData(this);
                else
                    clientSIs = null;*/

        // Semd the request
        _sender.sendRequest(_clientSIs);

        // Initial state
        _status = ST_INIT;
    }

    /**
     * Deletes the request state
     * @throws COPSPepException
     */
    protected void finalizeRequestState() throws COPSPepException {
        _sender.sendDeleteRequest();
        _status = ST_FINAL;
    }

    /**
     * Processes the decision message
     * @param    dMsg Decision message from the PDP
     * @throws   COPSPepException
     */
    protected void processDecision(COPSDecisionMsg dMsg) throws COPSPepException {
        // COPSDebug.out(getClass().getName(), "ClientId:" + getClientHandle().getId().str());

        //Hashtable decisionsPerContext = dMsg.getDecisions();

        //** Applies decisions to the configuration
        //_process.setDecisions(this, removeDecs, installDecs, errorDecs);
        // second param changed to dMsg so that the data processor
        // can check the 'solicited' flag
        boolean isFailReport = _process.setDecisions(this, dMsg /*decisionsPerContext*/);
        _status = ST_DECS;

        if (isFailReport) {
            // COPSDebug.out(getClass().getName(),"Sending FAIL Report\n");
            _sender.sendFailReport(_process.getReportData(this));
        } else {
            // COPSDebug.out(getClass().getName(),"Sending SUCCESS Report\n");
            _sender.sendSuccessReport(_process.getReportData(this));
        }
        _status = ST_REPORT;

        if (!_syncState) {
            _sender.sendSyncComplete();
            _syncState = true;
            _status = ST_SYNCALL;
        }
    }


    /**
     * Processes a COPS delete message
     * @param dMsg  <tt>COPSDeleteMsg</tt> received from the PDP
     * @throws COPSPepException
     */
    protected void processDeleteRequestState(COPSDecisionMsg dMsg) throws COPSPepException {
        if (_process != null)
            _process.closeRequestState(this);

        _status = ST_DEL;
    }

    /**
     * Processes the message SycnStateRequest.
     * The message SycnStateRequest indicates that the remote PDP
     * wishes the client (which appears in the common header)
     * to re-send its state.
     *
     * @param    ssMsg               The sync request from the PDP
     *
     * @throws   COPSPepException
     *
     */
    protected void processSyncStateRequest(COPSSyncStateMsg ssMsg) throws COPSPepException {
        _syncState = false;
        // If an object exists for retrieving the PEP features,
        // use it for retrieving them.

        // Send the request
        _sender.sendRequest(_clientSIs);

        _status = ST_SYNC;
    }

    /**
     * Called when connection is closed
     * @param error Reason
     * @throws COPSPepException
     */
    protected void processClosedConnection(COPSError error) throws COPSPepException {
        if (_process != null)
            _process.notifyClosedConnection(this, error);

        _status = ST_CCONN;
    }

    /**
     * Called when no keep-alive is received
     * @throws COPSPepException
     */
    protected void processNoKAConnection() throws COPSPepException {
        if (_process != null)
            _process.notifyNoKAliveReceived(this);

        _status = ST_NOKA;
    }

    /**
     * Processes the accounting report
     * @throws COPSPepException
     */
    protected void processAcctReport() throws COPSPepException {
        Vector report = new Vector();

        if (_process != null)
            report = _process.getAcctData(this);

        _sender.sendAcctReport(report);

        _status = ST_ACCT;
    }
}
