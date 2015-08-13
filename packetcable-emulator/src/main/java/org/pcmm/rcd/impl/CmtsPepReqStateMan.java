/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.rcd.impl;

import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.IPCMMError;
import org.pcmm.gates.impl.GateID;
import org.pcmm.gates.impl.PCMMError;
import org.pcmm.gates.impl.PCMMGateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpep.COPSPepException;
import org.umu.cops.prpep.COPSPepMsgSender;
import org.umu.cops.prpep.COPSPepReqStateMan;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * PEP State manager implementation for use in a CMTS.
 */
public class CmtsPepReqStateMan extends COPSPepReqStateMan {

    private final static Logger logger = LoggerFactory.getLogger(CmtsPepReqStateMan.class);

    /**
     * The configured gates
     */
    private final Map<Direction, Set<String>> gateConfig;

    /**
     * The connected CMTSs and whether or not they are up
     */
    private final Map<String, Boolean> cmStatus;

    /**
     * Contains the gates that have been set where the key is the gate name and the value is a Set of subIds
     * that are using this gate
     */
    private final Map<String, Set<String>> gatesSetMap;

    /**
     * Create a State Request Manager
     *
     * @param clientType - the client type for this connection
     * @param clientHandle - the client-handle for this connection
     * @param process - the data processor
     * @param socket - the socket connection
     * @param gateConfig - the configured service class names (gates)
     */
    public CmtsPepReqStateMan(final short clientType, final COPSHandle clientHandle, final CmtsDataProcessor process,
                              final Socket socket, final Map<Direction, Set<String>> gateConfig,
                              final Map<String, Boolean> cmStatus) {
        super(clientType, clientHandle, process, socket, new COPSPepMsgSender(clientType, clientHandle, socket));
        this.gateConfig = Collections.unmodifiableMap(gateConfig);
        this.cmStatus = Collections.unmodifiableMap(cmStatus);

        this.gatesSetMap = new HashMap<>();
        for (final Set<String> gateIdSet: gateConfig.values()) {
            for (final String gateId : gateIdSet) {
                gatesSetMap.put(gateId, new HashSet<String>());
            }
        }
    }

    @Override
    protected void processDecision(final COPSDecisionMsg dMsg) throws COPSException {
        logger.info("Processing decision message - " + dMsg);
        final Map<COPSContext, Set<COPSDecision>> decisions = dMsg.getDecisions();

        final Map<String, String> removeDecs = new HashMap<>();
        final Map<String, String> installDecs = new HashMap<>();

        for (final Set<COPSDecision> copsDecisions: decisions.values()) {
            final COPSDecision cmddecision = copsDecisions.iterator().next();
            switch (cmddecision.getCommand()) {
                case INSTALL:
                    for (final COPSDecision decision : copsDecisions) {
                        if (decision.getFlag().equals(DecisionFlag.REQERROR)) {
                            logger.info("processing decision");
                            // This is assuming a gate set right or wrong
                            if (dMsg.getDecisions().size() == 1 && dMsg.getDecSI() != null) {
                                final PCMMGateReq gateReq = new PCMMGateReq(dMsg.getDecSI().getData().getData());
                                if (gateReq.getGateSpec() != null) {
                                    processGateReq(gateReq, _socket);
                                }
                            }
                        }
                    }
                    break;
                case REMOVE:
                    for (final COPSDecision decision : copsDecisions) {
                        // TODO - implement gate delete
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

    private void processGateReq(final PCMMGateReq gateReq, final Socket socket) throws COPSException {
        // TODO - Check and/or Set state here
        // Gate ADD gateReq.getTrafficProfile() != null
        // Gate REMOVE gateReq.getTrafficProfile() == null
        final String subId = gateReq.getSubscriberID().getSourceIPAddress().getHostAddress();

        // Get direction here
        final Direction gateDir = gateReq.getGateSpec().getDirection();
        final Set<String> gateNames = gateConfig.get(gateDir);
        final String gateName = gateReq.getTrafficProfile().getData().str();

        IPCMMError error = new PCMMError();
        if (subId == null || gateDir == null || gateNames == null || gateName == null) {
            // Missing required object
            error.setErrorCode((short)3);
        } else if (!cmStatus.keySet().contains(subId)
                || (cmStatus.keySet().contains(subId) && !cmStatus.get(subId))) {
            // Invalid Object
            error.setErrorCode((short)13);
        } else if (!gateNames.contains(gateName.trim())) {
            error.setErrorCode((short)11);
        } else {
            error = null;
            gatesSetMap.get(gateName.trim()).add(subId);
        }
        gateReq.setError(error);

        logger.info("Processing gate request [" + gateName + "] with direction [" + gateDir + ']');

        // Get gate name

        // Set response
        final List<Byte> data = new ArrayList<>();
        for (final byte val : gateReq.getTransactionID().getAsBinaryArray())
            data.add(val);
        for (final byte val : gateReq.getAMID().getAsBinaryArray())
            data.add(val);
        for (final byte val : gateReq.getSubscriberID().getAsBinaryArray())
            data.add(val);
        if (error != null) for (final byte val : gateReq.getError().getAsBinaryArray())
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

        final ReportType reportType;
        if (gateReq.getError() == null) reportType = ReportType.SUCCESS; else reportType = ReportType.FAILURE;

        logger.info("Returning " + reportType + " for gate request [" + gateName + "] direction [" + gateDir
                + "] for host - " + subId);
        final COPSReportMsg reportMsg = new COPSReportMsg(_clientType, getClientHandle(),
                    new COPSReportType(reportType), si, null);
        try {
            reportMsg.writeData(socket);
        } catch (IOException e) {
            throw new COPSPepException("Error writing gate set SUCCESS Report", e);
        }
    }

}
