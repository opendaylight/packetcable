/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm;

import org.pcmm.gates.IGateID;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.ITransactionID.GateCommandType;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.gates.impl.TransactionID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSMsgSender;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.net.Socket;
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

    protected final short _transactionID;
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
        _transactionID = tID == 0 ? (short) (Math.random() * hashCode()) : tID;
        _classifierID = 0;
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
        // set transaction ID to gate set
        final ITransactionID trID = new TransactionID(_transactionID, GateCommandType.GATE_SET);

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
                final IPCMMGate responseGate = PCMMGateReq.parse(reportMsg.getClientSI().getData().getData());
                if (responseGate.getTransactionID() != null
                        && responseGate.getTransactionID().getGateCommandType().equals(GateCommandType.GATE_SET_ACK)) {
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
     * Sends a message asking that the request state be deleted
     *
     * @throws COPSPdpException
     */
    public void sendGateDelete(final IPCMMGate gate) throws COPSPdpException {
        // set transaction ID to gate set
        final ITransactionID trID = new TransactionID(_transactionID, GateCommandType.GATE_DELETE);
        gate.setTransactionID(trID);

        Short trIDnum = trID.getTransactionIdentifier();
        PCMMGlobalConfig.transactionGateMap.put(trIDnum, gate);

        // gateDelete only requires AMID, subscriberID, and gateID
        // remove the gateSpec, traffic profile, and classifiers from original gate request
        gate.setGateSpec(null);
        gate.setTrafficProfile(null);
        gate.setClassifiers(null);
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
