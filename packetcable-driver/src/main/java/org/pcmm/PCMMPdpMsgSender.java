/**
 @header@
 */

package org.pcmm;

import org.pcmm.gates.*;
import org.pcmm.gates.IGateSpec.DSCPTOS;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSMsgSender;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSClientSI.CSIType;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Example of an UNSOLICITED decision
 *
 * <Gate Control Command> = <COPS Common Header> <Client Handle> <Context> <Decision Flags> <ClientSI Data>
 *
 * <ClientSI Data> = <Gate-Set> | <Gate-Info> | <Gate-Delete> |
 *                   <PDP-Config> | <Synch-Request> | <Msg-Receipt>
 * <Gate-Set>      = <Decision Header> <TransactionID> <AMID> <SubscriberID> [<GateID>] <GateSpec>
 *                   <Traffic Profile> <classifier> [<classifier...>] [<Event Generation Info>]
 *                   [<Volume-Based Usage Limit>] [<Time-Based Usage Limit>][<Opaque Data>] [<UserID>]
 */

/**
 * COPS message transceiver class for provisioning connections at the PDP side.
 */
public class PCMMPdpMsgSender extends COPSMsgSender {

    public final static Logger logger = LoggerFactory.getLogger(PCMMPdpMsgSender.class);

    protected short _transactionID;
    protected final short _classifierID;

    // XXX - this does not need to be here
    protected IGateID _gateID;

    /**
     * Creates a PCMMPdpMsgSender
     *
     * @param clientType
     *            COPS client-type
     * @param clientHandle
     *            Client handle
     * @param sock
     *            Socket to the PEP
     */
    public PCMMPdpMsgSender(final short clientType, final COPSHandle clientHandle, final Socket sock) {
        this(clientType, (short)0, clientHandle, sock);
    }

    public PCMMPdpMsgSender(final short clientType, final short tID, final COPSHandle clientHandle,
                            final Socket sock) {
        super(clientType, clientHandle, sock);
        _transactionID = tID;
        _classifierID = 0;
    }

    /**
     * Gets the transaction-id
     *
     * @return transaction-id value
     */
    public short getTransactionID() {
        return _transactionID;
    }

    /**
     * Gets the gate-id
     *
     * @return the gate-id value
     */
    public IGateID getGateID() {
        return _gateID;
    }

