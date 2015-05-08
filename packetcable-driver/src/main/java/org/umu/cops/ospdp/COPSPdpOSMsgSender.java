package org.umu.cops.ospdp;

import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSHeader.Flag;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * COPS message transceiver class for outsourcing connections at the PDP side.
 *
 * TODO - change all references of Vector to List<>
 */
public class COPSPdpOSMsgSender {
    /**
     * Socket connected to PEP
     */
    protected final Socket _sock;

    /**
     * COPS client-type that identifies the policy client
     */
    protected final short _clientType;

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
    public COPSPdpOSMsgSender (final short clientType, final COPSHandle clientHandle, final Socket sock) {
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
    public short getClientType() {
        return _clientType;
    }

    /**
     * Sends a decision message which was requested by the PEP
     * @param removeDecs    Decisions to be removed
     * @param installDecs   Decisions to be installed
     * @throws COPSPdpException
     */
    public void sendSolicitedDecision(Vector removeDecs, Vector installDecs) throws COPSPdpException {
        sendDecision(removeDecs, installDecs, true);
    }

    /**
     * Sends a decision message which was not requested by the PEP
     * @param removeDecs    Decisions to be removed
     * @param installDecs   Decisions to be installed
     * @throws COPSPdpException
     */
    public void sendUnsolicitedDecision(Vector removeDecs, Vector installDecs) throws COPSPdpException {
        sendDecision(removeDecs, installDecs, false);
    }

    /**
     * Sends a decision message to the PEP
     * @param removeDecs    Decisions to be removed
     * @param installDecs   Decisions to be installed
     * @param solicited     <tt>true</tt> if the PEP requested this decision, <tt>false</tt> otherwise
     * @throws COPSPdpException
     */
    public void sendDecision(Vector removeDecs, Vector installDecs, boolean solicited) throws COPSPdpException {
        // Common Header holding the same ClientType as the request
        final Flag flag;
        if (solicited)
            flag= Flag.SOLICITED;
        else
            flag = Flag.UNSOLICITED;

        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();

        // Decisions (no flags supplied)
        //  <Context>
        final COPSContext cntxt = new COPSContext(RType.CONFIG, (short)0);

        // Remove Decisions
        //  <Decision: Flags>
        final COPSDecision rdec1;
        if (installDecs.size() == 0)
            rdec1 = new COPSDecision(Command.REMOVE);
        else
            rdec1 = new COPSDecision(Command.INSTALL);

        if (decisions.get(cntxt) == null) {
            final Set<COPSDecision> decisionSet = new HashSet<>();
            decisionSet.add(rdec1);
            decisions.put(cntxt, decisionSet);
        } else {
            decisions.get(cntxt).add(rdec1);
        }

        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(1, flag, getClientType(),
                new COPSHandle(getClientHandle().getId()), decisions, null);

        //** Send decision
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
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(Command.REMOVE, DecisionFlag.REQSTATE));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        // Common Header with the same ClientType as the request (default UNSOLICITED)
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), new COPSHandle(_handle.getId()),
                decisionMap, null);

        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the open new request state, reason: " + e.getMessage());
        }
    }

    /**
     * Method sendOpenNewRequestState
     *
     * @throws   COPSPdpException
     *
     */
    //FIXME: Unused?
    public void sendOpenNewRequestState() throws COPSPdpException {
        /* <Decision Message> ::= <Common Header: Flag UNSOLICITED>
         *                          <Client Handle>
         *                          *(<Decision>)
         *                          [<Integrity>]
         * <Decision> ::= <Context>
         *                  <Decision: Flags>
         * <Decision: Flags> ::= Install Request-State
         *
        */

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(Command.INSTALL, DecisionFlag.REQSTATE));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        // Common Header with the same ClientType as the request (default UNSOLICITED)
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), new COPSHandle(_handle.getId()),
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
    public void sendSyncRequestState() throws COPSPdpException {
        /* <Synchronize State Request>  ::= <Common Header>
         *                                  [<Client Handle>]
         *                                  [<Integrity>]
         */

        // Common Header with the same ClientType as the request
        // Client Handle with the same clientHandle as the request
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(_clientType, new COPSHandle(_handle.getId()), null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the sync state request, reason: " + e.getMessage());
        }
    }

}
