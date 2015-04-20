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
import java.util.Enumeration;
import java.util.Hashtable;

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
    public short getClientType() {
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
    public void sendRequest(Hashtable clientSIs)
    throws COPSPepException {
        // Create COPS Message
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_REQ, _clientType);
        COPSContext cntxt = new COPSContext(RType.CONFIG , (short) 0);

        COPSHandle handle = _handle;

        // Add the clientSIs
        COPSReqMsg msg = new COPSReqMsg();
        try {
            msg.add(hdr) ;
            msg.add(handle) ;
            msg.add(cntxt) ;

            if (clientSIs.size() > 0) {
                for (Enumeration e = clientSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) clientSIs.get(strprid);

                    //  (PRID)
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    COPSClientSI cSi = new COPSClientSI(CSIType.NAMED,
                            new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    COPSClientSI cSi2 = new COPSClientSI(CSIType.NAMED,
                            new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                    msg.add(cSi);
                    msg.add(cSi2);
                }
            }

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
     * Send Fail Report to PDP.
     *    The RPT message is used by the PEP to communicate to the PDP its
     *    success or failure in carrying out the PDP's decision, or to report
     *    an accounting related change in state.
     *
     * @throws   COPSPepException
     *
     */
    public void sendFailReport(Hashtable clientSIs)
    throws COPSPepException {
        COPSReportMsg msg = new COPSReportMsg();
        // Report FAIL
        try {
            COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_RPT, _clientType);
            COPSHandle hnd = _handle;

            COPSReportType report = new COPSReportType(ReportType.FAILURE);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);
            if (clientSIs.size() > 0) {
                for (Enumeration e = clientSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) clientSIs.get(strprid);

                    //  (PRID)
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    COPSClientSI cSi = new COPSClientSI(CSIType.NAMED,
                            new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    COPSClientSI cSi2 = new COPSClientSI(CSIType.NAMED,
                            new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                    msg.add(cSi);
                    msg.add(cSi2);
                }
            }

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
     * Send Succes Report to PDP.
     *    The RPT message is used by the PEP to communicate to the PDP its
     *    success or failure in carrying out the PDP's decision, or to report
     *    an accounting related change in state.
     *
     * @throws   COPSPepException
     *
     */
    public void sendSuccessReport(Hashtable clientSIs)
    throws COPSPepException {
        COPSReportMsg msg = new COPSReportMsg();
        // Report SUCESS
        try {
            COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_RPT, _clientType);
            COPSHandle hnd = _handle;

            COPSReportType report = new COPSReportType(ReportType.SUCCESS);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);

            if (clientSIs.size() > 0) {
                for (Enumeration e = clientSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) clientSIs.get(strprid);

                    //  (PRID)
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    COPSClientSI cSi = new COPSClientSI(CSIType.NAMED,
                            new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    COPSClientSI cSi2 = new COPSClientSI(CSIType.NAMED,
                            new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                    msg.add(cSi);
                    msg.add(cSi2);
                }
            }

        } catch (COPSException ex) {
            throw new COPSPepException("Error making Msg");
        }

        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the report, reason: " + e.getMessage());
        }
    }

    public void sendAcctReport(Hashtable clientSIs)
    throws COPSPepException {
        COPSReportMsg msg = new COPSReportMsg();
        // Report SUCESS
        try {
            COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_RPT, _clientType);
            COPSHandle hnd = _handle;

            COPSReportType report = new COPSReportType(ReportType.ACCOUNTING);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);

            if (clientSIs.size() > 0) {
                for (Enumeration e = clientSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) clientSIs.get(strprid);

                    //  (PRID)
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    COPSClientSI cSi = new COPSClientSI(CSIType.NAMED,
                            new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    COPSClientSI cSi2 = new COPSClientSI(CSIType.NAMED,
                            new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

                    msg.add(cSi);
                    msg.add(cSi2);
                }
            }

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
     * Send Sync State Complete to PDP.
     *   The Synchronize State Complete is sent by the PEP to the PDP after
     *   the PDP sends a synchronize state request to the PEP and the PEP has
     *   finished synchronization.
     *
     * @throws   COPSPepException
     *
     */
    public void sendSyncComplete()
    throws COPSPepException {
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
     * Send Delete Request to PDP.
     * When sent from the PEP this message indicates to the remote PDP that
     * the state identified by the client handle is no longer
     * available/relevant.
     *
     * @throws   COPSPepException
     *
     */
    public void sendDeleteRequest()
    throws COPSPepException {
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DRQ, _clientType);
        COPSHandle handle = _handle;

        // *** TODO: send a real reason
        COPSReason reason = new COPSReason(ReasonCode.NA, ReasonCode.NA);

        COPSDeleteMsg msg = new COPSDeleteMsg();
        try {
            msg.add(hdr);
            msg.add(handle);
            msg.add(reason);
        } catch (COPSException ex) {
            throw new COPSPepException("Error making Msg");
        }
        try {
            msg.writeData(_sock);
        } catch (IOException e) {
            throw new COPSPepException("Failed to send the delete request, reason: " + e.getMessage());
        }
    }
}




