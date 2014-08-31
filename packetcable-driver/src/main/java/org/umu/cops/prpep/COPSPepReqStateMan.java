/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSPrObjBase;
import org.umu.cops.stack.COPSSyncStateMsg;

/**
 * COPSPepReqStateMan manages Request State using Client Handle (RFC 2748 pag. 21)
 * in PEP.
 *
 *   The client handle is used to identify a unique request state for a
 *   single PEP per client-type. Client handles are chosen by the PEP and
 *   are opaque to the PDP. The PDP simply uses the request handle to
 *   uniquely identify the request state for a particular Client-Type over
 *   a particular TCP connection and generically tie its decisions to a
 *   corresponding request. Client handles are initiated in request
 *   messages and are then used by subsequent request, decision, and
 *   report messages to reference the same request state. When the PEP is
 *   ready to remove a local request state, it will issue a delete message
 *   to the PDP for the corresponding client handle. A handle MUST be
 *   explicitly deleted by the PEP before it can be used by the PEP to
 *   identify a new request state. Handles referring to different request
 *   states MUST be unique within the context of a particular TCP
 *   connection and client-type.
 *
 * @version COPSPepReqStateMan.java, v 2.00 2004
 *
 */
public class COPSPepReqStateMan {

    /**
     * Request State created
     */
    public final static short ST_CREATE = 1;
    /**
     * Request sent
     */
    public final static short ST_INIT = 2;
    /**
     * Decisions received
     */
    public final static short ST_DECS = 3;
    /**
     * Report sent
     */
    public final static short ST_REPORT = 4;
    /**
     * Request State finalized
     */
    public final static short ST_FINAL = 5;
    /**
     * New Request State solicited
     */
    public final static short ST_NEW = 6;
    /**
     * Delete Request State solicited
     */
    public final static short ST_DEL = 7;
    /**
     * SYNC Request received
     */
    public final static short ST_SYNC = 8;
    /**
     * SYNC Completed
     */
    public final static short ST_SYNCALL = 9;
    /**
     * Close Connection received
     */
    public final static short ST_CCONN = 10;
    /**
     * KAlive Time out
     */
    public final static short ST_NOKA = 11;
    /**
     * ACCT Time out
     */
    public final static short ST_ACCT = 12;

    /**
     * The client-type identifies the policy client
     */
    protected short _clientType;

    /**
     *  The client handle is used to uniquely identify a particular
     *  PEP's request for a client-type
     */
    protected COPSHandle _handle;

    /**
        The PolicyDataProcess is used to process policy data in the PEP
     */
    protected COPSPepDataProcess _process;

    /**
     *  State Request State
     */
    protected short _status;

    /**
        The Msg Sender is used to send COPS messages
     */
    protected COPSPepMsgSender _sender;

    /**
     * Sync State
     */
    protected boolean _syncState;

    /**
     * Create a State Request Manager
     *
     * @param    clientHandle                a Client Handle
     *
     */
    public COPSPepReqStateMan(short clientType, String clientHandle) {
        // COPS Handle
        _handle = new COPSHandle();
        COPSData id = new COPSData(clientHandle);
        _handle.setId(id);
        // client-type
        _clientType = clientType;
        _syncState = true;
        _status = ST_CREATE;
    }

    /**
     * Return client handle
     *
     * @return   a COPSHandle
     *
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Return client-type
     *
     * @return   a short
     *
     */
    public short getClientType() {
        return _clientType;
    }

    /**
     * Return Request State status
     *
     * @return      s short
     */
    public short getStatus() {
        return _status;
    }

    /**
     * Return the Policy Data Process
     *
     * @return   a PolicyConfigure
     *
     */
    public COPSPepDataProcess getDataProcess() {
        return _process;
    }

    /**
     * Establish the Policy Data Process
     *
     * @param    process              a  PolicyConfigure
     *
     */
    public void setDataProcess(COPSPepDataProcess process) {
        _process = process;
    }

    /**
     * Init Request State
     *
     * @throws   COPSPepException
     *
     */
    protected void initRequestState(Socket sock)
    throws COPSPepException {
        // Inits an object for sending COPS messages to the PDP
        _sender = new COPSPepMsgSender(_clientType, _handle, sock);

        // If an object for retrieving PEP features exists,
        // use it for retrieving them
        Hashtable clientSIs;
        if (_process != null)
            clientSIs = _process.getClientData(this);
        else
            clientSIs = null;

        // Send the request
        _sender.sendRequest(clientSIs);

        // Initial state
        _status = ST_INIT;
    }

    /**
     * Finalize Request State
     *
     * @throws   COPSPepException
     *
     */
    protected void finalizeRequestState()
    throws COPSPepException {
        _sender.sendDeleteRequest();
        _status = ST_FINAL;
    }

