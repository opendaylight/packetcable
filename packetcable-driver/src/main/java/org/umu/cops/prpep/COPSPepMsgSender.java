/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSClientSI.CSIType;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSReason.ReasonCode;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * COPSPepMsgSender sends COPS messages to PDP.
 *
 * @version COPSPepMsgSender.java, v 2.00 2004
 *
 */
public class COPSPepMsgSender {

    /**
     * Socket connection to PDP
     */
    protected Socket _sock;

    /**
     * The client-type identifies the policy client
     */
    protected short _clientType;

    /**
     * The client handle is used to uniquely identify a particular
     * PEP's request for a client-type
     */
    protected COPSHandle _handle;

    /**
     * Create a COPSPepMsgSender
     *
     * @param clientType        client-type
     * @param clientHandle      client handle
     * @param sock              socket of PDP connection
     */
    public COPSPepMsgSender (short clientType, COPSHandle clientHandle, Socket sock) {
        // COPS Handle
        _handle = clientHandle;
        _clientType = clientType;

        _sock = sock;
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
    public int getClientType() {
        return _clientType;
    }

    /**
     * Send Request to PDP.
     *   The PEP establishes a request state client handle for which the
     *   remote PDP may maintain state.
     *
     * @param    clientSIs              a  Hashtable
     *
     * @throws   COPSPepException
     *
     */
    public void sendRequest(final Map<String, String> clientSIs) throws COPSPepException {
        final Set<COPSClientSI> clientSISet = new HashSet<>();
        // Add the clientSIs
        for (final Map.Entry<String, String> entry : clientSIs.entrySet()) {
            //  (PRID)
            final COPSPrID prid = new COPSPrID();
            prid.setData(new COPSData(entry.getKey()));
            clientSISet.add(new COPSClientSI(CSIType.NAMED, new COPSData(prid.getDataRep(), 0, prid.getDataLength())));

            //  (EPD)
            final COPSPrEPD epd = new COPSPrEPD();
            epd.setData(new COPSData(entry.getValue()));
            clientSISet.add(new COPSClientSI(CSIType.NAMED, new COPSData(epd.getDataRep(), 0, epd.getDataLength())));
        }
        final COPSReqMsg msg = new COPSReqMsg(_clientType, _handle, new COPSContext(RType.CONFIG, (short)0),
                null, null, null, clientSISet, null);

        // Send message
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the request, reason: " + e.getMessage());
        }
    }

    /**
     * Send Fail Report to PDP.
     *    The RPT message is used by the PEP to communicate to the PDP its
     *    success or failure in carrying out the PDP's decision, or to report
     *    an accounting related change in state.
     *
     * @throws   COPSPepException
     *
     */
    public void sendFailReport(final Map<String, String> clientSIs) throws COPSPepException {
        sendReport(clientSIs, new COPSReportType(ReportType.FAILURE));
    }

    /**
     * Send Succes Report to PDP.
     *    The RPT message is used by the PEP to communicate to the PDP its
     *    success or failure in carrying out the PDP's decision, or to report
     *    an accounting related change in state.
     *
     * @throws   COPSPepException
     *
     */
    public void sendSuccessReport(final Map<String, String> clientSIs) throws COPSPepException {
        sendReport(clientSIs, new COPSReportType(ReportType.SUCCESS));
    }

    public void sendAcctReport(final Map<String, String> clientSIs) throws COPSPepException {
        sendReport(clientSIs, new COPSReportType(ReportType.ACCOUNTING));
    }

    private void sendReport(final Map<String, String> clientSIs, final COPSReportType reportType)
            throws COPSPepException {
        // Report SUCESS
        for (final Map.Entry<String, String> entry : clientSIs.entrySet()) {
            //  (PRID)
            final COPSPrID prid = new COPSPrID();
            prid.setData(new COPSData(entry.getKey()));

            final COPSReportMsg pridMsg = new COPSReportMsg(_clientType, _handle, reportType,
                    new COPSClientSI(CSIType.NAMED, new COPSData(prid.getDataRep(), 0, prid.getDataLength())), null);
            try {
                pridMsg.writeData(_sock);
            } catch (IOException e) {
                throw new COPSPepException("Failed to send the report, reason: " + e.getMessage());
            }

            //  (EPD)
            final COPSPrEPD epd = new COPSPrEPD();
            epd.setData(new COPSData(entry.getValue()));
            final COPSReportMsg epdMsg = new COPSReportMsg(_clientType, _handle, reportType,
                    new COPSClientSI(CSIType.NAMED, new COPSData(epd.getDataRep(), 0, epd.getDataLength())), null);
            try {
                pridMsg.writeData(_sock);
            } catch (IOException e) {
                throw new COPSPepException("Failed to send the report, reason: " + e.getMessage());
            }
        }
    }

    /**
     * Send Sync State Complete to PDP.
     *   The Synchronize State Complete is sent by the PEP to the PDP after
     *   the PDP sends a synchronize state request to the PEP and the PEP has
     *   finished synchronization.
     *
     * @throws   COPSPepException
     *
     */
    public void sendSyncComplete() throws COPSPepException {
        final COPSSyncStateMsg msg = new COPSSyncStateMsg(_clientType, _handle, null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the sync state request, reason: " + e.getMessage());
        }
    }

    /**
     * Send Delete Request to PDP.
     * When sent from the PEP this message indicates to the remote PDP that
     * the state identified by the client handle is no longer
     * available/relevant.
     *
     * @throws   COPSPepException
     *
     */
    public void sendDeleteRequest() throws COPSPepException {
        // *** TODO: send a real reason
        final COPSReason reason = new COPSReason(ReasonCode.UNSPECIFIED, ReasonCode.NA);
        final COPSDeleteMsg msg = new COPSDeleteMsg(_clientType, _handle, reason, null);
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the delete request, reason: " + e.getMessage());
        }
    }
}




