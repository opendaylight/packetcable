/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDeleteMsg;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSPrEPD;
import org.umu.cops.stack.COPSPrID;
import org.umu.cops.stack.COPSReason;
import org.umu.cops.stack.COPSReportMsg;
import org.umu.cops.stack.COPSReportType;
import org.umu.cops.stack.COPSReqMsg;
import org.umu.cops.stack.COPSSyncStateMsg;

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
        COPSContext cntxt = new COPSContext(COPSContext.CONFIG , (short) 0);

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
                    COPSClientSI cSi = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    cSi.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSClientSI cSi2 = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    cSi2.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

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

            COPSReportType report = new COPSReportType(COPSReportType.FAILURE);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);
            if (clientSIs.size() > 0) {
                for (Enumeration e = clientSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) clientSIs.get(strprid);

                    //  (PRID)
                    COPSClientSI cSi = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    cSi.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSClientSI cSi2 = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    cSi2.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

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

            COPSReportType report = new COPSReportType(COPSReportType.SUCCESS);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);

            if (clientSIs.size() > 0) {
                for (Enumeration e = clientSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) clientSIs.get(strprid);

                    //  (PRID)
                    COPSClientSI cSi = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    cSi.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSClientSI cSi2 = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    cSi2.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

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

            COPSReportType report = new COPSReportType(COPSReportType.ACCT);

            msg.add(hdr);
            msg.add(hnd);
            msg.add(report);

            if (clientSIs.size() > 0) {
                for (Enumeration e = clientSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) clientSIs.get(strprid);

                    //  (PRID)
                    COPSClientSI cSi = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrID prid = new COPSPrID();
                    prid.setData(new COPSData(strprid));
                    cSi.setData(new COPSData(prid.getDataRep(), 0, prid.getDataLength()));

                    //  (EPD)
                    COPSClientSI cSi2 = new COPSClientSI(COPSClientSI.CSI_NAMED);
                    COPSPrEPD epd = new COPSPrEPD();
                    epd.setData(new COPSData(strepd));
                    cSi2.setData(new COPSData(epd.getDataRep(), 0, epd.getDataLength()));

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
        COPSReason reason = new COPSReason((short) 234, (short) 345);

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




