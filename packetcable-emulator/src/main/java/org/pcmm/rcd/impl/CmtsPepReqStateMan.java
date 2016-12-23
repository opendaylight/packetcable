/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.primitives.Bytes;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IAMID;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.IGateID;
import org.pcmm.gates.IGateSpec;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.IGateState;
import org.pcmm.gates.IPCMMError;
import org.pcmm.gates.IPCMMError.ErrorCode;
import org.pcmm.gates.ISubscriberID;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.impl.AMID;
import org.pcmm.gates.impl.DOCSISServiceClassNameTrafficProfile;
import org.pcmm.gates.impl.DOCSISFlowSpecTrafficProfile;
import org.pcmm.gates.impl.GateID;
import org.pcmm.gates.impl.GateSpec;
import org.pcmm.gates.impl.GateState;
import org.pcmm.gates.impl.GateTimeInfo;
import org.pcmm.gates.impl.GateUsageInfo;
import org.pcmm.gates.impl.PCMMError;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.gates.impl.TransactionID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpep.COPSPepException;
import org.umu.cops.prpep.COPSPepMsgSender;
import org.umu.cops.prpep.COPSPepReqStateMan;
import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSMsgParser;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;
import org.umu.cops.stack.COPSReportMsg;
import org.umu.cops.stack.COPSReportType;
import org.umu.cops.stack.COPSReportType.ReportType;

/**
 * PEP State manager implementation for use in a CMTS.
 */
public class CmtsPepReqStateMan extends COPSPepReqStateMan {

    private static final Logger logger = LoggerFactory.getLogger(CmtsPepReqStateMan.class);

    private final CMTSConfig config;

    private final Map<IGateID, GateMetaData> gateStateMap;

    private static class GateMetaData {

        private final PCMMGateReq gateReq;
        private long commitTime;
        private long kiloBytesTransmitted;

        private Random random;

        public GateMetaData(final PCMMGateReq gateReq) {
            this.gateReq = checkNotNull(gateReq);
            updateCommitTime();
            kiloBytesTransmitted = 0;
            this.random = new Random(gateReq.getGateID().getGateID());
        }

        public long updateCommitTime() {
            commitTime = System.currentTimeMillis() / 1000L;
            return commitTime;
        }

        public PCMMGateReq getGateReq() {
            return gateReq;
        }

        public long getCommitTime() {
            return commitTime;
        }

        public int getCommitDuration() {
            return (int)((System.currentTimeMillis() / 1000L) - commitTime);
        }

        public long updateKiloBytesTransmitted() {
            kiloBytesTransmitted += random.nextInt(2000);
            return kiloBytesTransmitted;
        }

        public long getKiloBytesTransmitted() {
            return kiloBytesTransmitted;
        }
    }

    private static class GateKey {
        private IAMID amID;
        private ISubscriberID subscriberID;
        private IGateID gateID;

        public GateKey(final IAMID amID, final ISubscriberID subscriberID, final IGateID gateID) {
            this.amID = checkNotNull(amID);
            this.subscriberID = checkNotNull(subscriberID);
            this.gateID = checkNotNull(gateID);
        }

        public IAMID getAmID() {
            return amID;
        }

        public ISubscriberID getSubscriberID() {
            return subscriberID;
        }

        public IGateID getGateID() {
            return gateID;
        }

        public boolean matches(final AMID otherAMID, final ISubscriberID otherSubscriberID) {
            checkNotNull(otherAMID);
            checkNotNull(otherSubscriberID);

            return Objects.equal(amID, otherAMID) && Objects.equal(subscriberID, otherSubscriberID);

        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final GateKey otherGateKey = (GateKey) o;
            return Objects.equal(amID, otherGateKey.amID) &&
                    Objects.equal(subscriberID, otherGateKey.subscriberID) &&
                    Objects.equal(gateID, otherGateKey.gateID);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(amID, subscriberID, gateID);
        }
    }

