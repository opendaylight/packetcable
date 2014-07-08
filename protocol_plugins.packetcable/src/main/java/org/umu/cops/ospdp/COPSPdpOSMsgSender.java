package org.umu.cops.ospdp;

import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSSyncStateMsg;

/**
 * COPS message transceiver class for outsourcing connections at the PDP side.
 */
public class COPSPdpOSMsgSender {
    /**
     * Socket connected to PEP
     */
    protected Socket _sock;

    /**
     * COPS client-type that identifies the policy client
     */
    protected short _clientType;

    /**
     * COPS client handle used to uniquely identify a particular
     * PEP's request for a client-type
     */
    protected COPSHandle _handle;

    /**
     * Creates a COPSPepMsgSender
     *
     * @param clientType        COPS client-type
     * @param clientHandle      Client handle
     * @param sock              Socket to the PEP
     */
    public COPSPdpOSMsgSender (short clientType, COPSHandle clientHandle, Socket sock) {
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
        COPSHeader hdr = new COPSHeader (COPSHeader.COPS_OP_DEC, getClientType());

        if (solicited)
            hdr.setFlag(COPSHeader.COPS_FLAG_SOLICITED);

        // Client Handle with the same clientHandle as the request
        COPSHandle handle = new COPSHandle();
        handle.setId(getClientHandle().getId());

        COPSDecisionMsg decisionMsg = new COPSDecisionMsg();
        try {
            decisionMsg.add(hdr);
            decisionMsg.add(handle);

            // Decisions (no flags supplied)
            //  <Context>
            COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);

            // Remove Decisions
            //  <Decision: Flags>
            if (removeDecs.size() > 0) {
                COPSDecision rdec1 = new COPSDecision();
                rdec1.setCmdCode(COPSDecision.DEC_REMOVE);

                decisionMsg.addDecision(rdec1, cntxt);

                Enumeration removeDecsEnum = removeDecs.elements();
                while (removeDecsEnum.hasMoreElements())
                    decisionMsg.addDecision((COPSDecision) removeDecsEnum.nextElement(), cntxt);
            }

            // Install Decisions
            //  <Decision: Flags>
            if (installDecs.size() > 0) {
                COPSDecision idec1 = new COPSDecision();
                idec1.setCmdCode(COPSDecision.DEC_INSTALL);

                decisionMsg.addDecision(idec1, cntxt);

                Enumeration installDecsEnum = installDecs.elements();
                while (installDecsEnum.hasMoreElements())
                    decisionMsg.addDecision((COPSDecision) installDecsEnum.nextElement(), cntxt);
                /**
                COPSIntegrity intr = new COPSIntegrity();
                intr.setKeyId(19);
                intr.setSeqNum(9);
                intr.setKeyDigest(new COPSData("KEY DIGEST"));
                decisionMsg.add(intr);
                /**/
            }
        } catch (COPSException e) {
            e.printStackTrace();
            throw new COPSPdpException("Error making Msg");
        }

        //** Send decision
        //**
        try {
            decisionMsg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPdpException("Failed to send the decision, reason: " + e.getMessage());
        }
    }

    /**FIXME: unused?
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

        // Common Header with the same ClientType as the request (default UNSOLICITED)
        COPSHeader hdr = new COPSHeader (COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle clienthandle = new COPSHandle();
        clienthandle.setId(_handle.getId());

        // Decisions
        //  <Context>
        COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
        //  <Decision: Flags>
        COPSDecision dec = new COPSDecision();
        dec.setCmdCode(COPSDecision.DEC_REMOVE);
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

        // Common Header with the same ClientType as the request (default UNSOLICITED)
        COPSHeader hdr = new COPSHeader (COPSHeader.COPS_OP_DEC, getClientType());

        // Client Handle with the same clientHandle as the request
        COPSHandle clienthandle = new COPSHandle();
        clienthandle.setId(_handle.getId());

        // Decisions
        //  <Context>
        COPSContext cntxt = new COPSContext(COPSContext.CONFIG, (short) 0);
        //  <Decision: Flags>
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

        // Common Header with the same ClientType as the request
        COPSHeader hdr = new COPSHeader (COPSHeader.COPS_OP_SSQ, getClientType());

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
            throw new COPSPdpException("Failed to send the sync state request, reason: " + e.getMessage());
        }
    }

}
