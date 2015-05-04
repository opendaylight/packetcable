/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.pcmm.gates.impl.GateID;
import org.pcmm.gates.impl.PCMMGateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSStateMan;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

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
    protected transient COPSPepMsgSender _sender;

    /**
     * Sync State
     */
    protected transient boolean _syncState;

    /**
     * Create a State Request Manager
     *
     * @param    clientHandle                a Client Handle
     *
     */
    public COPSPepReqStateMan(final short clientType, final COPSHandle clientHandle, final COPSPepDataProcess process) {
        super(clientType, clientHandle);
        this._process = process;
        _syncState = true;
    }

    /**
     * Init Request State
     *
     * @throws   COPSPepException
     *
     */
    protected void initRequestState(final Socket sock) throws COPSException {
        // Inits an object for sending COPS messages to the PDP
        _sender = new COPSPepMsgSender(_clientType, _handle, sock);

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
    protected void finalizeRequestState() throws COPSPepException {
        _sender.sendDeleteRequest();
        _status = Status.ST_FINAL;
    }

    /**
     * Process the message Decision
     *
     * @param    dMsg                a  COPSDecisionMsg
     *
     * @throws   COPSPepException
     *
     */
    protected void processDecision(final COPSDecisionMsg dMsg, final Socket socket) throws COPSPepException {
        logger.info("Processing decision message - " + dMsg);
        final Map<COPSContext, Set<COPSDecision>> decisions = dMsg.getDecisions();

        final Map<String, String> removeDecs = new HashMap<>();
        final Map<String, String> installDecs = new HashMap<>();

        for (final Set<COPSDecision> copsDecisions: decisions.values()) {
            final COPSDecision cmddecision = copsDecisions.iterator().next();
            String prid = "";
            switch (cmddecision.getCommand()) {
                case INSTALL:
                    // TODO - break up this block
                    for (final COPSDecision decision : copsDecisions) {
                        if (decision.getData().getData().length != 0) {
                            final COPSPrObjBase obj = new COPSPrObjBase(decision.getData().getData());
                            switch (obj.getSNum()) {
                                case COPSPrObjBase.PR_PRID:
                                    prid = obj.getData().str();
                                    break;
                                case COPSPrObjBase.PR_EPD:
                                    installDecs.put(prid, obj.getData().str());
                                    break;
                            }
                        }
                        if (decision.getFlag().equals(DecisionFlag.REQERROR)) {
                            // This is assuming a gate set right or wrong
                            if (dMsg.getDecisions().size() == 1 && dMsg.getDecSI() != null) {
                                final PCMMGateReq gateReq = new PCMMGateReq(dMsg.getDecSI().getData().getData());
                                // TODO - Check and/or Set state here
                                // Gate ADD gateReq.getTrafficProfile() != null
                                // Gate REMOVE gateReq.getTrafficProfile() == null
//                                    final String gateName = trafficProfile.getData().str();
//                                    final Direction gateDir = gateReq.getGateSpec().getDirection();
                                final boolean success = true;

                                // Set response
                                final List<Byte> data = new ArrayList<>();
                                for (final byte val : gateReq.getTransactionID().getAsBinaryArray())
                                    data.add(val);
                                for (final byte val : gateReq.getAMID().getAsBinaryArray())
                                    data.add(val);
                                for (final byte val : gateReq.getSubscriberID().getAsBinaryArray())
                                    data.add(val);

                                // Assign a gate ID
                                final GateID gateID = new GateID();
                                gateID.setGateID(UUID.randomUUID().hashCode());
                                for (final byte val : gateID.getAsBinaryArray())
                                    data.add(val);


                                final byte[] csiArr = new byte[data.size()];
                                for (int i = 0; i < data.size(); i++) {
                                    csiArr[i] = data.get(i);
                                }
                                final COPSClientSI si = new COPSClientSI(CNum.CSI, CType.DEF, new COPSData(csiArr, 0, csiArr.length));

                                final COPSReportMsg reportMsg;
                                if (success) {
                                    reportMsg = new COPSReportMsg(_clientType, getClientHandle(),
                                            new COPSReportType(ReportType.SUCCESS), si, null);
                                } else {
                                    reportMsg = new COPSReportMsg(_clientType, getClientHandle(),
                                            new COPSReportType(ReportType.FAILURE), si, null);
                                }

                                try {
                                    reportMsg.writeData(socket);
                                } catch (IOException e) {
                                    throw new COPSPepException("Error writing gate set SUCCESS Report", e);
                                }
                            }
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
            // COPSDebug.out(getClass().getName(),"Sending FAIL Report\n");
            _sender.sendFailReport(_process.getReportData(this));
        } else {
            // COPSDebug.out(getClass().getName(),"Sending SUCCESS Report\n");
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
     *
     * @throws   COPSPepException
     *
     */
    protected void processOpenNewRequestState() throws COPSPepException {

        if (_process != null)
            _process.newRequestState(this);

        _status = Status.ST_NEW;
    }

    /**
     * Process the message DeleteRequestState
     *
     * @param    dMsg                a  COPSDecisionMsg
     *
     * @throws   COPSPepException
     *
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
     *
     * @param    ssMsg               a  COPSSyncStateMsg
     *
     * @throws   COPSPepException
     *
     */
    protected void processSyncStateRequest(final COPSSyncStateMsg ssMsg) throws COPSPepException {
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

    protected void processClosedConnection(final COPSError error) throws COPSPepException {
        if (_process != null)
            _process.notifyClosedConnection(this, error);

        _status = Status.ST_CCONN;
    }

    protected void processNoKAConnection() throws COPSPepException {
        if (_process != null)
            _process.notifyNoKAliveReceived(this);

        _status = Status.ST_NOKA;
    }

    protected void processAcctReport() throws COPSPepException {
        final Map<String, String> report;
        if (_process != null) report = _process.getAcctData(this);
        else report = new HashMap<>();

        // TODO - do we really want to send when the map is empty???
        _sender.sendAcctReport(report);

        _status = Status.ST_ACCT;
    }

}
