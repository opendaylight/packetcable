package org.umu.cops.ospdp;

import org.umu.cops.prpdp.COPSPdpReqStateMan;
import org.umu.cops.stack.*;

import java.net.Socket;
import java.util.List;

/**
 * State manager class for outsourcing requests, at the PDP side.
 */
public class COPSPdpOSReqStateMan extends COPSPdpReqStateMan {

    /**
     * Object for performing policy data processing
     */
    private final COPSPdpOSDataProcess _thisProcess;

    /** COPS message transceiver used to send COPS messages */
    private final COPSPdpOSMsgSender _sender;

    /**
     * Creates a request state manager
     * @param clientType    Client-type
     * @param clientHandle  Client handle
     * @param process       The PDP OS Data Processor
     */
    public COPSPdpOSReqStateMan(final short clientType, final COPSHandle clientHandle,
                                final COPSPdpOSDataProcess process, final Socket socket) {
        super(clientType, clientHandle, process, socket);
        this._thisProcess = process;
        this._sender = new COPSPdpOSMsgSender(_clientType, _handle, _socket);
        _status = Status.ST_INIT;
    }

    @Override
    protected void processRequest(final COPSReqMsg msg) throws COPSException {
        if (msg.getClientSI() != null)
            _thisProcess.setClientData(this, msg.getClientSI().toArray(new COPSClientSI[msg.getClientSI().size()]));

        // TODO - Add type to List once we know what it should be.
        final List removeDecs = _thisProcess.getRemovePolicy(this);
        final List installDecs = _thisProcess.getInstallPolicy(this);

        //** We create a SOLICITED decision
        //**
        _sender.sendSolicitedDecision(removeDecs, installDecs);
        _status = Status.ST_DECS;
    }

    @Override
    protected void processReport(final COPSReportMsg msg) throws COPSPdpException {
        final COPSReportType rtypemsg = msg.getReport();
        if (msg.getClientSI() != null) {
            //** Here we must act in accordance with
            //** the report received
            switch (rtypemsg.getReportType()) {
                case SUCCESS:
                    _status = Status.ST_REPORT;
                    _thisProcess.successReport(this, msg.getClientSI());
                    break;
                case FAILURE:
                    _status = Status.ST_REPORT;
                    _thisProcess.failReport(this, msg.getClientSI());
                    break;
                case ACCOUNTING:
                    _status = Status.ST_ACCT;
                    _thisProcess.acctReport(this, msg.getClientSI());
                    break;
            }
        }
    }

}
