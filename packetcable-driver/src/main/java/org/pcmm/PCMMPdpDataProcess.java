/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm;

import org.pcmm.gates.ITransactionID.GateCommandType;
import org.pcmm.gates.impl.PCMMGateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSStateMan;
import org.umu.cops.prpdp.COPSPdpDataProcess;
import org.umu.cops.prpdp.COPSPdpReqStateMan;
import org.umu.cops.stack.COPSError;

import java.util.HashMap;
import java.util.Map;


public class PCMMPdpDataProcess implements COPSPdpDataProcess {

    public final static Logger logger = LoggerFactory.getLogger(PCMMPdpDataProcess.class);

    @Override
    public Map getRemovePolicy(final COPSPdpReqStateMan man) {
        logger.info("Retrieving the remove policy");
        // TODO - Implement me
        return new HashMap();
    }

    @Override
    public Map getInstallPolicy(final COPSPdpReqStateMan man) {
        logger.info("Retrieving the remove policy");
        // TODO - Implement me
        return new HashMap();
    }

    @Override
    public void setClientData(final COPSPdpReqStateMan man, final Map<String, String> reqSIs) {
        logger.info("Setting the client data");
        // TODO - Implement me
    }

    /**
     * Fail report received
     * @param man - the state manager
     * @param gateMsg - the gate request message
     */
    public void failReport(final COPSPdpReqStateMan man, final PCMMGateReq gateMsg) {
        logger.info("Fail Report notified with error - " + gateMsg.getError().toString());
        // TODO - Implement me
    }

    @Override
    public void failReport(final COPSPdpReqStateMan man, final Map<String, String> reportSIs) {
        logger.info("Fail report notified");
        // TODO - Implement me
    }

    @Override
    public void successReport(final COPSPdpReqStateMan man, final Map<String, String> reportSIs) {
        logger.info("Success report notified");
        // TODO - Implement me
    }

    /**
     * Positive report received
     * @param man - the state manager
     * @param gateMsg - the gate request message
     */
    public void successReport(final COPSPdpReqStateMan man, final PCMMGateReq gateMsg) {
        logger.info("Success Report notified.");

        if ( gateMsg.getTransactionID().getGateCommandType().equals(GateCommandType.GATE_DELETE_ACK)) {
            logger.info("GateDeleteAck: GateID = " + gateMsg.getGateID().getGateID());
            if (gateMsg.getGateID().getGateID() == PCMMGlobalConfig.getGateID1())
                PCMMGlobalConfig.setGateID1(0);
            if (gateMsg.getGateID().getGateID() == PCMMGlobalConfig.getGateID2())
                PCMMGlobalConfig.setGateID2(0);

        }
        if ( gateMsg.getTransactionID().getGateCommandType().equals(GateCommandType.GATE_SET_ACK)) {
            logger.info("GateSetAck : GateID = " + gateMsg.getGateID().getGateID());
            if (0 == PCMMGlobalConfig.getGateID1())
                PCMMGlobalConfig.setGateID1(gateMsg.getGateID().getGateID());
            if (0 == PCMMGlobalConfig.getGateID2())
                PCMMGlobalConfig.setGateID2(gateMsg.getGateID().getGateID());
        }
    }

    /**
     * Accounting report received
     * @param man - the state manager
     * @param gateMsg - the gate request message
     */
    public void acctReport(final PCMMPdpReqStateMan man, final PCMMGateReq gateMsg) {
        logger.info("Acct Report notified.");
        // TODO - Impelement me
    }

    @Override
    public void acctReport (final COPSPdpReqStateMan man, final Map reportSIs) {
        logger.info("Acct Report notified.");
        // TODO - had to implement but do not know what to do here
    }

    @Override
    public void notifyNoAcctReport(final COPSPdpReqStateMan man) {
        logger.info("No Acct Report notified.");
        // TODO - Impelement me
    }

    @Override
    public void notifyNoKAliveReceived(final COPSStateMan man) {
        logger.info("Notify No K alive received.");
        // TODO - Impelement me
    }

    @Override
    public void notifyClosedConnection(final COPSStateMan man, final COPSError error) {
        logger.info("Connection was closed by PEP");
        // TODO - Implement me
    }

    @Override
    public void notifyDeleteRequestState(final COPSPdpReqStateMan man) {
        logger.info("Delete request state notified");
        // TODO - Impelement me
    }

    @Override
    public void closeRequestState(final COPSStateMan man) {
        logger.info("Close request state notified");
        // TODO - Impelement me
    }
}
