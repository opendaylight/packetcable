/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.umu.cops.COPSMsgSender;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * COPS message transceiver class for provisioning connections at the PDP side.
 *
 * TODO - Need to continue refactoring by removing all instances of Hashtable (change to Map<>)
 */
public class COPSPdpMsgSender extends COPSMsgSender {

    /**
     * Creates a COPSPepMsgSender
     *
     * @param clientType        COPS client-type
     * @param clientHandle      Client handle
     * @param sock              Socket to the PEP
     */
    public COPSPdpMsgSender (final short clientType, final COPSHandle clientHandle, final Socket sock) {
        super(clientType, clientHandle, sock);
    }

    /**
     * Sends a decision message
     * @param removeDecs    Decisions to be removed
     * @param installDecs   Decisions to be installed
     * @throws COPSPdpException
     */
    public void sendDecision(final Map<String, String> removeDecs, Map<String, String> installDecs)
            throws COPSPdpException {

        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();

        // Decisions (no flags supplied)
        //  <Context>
        final COPSContext cntxt = new COPSContext(RType.CONFIG, (short)0);

        // Remove Decisions
        //  <Decision: Flags>
        final COPSDecision rdec1 = new COPSDecision(Command.REMOVE);

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(rdec1);
        decisionMap.put(cntxt, decisionSet);

        for (final Map.Entry<String, String> entry : removeDecs.entrySet()) {
            //  <Named Decision Data: Provisioning> (PRID)
            final COPSPrID prid = new COPSPrID();
            prid.setData(new COPSData(entry.getKey()));
            final COPSDecision decisionPrid = new COPSDecision(CType.NAMED,
                    new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

            decisionMap.get(cntxt).add(decisionPrid);

            final COPSPrEPD epd = new COPSPrEPD();
            final COPSDecision decisionPrepd = new COPSDecision(CType.NAMED,
                    new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

            decisionMap.get(cntxt).add(decisionPrepd);
        }

        // Install Decisions
        //  <Decision: Flags>
        final COPSDecision idec1 = new COPSDecision(Command.INSTALL);
        decisionMap.get(cntxt).add(idec1);

        for (final Map.Entry<String, String> entry : installDecs.entrySet()) {
            //  <Named Decision Data: Provisioning> (PRID)
            final COPSPrID prid = new COPSPrID();
            prid.setData(new COPSData(entry.getKey()));
            final COPSDecision decisionPrid2 = new COPSDecision(CType.NAMED,
                    new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

            decisionMap.get(cntxt).add(decisionPrid2);
        }

        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, _handle, decisionMap, null, null);

        //** Send the decision
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the decision, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a decision message which was not requested by the PEP
     * @param removeDecs    Decisions to be removed
     * @param installDecs   Decisions to be installed
     * @throws COPSPdpException
     */
    public void sendUnsolicitedDecision(final Map<String, String> removeDecs, final Map<String, String> installDecs)
            throws COPSPdpException {
        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        // Decisions (no flags supplied)
        //  <Context>
        final COPSContext cntxt = new COPSContext(RType.CONFIG, (short)0);

        // Remove Decisions
        //  <Decision: Flags>
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(Command.REMOVE));
        decisionMap.put(cntxt, decisionSet);

        for (final Map.Entry<String, String> entry : removeDecs.entrySet()) {
            //  <Named Decision Data: Provisioning> (PRID)
            final COPSPrID prid = new COPSPrID();
            prid.setData(new COPSData(entry.getKey()));
            decisionMap.get(cntxt).add(new COPSDecision(CType.NAMED,
                    new COPSData(prid.getDataRep(), 0, prid.getDataLength())));

            //  <Named Decision Data: Provisioning> (EPD)
            final COPSPrEPD epd = new COPSPrEPD();
            epd.setData(new COPSData(entry.getValue()));
            decisionMap.get(cntxt).add(
                    new COPSDecision(CType.NAMED, new COPSData(epd.getDataRep(), 0, epd.getDataLength())));
        }

        // Install Decisions
        //  <Decision: Flags>
        decisionMap.get(cntxt).add(new COPSDecision(Command.INSTALL));

        for (final Map.Entry<String, String> entry : installDecs.entrySet()) {
            //  <Named Decision Data: Provisioning> (PRID)
            final COPSPrID prid = new COPSPrID();
            prid.setData(new COPSData(entry.getKey()));
            decisionMap.get(cntxt).add(new COPSDecision(CType.NAMED,
                    new COPSData(prid.getDataRep(), 0, prid.getDataLength())));

            final COPSPrEPD epd = new COPSPrEPD();
            epd.setData(new COPSData(entry.getValue()));
            decisionMap.get(cntxt).add(
                    new COPSDecision(CType.NAMED, new COPSData(epd.getDataRep(), 0, epd.getDataLength())));
        }

        /**
        COPSIntegrity intr = new COPSIntegrity();
        intr.setKeyId(19);
        intr.setSeqNum(9);
        intr.setKeyDigest(new COPSData("KEY DIGEST"));
        decisionMsg.add(intr);
        /**/

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, _handle, decisionMap, null, null);

        //** Send the decision
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the decision, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a message asking that the request state be deleted
     * @throws COPSException
     */
    public void sendDeleteRequestState() throws COPSException {
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(Command.REMOVE, DecisionFlag.REQSTATE));
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), _handle, decisionMap, null, null);
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the open new request state, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a request asking that a new request state be created
     * @throws COPSException
     */
    public void sendOpenNewRequestState() throws COPSException {
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(Command.INSTALL, DecisionFlag.REQSTATE));
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, _handle, decisionMap, null, null);

        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the open new request state, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a message asking for a COPS sync operation
     * @throws COPSException
     */
    public void sendSyncRequestState() throws COPSException {
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(_clientType, _handle, null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the sync state request, reason: " + e.getMessage());
        }
    }
}
