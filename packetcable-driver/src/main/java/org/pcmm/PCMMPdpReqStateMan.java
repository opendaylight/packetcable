/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm;

import org.pcmm.gates.IGateID;
import org.pcmm.gates.IGateState;
import org.pcmm.gates.IGateTimeInfo;
import org.pcmm.gates.IGateUsageInfo;
import org.pcmm.gates.IPCMMError.ErrorCode;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.ITransactionID.GateCommandType;
import org.pcmm.gates.impl.PCMMError;
import org.pcmm.gates.impl.PCMMGateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.prpdp.COPSPdpReqStateMan;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * State manager class for provisioning requests, at the PDP side.
 */
public class PCMMPdpReqStateMan extends COPSPdpReqStateMan {

    private final static Logger logger = LoggerFactory.getLogger(PCMMPdpReqStateMan.class);

    /**
     * Object for performing policy data processing
     */
    protected final PCMMPdpDataProcess _thisProcess;

    /** COPS message transceiver used to send COPS messages */
    protected final PCMMPdpMsgSender _sender;

    /**
     * Creates a request state manager
     * @param clientType    Client-type
     * @param clientHandle  Client handle
     */
    // TODO - consider sending in the COPSHandle object instead
    public PCMMPdpReqStateMan(final short clientType, final COPSHandle clientHandle, final PCMMPdpDataProcess process,
                              final Socket socket) {
        super(clientType, clientHandle, process, socket);
        this._thisProcess = process;
        _sender = new PCMMPdpMsgSender(_clientType, _handle, _socket);
        // Initial state
        _status = Status.ST_INIT;
    }

    @Override
    public void processRequest(final COPSReqMsg msg) throws COPSPdpException {
        // TODO - Implement me - see commented out code from history prior to May 4, 2015...
    }

     // TODO - break apart this method
    @Override
    protected void processReport(final COPSReportMsg msg) throws COPSPdpException {
        // Report Type
        final COPSReportType rtypemsg = msg.getReport();

        if (msg.getClientSI() != null) {
            final COPSClientSI clientSI = msg.getClientSI();
            // Named ClientSI
            final byte[] data = Arrays.copyOfRange(clientSI.getData().getData(), 0, clientSI.getData().getData().length);

            // PCMMUtils.WriteBinaryDump("COPSReportClientSI", data);
            logger.info("PCMMGateReq Parse Gate Message");
            final PCMMGateReq gateMsg = PCMMGateReq.parse(data);

            // TODO FIXME - Why is this Map being filled but never used???
            final Map<String, String> repSIs = new HashMap<>();
            String strobjprid = "";
            final COPSPrObjBase obj = new COPSPrObjBase(clientSI.getData().getData());
            switch (obj.getSNum()) {
                case COPSPrObjBase.PR_PRID:
                    logger.info("COPSPrObjBase.PR_PRID");
                    // TODO FIXME - this value is never used
                    strobjprid = obj.getData().str();
                    break;
                case COPSPrObjBase.PR_EPD:
                    logger.info("COPSPrObjBase.PR_EPD");
                    // TODO FIXME - strobjprid is always empty
                    repSIs.put(strobjprid, obj.getData().str());
                    logger.info("PRID: " + strobjprid);
                    logger.info("EPD: " + obj.getData().str());
                    break;
                default:
                    logger.error("Object s-num: " + obj.getSNum() + "stype " + obj.getSType());
                    logger.error("PRID: " + strobjprid);
                    logger.error("EPD: " + obj.getData().str());
                    break;
            }

            logger.info("rtypemsg process");
            //** Here we must act in accordance with
            //** the report received

            // retrieve and remove the transactionId to gate request map entry
            // see PCMMPdpMsgSender.sendGateSet(IPCMMGate gate)
            final ITransactionID trID = gateMsg.getTransactionID();
            final Short trIDnum = trID.getTransactionIdentifier();

            logger.info("Removing gate from cache with key - " + trIDnum);
            final IPCMMGate gate = PCMMGlobalConfig.transactionGateMap.remove(trIDnum);
            if (gate != null) {
                // capture the "error" message if any
                gate.setError(gateMsg.getError());
                logger.info("Setting error on gate - " + gateMsg.getError());
            }else {
                logger.error("processReport(): gateReq not found for transactionID {}", trIDnum);
                return;
            }

            if (rtypemsg.getReportType().equals(ReportType.SUCCESS)) {
                logger.info("rtypemsg success");
                _status = Status.ST_REPORT;
                final IGateID gateID = gateMsg.getGateID();
                //logger.info("Setting gate ID on gate object - " + gateID);
                gate.setGateID(gateID);
                
                //setting the Gate State, Time Info and Usage Info
                final IGateState igateState = gateMsg.getGateState();
                gate.setGateState(igateState);
                final IGateTimeInfo gateTimeInfo = gateMsg.getGateTimeInfo();
                gate.setGateTimeInfo(gateTimeInfo);
                final IGateUsageInfo gateUsageInfo = gateMsg.getGateUsageInfo();
                gate.setGateUsageInfo(gateUsageInfo);
                
                if (_thisProcess != null)
                    _thisProcess.successReport(this, gateMsg);
            } else {
                final String cmdType;
                if (trID.getGateCommandType().equals(GateCommandType.GATE_DELETE_ACK)) {
                    cmdType = "GateDeleteAck";
                } else if (trID.getGateCommandType().equals(GateCommandType.GATE_SET_ACK)) {
                    cmdType = "GateSetAck";
                } else if (trID.getGateCommandType().equals(GateCommandType.GATE_INFO_ACK)) {
                    cmdType = "GateInfoAck";
                } else cmdType = null;
                // capture the gateId from the response message
                final IGateID gateID = gateMsg.getGateID();
                logger.info("Setting gate ID on gate object - " + gateID);
                gate.setGateID(gateID);
                // capture the gate state from the response message
                final IGateState igateState = gateMsg.getGateState();
                logger.info("Setting gate ID on gate object - " + gateID);
                gate.setGateState(igateState);
                if (gateID != null) {
                    int gateIdInt = gateID.getGateID();
                    String gateIdHex = String.format("%08x", gateIdInt);
                    logger.info(getClass().getName() + ": " + cmdType + ": GateID = " + gateIdHex);
                } else {
                    logger.warn("Gate ID is null");
                }
            }
            if (rtypemsg.getReportType().equals(ReportType.FAILURE)) {
                logger.info("rtypemsg failure");
                _status = Status.ST_REPORT;
                if (_thisProcess != null)
                    _thisProcess.failReport(this, gateMsg);
                else
                    if (gateMsg.getError() != null)
                        logger.info("Gate message error - " + gateMsg.getError().toString());
                    else {
                        // TODO - Determine if this is the correct error code
                        final PCMMError error = new PCMMError(ErrorCode.UNK_GATE_CMD);
                        gate.setError(error);
                        logger.warn("Gate request failed without an error, setting one - " + error);
                    }
            } else if (rtypemsg.getReportType().equals(ReportType.ACCOUNTING)) {
                    logger.info("rtypemsg account");
                    _status = Status.ST_ACCT;
                    if (_thisProcess != null)
                        _thisProcess.acctReport(this, gateMsg);
            }

            // let the waiting gateSet/gateDelete sender proceed
            // TODO - see PCMMService#processReport() gate.notify(). Should determine a better means to
            // TODO - handle this synchronization.
            logger.info("Notify gate request has been updated with ID - " + gate.getGateID());
            synchronized(gate) {
                gate.notify();
            }
            logger.info("Out processReport");
        }
    }

}
