/**
 @header@
 */

package org.pcmm;

import org.pcmm.gates.IGateID;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ITransactionID;
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
    protected final PCMMPdpDataProcess _process;

    /** COPS message transceiver used to send COPS messages */
    protected transient PCMMPdpMsgSender _sender;

    /**
     * Creates a request state manager
     * @param clientType    Client-type
     * @param clientHandle  Client handle
     */
    // TODO - consider sending in the COPSHandle object instead
    public PCMMPdpReqStateMan(final short clientType, final COPSHandle clientHandle, final PCMMPdpDataProcess process) {
        super(clientType, clientHandle, process);
        this._process = process;
    }

    @Override
    protected void initRequestState(final Socket sock) throws COPSPdpException {
        // Inits an object for sending COPS messages to the PEP
        _sender = new PCMMPdpMsgSender(_clientType, _handle, sock);

        // Initial state
        _status = Status.ST_INIT;
    }



    /**
     * Processes a COPS request
     * @param msg   COPS request received from the PEP
     * @throws COPSPdpException
     */
    public void processRequest(final COPSReqMsg msg) throws COPSPdpException {

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
     * TODO - break apart this method
     */
    protected void processReport(final COPSReportMsg msg) throws COPSPdpException {
        // Report Type
        final COPSReportType rtypemsg = msg.getReport();

        if (msg.getClientSI() != null) {
            final COPSClientSI clientSI = msg.getClientSI();
            // Named ClientSI
            final byte[] data = Arrays.copyOfRange(clientSI.getData().getData(), 0, clientSI.getData().getData().length);

            // PCMMUtils.WriteBinaryDump("COPSReportClientSI", data);
            logger.info("PCMMGateReq Parse Gate Message");
            final PCMMGateReq gateMsg = new PCMMGateReq(data);

            // TODO FIXME - Why is this Map being filled but never used???
            final Map<String, String> repSIs = new HashMap<>();
            String strobjprid = "";
            final COPSPrObjBase obj = new COPSPrObjBase(clientSI.getData().getData());
            switch (obj.getSNum()) {
                case COPSPrObjBase.PR_PRID:
                    logger.info("COPSPrObjBase.PR_PRID");
                    strobjprid = obj.getData().str();
                    break;
                case COPSPrObjBase.PR_EPD:
                    logger.info("COPSPrObjBase.PR_EPD");
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
                logger.info("Setting gate ID on gate object - " + gateID);
                gate.setGateID(gateID);
                if (_process != null)
                    _process.successReport(this, gateMsg);
            } else {
                final String cmdType;
                if ( trID.getGateCommandType() == ITransactionID.GateDeleteAck ) {
                    cmdType = "GateDeleteAck";
                } else if ( trID.getGateCommandType() == ITransactionID.GateSetAck ) {
                    cmdType = "GateSetAck";
                } else cmdType = null;
                // capture the gateId from the response message
                final IGateID gateID = gateMsg.getGateID();
                logger.info("Setting gate ID on gate object - " + gateID);
                gate.setGateID(gateID);
                int gateIdInt = gateID.getGateID();
                String gateIdHex = String.format("%08x", gateIdInt);
                logger.info(getClass().getName() + ": " + cmdType + ": GateID = " + gateIdHex);
            }
            if (rtypemsg.getReportType().equals(ReportType.FAILURE)) {
                logger.info("rtypemsg failure");
                _status = Status.ST_REPORT;
                if (_process != null)
                    _process.failReport(this, gateMsg);
                else
                    logger.info("Gate message error - " + gateMsg.getError().toString());
            } else if (rtypemsg.getReportType().equals(ReportType.ACCOUNTING)) {
                    logger.info("rtypemsg account");
                    _status = Status.ST_ACCT;
                    if (_process != null)
                        _process.acctReport(this, gateMsg);
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

    /**
    * Called when connection is closed
    * @param error  Reason
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
    protected void processDeleteRequestState(COPSDeleteMsg dMsg) throws COPSPdpException {
        if (_process != null)
            _process.closeRequestState(this);

        _status = Status.ST_DEL;
    }

}
