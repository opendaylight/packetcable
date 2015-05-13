/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSStateMan;
import org.umu.cops.stack.*;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * COPSPepReqStateMan manages Request State using Client Handle (RFC 2748 pag. 21)
 * in PEP.
 *
 *   The client handle is used to identify a unique request state for a
 *   single PEP per client-type. Client handles are chosen by the PEP and
 *   are opaque to the PDP. The PDP simply uses the request handle to
 *   uniquely identify the request state for a particular Client-Type over
 *   a particular TCP connection and generically tie its decisions to a
 *   corresponding request. Client handles are initiated in request
 *   messages and are then used by subsequent request, decision, and
 *   report messages to reference the same request state. When the PEP is
 *   ready to remove a local request state, it will issue a delete message
 *   to the PDP for the corresponding client handle. A handle MUST be
 *   explicitly deleted by the PEP before it can be used by the PEP to
 *   identify a new request state. Handles referring to different request
 *   states MUST be unique within the context of a particular TCP
 *   connection and client-type.
 *
 * @version COPSPepReqStateMan.java, v 2.00 2004
 *
 */
public class COPSPepReqStateMan extends COPSStateMan {

    private final static Logger logger = LoggerFactory.getLogger(COPSPepReqStateMan.class);

    /**
        The PolicyDataProcess is used to process policy data in the PEP
     */
    protected final COPSPepDataProcess _process;

    /**
        The Msg Sender is used to send COPS messages
     */
    protected final COPSPepMsgSender _sender;

    /**
     * Sync State
     */
    protected transient boolean _syncState;

    /**
     * Constructor for this class
     * @param clientType - the PEP client type
     * @param clientHandle - the client-handle
     * @param process - the data processor
     * @param socket - the socket connection
     */
    public COPSPepReqStateMan(final short clientType, final COPSHandle clientHandle, final COPSPepDataProcess process,
                              final Socket socket) {
        this(clientType, clientHandle, process, socket, new COPSPepMsgSender(clientType, clientHandle, socket));
    }

    /**
     * Constructor for sub-classes
     * @param clientType - the PEP client type
     * @param clientHandle - the client-handle
     * @param process - the data processor
     * @param socket - the socket connection
     * @param sender - responsible for sending COPS messages to the PEP
     */
    protected COPSPepReqStateMan(final short clientType, final COPSHandle clientHandle, final COPSPepDataProcess process,
                                 final Socket socket, final COPSPepMsgSender sender) {

        super(clientType, clientHandle, socket);
        this._process = process;
        _syncState = true;
        // Inits an object for sending COPS messages to the PDP
        _sender = sender;
    }

    /**
     * Init Request State
     * @throws   COPSPepException
     */
    public void initRequestState() throws COPSException {
        // If an object for retrieving PEP features exists,
        // use it for retrieving them
        final Map<String, String> clientSIs;
        if (_process != null)
            clientSIs = _process.getClientData(this);
        else
            clientSIs = new HashMap<>();

        // Send the request
        // TODO - do we really want to send when this is empty???
        _sender.sendRequest(clientSIs);

        // Initial state
        _status = Status.ST_INIT;
    }

    /**
     * Finalize Request State
     *
     * @throws   COPSPepException
     *
     */
    public void finalizeRequestState() throws COPSException {
        _sender.sendDeleteRequest();
        _status = Status.ST_FINAL;
    }

    /**
     * Process the message Decision
     * @param    dMsg                a  COPSDecisionMsg
     * @throws   COPSPepException
     */
    protected void processDecision(final COPSDecisionMsg dMsg) throws COPSException {
        logger.info("Processing decision message - " + dMsg);
        final Map<COPSContext, Set<COPSDecision>> decisions = dMsg.getDecisions();

        final Map<String, String> removeDecs = new HashMap<>();
        final Map<String, String> installDecs = new HashMap<>();

        for (final Set<COPSDecision> copsDecisions: decisions.values()) {
            final COPSDecision cmddecision = copsDecisions.iterator().next();
            String prid = "";
            switch (cmddecision.getCommand()) {
                case INSTALL:
                    for (final COPSDecision decision : copsDecisions) {
                        final COPSPrObjBase obj = new COPSPrObjBase(decision.getData().getData());
                        switch (obj.getSNum()) {
                            case COPSPrObjBase.PR_PRID:
                                prid = obj.getData().str();
                                break;
                            case COPSPrObjBase.PR_EPD:
                                installDecs.put(prid, obj.getData().str());
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case REMOVE:
                    for (final COPSDecision decision : copsDecisions) {
                        final COPSPrObjBase obj = new COPSPrObjBase(decision.getData().getData());
                        switch (obj.getSNum()) {
                            case COPSPrObjBase.PR_PRID:
                                prid = obj.getData().str();
                                break;
                            case COPSPrObjBase.PR_EPD:
                                removeDecs.put(prid, obj.getData().str());
                                break;
                            default:
                                break;
                        }
                    }
                    break;
            }

        }

        //** Apply decisions to the configuration
        // TODO - why is this collection never getting populated???
        final Map<String, String> errorDecs = new HashMap<>();
        _process.setDecisions(this, removeDecs, installDecs, errorDecs);
        _status = Status.ST_DECS;


        if (_process.isFailReport(this)) {
            logger.info("Sending FAIL report");
            _sender.sendFailReport(_process.getReportData(this));
        } else {
            logger.info("Sending SUCCESS report");
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
     * Process the message NewRequestState
     * @throws   COPSPepException
     */
    protected void processOpenNewRequestState() throws COPSPepException {
        if (_process != null)
            _process.newRequestState(this);

        _status = Status.ST_NEW;
    }

    /**
     * Process the message DeleteRequestState
     * @param    dMsg                a  COPSDecisionMsg
     * @throws   COPSPepException
     */
    protected void processDeleteRequestState(final COPSDecisionMsg dMsg) throws COPSPepException {
        if (_process != null)
            _process.closeRequestState(this);

        _status = Status.ST_DEL;
    }

    /**
     * Process the message SycnStateRequest.
     * The message SycnStateRequest indicates that the remote PDP
     * wishes the client (which appears in the common header)
     * to re-send its state.
     * @param    ssMsg               a  COPSSyncStateMsg
     * @throws   COPSPepException
     */
    protected void processSyncStateRequest(final COPSSyncStateMsg ssMsg) throws COPSException {
        _syncState = false;
        // If an object for retrieving PEP features exists,
        // use it for retrieving them
        final Map<String, String> clientSIs;
        if (_process != null)
            clientSIs = _process.getClientData(this);
        else
            clientSIs = new HashMap<>();

        // Send request
        // TODO - do we really want to send the request when the map is empty???
        _sender.sendRequest(clientSIs);

        _status = Status.ST_SYNC;
    }

    public void processClosedConnection(final COPSError error) throws COPSPepException {
        if (_process != null)
            _process.notifyClosedConnection(this, error);

        _status = Status.ST_CCONN;
    }

    public void processNoKAConnection() throws COPSException {
        if (_process != null)
            _process.notifyNoKAliveReceived(this);

        _status = Status.ST_NOKA;
    }

    /**
     * Creates and sends an accounting report
     * @throws COPSException
     */
    public void processAcctReport() throws COPSException {
        final Map<String, String> report;
        if (_process != null) report = _process.getAcctData(this);
        else report = new HashMap<>();

        // TODO - do we really want to send when the map is empty???
        _sender.sendAcctReport(report);

        _status = Status.ST_ACCT;
    }

}