    /**
     * Create a State Request Manager
     *
     * @param clientType
     *         - the client type for this connection
     * @param clientHandle
     *         - the client-handle for this connection
     * @param process
     *         - the data processor
     * @param socket
     *         - the socket connection
     */
    public CmtsPepReqStateMan(final short clientType, final COPSHandle clientHandle, final CmtsDataProcessor process,
            final Socket socket, final CMTSConfig config) {
        super(clientType, clientHandle, process, socket, new COPSPepMsgSender(clientType, clientHandle, socket));
        this.config = checkNotNull(config);
        gateStateMap = new HashMap<>();
    }

    @Override
    protected void processDecision(final COPSDecisionMsg dMsg) throws COPSException {
        logger.info("Processing decision message - " + dMsg);
        final Map<COPSContext, Set<COPSDecision>> decisions = dMsg.getDecisions();

        final Map<String, String> removeDecs = new HashMap<>();
        final Map<String, String> installDecs = new HashMap<>();

        for (final Set<COPSDecision> copsDecisions : decisions.values()) {
            final COPSDecision cmddecision = copsDecisions.iterator().next();
            logger.debug("decision command: " + cmddecision.getCommand());
            switch (cmddecision.getCommand()) {
                case INSTALL:
                    for (final COPSDecision decision : copsDecisions) {
                        if (decision.getFlag().equals(DecisionFlag.REQERROR)) {
                            logger.info("processing decision: " + dMsg.getDecSI());
                            // This is assuming a gate set right or wrong
                            if (dMsg.getDecisions().size() == 1 && dMsg.getDecSI() != null) {
                                final PCMMGateReq gateReq = PCMMGateReq.parse(dMsg.getDecSI().getData().getData());
                                if (gateReq != null) {
                                    processGateReq(gateReq, _socket);
                                }
                                else {
                                    logger.error("gateReq failed to parse");
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

        switch (gateReq.getTransactionID().getGateCommandType()) {
            case GATE_SET:
                processGateSet(gateReq, socket);
                break;
            case GATE_INFO:
                processGateInfo(gateReq, socket);
                break;
            case GATE_DELETE:
                processGateDelete(gateReq, socket);
                break;
            default:
                logger.error("Emulator does not support gate command: {}",
                        gateReq.getTransactionID().getGateCommandType());
        }

    }


    private IPCMMError checkForMissingObjects(final PCMMGateReq gateReq) {
        // In cases where multiple valid alternatives exist for the S-Type of a missing object,
        // this portion of the Error-Subcode MUST be set to zero.

        if (gateReq.getTransactionID() == null) {
            final short subCode =
                    COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.TRANSACTION_ID.getValue(), TransactionID.STYPE);
            return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
        }

        final ITransactionID.GateCommandType gateCommand = gateReq.getTransactionID().getGateCommandType();

        if (gateCommand == ITransactionID.GateCommandType.GATE_SET) {
            // Gate set does not allow gateID
            if (gateReq.getGateID() != null) {
                final short subCode = COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.GATE_ID.getValue(), GateID.STYPE);
                return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
            }

            if (gateReq.getTrafficProfile() == null) {
                final short subCode = COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.TRAFFIC_PROFILE.getValue(), (byte) 0);
                return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
            }

            if (gateReq.getClassifiers() == null || gateReq.getClassifiers().isEmpty()) {
                final short subCode = COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.CLASSIFIERS.getValue(), (byte) 0);
                return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
            }

            if (gateReq.getGateSpec() == null) {
                final short subCode = COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.GATE_SPEC.getValue(), GateSpec.STYPE);
                return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
            }

            final IGateSpec gateSpec = gateReq.getGateSpec();
            if (gateSpec.getDirection() == null) {
                return new PCMMError(ErrorCode.INVALID_FIELD);
            }

        }
        else {

            if (gateReq.getGateID() == null) {
                final short subCode = COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.GATE_ID.getValue(), GateID.STYPE);
                return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
            }
        }

        if (gateReq.getAMID() == null) {
            final short subCode = COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.AMID.getValue(), AMID.STYPE);
            return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
        }

