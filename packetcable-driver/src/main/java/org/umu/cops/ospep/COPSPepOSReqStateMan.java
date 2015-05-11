package org.umu.cops.ospep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpep.COPSPepReqStateMan;
import org.umu.cops.stack.*;

import java.net.Socket;
import java.util.*;

/**
 * State manager class for outsourcing requests, at the PEP side.
 */
public class COPSPepOSReqStateMan extends COPSPepReqStateMan {

    private final static Logger logger = LoggerFactory.getLogger(COPSPepOSReqStateMan.class);

    /**
        Object for performing policy data processing
     */
    protected final COPSPepOSDataProcess _thisProcess;

    /**
        COPS message transceiver used to send COPS messages
     */
    protected final COPSPepOSMsgSender _thisSender;

    /**
     * ClientSI data populated when initRequestState() is called.
     */
    protected transient Set<COPSClientSI> _clientSIs;

    /**
     * Constructor
     * @param clientType - the PEP client type
     * @param clientHandle - the client-handle
     * @param process - the data processor
     * @param clientSIs - the known client SI objects
     * @param socket - the socket connection
     */
    public COPSPepOSReqStateMan(final short clientType, final COPSHandle clientHandle,
                                final COPSPepOSDataProcess process, final Collection<COPSClientSI> clientSIs,
                                final Socket socket) {
        super(clientType, clientHandle, process, socket, new COPSPepOSMsgSender(clientType, clientHandle, socket));
        this._thisProcess = process;
        if (clientSIs == null)
            this._clientSIs = new HashSet<>();
        else this._clientSIs = new HashSet<>(clientSIs);

        // Inits an object for sending COPS messages to the PDP
        _thisSender = new COPSPepOSMsgSender(_clientType, _handle, _socket);
    }

    @Override
    public void initRequestState() throws COPSException {
        _clientSIs.addAll(_thisProcess.getClientData(this));
        // Send the request
        _thisSender.sendRequest(new HashSet<>(_clientSIs));

        // Initial state
        _status = Status.ST_INIT;
    }

    @Override
    protected void processDecision(final COPSDecisionMsg dMsg) throws COPSException {
        //** Applies decisions to the configuration
        //_thisProcess.setDecisions(this, removeDecs, installDecs, errorDecs);
        // second param changed to dMsg so that the data processor
        // can check the 'solicited' flag
        final boolean isFailReport = _thisProcess.setDecisions(this, dMsg);
        _status = Status.ST_DECS;

        if (isFailReport) {
            logger.info("Sending FAIL Report");
            _thisSender.sendFailReport(_thisProcess.getReportData(this));
        } else {
            logger.info("Sending SUCCESS Report");
            _thisSender.sendSuccessReport(_thisProcess.getReportData(this));
        }
        _status = Status.ST_REPORT;

        if (!_syncState) {
            _thisSender.sendSyncComplete();
            _syncState = true;
            _status = Status.ST_SYNCALL;
        }
    }

    @Override
    protected void processSyncStateRequest(final COPSSyncStateMsg ssMsg) throws COPSException {
        _syncState = false;
        // If an object exists for retrieving the PEP features,
        // use it for retrieving them.

        // Send the request
        _thisSender.sendRequest(_clientSIs);

        _status = Status.ST_SYNC;
    }

    @Override
    public void processAcctReport() throws COPSPepException {
        final List<COPSClientSI> report;
        if (_thisProcess != null) report = new ArrayList<>(_thisProcess.getAcctData(this));
        else report = new ArrayList<>();

        _thisSender.sendAcctReport(report);

        _status = Status.ST_ACCT;
    }
}
