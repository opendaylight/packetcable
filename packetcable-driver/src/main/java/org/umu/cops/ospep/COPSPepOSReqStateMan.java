package org.umu.cops.ospep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSStateMan;
import org.umu.cops.stack.*;

import java.net.Socket;
import java.util.*;

/**
 * State manager class for outsourcing requests, at the PEP side.
 */
public class COPSPepOSReqStateMan extends COPSStateMan {

    private final static Logger logger = LoggerFactory.getLogger(COPSPepOSReqStateMan.class);

    /**
     * ClientSI data from signaling.
     */
    protected final Set<COPSClientSI> _clientSIs;

    /**
        Object for performing policy data processing
     */
    protected final COPSPepOSDataProcess _process;

    /**
        COPS message transceiver used to send COPS messages
     */
    protected transient COPSPepOSMsgSender _sender;

    /**
     * Sync state
     */
    protected transient boolean _syncState;

    /**
     * Creates a state request manager
     * @param    clientType Client-type
     * @param   clientHandle    Client's <tt>COPSHandle</tt>
     */
    public COPSPepOSReqStateMan(final short clientType, final COPSHandle clientHandle, final COPSPepOSDataProcess process,
                                final Collection<COPSClientSI> clientSIs) {
        super(clientType, clientHandle);
        this._process = process;
        this._clientSIs = new HashSet<>(clientSIs);
        _syncState = true;
    }

    @Override
    protected void initRequestState(final Socket sock) throws COPSException {
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
        _status = Status.ST_INIT;
    }

    /**
     * Deletes the request state
     * @throws COPSPepException
     */
    protected void finalizeRequestState() throws COPSPepException {
        _sender.sendDeleteRequest();
        _status = Status.ST_FINAL;
    }

    /**
     * Processes the decision message
     * @param    dMsg Decision message from the PDP
     * @throws   COPSPepException
     */
    protected void processDecision(final COPSDecisionMsg dMsg) throws COPSPepException {
        //Hashtable decisionsPerContext = dMsg.getDecisions();

        //** Applies decisions to the configuration
        //_process.setDecisions(this, removeDecs, installDecs, errorDecs);
        // second param changed to dMsg so that the data processor
        // can check the 'solicited' flag
        final boolean isFailReport = _process.setDecisions(this, dMsg /*decisionsPerContext*/);
        _status = Status.ST_DECS;

        if (isFailReport) {

            logger.info("Sending FAIL Report");
            _sender.sendFailReport(_process.getReportData(this));
        } else {
            logger.info("Sending SUCCESS Report");
            _sender.sendSuccessReport(_process.getReportData(this));
        }
        _status = Status.ST_REPORT;

        if (!_syncState) {
            _sender.sendSyncComplete();
            _syncState = true;
            _status = Status.ST_SYNCALL;
        }
    }


    /**
     * Processes a COPS delete message
     * @param dMsg  <tt>COPSDeleteMsg</tt> received from the PDP
     * @throws COPSPepException
     */
    protected void processDeleteRequestState(final COPSDecisionMsg dMsg) throws COPSPepException {
        if (_process != null)
            _process.closeRequestState(this);

        _status = Status.ST_DEL;
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
    protected void processSyncStateRequest(final COPSSyncStateMsg ssMsg) throws COPSException {
        _syncState = false;
        // If an object exists for retrieving the PEP features,
        // use it for retrieving them.

        // Send the request
        _sender.sendRequest(_clientSIs);

        _status = Status.ST_SYNC;
    }

    /**
     * Called when connection is closed
     * @param error Reason
     * @throws COPSPepException
     */
    protected void processClosedConnection(final COPSError error) throws COPSPepException {
        if (_process != null)
            _process.notifyClosedConnection(this, error);

        _status = Status.ST_CCONN;
    }

    /**
     * Called when no keep-alive is received
     * @throws COPSPepException
     */
    protected void processNoKAConnection() throws COPSPepException {
        if (_process != null)
            _process.notifyNoKAliveReceived(this);

        _status = Status.ST_NOKA;
    }

    /**
     * Processes the accounting report
     * @throws COPSPepException
     */
    protected void processAcctReport() throws COPSPepException {
        final List<COPSClientSI> report;
        if (_process != null) report = new ArrayList<>(_process.getAcctData(this));
        else report = new ArrayList<>();

        _sender.sendAcctReport(report);

        _status = Status.ST_ACCT;
    }
}