        if (gateReq.getSubscriberID() == null || gateReq.getSubscriberID().getSourceIPAddress() == null
                || gateReq.getSubscriberID().getSourceIPAddress().getHostAddress() == null) {
            final short subCode = COPSMsgParser.bytesToShort(PCMMBaseObject.SNum.SUBSCRIBER_ID.getValue(), (byte) 0);
            return new PCMMError(ErrorCode.MISSING_REQ_OBJ, subCode);
        }


        return null;
    }

    private IPCMMError checkForInvalidObjects(final PCMMGateReq gateReq) {
        final ITransactionID.GateCommandType gateCommand = gateReq.getTransactionID().getGateCommandType();

        // GateID
        if (gateCommand == ITransactionID.GateCommandType.GATE_INFO) {
            if (!gateStateMap.containsKey(gateReq.getGateID())) {
                return new PCMMError(ErrorCode.UNK_GATE_ID);
            }
        }
        else {
            // Traffic profile type check
            if (!(gateReq.getTrafficProfile() instanceof DOCSISServiceClassNameTrafficProfile) &&
                !(gateReq.getTrafficProfile() instanceof DOCSISFlowSpecTrafficProfile)) {
                logger.error("Currently only DOCSIS Service Class Name and Flow Spec Traffic Profiles are supported: attempted ",
                        gateReq.getTrafficProfile().getClass().getName());
                return new PCMMError(ErrorCode.OTHER_UNSPECIFIED);
            }
            // ServiceClassName match check
            else if (gateReq.getTrafficProfile() instanceof DOCSISServiceClassNameTrafficProfile) {
                final DOCSISServiceClassNameTrafficProfile scnTrafficProfile =
                        (DOCSISServiceClassNameTrafficProfile) gateReq.getTrafficProfile();

                Set<String> directionSCNs;
                if (gateReq.getGateSpec().getDirection().equals(Direction.DOWNSTREAM)) {
                    directionSCNs = config.getDownstreamServiceClassNames();
                } else {
                    directionSCNs = config.getUpstreamServiceClassNames();
                }
                if (!directionSCNs.contains(scnTrafficProfile.getScnName())) {
                    return new PCMMError(ErrorCode.UNDEF_SCN_NAME);
                }
            }

            // number of classifiers
            if (config.getNumberOfSupportedClassifiers() < gateReq.getClassifiers().size()) {
                return new PCMMError(ErrorCode.NUM_CLASSIFIERS, config.getNumberOfSupportedClassifiers());
            }
        }

        // SubscriberID
        String subId = gateReq.getSubscriberID().getSourceIPAddress().getHostAddress();
        if(!config.getModemStatus().containsKey(subId) || !config.getModemStatus().get(subId)) {
            return new PCMMError(ErrorCode.INVALID_SUB_ID);
        }

        // Iff the gate exists
        if (gateReq.getGateID() != null
                && gateStateMap.containsKey(gateReq.getGateID())) {
            GateMetaData existingGate = gateStateMap.get(gateReq.getGateID());

            // Unauthorized AMID - only the AM that created a gate may change it
            if (!existingGate.getGateReq().getAMID().equals(gateReq.getAMID())) {
                return new PCMMError(ErrorCode.UNAUTH_AMID);
            }
        }

        return null;
    }

    private IPCMMError getGateError(final PCMMGateReq gateReq) {

        IPCMMError error = null;
        error = checkForMissingObjects(gateReq);
        if (error != null) {
            return error;
        }

        error = checkForInvalidObjects(gateReq);
        if (error != null) {
            return error;
        }

        return null;
    }

    private void processGateSet(final PCMMGateReq gateReq, final Socket socket) throws COPSException {

        final String subId = gateReq.getSubscriberID().getSourceIPAddress().getHostAddress();
        final Direction gateDir = gateReq.getGateSpec().getDirection();

        final String serviceClassName;
        if (gateReq.getTrafficProfile() instanceof DOCSISServiceClassNameTrafficProfile) {
            serviceClassName = ((DOCSISServiceClassNameTrafficProfile)gateReq.getTrafficProfile()).getScnName();
            logger.info("Processing ServiceClassName[" + serviceClassName + "] gate set with direction [" + gateDir + ']');
        } else if (gateReq.getTrafficProfile() instanceof DOCSISFlowSpecTrafficProfile) {
            serviceClassName = null;
            logger.info("Processing FlowSpec gate set with direction [" + gateDir + ']');
        } else {
            serviceClassName = null;
            logger.error("Unknown Traffic Profile type: " + gateReq.getTrafficProfile().getClass().getName());
        }

        final IPCMMError error = getGateError(gateReq);
        gateReq.setError(error);

        logger.info("Processing gate set request [" + serviceClassName + "] with direction [" + gateDir + ']');

        // Set response

        final ITransactionID.GateCommandType gateCommand = (error == null)
                ? ITransactionID.GateCommandType.GATE_SET_ACK
                : ITransactionID.GateCommandType.GATE_SET_ERR;

        final TransactionID transactionID =
                new TransactionID(gateReq.getTransactionID().getTransactionIdentifier(), gateCommand);

        final List<Byte> data = new ArrayList<>();
        addBytesToList(transactionID.getAsBinaryArray(), data);
        addBytesToList(gateReq.getAMID().getAsBinaryArray(), data);
        addBytesToList(gateReq.getSubscriberID().getAsBinaryArray(), data);


        if (error == null) {
            // Assign a gate ID
            final GateID gateID = new GateID(UUID.randomUUID().hashCode());
            for (final byte val : gateID.getAsBinaryArray()) {
                data.add(val);
            }
            gateReq.setGateID(gateID);

            int timeStamp = (int)(System.currentTimeMillis() / 1000L);
            gateReq.setGateTimeInfo(new GateTimeInfo(timeStamp));

            gateStateMap.put(gateID, new GateMetaData(gateReq));
        }
        else {
            addBytesToList(error.getAsBinaryArray(), data);
        }

        final byte[] csiArr = Bytes.toArray(data);
        final COPSClientSI si = new COPSClientSI(CNum.CSI, CType.DEF, new COPSData(csiArr, 0, csiArr.length));

        final ReportType reportType;
        if (gateReq.getError() == null) {
            reportType = ReportType.SUCCESS;
        } else {
            reportType = ReportType.FAILURE;
        }

        logger.info("Returning " + reportType + " for gate request [" + serviceClassName + "] direction [" + gateDir
                + "] for host - " + subId);
        sendReport(reportType, si, socket);

    }

    private void processGateInfo(final PCMMGateReq gateReq, final Socket socket) throws COPSException {
        logger.info("GateInfo");

        IPCMMError error = getGateError(gateReq);

        final TransactionID transactionID;
        final ReportType reportType;
        if (error != null) {
             transactionID = new TransactionID(gateReq.getTransactionID().getTransactionIdentifier(),
                            ITransactionID.GateCommandType.GATE_INFO_ERR);
            reportType = ReportType.FAILURE;
        }
        else {
            transactionID = new TransactionID(gateReq.getTransactionID().getTransactionIdentifier(),
                    ITransactionID.GateCommandType.GATE_INFO_ACK);
            reportType = ReportType.SUCCESS;
        }

        final List<Byte> data = new ArrayList<>();
        addBytesToList(transactionID.getAsBinaryArray(), data);
        addBytesToList(gateReq.getAMID().getAsBinaryArray(), data);
        addBytesToList(gateReq.getSubscriberID().getAsBinaryArray(), data);
        addBytesToList(gateReq.getGateID().getAsBinaryArray(), data);

        if (error != null) {
            addBytesToList(error.getAsBinaryArray(), data);
        }
        else {
            GateMetaData exisitingGate = gateStateMap.get(gateReq.getGateID());

            addBytesToList(exisitingGate.getGateReq().getGateSpec().getAsBinaryArray(), data);

            for (IClassifier classifier : exisitingGate.getGateReq().getClassifiers()) {
                addBytesToList(classifier.getAsBinaryArray(), data);
            }

            addBytesToList(exisitingGate.getGateReq().getTrafficProfile().getAsBinaryArray(), data);

            GateTimeInfo timeInfo = new GateTimeInfo(exisitingGate.getCommitDuration());
            addBytesToList(timeInfo.getAsBinaryArray(), data);

            GateUsageInfo gateUsageInfo = new GateUsageInfo(exisitingGate.updateKiloBytesTransmitted());
            addBytesToList(gateUsageInfo.getAsBinaryArray(), data);

            GateState gateState = new GateState(IGateState.GateStateType.COMMITTED,
                    IGateState.GateStateReasonType.OTHER);
            addBytesToList(gateState.getAsBinaryArray(), data);

            logger.info("Returning " + reportType + " for gate info request on gate " + exisitingGate.getGateReq().getGateID() );
        }

        final byte[] csiArr = Bytes.toArray(data);
        COPSClientSI copsClientSI = new COPSClientSI(CNum.CSI, CType.DEF, new COPSData(csiArr, 0, csiArr.length));

        sendReport(reportType, copsClientSI, socket);
    }

    private void processGateDelete(final PCMMGateReq gateReq, final Socket socket) throws COPSException {
        logger.info("GateDelete");

        final TransactionID transactionID;
        final ReportType reportType;
        transactionID = new TransactionID(gateReq.getTransactionID().getTransactionIdentifier(),
                                              ITransactionID.GateCommandType.GATE_DELETE_ACK);
        reportType = ReportType.SUCCESS;

        final List<Byte> data = new ArrayList<>();
        addBytesToList(transactionID.getAsBinaryArray(), data);
        addBytesToList(gateReq.getAMID().getAsBinaryArray(), data);
        addBytesToList(gateReq.getSubscriberID().getAsBinaryArray(), data);
        addBytesToList(gateReq.getGateID().getAsBinaryArray(), data);

        GateMetaData exisitingGate = gateStateMap.get(gateReq.getGateID());
        gateStateMap.remove(gateReq.getGateID());

        GateState gateState = new GateState(IGateState.GateStateType.COMMITTED,
                                                IGateState.GateStateReasonType.OTHER);
        addBytesToList(gateState.getAsBinaryArray(), data);

        logger.info("Deleting " + reportType + " for gate delete request on gate " + exisitingGate.getGateReq().getGateID() );

        final byte[] csiArr = Bytes.toArray(data);
        COPSClientSI copsClientSI = new COPSClientSI(CNum.CSI, CType.DEF, new COPSData(csiArr, 0, csiArr.length));
        
        sendReport(reportType, copsClientSI, socket);
    }

    private void sendReport(ReportType reportType, COPSClientSI copsClientSI, final Socket socket)
            throws COPSPepException {
        logger.info("Returning {} for gate request", reportType);

        final COPSReportMsg reportMsg =
                new COPSReportMsg(_clientType, getClientHandle(), new COPSReportType(reportType), copsClientSI, null);
        try {
            reportMsg.writeData(socket);
        } catch (IOException e) {
            throw new COPSPepException("Error writing gate set SUCCESS Report", e);
        }

    }

    private static void addBytesToList(byte[] array, List<Byte> list) {
        checkNotNull(array);
        checkNotNull(list);

        if (array.length == 0) return;

        // if list supports resizing do so
        if (list instanceof ArrayList) {
            ArrayList<Byte> arrayList = (ArrayList<Byte>) list;
            arrayList.ensureCapacity(list.size() + array.length);
        }
        else if (list instanceof Vector){
            Vector<Byte> vector = (Vector<Byte>) list;
            vector.ensureCapacity(vector.size() + array.length);
        }

        // Add all
        for (byte b : array) {
            list.add(b);
        }
    }

}
