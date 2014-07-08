/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSPrEPD;
import org.umu.cops.stack.COPSPrID;
import org.umu.cops.stack.COPSSyncStateMsg;

/**
 * COPS message transceiver class for provisioning connections at the PDP side.
 */
public class COPSPdpMsgSender {

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
    public COPSPdpMsgSender (short clientType, COPSHandle clientHandle, Socket sock) {
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
     * Sends a decision message
     * @param removeDecs    Decisions to be removed
     * @param installDecs   Decisions to be installed
     * @throws COPSPdpException
     */
    public void sendDecision(Hashtable removeDecs, Hashtable installDecs)
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

        // Common Header with the same ClientType as the request
        COPSHeader hdr = new COPSHeader (COPSHeader.COPS_OP_DEC, getClientType());
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

                for (Enumeration e = removeDecs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) removeDecs.get(strprid);

                    //  <Named Decision Data: Provisioning> (PRID)
                    COPSDecision dec2 = new COPSDecision(COPSDecision.DEC_NAMED);
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    dec2.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));
                    //  <Named Decision Data: Provisioning> (EPD)
                    COPSDecision dec3 = new COPSDecision(COPSDecision.DEC_NAMED);
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    dec3.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                    decisionMsg.addDecision(dec2, cntxt);
                    decisionMsg.addDecision(dec3, cntxt);
                }
            }

            // Install Decisions
            //  <Decision: Flags>
            if (installDecs.size() > 0) {
                COPSDecision idec1 = new COPSDecision();
                idec1.setCmdCode(COPSDecision.DEC_INSTALL);

                decisionMsg.addDecision(idec1, cntxt);

                for (Enumeration e = installDecs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) installDecs.get(strprid);

                    //  <Named Decision Data: Provisioning> (PRID)
                    COPSDecision dec2 = new COPSDecision(COPSDecision.DEC_NAMED);
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    dec2.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));
                    //  <Named Decision Data: Provisioning> (EPD)
                    COPSDecision dec3 = new COPSDecision(COPSDecision.DEC_NAMED);
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    dec3.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                    decisionMsg.addDecision(dec2, cntxt);
                    decisionMsg.addDecision(dec3, cntxt);
                }

                /**
                COPSIntegrity intr = new COPSIntegrity();
                intr.setKeyId(19);
                intr.setSeqNum(9);
                intr.setKeyDigest(new COPSData("KEY DIGEST"));
                decisionMsg.add(intr);
                /**/
            }
        } catch (COPSException e) {
            throw new COPSPdpException("Error making Msg");
        }

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
    public void sendUnsolicitedDecision(Hashtable removeDecs, Hashtable installDecs)
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
        COPSHeader hdr = new COPSHeader (COPSHeader.COPS_OP_DEC, getClientType());

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
            COPSDecision rdec1 = new COPSDecision();
            rdec1.setCmdCode(COPSDecision.DEC_REMOVE);

            decisionMsg.addDecision(rdec1, cntxt);

            for (Enumeration e = removeDecs.keys() ; e.hasMoreElements() ;) {
                String strprid = (String) e.nextElement();
                String strepd = (String) removeDecs.get(strprid);

                //  <Named Decision Data: Provisioning> (PRID)
                COPSDecision dec2 = new COPSDecision(COPSDecision.DEC_NAMED);
                COPSPrID prid = new COPSPrID();
                prid.setData(new COPSData(strprid));
                dec2.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));
                //  <Named Decision Data: Provisioning> (EPD)
                COPSDecision dec3 = new COPSDecision(COPSDecision.DEC_NAMED);
                COPSPrEPD epd = new COPSPrEPD();
                epd.setData(new COPSData(strepd));
                dec3.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                decisionMsg.addDecision(dec2, cntxt);
                decisionMsg.addDecision(dec3, cntxt);
            }

            // Install Decisions
            //  <Decision: Flags>
            COPSDecision idec1 = new COPSDecision();
            idec1.setCmdCode(COPSDecision.DEC_INSTALL);

            decisionMsg.addDecision(idec1, cntxt);

            for (Enumeration e = installDecs.keys() ; e.hasMoreElements() ;) {
                String strprid = (String) e.nextElement();
                String strepd = (String) installDecs.get(strprid);

                //  <Named Decision Data: Provisioning> (PRID)
                COPSDecision dec2 = new COPSDecision(COPSDecision.DEC_NAMED);
                COPSPrID prid = new COPSPrID();
                prid.setData(new COPSData(strprid));
                dec2.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));
                //  <Named Decision Data: Provisioning> (EPD)
                COPSDecision dec3 = new COPSDecision(COPSDecision.DEC_NAMED);
                COPSPrEPD epd = new COPSPrEPD();
                epd.setData(new COPSData(strepd));
                dec3.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                decisionMsg.addDecision(dec2, cntxt);
                decisionMsg.addDecision(dec3, cntxt);
            }

            /**
            COPSIntegrity intr = new COPSIntegrity();
            intr.setKeyId(19);
            intr.setSeqNum(9);
            intr.setKeyDigest(new COPSData("KEY DIGEST"));
            decisionMsg.add(intr);
            /**/
        } catch (COPSException e) {
            throw new COPSPdpException("Error making Msg");
        }

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
    public void sendDeleteRequestState()
    throws COPSPdpException {
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
