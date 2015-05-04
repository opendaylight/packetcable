/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.COPSStateMan;
import org.umu.cops.stack.*;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * State manager class for provisioning requests, at the PDP side.
 */
public class COPSPdpReqStateMan extends COPSStateMan {

    private final static Logger logger = LoggerFactory.getLogger(COPSPdpReqStateMan.class);

    /**
     * Object for performing policy data processing
     */
    protected final COPSPdpDataProcess _process;

    /** COPS message transceiver used to send COPS messages */
    protected transient COPSPdpMsgSender _sender;

    /**
     * Creates a request state manager
     * @param clientType    Client-type
     * @param clientHandle  Client handle
     */
    public COPSPdpReqStateMan(final short clientType, final COPSHandle clientHandle, final COPSPdpDataProcess process) {
        super(clientType, clientHandle);
        this._process = process;
    }

    @Override
    protected void initRequestState(final Socket sock) throws COPSPdpException {
        // Inits an object for sending COPS messages to the PEP
        _sender = new COPSPdpMsgSender(_clientType, _handle, sock);

        // Initial state
        _status = Status.ST_INIT;
    }

    /**
     * Processes a COPS request
     * @param msg   COPS request received from the PEP
     * @throws COPSPdpException
     */
    protected void processRequest(final COPSReqMsg msg) throws COPSPdpException {

        // TODO - Implement me
//        COPSHeader hdrmsg = msg.getHeader();
//        COPSHandle handlemsg = msg.getClientHandle();
//        COPSContext contextmsg = msg.getContext();

        //** Analyze the request
        //**

        /* <Request> ::= <Common Header>
        *                   <Client Handle>
        *                   <Context>
        *                   *(<Named ClientSI>)
        *                   [<Integrity>]
        * <Named ClientSI> ::= <*(<PRID> <EPD>)>
        *
        * Very important, this is actually being treated like this:
        * <Named ClientSI> ::= <PRID> | <EPD>
        *

        // Named ClientSI
        Vector clientSIs = msg.getClientSI();
        Hashtable reqSIs = new Hashtable(40);
        String strobjprid = new String();
        for (Enumeration e = clientSIs.elements() ; e.hasMoreElements() ;) {
            COPSClientSI clientSI = (COPSClientSI) e.nextElement();

            COPSPrObjBase obj = new COPSPrObjBase(clientSI.getData().getData());
            switch (obj.getSNum())
            {
                case COPSPrObjBase.PR_PRID:
                    strobjprid = obj.getData().str();
                    break;
                case COPSPrObjBase.PR_EPD:
                    reqSIs.put(strobjprid, obj.getData().str());
                    // COPSDebug.out(getClass().getName(),"PRID: " + strobjprid);
                    // COPSDebug.out(getClass().getName(),"EPD: " + obj.getData().str());
                    break;
                default:
                    break;
            }
        }

        //** Here we must retrieve a decision depending on
        //** the supplied ClientSIs
        // reqSIs is a hashtable with the prid and epds

        // ................
        //
        Hashtable removeDecs = new Hashtable();
        Hashtable installDecs = new Hashtable();
        _process.setClientData(this, reqSIs);

        removeDecs = _process.getRemovePolicy(this);
        installDecs = _process.getInstallPolicy(this);

        //** We create the SOLICITED decision
        //**
        _sender.sendDecision(removeDecs, installDecs);
        _status = ST_DECS;
        */
    }

    /**
     * Processes a report
     * @param msg   Report message from the PEP
     * @throws COPSPdpException
     */
    protected void processReport(final COPSReportMsg msg) throws COPSPdpException {

        //** Analyze the report
        //**

        /*
         * <Report State> ::= <Common Header>
         *                      <Client Handle>
         *                      <Report Type>
         *                      *(<Named ClientSI>)
         *                      [<Integrity>]
         * <Named ClientSI: Report> ::= <[<GPERR>] *(<report>)>
         * <report> ::= <ErrorPRID> <CPERR> *(<PRID><EPD>)
         *
         * Important, <Named ClientSI> is not parsed
        */

        // COPSHeader hdrmsg = msg.getHeader();
        // COPSHandle handlemsg = msg.getClientHandle();

        if (msg.getClientSI() != null) {
            // Report Type
            final COPSReportType rtypemsg = msg.getReport();
            final Map<String, String> repSIs = new HashMap<>();
            String strobjprid = "";
            final COPSPrObjBase obj = new COPSPrObjBase(msg.getClientSI().getData().getData());
            switch (obj.getSNum()) {
                case COPSPrObjBase.PR_PRID:
                    strobjprid = obj.getData().str();
                    break;
                case COPSPrObjBase.PR_EPD:
                    repSIs.put(strobjprid, obj.getData().str());
                    logger.info("PRID: " + strobjprid);
                    logger.info("EPD: " + obj.getData().str());
                    break;
                default:
                    break;
            }

            //** Here we must act in accordance with
            //** the report received
            switch (rtypemsg.getReportType()) {
                case SUCCESS:
                    _status = Status.ST_REPORT;
                    _process.successReport(this, repSIs);
                    break;
                case FAILURE:
                    _status = Status.ST_REPORT;
                    _process.failReport(this, repSIs);
                    break;
                case ACCOUNTING:
                    _status = Status.ST_ACCT;
                    _process.acctReport(this, repSIs);
                    break;
            }
        }


    }

    /**
     * Called when connection is closed
     * @param error Reason
     * @throws COPSPdpException
     */
    protected void processClosedConnection(final COPSError error) throws COPSPdpException {
        if (_process != null)
            _process.notifyClosedConnection(this, error);

        _status = Status.ST_CCONN;
    }

    /**
     * Called when no keep-alive is received
     * @throws COPSPdpException
     */
    protected void processNoKAConnection() throws COPSPdpException {
        if (_process != null)
            _process.notifyNoKAliveReceived(this);

        _status = Status.ST_NOKA;
    }

    /**
     * Deletes the request state
     * @throws COPSPdpException
     */
    protected void finalizeRequestState() throws COPSPdpException {
        _sender.sendDeleteRequestState();
        _status = Status.ST_FINAL;
    }

    /**
     * Asks for a COPS sync
     * @throws COPSPdpException
     */
    protected void syncRequestState() throws COPSPdpException {
        _sender.sendSyncRequestState();
        _status = Status.ST_SYNC;
    }

    /**
     * Opens a new request state
     * @throws COPSPdpException
     */
    protected void openNewRequestState() throws COPSPdpException {
        _sender.sendOpenNewRequestState();
        _status = Status.ST_NEW;
    }

    /**
     * Processes a COPS delete message
     * @param dMsg  <tt>COPSDeleteMsg</tt> received from the PEP
     * @throws COPSPdpException
     */
    protected void processDeleteRequestState(final COPSDeleteMsg dMsg) throws COPSPdpException {
        if (_process != null)
            _process.closeRequestState(this);

        _status = Status.ST_DEL;
    }

}
