package org.umu.cops.ospdp;

import org.umu.cops.COPSMsgSender;
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
public class COPSPdpOSMsgSender extends COPSMsgSender {

    /**
     * Creates a COPSPepMsgSender
     *
     * @param clientType        COPS client-type
     * @param clientHandle      Client handle
     * @param sock              Socket to the PEP
     */
    public COPSPdpOSMsgSender(final short clientType, final COPSHandle clientHandle, final Socket sock) {
        super(clientType, clientHandle, sock);
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
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(1, flag, getClientType(), _handle, decisions,
                null, null);

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
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), _handle, decisionMap, null, null);

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
    public void sendOpenNewRequestState() throws COPSPdpException {
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(Command.INSTALL, DecisionFlag.REQSTATE));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        // Common Header with the same ClientType as the request (default UNSOLICITED)
        // Client Handle with the same clientHandle as the request
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(getClientType(), _handle, decisionMap, null, null);

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
        // Client Handle with the same clientHandle as the request
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(_clientType, _handle, null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the sync state request, reason: " + e.getMessage());
        }
    }

}
