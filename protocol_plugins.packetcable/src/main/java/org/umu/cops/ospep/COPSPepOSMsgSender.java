package org.umu.cops.ospep;

import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSDeleteMsg;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSReason;
import org.umu.cops.stack.COPSReportMsg;
import org.umu.cops.stack.COPSReportType;
import org.umu.cops.stack.COPSReqMsg;
import org.umu.cops.stack.COPSSyncStateMsg;

/**
 * COPS message transceiver class for outsourcing connections at the PEP side.
 */
public class COPSPepOSMsgSender {
    /**
     * Socket connection to PDP
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
     * @param clientType        Client-type
     * @param clientHandle      Client handle
     * @param sock              Socket connected to the PDP
     */
    public COPSPepOSMsgSender (short clientType, COPSHandle clientHandle, Socket sock) {
        // COPS Handle
        _handle = clientHandle;
        _clientType = clientType;

        _sock = sock;
    }

    /**
     * Gets the client handle
     * @return  Client's <tt>COPSHandle</tt>
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Gets the client-type
     * @return  Client-type value
     */
    public short getClientType() {
        return _clientType;
    }

    /**
     * Sends a request to the PDP.
     * The PEP establishes a request state client handle for which the
     * remote PDP may maintain state.
     * @param    clientSIs              Client data
     * @throws   COPSPepException
     */
    public void sendRequest(Vector clientSIs) throws COPSPepException {
        // Create COPS Message
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_REQ, _clientType);

        COPSContext cntxt = new COPSContext(COPSContext.CONFIG , (short) 0);

        COPSHandle handle = _handle;

        COPSReqMsg msg = new COPSReqMsg();
        try {
            msg.add(hdr) ;
            msg.add(handle) ;
            msg.add(cntxt) ;

            Enumeration clientSIEnum = clientSIs.elements();
            while (clientSIEnum.hasMoreElements())
                msg.add( (COPSClientSI) clientSIEnum.nextElement());
        } catch (COPSException e) {
            throw new COPSPepException("Error making Request Msg, reason: " + e.getMessage());
        }

        // Send message
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the request, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a failure report to the PDP. This report message notifies the PDP
     * of failure when carrying out the PDP's decision, or when reporting
     *  an accounting related state change.
     * @param clientSIs Report data
     * @throws   COPSPepException
     */
    public void sendFailReport(Vector clientSIs) throws COPSPepException {
        COPSReportMsg msg = new COPSReportMsg();
        // Report FAIL
        try {
            COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_RPT, _clientType);
            COPSHandle hnd = _handle;

            COPSReportType report = new COPSReportType(COPSReportType.FAILURE);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);

            Enumeration clientSIEnum = clientSIs.elements();
            while (clientSIEnum.hasMoreElements())
                msg.add( (COPSClientSI) clientSIEnum.nextElement());
        } catch (COPSException ex) {
            throw new COPSPepException("Error making Msg");
        }

        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the report, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a success report to the PDP. This report message notifies the PDP
     * of success when carrying out the PDP's decision, or when reporting
     *  an accounting related state change.
     * @param   clientSIs   Report data
     * @throws  COPSPepException
     */
    public void sendSuccessReport(Vector clientSIs) throws COPSPepException {
        COPSReportMsg msg = new COPSReportMsg();
        // Report SUCESS
        try {
            COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_RPT, _clientType);
            COPSHandle hnd = _handle;

            COPSReportType report = new COPSReportType(COPSReportType.SUCCESS);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);

            Enumeration clientSIEnum = clientSIs.elements();
            while (clientSIEnum.hasMoreElements())
                msg.add( (COPSClientSI) clientSIEnum.nextElement());
        } catch (COPSException ex) {
            throw new COPSPepException("Error making Msg");
        }

        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the report, reason: " + e.getMessage());
        }
    }

    /**
     * Sends an accounting report to the PDP
     * @param clientSIs Report data
     * @throws COPSPepException
     */
    public void sendAcctReport(Vector clientSIs) throws COPSPepException {
        COPSReportMsg msg = new COPSReportMsg();
        // Report SUCCESS
        try {
            COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_RPT, _clientType);
            COPSHandle hnd = _handle;

            COPSReportType report = new COPSReportType(COPSReportType.ACCT);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);

            Enumeration clientSIEnum = clientSIs.elements();
            while (clientSIEnum.hasMoreElements())
                msg.add( (COPSClientSI) clientSIEnum.nextElement());
        } catch (COPSException ex) {
            throw new COPSPepException("Error making Msg");
        }

        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the report, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a sync-complete message to the PDP. This indicates the
     * end of a synchronization requested by the PDP.
     * @throws   COPSPepException
     */
    public void sendSyncComplete() throws COPSPepException {
        // Common Header with the same ClientType as the request
        COPSHeader hdr = new COPSHeader (COPSHeader.COPS_OP_SSC, _clientType);

        // Client Handle with the same clientHandle as the request
        COPSHandle clienthandle = _handle;

        COPSSyncStateMsg msg = new COPSSyncStateMsg();
        try {
            msg.add(hdr);
            msg.add(clienthandle);
        } catch (Exception e) {
            throw new COPSPepException("Error making Msg");
        }

        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the sync state request, reason: " + e.getMessage());
        }
    }

    /**
     * Sends a delete request to the PDP.
     * When sent from the PEP this message indicates to the remote PDP that
     * the state identified by the client handle is no longer
     * available/relevant.
     * @throws   COPSPepException
     */
    public void sendDeleteRequest() throws COPSPepException {
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DRQ, _clientType);
        COPSHandle handle = _handle;

        // *** TODO: use real reason codes
        COPSReason reason = new COPSReason((short) 234, (short) 345);

        COPSDeleteMsg msg = new COPSDeleteMsg();
        try {
            msg.add(hdr);
            msg.add(handle);
            msg.add(reason);
            msg.writeData(_sock);
        } catch (COPSException ex) {
            throw new COPSPepException("Error making Msg");
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the delete request, reason: " + e.getMessage());
        }
    }

}
