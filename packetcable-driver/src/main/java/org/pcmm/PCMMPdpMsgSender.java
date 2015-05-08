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
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSHeader.ClientType;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//temp
//pcmm
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
public class PCMMPdpMsgSender {

    public final static Logger logger = LoggerFactory.getLogger(PCMMPdpMsgSender.class);

    /**
     * Socket connected to PEP
     */
    protected Socket _sock;

    /**
     * COPS client-type that identifies the policy client
     */
    protected ClientType _clientType;

    /**
     * COPS client handle used to uniquely identify a particular PEP's request
     * for a client-type
     */
    protected COPSHandle _handle;

    /**
     *
     */
    protected short _transactionID;
    protected short _classifierID;
    // XXX - this does not need to be here
    protected int _gateID;

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
    public PCMMPdpMsgSender(final ClientType clientType, final COPSHandle clientHandle, final Socket sock) {
        // COPS Handle
        _handle = clientHandle;
        _clientType = clientType;

        _transactionID = 0;
        _classifierID = 0;
        _sock = sock;
    }

    public PCMMPdpMsgSender(final ClientType clientType, final short tID, final COPSHandle clientHandle,
                            final Socket sock) {
        // COPS Handle
        _handle = clientHandle;
        _clientType = clientType;
        _transactionID = tID;
        _classifierID = 0;
        _sock = sock;
    }

    /**
     * Gets the client handle
     *
     * @return Client's <tt>COPSHandle</tt>
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Gets the client-type
     *
     * @return Client-type value
     */
    public ClientType getClientType() {
        return _clientType;
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


        // new pcmm specific clientsi
        final byte[] data = gate.getData();
        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR,
                new COPSData(data, 0, data.length)));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, new COPSHandle(getClientHandle().getId()),
                decisionMap, null);
//                new COPSClientSI(CSIType.SIGNALED, new COPSData(data, 0, data.length)), decisionMap);
        //                new COPSClientSI(CNum.DEC, (byte) 4, new COPSData(data, 0, data.length), null));
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
            eclassifier.setProtocol(IClassifier.Protocol.TCP);
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
            classifier.setProtocol(IClassifier.Protocol.TCP);
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
        decisionSet.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR,
                new COPSData(data, 0, data.length)));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(),
                new COPSHandle(getClientHandle().getId()), decisionMap, null);
//                new COPSClientSI(CSIType.SIGNALED, new COPSData(data, 0, data.length)), decisionMap);
                //                new COPSClientSI(CNum.DEC, (byte) 4, new COPSData(data, 0, data.length))); TODO - what does the value of 4 mean here???

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
            eclassifier.setProtocol(IClassifier.Protocol.TCP);
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
            classifier.setProtocol(IClassifier.Protocol.TCP);
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
        decisionSet.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR, new COPSData(data, 0, data.length)));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, new COPSHandle(getClientHandle().getId()),
                decisionMap, null);
//                new COPSClientSI(CSIType.SIGNALED, new COPSData(data, 0, data.length)), decisionMap);
        //                new COPSClientSI(CNum.DEC, (byte) 4, new COPSData(data, 0, data.length), null)); TODO - what does the value of 4 mean here???

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
                    _gateID = responseGate.getGateID().getGateID();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } catch (Exception e) { // COPSException, IOException
            throw new COPSPdpException("Error COPSTransceiver.receiveMsg");
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

        // byte[] content = "1234".getBytes();

        // handle.setId(new COPSData(content, 0, content.length));

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

        classifier.setProtocol(IClassifier.Protocol.TCP);
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
        decisionSet.add(new COPSDecision(CType.CSI, Command.INSTALL,
                DecisionFlag.REQERROR, new COPSData(data, 0, data.length)));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(),
                new COPSHandle(getClientHandle().getId()), decisionMap, null);
//                new COPSClientSI(CSIType.SIGNALED, new COPSData(data, 0, data.length)), decisionMap);
        //                new COPSClientSI(CNum.DEC, (byte) 4, new COPSData(data, 0, data.length), null));

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
    public void sendGateDelete(int gID) throws COPSPdpException {
        /*
         * Example of an UNSOLICITED decision <Gate Control Command> = <COPS
         * Common Header> <Client Handle> <Context> <Decision Flags> <ClientSI
         * Data> <ClientSI Data> = <Gate-Set> | <Gate-Info> | <Gate-Delete> |
         * <PDP-Config> | <Synch-Request> | <Msg-Receipt> <Gate-Delete> =
         * <Decision Header> <TransactionID> <AMID> <SubscriberID> <GateID>
         */
        // Common Header with the same ClientType as the request
        final IPCMMGate gate = new PCMMGateReq();
        final ITransactionID trID = new TransactionID();

        final IAMID amid = new AMID();
        final ISubscriberID subscriberID = new SubscriberID();
        final IGateSpec gateSpec = new GateSpec();
        final IGateID gateID = new GateID();

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateDelete);
        _transactionID = (_transactionID == 0 ? (short) (Math.random() * hashCode()) : _transactionID);
        trID.setTransactionIdentifier(_transactionID);

        amid.setApplicationType((short) 1);
        amid.setApplicationMgrTag((short) 1);
        gateID.setGateID(gID);

        try {
            InetAddress subIP = InetAddress.getByName(PCMMGlobalConfig.SubscriberID);
            subscriberID.setSourceIPAddress(subIP);
        } catch (UnknownHostException unae) {
            logger.error("Error getByName", unae);
        }

        gate.setTransactionID(trID);
        gate.setAMID(amid);
        gate.setSubscriberID(subscriberID);
        gate.setGateID(gateID);

        // XXX - GateID
        final byte[] data = gate.getData();

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR,
                new COPSData(data, 0, data.length)));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(),
                new COPSHandle(getClientHandle().getId()), decisionMap, null);
//                new COPSClientSI(CSIType.SIGNALED, new COPSData(data, 0, data.length)), decisionMap);
        //                new COPSClientSI(CNum.DEC, (byte) 4, new COPSData(data, 0, data.length), null));

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

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), new COPSHandle(_handle.getId()),
                decisionMap, null);

        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException(
                "Failed to send the open new request state, reason: "
                + e.getMessage());
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
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(getClientType(), new COPSHandle(_handle.getId()), null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException(
                "Failed to send the GateInfo request, reason: "
                + e.getMessage());
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
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(getClientType(), new COPSHandle(_handle.getId()), null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException(
                "Failed to send the sync state request, reason: "
                + e.getMessage());
        }
    }
    // XXX - Temp
    public void sendSyncRequestState() throws COPSPdpException {
    }
    // XXX - Temp
    public void sendDeleteRequestState() throws COPSPdpException {
    }
}
