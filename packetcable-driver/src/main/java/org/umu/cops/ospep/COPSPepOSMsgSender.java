package org.umu.cops.ospep;

import org.umu.cops.prpep.COPSPepMsgSender;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;

/**
 * COPS message transceiver class for outsourcing connections at the PEP side.
 */
public class COPSPepOSMsgSender extends COPSPepMsgSender {

    /**
     * Creates a COPSPepMsgSender
     *
     * @param clientType        Client-type
     * @param clientHandle      Client handle
     * @param sock              Socket connected to the PDP
     */
    public COPSPepOSMsgSender(final short clientType, final COPSHandle clientHandle, final Socket sock) {
        super(clientType, clientHandle, sock);
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

}
