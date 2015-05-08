package org.umu.cops.ospep;

import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSReason.ReasonCode;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;

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
    public COPSPepOSMsgSender (final short clientType, final COPSHandle clientHandle, final Socket sock) {
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
    public int getClientType() {
        return _clientType;
    }

    /**
     * Sends a request to the PDP.
     * The PEP establishes a request state client handle for which the
     * remote PDP may maintain state.
     * @param    clientSIs              Client data
     * @throws   COPSPepException
     */
    public void sendRequest(final Set<COPSClientSI> clientSIs) throws COPSPepException {
        // Create COPS Message
        final COPSReqMsg msg = new COPSReqMsg(_clientType, _handle, new COPSContext(RType.CONFIG, (short)0), null,
                null, null, clientSIs, null);

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
    public void sendFailReport(final List<COPSClientSI> clientSIs) throws COPSPepException {
        sendReport(clientSIs, new COPSReportType(ReportType.FAILURE));
    }

    /**
     * Sends a success report to the PDP. This report message notifies the PDP
     * of success when carrying out the PDP's decision, or when reporting
     *  an accounting related state change.
     * @param   clientSIs   Report data
     * @throws  COPSPepException
     */
    public void sendSuccessReport(final List<COPSClientSI> clientSIs) throws COPSPepException {
        sendReport(clientSIs, new COPSReportType(ReportType.SUCCESS));
    }

    /**
     * Sends an accounting report to the PDP
     * @param clientSIs Report data
     * @throws COPSPepException
     */
    public void sendAcctReport(final List<COPSClientSI> clientSIs) throws COPSPepException {
        sendReport(clientSIs, new COPSReportType(ReportType.ACCOUNTING));
    }

    private void sendReport(final List<COPSClientSI> clientSIs, final COPSReportType type) throws COPSPepException {
        // Change back to old way if it is ultimately determined that a report may contain more than one COPSClientSI
        final COPSReportMsg msg = new COPSReportMsg(_clientType, _handle, type, null, null);
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
        // Client Handle with the same clientHandle as the request
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(_clientType, _handle, null);
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
        // *** TODO: use real reason codes
        COPSReason reason = new COPSReason(ReasonCode.UNSPECIFIED, ReasonCode.NA);

        final COPSDeleteMsg msg = new COPSDeleteMsg(_clientType, _handle, reason, null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the delete request, reason: " + e.getMessage());
        }
    }

}
