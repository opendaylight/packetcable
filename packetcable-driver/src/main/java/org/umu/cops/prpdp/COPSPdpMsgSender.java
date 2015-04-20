/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSHeader.ClientType;
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
public class COPSPdpMsgSender {

    /**
     * Socket connected to PEP
     */
    protected final Socket _sock;

    /**
     * COPS client-type that identifies the policy client
     */
    protected final ClientType _clientType;

    /**
     * COPS client handle used to uniquely identify a particular
     * PEP's request for a client-type
     */
    protected final COPSHandle _handle;

    /**
     * Creates a COPSPepMsgSender
     *
     * @param clientType        COPS client-type
     * @param clientHandle      Client handle
     * @param sock              Socket to the PEP
     */
    public COPSPdpMsgSender (final ClientType clientType, final COPSHandle clientHandle, final Socket sock) {
        // COPS Handle
        _handle = clientHandle;
        _clientType = clientType;

        _sock = sock;
    }

    /**
     * Gets the client handle
     * @return   Client's <tt>COPSHandle</tt>
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Gets the client-type
     * @return   Client-type value
     */
    public ClientType getClientType() {
        return _clientType;
    }

    /**
     * Sends a decision message
     * @param removeDecs    Decisions to be removed
     * @param installDecs   Decisions to be installed
     * @throws COPSPdpException
     */
    public void sendDecision(final Map<String, String> removeDecs, Map<String, String> installDecs)
            throws COPSPdpException {
        /* <Decision Message> ::= <Common Header: Flag SOLICITED>
         *                          <Client Handle>
         *                          *(<Decision>) | <Error>
         *                          [<Integrity>]
         * <Decision> ::= <Context>
         *                  <Decision: Flags>
         *                  [<Named Decision Data: Provisioning>]
         * <Decision: Flags> ::= <Command-Code> NULLFlag
         * <Command-Code> ::= NULLDecision | Install | Remove
         * <Named Decision Data> ::= <<Install Decision> | <Remove Decision>>
         * <Install Decision> ::= *(<PRID> <EPD>)
         * <Remove Decision> ::= *(<PRID> | <PPRID>)
         *
         * Very important, this is actually being treated like this:
         * <Install Decision> ::= <PRID> | <EPD>
         * <Remove Decision> ::= <PRID> | <PPRID>
         *
        */

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
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, new COPSHandle(getClientHandle().getId()),
                decisionMap, null);

        //** Send the decision
        //**
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
        //** Example of an UNSOLICITED decision
        //**

        /* <Decision Message> ::= <Common Header: Flag UNSOLICITED>
         *                          <Client Handle>
         *                          *(<Decision>) | <Error>
         *                          [<Integrity>]
         * <Decision> ::= <Context>
         *                  <Decision: Flags>
         *                  [<Named Decision Data: Provisioning>]
         * <Decision: Flags> ::= <Command-Code> NULLFlag
         * <Command-Code> ::= NULLDecision | Install | Remove
         * <Named Decision Data> ::= <<Install Decision> | <Remove Decision>>
         * <Install Decision> ::= *(<PRID> <EPD>)
         * <Remove Decision> ::= *(<PRID> | <PPRID>)
         *
         * Very important, this is actually being treated like this:
         * <Install Decision> ::= <PRID> | <EPD>
         * <Remove Decision> ::= <PRID> | <PPRID>
         *
        */

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

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, new COPSHandle(getClientHandle().getId()),
                decisionMap, null);

        //** Send the decision
        //**
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the decision, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a message asking that the request state be deleted
     * @throws   COPSPdpException
     */
    public void sendDeleteRequestState() throws COPSPdpException {
        /* <Decision Message> ::= <Common Header: Flag UNSOLICITED>
         *                          <Client Handle>
         *                          *(<Decision>)
         *                          [<Integrity>]
         * <Decision> ::= <Context>
         *                  <Decision: Flags>
         * <Decision: Flags> ::= Remove Request-State
         *
        */

        // Decisions
        //  <Context>
        //  <Decision: Flags>
        final COPSDecision dec = new COPSDecision(Command.REMOVE, DecisionFlag.REQSTATE);
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(dec);
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), new COPSHandle(_handle.getId()),
                decisionMap, null);
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the open new request state, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a request asking that a new request state be created
     * @throws   COPSPdpException
     */
    public void sendOpenNewRequestState()
    throws COPSPdpException {
        /* <Decision Message> ::= <Common Header: Flag UNSOLICITED>
         *                          <Client Handle>
         *                          *(<Decision>)
         *                          [<Integrity>]
         * <Decision> ::= <Context>
         *                  <Decision: Flags>
         * <Decision: Flags> ::= Install Request-State
         *
        */

        //  <Decision: Flags>
        final COPSDecision dec = new COPSDecision(Command.INSTALL, DecisionFlag.REQSTATE);
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(dec);
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(_clientType, new COPSHandle(_handle.getId()),
                decisionMap, null);

        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the open new request state, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a message asking for a COPS sync operation
     * @throws COPSPdpException
     */
    public void sendSyncRequestState()
    throws COPSPdpException {
        /* <Synchronize State Request>  ::= <Common Header>
         *                                  [<Client Handle>]
         *                                  [<Integrity>]
         */

        final COPSSyncStateMsg msg = new COPSSyncStateMsg(_clientType, new COPSHandle(_handle.getId()), null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the sync state request, reason: " + e.getMessage());
        }
    }
}
