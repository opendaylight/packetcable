package org.umu.cops.ospdp;

import org.umu.cops.COPSStateMan;
import org.umu.cops.stack.*;

import java.net.Socket;
import java.util.Vector;

/**
 * State manager class for outsourcing requests, at the PDP side.
 */
public class COPSPdpOSReqStateMan extends COPSStateMan {

    /**
     * Object for performing policy data processing
     */
    private final COPSPdpOSDataProcess _process;

    /** COPS message transceiver used to send COPS messages */
    private transient COPSPdpOSMsgSender _sender;

    /**
     * Creates a request state manager
     * @param clientType    Client-type
     * @param clientHandle  Client handle
     */
    // TODO - consider sending in the COPSHandle object instead
    public COPSPdpOSReqStateMan(final short clientType, final COPSHandle clientHandle, final COPSPdpOSDataProcess process) {
        super(clientType, clientHandle);
        this._process = process;
    }

    @Override
    protected void initRequestState(final Socket sock) throws COPSException {
        // Inits an object for sending COPS messages to the PDP
        _sender = new COPSPdpOSMsgSender(_clientType, _handle, sock);

        // Initial state
        _status = Status.ST_INIT;
    }

    /**
     * Processes a COPS request
     * @param msg   COPS request received from the PEP
     * @throws COPSException
     */
    protected void processRequest(COPSReqMsg msg) throws COPSException {
        //** Here we must retrieve a decision depending on the
        //** supplied ClientSIs
        /*Vector removeDecs = new Vector();
        Vector installDecs = new Vector();*/
        if (msg.getClientSI() != null)
            _process.setClientData(this, msg.getClientSI().toArray(new COPSClientSI[msg.getClientSI().size()]));

        Vector removeDecs = _process.getRemovePolicy(this);
        Vector installDecs = _process.getInstallPolicy(this);

        //** We create a SOLICITED decision
        //**
        _sender.sendSolicitedDecision(removeDecs, installDecs);
        _status = Status.ST_DECS;
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

        // Report Type
        final COPSReportType rtypemsg = msg.getReport();

        // Named ClientSI
        if (msg.getClientSI() != null) {
            //** Here we must act in accordance with
            //** the report received
            switch (rtypemsg.getReportType()) {
                case SUCCESS:
                    _status = Status.ST_REPORT;
                    _process.successReport(this, msg.getClientSI());
                    break;
                case FAILURE:
                    _status = Status.ST_REPORT;
                    _process.failReport(this, msg.getClientSI());
                    break;
                case ACCOUNTING:
                    _status = Status.ST_ACCT;
                    _process.acctReport(this, msg.getClientSI());
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
    protected void finalizeRequestState() throws COPSException {
        _sender.sendDeleteRequestState();
        _status = Status.ST_FINAL;
    }

    /**
     * Asks for a COPS sync
     * @throws COPSPdpException
     */
    protected void syncRequestState() throws COPSException {
        _sender.sendSyncRequestState();
        _status = Status.ST_SYNC;
    }

    /**
     * Opens a new request state
     * @throws COPSPdpException
     */
    protected void openNewRequestState() throws COPSException {
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