    /**
     * Process the message Decision
     *
     * @param    dMsg                a  COPSDecisionMsg
     *
     * @throws   COPSPepException
     *
     */
    protected void processDecision(COPSDecisionMsg dMsg)
    throws COPSPepException {
        // COPSDebug.out(getClass().getName(), "ClientId:" + getClientHandle().getId().str());

        // COPSHandle handle = dMsg.getClientHandle();
        Hashtable decisions = dMsg.getDecisions();

        Hashtable removeDecs = new Hashtable(40);
        Hashtable installDecs = new Hashtable(40);
        Hashtable errorDecs = new Hashtable(40);
        for (Enumeration e = decisions.keys() ; e.hasMoreElements() ;) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) decisions.get(context);
            Enumeration ee = v.elements();
            COPSDecision cmddecision = (COPSDecision) ee.nextElement();

            // cmddecision --> we must check whether it is an error!

            if (cmddecision.isInstallDecision()) {
                String prid = new String();
                for (; ee.hasMoreElements() ;) {
                    COPSDecision decision = (COPSDecision) ee.nextElement();

                    COPSPrObjBase obj = new COPSPrObjBase(decision.getData().getData());
                    switch (obj.getSNum()) {
                    case COPSPrObjBase.PR_PRID:
                        prid = obj.getData().str();
                        break;
                    case COPSPrObjBase.PR_EPD:
                        installDecs.put(prid, obj.getData().str());
                        break;
                    default:
                        break;
                    }
                }
            }

            if (cmddecision.isRemoveDecision()) {

                String prid = new String();
                for (; ee.hasMoreElements() ;) {
                    COPSDecision decision = (COPSDecision) ee.nextElement();

                    COPSPrObjBase obj = new COPSPrObjBase(decision.getData().getData());
                    switch (obj.getSNum()) {
                    case COPSPrObjBase.PR_PRID:
                        prid = obj.getData().str();
                        break;
                    case COPSPrObjBase.PR_EPD:
                        removeDecs.put(prid, obj.getData().str());
                        break;
                    default:
                        break;
                    }
                }
            }
        }

        //** Apply decisions to the configuration
        _process.setDecisions(this, removeDecs, installDecs, errorDecs);
        _status = ST_DECS;


        if (_process.isFailReport(this)) {
            // COPSDebug.out(getClass().getName(),"Sending FAIL Report\n");
            _sender.sendFailReport(_process.getReportData(this));
        } else {
            // COPSDebug.out(getClass().getName(),"Sending SUCCESS Report\n");
            _sender.sendSuccessReport(_process.getReportData(this));
        }
        _status = ST_REPORT;

        if (!_syncState) {
            _sender.sendSyncComplete();
            _syncState = true;
            _status = ST_SYNCALL;
        }
    }

    /**
     * Process the message NewRequestState
     *
     * @throws   COPSPepException
     *
     */
    protected void processOpenNewRequestState()
    throws COPSPepException {

        if (_process != null)
            _process.newRequestState(this);

        _status = ST_NEW;
    }

    /**
     * Process the message DeleteRequestState
     *
     * @param    dMsg                a  COPSDecisionMsg
     *
     * @throws   COPSPepException
     *
     */
    protected void processDeleteRequestState(COPSDecisionMsg dMsg)
    throws COPSPepException {
        if (_process != null)
            _process.closeRequestState(this);

        _status = ST_DEL;
    }

    /**
     * Process the message SycnStateRequest.
     * The message SycnStateRequest indicates that the remote PDP
     * wishes the client (which appears in the common header)
     * to re-send its state.
     *
     * @param    ssMsg               a  COPSSyncStateMsg
     *
     * @throws   COPSPepException
     *
     */
    protected void processSyncStateRequest(COPSSyncStateMsg ssMsg)
    throws COPSPepException {
        _syncState = false;
        // If an object for retrieving PEP features exists,
        // use it for retrieving them
        Hashtable clientSIs;
        if (_process != null)
            clientSIs = _process.getClientData(this);
        else
            clientSIs = null;

        // Send request
        _sender.sendRequest(clientSIs);

        _status = ST_SYNC;
    }

    protected void processClosedConnection(COPSError error)
    throws COPSPepException {
        if (_process != null)
            _process.notifyClosedConnection(this, error);

        _status = ST_CCONN;
    }

    protected void processNoKAConnection()
    throws COPSPepException {
        if (_process != null)
            _process.notifyNoKAliveReceived(this);

        _status = ST_NOKA;
    }

    protected void processAcctReport()
    throws COPSPepException {

        Hashtable report = new Hashtable();
        if (_process != null)
            report = _process.getAcctData(this);

        _sender.sendAcctReport(report);

        _status = ST_ACCT;
    }

}