    /**
     * Sends a PCMM GateSet COPS Decision message
     * @param gate - the gate
     * @throws COPSPdpException
     */
    public void sendGateSet(final IPCMMGate gate) throws COPSPdpException {
        final ITransactionID trID = new TransactionID();

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (_transactionID == 0 ? (short) (Math.random() * hashCode()) : _transactionID);
        trID.setTransactionIdentifier(_transactionID);

        gate.setTransactionID(trID);
        // retain the transactionId to gate request mapping for gateID recovery after response
        // see PCMMPdpReqStateMan.processReport()
        final Short trIDnum = trID.getTransactionIdentifier();
        logger.info("Adding gate to cache - " + gate + " with key - " + trIDnum);
        PCMMGlobalConfig.transactionGateMap.put(trIDnum, gate);

        // new pcmm specific clientsi
        final byte[] data = gate.getData();
        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.DEF, Command.INSTALL, DecisionFlag.REQERROR));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSClientSI clientSD = new COPSClientSI(CNum.DEC, CType.CSI, new COPSData(data, 0, data.length));
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, _handle, decisionMap, null, clientSD);

        // ** Send the GateSet Decision
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            logger.error("Failed to send the decision", e);
        }

    }

    /**
     * Sends a PCMM GateSet COPS Decision message
     *
     * @param num - the number
     * @throws COPSPdpException
     */
    public void sendGateSetDemo(int num) throws COPSPdpException {
        final IPCMMGate gate = new PCMMGateReq();
        final ITransactionID trID = new TransactionID();
        final IAMID amid = new AMID();
        final ISubscriberID subscriberID = new SubscriberID();
        final IGateSpec gateSpec = new GateSpec();
        final IClassifier classifier = new Classifier();
        final IExtendedClassifier eclassifier = new ExtendedClassifier();
        final int trafficRate;
        if (num == 1)
            trafficRate =   PCMMGlobalConfig.DefaultBestEffortTrafficRate;
        else
            trafficRate =   PCMMGlobalConfig.DefaultLowBestEffortTrafficRate;

        final ITrafficProfile trafficProfile = new BestEffortService(
            (byte) 7); //BestEffortService.DEFAULT_ENVELOP);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setMaximumSustainedTrafficRate(
            trafficRate);
        //  PCMMGlobalConfig.DefaultLowBestEffortTrafficRate );
        //  PCMMGlobalConfig.DefaultBestEffortTrafficRate);

        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);
        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setMaximumSustainedTrafficRate(
            trafficRate);
        //  PCMMGlobalConfig.DefaultLowBestEffortTrafficRate );
        //  PCMMGlobalConfig.DefaultBestEffortTrafficRate);


        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);
        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setMaximumSustainedTrafficRate(
            trafficRate);
        //  PCMMGlobalConfig.DefaultLowBestEffortTrafficRate );
        //  PCMMGlobalConfig.DefaultBestEffortTrafficRate);



        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (_transactionID == 0 ? (short) (Math.random() * hashCode()) : _transactionID);
        trID.setTransactionIdentifier(_transactionID);

        amid.setApplicationType((short) 1);
        amid.setApplicationMgrTag((short) 1);
        gateSpec.setDirection(Direction.UPSTREAM);
        gateSpec.setDSCP_TOSOverwrite(DSCPTOS.OVERRIDE);
        gateSpec.setTimerT1(PCMMGlobalConfig.GateT1);
        gateSpec.setTimerT2(PCMMGlobalConfig.GateT2);
        gateSpec.setTimerT3(PCMMGlobalConfig.GateT3);
        gateSpec.setTimerT4(PCMMGlobalConfig.GateT4);

        // XXX - if the version major is less than 4 we need to use Classifier
        // TODO - Use some variable here or remove...
        if (true) {
            //eclassifier.setProtocol(IClassifier.Protocol.NONE);
//            eclassifier.setProtocol(IClassifier.Protocol.TCP);
            try {
                InetAddress subIP = InetAddress
                                    .getByName(PCMMGlobalConfig.SubscriberID);
                InetAddress srcIP = InetAddress
                                    .getByName(PCMMGlobalConfig.srcIP);
                InetAddress dstIP = InetAddress
                                    .getByName(PCMMGlobalConfig.dstIP);
                InetAddress mask = InetAddress.getByName("0.0.0.0");
                subscriberID.setSourceIPAddress(subIP);
                eclassifier.setSourceIPAddress(srcIP);
                eclassifier.setDestinationIPAddress(dstIP);
                eclassifier.setIPDestinationMask(mask);
                eclassifier.setIPSourceMask(mask);
            } catch (UnknownHostException unae) {
                logger.error("Error getByName", unae);
            }
            eclassifier.setSourcePortStart(PCMMGlobalConfig.srcPort);
            eclassifier.setSourcePortEnd(PCMMGlobalConfig.srcPort);
            eclassifier.setDestinationPortStart(PCMMGlobalConfig.dstPort);
            eclassifier.setDestinationPortEnd(PCMMGlobalConfig.dstPort);
            eclassifier.setActivationState((byte) 0x01);
            // check if we have a stored value of classifierID else we just
            // create
            // one
            // eclassifier.setClassifierID((short) 0x01);
            eclassifier.setClassifierID((short) (_classifierID == 0 ? Math
                                                 .random() * hashCode() : _classifierID));
            // XXX - testie
            // eclassifier.setClassifierID((short) 1);

            eclassifier.setAction((byte) 0x00);
            // XXX - temp default until Gate Modify is hacked in
            // eclassifier.setPriority(PCMMGlobalConfig.EClassifierPriority);
            eclassifier.setPriority((byte) 65);

        } else {
//            classifier.setProtocol(IClassifier.Protocol.TCP);
            try {
                InetAddress subIP = InetAddress
                                    .getByName(PCMMGlobalConfig.SubscriberID);
                InetAddress srcIP = InetAddress
                                    .getByName(PCMMGlobalConfig.srcIP);
                InetAddress dstIP = InetAddress
                                    .getByName(PCMMGlobalConfig.dstIP);
                subscriberID.setSourceIPAddress(subIP);
                classifier.setSourceIPAddress(srcIP);
                classifier.setDestinationIPAddress(dstIP);
            } catch (UnknownHostException unae) {
                logger.error("Error getByName", unae);
            }
            classifier.setSourcePort(PCMMGlobalConfig.srcPort);
            classifier.setDestinationPort(PCMMGlobalConfig.dstPort);
        }

        gate.setTransactionID(trID);
        gate.setAMID(amid);
        gate.setSubscriberID(subscriberID);
        gate.setGateSpec(gateSpec);
        gate.setTrafficProfile(trafficProfile);
        gate.setClassifier(eclassifier);

        final byte[] data = gate.getData();

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.NA, Command.INSTALL, DecisionFlag.REQERROR));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);
        final COPSClientSI clientSD = new COPSClientSI(CSIType.NAMED, new COPSData(data, 0, data.length));

        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), _handle, decisionMap, null, clientSD);

        // ** Send the GateSet Decision
        // **
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            logger.error("Failed to send the decision", e);
        }

    }
    /**
     * Sends a PCMM GateSet COPS Decision message
     * @throws COPSPdpException
     */
    public void sendGateSetBestEffortWithExtendedClassifier() throws COPSPdpException {
        final IPCMMGate gate = new PCMMGateReq();
        final ITransactionID trID = new TransactionID();
        final IAMID amid = new AMID();
        final ISubscriberID subscriberID = new SubscriberID();
        final IGateSpec gateSpec = new GateSpec();
        final IClassifier classifier = new Classifier();
        final IExtendedClassifier eclassifier = new ExtendedClassifier();

        // XXX check if other values should be provided
        //
        final ITrafficProfile trafficProfile = new BestEffortService(
            (byte) 7); //BestEffortService.DEFAULT_ENVELOP);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setMaximumSustainedTrafficRate(
            PCMMGlobalConfig.DefaultLowBestEffortTrafficRate );
        //  PCMMGlobalConfig.DefaultBestEffortTrafficRate);

        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);
        ((BestEffortService) trafficProfile).getReservedEnvelop()
        .setMaximumSustainedTrafficRate(
            PCMMGlobalConfig.DefaultLowBestEffortTrafficRate );
        //  PCMMGlobalConfig.DefaultBestEffortTrafficRate);


        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);
        ((BestEffortService) trafficProfile).getCommittedEnvelop()
        .setMaximumSustainedTrafficRate(
            PCMMGlobalConfig.DefaultLowBestEffortTrafficRate );
        //  PCMMGlobalConfig.DefaultBestEffortTrafficRate);



        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (_transactionID == 0 ? (short) (Math.random() * hashCode()) : _transactionID);
        trID.setTransactionIdentifier(_transactionID);

        amid.setApplicationType((short) 1);
        amid.setApplicationMgrTag((short) 1);
        gateSpec.setDirection(Direction.UPSTREAM);
        gateSpec.setDSCP_TOSOverwrite(DSCPTOS.OVERRIDE);
        gateSpec.setTimerT1(PCMMGlobalConfig.GateT1);
        gateSpec.setTimerT2(PCMMGlobalConfig.GateT2);
        gateSpec.setTimerT3(PCMMGlobalConfig.GateT3);
        gateSpec.setTimerT4(PCMMGlobalConfig.GateT4);

        // XXX - if the version major is less than 4 we need to use Classifier
        if (true) {
            //eclassifier.setProtocol(IClassifier.Protocol.NONE);
//            eclassifier.setProtocol(IClassifier.Protocol.TCP);
            try {
                InetAddress subIP = InetAddress
                                    .getByName(PCMMGlobalConfig.SubscriberID);
                InetAddress srcIP = InetAddress
                                    .getByName(PCMMGlobalConfig.srcIP);
                InetAddress dstIP = InetAddress
                                    .getByName(PCMMGlobalConfig.dstIP);
                InetAddress mask = InetAddress.getByName("0.0.0.0");
                subscriberID.setSourceIPAddress(subIP);
                eclassifier.setSourceIPAddress(srcIP);
                eclassifier.setDestinationIPAddress(dstIP);
                eclassifier.setIPDestinationMask(mask);
                eclassifier.setIPSourceMask(mask);
            } catch (UnknownHostException unae) {
                logger.error("Error getByName", unae);
            }
            eclassifier.setSourcePortStart(PCMMGlobalConfig.srcPort);
            eclassifier.setSourcePortEnd(PCMMGlobalConfig.srcPort);
            eclassifier.setDestinationPortStart(PCMMGlobalConfig.dstPort);
            eclassifier.setDestinationPortEnd(PCMMGlobalConfig.dstPort);
            eclassifier.setActivationState((byte) 0x01);
            // check if we have a stored value of classifierID else we just
            // create
            // one
            // eclassifier.setClassifierID((short) 0x01);
            eclassifier.setClassifierID((short) (_classifierID == 0 ? Math
                                                 .random() * hashCode() : _classifierID));
            // XXX - testie
            // eclassifier.setClassifierID((short) 1);

            eclassifier.setAction((byte) 0x00);
            // XXX - temp default until Gate Modify is hacked in
            // eclassifier.setPriority(PCMMGlobalConfig.EClassifierPriority);
            eclassifier.setPriority((byte) 65);

        } else {
//            classifier.setProtocol(IClassifier.Protocol.TCP);
            try {
                InetAddress subIP = InetAddress
                                    .getByName(PCMMGlobalConfig.SubscriberID);
                InetAddress srcIP = InetAddress
                                    .getByName(PCMMGlobalConfig.srcIP);
                InetAddress dstIP = InetAddress
                                    .getByName(PCMMGlobalConfig.dstIP);
                subscriberID.setSourceIPAddress(subIP);
                classifier.setSourceIPAddress(srcIP);
                classifier.setDestinationIPAddress(dstIP);
            } catch (UnknownHostException unae) {
                logger.error("Error getByName", unae);
            }
            classifier.setSourcePort(PCMMGlobalConfig.srcPort);
            classifier.setDestinationPort(PCMMGlobalConfig.dstPort);
        }

        gate.setTransactionID(trID);
        gate.setAMID(amid);
        gate.setSubscriberID(subscriberID);
        gate.setGateSpec(gateSpec);
        gate.setTrafficProfile(trafficProfile);
        gate.setClassifier(eclassifier);

        byte[] data = gate.getData();

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);
        final COPSClientSI clientSD = new COPSClientSI(CSIType.NAMED, new COPSData(data, 0, data.length));

        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, _handle, decisionMap, null, clientSD);

        // ** Send the GateSet Decision
        // **
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            logger.error("Failed to send the decision", e);
        }

    }


    public boolean handleGateReport(final Socket socket) throws COPSPdpException {
        try {
            // waits for the gate-set-ack or error
            final COPSMsg responseMsg = COPSTransceiver.receiveMsg(socket);
            if (responseMsg.getHeader().getOpCode().equals(OPCode.RPT)) {
                logger.info("processing received report from CMTS");
                final COPSReportMsg reportMsg = (COPSReportMsg) responseMsg;
                if (reportMsg.getClientSI() == null) {
                    return false;
                }
                final IPCMMGate responseGate = new PCMMGateReq(reportMsg.getClientSI().getData().getData());
                if (responseGate.getTransactionID() != null
                        && responseGate.getTransactionID().getGateCommandType() == ITransactionID.GateSetAck) {
                    logger.info("the CMTS has sent a Gate-Set-Ack response");
                    // here CMTS responded that he acknowledged the Gate-Set
                    // TODO do further check of Gate-Set-Ack GateID etc...
                    _gateID = responseGate.getGateID();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } catch (Exception e) { // COPSException, IOException
            throw new COPSPdpException("Error COPSTransceiver.receiveMsg", e);
        }
    }


    /**
     * Sends a PCMM GateSet COPS Decision message
     *
     * @throws COPSPdpException
     */
    public void sendGateSet() throws COPSPdpException {
        // Common Header with the same ClientType as the request

        final IPCMMGate gate = new PCMMGateReq();
        final ITransactionID trID = new TransactionID();

        final IAMID amid = new AMID();
        final ISubscriberID subscriberID = new SubscriberID();
        final IGateSpec gateSpec = new GateSpec();
        final IClassifier classifier = new Classifier();
        // XXX check if other values should be provided
        final ITrafficProfile trafficProfile = new BestEffortService(
            BestEffortService.DEFAULT_ENVELOP);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (_transactionID == 0 ? (short) (Math.random() * hashCode()) : _transactionID);
        trID.setTransactionIdentifier(_transactionID);

        amid.setApplicationType((short) 1);
        amid.setApplicationMgrTag((short) 1);
        gateSpec.setDirection(Direction.UPSTREAM);
        gateSpec.setDSCP_TOSOverwrite(DSCPTOS.OVERRIDE);
        gateSpec.setTimerT1(PCMMGlobalConfig.GateT1);
        gateSpec.setTimerT2(PCMMGlobalConfig.GateT2);
        gateSpec.setTimerT3(PCMMGlobalConfig.GateT3);
        gateSpec.setTimerT4(PCMMGlobalConfig.GateT4);

        /*
         * ((DOCSISServiceClassNameTrafficProfile) trafficProfile)
         * .setServiceClassName("S_up");
         */

//        classifier.setProtocol(IClassifier.Protocol.TCP);
        try {
            InetAddress subIP = InetAddress.getByName(PCMMGlobalConfig.SubscriberID);
            InetAddress srcIP = InetAddress.getByName(PCMMGlobalConfig.srcIP);
            InetAddress dstIP = InetAddress.getByName(PCMMGlobalConfig.dstIP);
            subscriberID.setSourceIPAddress(subIP);
            classifier.setSourceIPAddress(srcIP);
            classifier.setDestinationIPAddress(dstIP);
        } catch (UnknownHostException unae) {
            logger.error("Error getByName", unae);
        }
        classifier.setSourcePort(PCMMGlobalConfig.srcPort);
        classifier.setDestinationPort(PCMMGlobalConfig.dstPort);

        gate.setTransactionID(trID);
        gate.setAMID(amid);
        gate.setSubscriberID(subscriberID);
        gate.setGateSpec(gateSpec);
        gate.setTrafficProfile(trafficProfile);
        gate.setClassifier(classifier);

        final byte[] data = gate.getData();

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSClientSI clientSD = new COPSClientSI(CSIType.NAMED, new COPSData(data, 0, data.length));

        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), _handle, decisionMap, null, clientSD);

        // ** Send the GateSet Decision
        // **
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            logger.error("Failed to send the decision", e);
        }
    }

    /**
     * Sends a message asking that the request state be deleted
     *
     * @throws COPSPdpException
     */
    public void sendGateDelete(final IPCMMGate gate) throws COPSPdpException {
        // set transaction ID to gate set
        final ITransactionID trID = new TransactionID();
        trID.setGateCommandType(ITransactionID.GateDelete);
        _transactionID = (_transactionID == 0 ? (short) (Math.random() * hashCode()) : _transactionID);
        trID.setTransactionIdentifier(_transactionID);
        gate.setTransactionID(trID);

        Short trIDnum = trID.getTransactionIdentifier();
        PCMMGlobalConfig.transactionGateMap.put(trIDnum, gate);

        // gateDelete only requires AMID, subscriberID, and gateID
        // remove the gateSpec, traffic profile, and classifiers from original gate request
        gate.setGateSpec(null);
        gate.setTrafficProfile(null);
        gate.setClassifier(null);
        // clear the error object
        gate.setError(null);

        // XXX - GateID
        final byte[] data = gate.getData();
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.DEF, Command.INSTALL, DecisionFlag.REQERROR));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);
        final COPSClientSI clientSD = new COPSClientSI(CNum.DEC, CType.CSI, new COPSData(data, 0, data.length));

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), _handle, decisionMap, null, clientSD);

        // ** Send the GateDelete Decision
        // **
        try {
            decisionMsg.writeData(_sock);
            // decisionMsg.writeData(socket_id);
        } catch (IOException e) {
            logger.error("Failed to send the decision", e);
        }
    }

    /**
     * Sends a request asking that a new request state be created
     *
     * @throws COPSPdpException
     */
    public void sendOpenNewRequestState() throws COPSPdpException {
        /*
         * <Decision Message> ::= <Common Header: Flag UNSOLICITED> <Client
         * Handle> *(<Decision>) [<Integrity>] <Decision> ::= <Context>
         * <Decision: Flags> <Decision: Flags> ::= Install Request-State
         */

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(Command.INSTALL, DecisionFlag.REQSTATE));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), _handle, decisionMap, null, null);

        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the open new request state", e);
        }
    }

    /**
     * Sends a message asking for a COPS sync operation
     *
     * @throws COPSPdpException
     */
    public void sendGateInfo() throws COPSPdpException {
        /*
         * <Gate-Info> ::= <Common Header> [<Client Handle>] [<Integrity>]
         */
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(getClientType(), _handle, null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the GateInfo request", e);
        }
    }

    /**
     * Sends a message asking for a COPS sync operation
     *
     * @throws COPSPdpException
     */
    public void sendSyncRequest() throws COPSPdpException {
        /*
         * <Synchronize State Request> ::= <Common Header> [<Client Handle>]
         * [<Integrity>]
         */

        // Client Handle with the same clientHandle as the request
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(getClientType(), _handle, null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the sync state request", e);
        }
    }
    // XXX - Temp
    public void sendSyncRequestState() throws COPSPdpException {
    }
    // XXX - Temp
    public void sendDeleteRequestState() throws COPSPdpException {
    }
}
