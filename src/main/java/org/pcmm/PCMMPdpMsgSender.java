/**
 @header@
 */

package org.pcmm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.pcmm.gates.IAMID;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.IExtendedClassifier;
import org.pcmm.gates.IGateID;
import org.pcmm.gates.IGateSpec;
import org.pcmm.gates.IGateSpec.DSCPTOS;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ISubscriberID;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.impl.AMID;
import org.pcmm.gates.impl.BestEffortService;
import org.pcmm.gates.impl.Classifier;
import org.pcmm.gates.impl.ExtendedClassifier;
import org.pcmm.gates.impl.GateID;
import org.pcmm.gates.impl.GateSpec;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.gates.impl.SubscriberID;
import org.pcmm.gates.impl.TransactionID;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSObjHeader;
//temp
import org.umu.cops.stack.COPSReportMsg;
import org.umu.cops.stack.COPSSyncStateMsg;
import org.umu.cops.stack.COPSTransceiver;
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

    /**
     * Socket connected to PEP
     */
    protected Socket _sock;

    /**
     * COPS client-type that identifies the policy client
     */
    protected short _clientType;

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
    public PCMMPdpMsgSender(short clientType, COPSHandle clientHandle,
                            Socket sock) {
        // COPS Handle
        _handle = clientHandle;
        _clientType = clientType;

        _transactionID = 0;
        _classifierID = 0;
        _sock = sock;
    }

    public PCMMPdpMsgSender(short clientType, short tID,
                            COPSHandle clientHandle, Socket sock) {
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
    public short getClientType() {
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
     *
     * @param
     * @throws COPSPdpException
     */
    public void sendGateSet(IPCMMGate gate)
    throws COPSPdpException {
        // Common Header with the same ClientType as the request

        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle handle = new COPSHandle();
        COPSDecisionMsg decisionMsg = new COPSDecisionMsg();
        ITransactionID trID = new TransactionID();

        handle.setId(getClientHandle().getId());

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (short) (_transactionID == 0 ? (short) (Math.random() * hashCode())
                                  : _transactionID);
        trID.setTransactionIdentifier(_transactionID);

        gate.setTransactionID(trID);


        // new pcmm specific clientsi
        COPSClientSI clientSD = new COPSClientSI(COPSObjHeader.COPS_DEC, (byte) 4);
        byte[] data = gate.getData();
        clientSD.setData(new COPSData(data, 0, data.length));
        try {
            decisionMsg.add(hdr);
            decisionMsg.add(handle);
            // Decisions (no flags supplied)
            // <Context>
            COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
            COPSDecision install = new COPSDecision();
            install.setCmdCode(COPSDecision.DEC_INSTALL);
            install.setFlags(COPSDecision.F_REQERROR);
            decisionMsg.addDecision(install, cntxt);
            decisionMsg.add(clientSD); // setting up the gate
            /*
                        try {
                            decisionMsg.dump(System.out);
                        } catch (IOException unae) {
                            System.out.println("Error dumping " + unae.getMessage());
                        }
            */

        } catch (COPSException e) {
            System.out.println("Error making Msg" + e.getMessage());
        }

        // ** Send the GateSet Decision
        // **
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            System.out.println("Failed to send the decision, reason: "
                               + e.getMessage());
        }

    }

    /**
     * Sends a PCMM GateSet COPS Decision message
     *
     * @param
     * @throws COPSPdpException
     */
    public void sendGateSetDemo(int num)
    throws COPSPdpException {

        // Common Header with the same ClientType as the request

        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle handle = new COPSHandle();
        COPSDecisionMsg decisionMsg = new COPSDecisionMsg();

        IPCMMGate gate = new PCMMGateReq();
        ITransactionID trID = new TransactionID();

        IAMID amid = new AMID();
        ISubscriberID subscriberID = new SubscriberID();
        IGateSpec gateSpec = new GateSpec();
        IClassifier classifier = new Classifier();
        IExtendedClassifier eclassifier = new ExtendedClassifier();
        int TrafficRate = 0;

        if (num == 1)
            TrafficRate =   PCMMGlobalConfig.DefaultBestEffortTrafficRate;
        else
            TrafficRate =   PCMMGlobalConfig.DefaultLowBestEffortTrafficRate;

        ITrafficProfile trafficProfile = new BestEffortService(
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
            TrafficRate);
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
            TrafficRate);
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
            TrafficRate);
        //  PCMMGlobalConfig.DefaultLowBestEffortTrafficRate );
        //  PCMMGlobalConfig.DefaultBestEffortTrafficRate);



        // new pcmm specific clientsi
        COPSClientSI clientSD = new COPSClientSI(COPSObjHeader.COPS_DEC, (byte) 4);

        handle.setId(getClientHandle().getId());

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (short) (_transactionID == 0 ? (short) (Math.random() * hashCode())
                                  : _transactionID);
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
                System.out.println("Error getByName" + unae.getMessage());
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
                System.out.println("Error getByName" + unae.getMessage());
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

        // new pcmm specific clientsi
        clientSD.setData(new COPSData(data, 0, data.length));
        try {
            decisionMsg.add(hdr);
            decisionMsg.add(handle);
            // Decisions (no flags supplied)
            // <Context>
            COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
            COPSDecision install = new COPSDecision();
            install.setCmdCode(COPSDecision.DEC_INSTALL);
            install.setFlags(COPSDecision.F_REQERROR);
            decisionMsg.addDecision(install, cntxt);
            decisionMsg.add(clientSD); // setting up the gate
            /*
                        try {
                            decisionMsg.dump(System.out);
                        } catch (IOException unae) {
                            System.out.println("Error dumping " + unae.getMessage());
                        }
            */

        } catch (COPSException e) {
            System.out.println("Error making Msg" + e.getMessage());
        }

        // ** Send the GateSet Decision
        // **
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            System.out.println("Failed to send the decision, reason: "
                               + e.getMessage());
        }

    }
    /**
     * Sends a PCMM GateSet COPS Decision message
     *
     * @param
     * @throws COPSPdpException
     */
    public void sendGateSetBestEffortWithExtendedClassifier()
    throws COPSPdpException {
        // Common Header with the same ClientType as the request

        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle handle = new COPSHandle();
        COPSDecisionMsg decisionMsg = new COPSDecisionMsg();

        IPCMMGate gate = new PCMMGateReq();
        ITransactionID trID = new TransactionID();

        IAMID amid = new AMID();
        ISubscriberID subscriberID = new SubscriberID();
        IGateSpec gateSpec = new GateSpec();
        IClassifier classifier = new Classifier();
        IExtendedClassifier eclassifier = new ExtendedClassifier();

        // XXX check if other values should be provided
        //
        ITrafficProfile trafficProfile = new BestEffortService(
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



        // new pcmm specific clientsi
        COPSClientSI clientSD = new COPSClientSI(COPSObjHeader.COPS_DEC,
                (byte) 4);

        handle.setId(getClientHandle().getId());

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (short) (_transactionID == 0 ? (short) (Math.random() * hashCode())
                                  : _transactionID);
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
                System.out.println("Error getByName" + unae.getMessage());
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
                System.out.println("Error getByName" + unae.getMessage());
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

        // new pcmm specific clientsi
        clientSD.setData(new COPSData(data, 0, data.length));
        try {
            decisionMsg.add(hdr);
            decisionMsg.add(handle);
            // Decisions (no flags supplied)
            // <Context>
            COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
            COPSDecision install = new COPSDecision();
            install.setCmdCode(COPSDecision.DEC_INSTALL);
            install.setFlags(COPSDecision.F_REQERROR);
            decisionMsg.addDecision(install, cntxt);
            decisionMsg.add(clientSD); // setting up the gate
            /*
                        try {
                            decisionMsg.dump(System.out);
                        } catch (IOException unae) {
                            System.out.println("Error dumping " + unae.getMessage());
                        }
            */

        } catch (COPSException e) {
            System.out.println("Error making Msg" + e.getMessage());
        }

        // ** Send the GateSet Decision
        // **
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            System.out.println("Failed to send the decision, reason: "
                               + e.getMessage());
        }

    }


    public boolean handleGateReport(Socket socket) throws COPSPdpException {
        try {
            // waits for the gate-set-ack or error
            COPSMsg responseMsg = COPSTransceiver.receiveMsg(socket);
            if (responseMsg.getHeader().isAReport()) {
                System.out.println("processing received report from CMTS");
                COPSReportMsg reportMsg = (COPSReportMsg) responseMsg;
                if (reportMsg.getClientSI().size() == 0) {
                    return false;
                }
                COPSClientSI clientSI = (COPSClientSI) reportMsg.getClientSI()
                                        .elementAt(0);
                IPCMMGate responseGate = new PCMMGateReq(clientSI.getData()
                        .getData());
                if (responseGate.getTransactionID() != null
                        && responseGate.getTransactionID().getGateCommandType() == ITransactionID.GateSetAck) {
                    System.out.println("the CMTS has sent a Gate-Set-Ack response");
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
     * @param
     * @throws COPSPdpException
     */
    public void sendGateSet() throws COPSPdpException {
        // Common Header with the same ClientType as the request

        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle handle = new COPSHandle();
        COPSDecisionMsg decisionMsg = new COPSDecisionMsg();

        IPCMMGate gate = new PCMMGateReq();
        ITransactionID trID = new TransactionID();

        IAMID amid = new AMID();
        ISubscriberID subscriberID = new SubscriberID();
        IGateSpec gateSpec = new GateSpec();
        IClassifier classifier = new Classifier();
        // XXX check if other values should be provided
        ITrafficProfile trafficProfile = new BestEffortService(
            BestEffortService.DEFAULT_ENVELOP);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setMaximumTrafficBurst(
            BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
        ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
        .setRequestTransmissionPolicy(
            PCMMGlobalConfig.BETransmissionPolicy);

        // new pcmm specific clientsi
        COPSClientSI clientSD = new COPSClientSI(COPSObjHeader.COPS_DEC,
                (byte) 4);

        handle.setId(getClientHandle().getId());
        // byte[] content = "1234".getBytes();

        // handle.setId(new COPSData(content, 0, content.length));

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateSet);
        _transactionID = (short) (_transactionID == 0 ? (short) (Math.random() * hashCode())
                                  : _transactionID);
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
            System.out.println("Error getByName" + unae.getMessage());
        }
        classifier.setSourcePort(PCMMGlobalConfig.srcPort);
        classifier.setDestinationPort(PCMMGlobalConfig.dstPort);

        gate.setTransactionID(trID);
        gate.setAMID(amid);
        gate.setSubscriberID(subscriberID);
        gate.setGateSpec(gateSpec);
        gate.setTrafficProfile(trafficProfile);
        gate.setClassifier(classifier);

        byte[] data = gate.getData();

        // new pcmm specific clientsi
        clientSD.setData(new COPSData(data, 0, data.length));

        try {
            decisionMsg.add(hdr);
            decisionMsg.add(handle);
            // Decisions (no flags supplied)
            // <Context>
            COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
            COPSDecision install = new COPSDecision();
            install.setCmdCode(COPSDecision.DEC_INSTALL);
            install.setFlags(COPSDecision.F_REQERROR);
            decisionMsg.addDecision(install, cntxt);
            decisionMsg.add(clientSD); // setting up the gate
            /*
                        try {
                            decisionMsg.dump(System.out);
                        } catch (IOException unae) {
                            System.out.println("Error dumping " + unae.getMessage());
                        }
            */

        } catch (COPSException e) {
            System.out.println("Error making Msg" + e.getMessage());
        }

        // ** Send the GateSet Decision
        // **
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            System.out.println("Failed to send the decision, reason: "
                               + e.getMessage());
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
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle handle = new COPSHandle();
        COPSDecisionMsg decisionMsg = new COPSDecisionMsg();

        IPCMMGate gate = new PCMMGateReq();
        ITransactionID trID = new TransactionID();

        IAMID amid = new AMID();
        ISubscriberID subscriberID = new SubscriberID();
        IGateSpec gateSpec = new GateSpec();
        IGateID gateID = new GateID();

        // new pcmm specific clientsi
        COPSClientSI clientSD = new COPSClientSI(COPSObjHeader.COPS_DEC, (byte) 4);

        handle.setId(getClientHandle().getId());

        // set transaction ID to gate set
        trID.setGateCommandType(ITransactionID.GateDelete);
        _transactionID = (short) (_transactionID == 0 ? (short) (Math.random() * hashCode())
                                  : _transactionID);
        trID.setTransactionIdentifier(_transactionID);

        amid.setApplicationType((short) 1);
        amid.setApplicationMgrTag((short) 1);
        gateID.setGateID(gID);

        try {
            InetAddress subIP = InetAddress.getByName(PCMMGlobalConfig.SubscriberID);
            subscriberID.setSourceIPAddress(subIP);
        } catch (UnknownHostException unae) {
            System.out.println("Error getByName" + unae.getMessage());
        }

        gate.setTransactionID(trID);
        gate.setAMID(amid);
        gate.setSubscriberID(subscriberID);
        gate.setGateID(gateID);

        // XXX - GateID
        byte[] data = gate.getData();

        // new pcmm specific clientsi
        clientSD.setData(new COPSData(data, 0, data.length));

        try {
            decisionMsg.add(hdr);
            decisionMsg.add(handle);
            // Decisions (no flags supplied)
            // <Context>
            COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
            COPSDecision install = new COPSDecision();
            install.setCmdCode(COPSDecision.DEC_INSTALL);
            install.setFlags(COPSDecision.F_REQERROR);
            decisionMsg.addDecision(install, cntxt);
            decisionMsg.add(clientSD); // setting up the gate
            /*
                        try {
                            decisionMsg.dump(System.out);
                        } catch (IOException unae) {
                            System.out.println("Error dumping " + unae.getMessage());
                        }
            */

        } catch (COPSException e) {
            System.out.println("Error making Msg" + e.getMessage());
        }

        // ** Send the GateDelete Decision
        // **
        try {
            decisionMsg.writeData(_sock);
            // decisionMsg.writeData(socket_id);
        } catch (IOException e) {
            System.out.println("Failed to send the decision, reason: "
                               + e.getMessage());
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

        // Common Header with the same ClientType as the request (default
        // UNSOLICITED)
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle clienthandle = new COPSHandle();
        clienthandle.setId(_handle.getId());

        // Decisions
        // <Context>
        COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
        // <Decision: Flags>
        COPSDecision dec = new COPSDecision();
        dec.setCmdCode(COPSDecision.DEC_INSTALL);
        dec.setFlags(COPSDecision.F_REQSTATE);

        COPSDecisionMsg decisionMsg = new COPSDecisionMsg();
        try {
            decisionMsg.add(hdr);
            decisionMsg.add(clienthandle);
            decisionMsg.addDecision(dec, cntxt);
        } catch (COPSException e) {
            throw new COPSPdpException("Error making Msg");
        }

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

        // Common Header with the same ClientType as the request
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_SSQ, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle clienthandle = new COPSHandle();
        clienthandle.setId(_handle.getId());

        COPSSyncStateMsg msg = new COPSSyncStateMsg();
        try {
            msg.add(hdr);
            msg.add(clienthandle);
        } catch (Exception e) {
            throw new COPSPdpException("Error making Msg");
        }

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

        // Common Header with the same ClientType as the request
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_SSQ, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle clienthandle = new COPSHandle();
        clienthandle.setId(_handle.getId());

        COPSSyncStateMsg msg = new COPSSyncStateMsg();
        try {
            msg.add(hdr);
            msg.add(clienthandle);
        } catch (Exception e) {
            throw new COPSPdpException("Error making Msg");
        }

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
