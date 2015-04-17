/**
 @header@
 */

package org.pcmm;

import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.impl.PCMMGateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSError;

import java.util.Hashtable;


public class PCMMPdpDataProcess { // extends COPSPdpDataProcess

    public final static Logger logger = LoggerFactory.getLogger(PCMMPdpDataProcess.class);

    private Hashtable installPolicy;
    private Hashtable removePolicy;

    public PCMMPdpDataProcess() {
    }

    /**
     * PDPAgent gets the policies to delete from PEP
     *
     * @param man
     * @return
     */
    public Hashtable getRemovePolicy(PCMMPdpReqStateMan man) {
        return removePolicy;
    }

    /**
     * PDPAgent gets the policies to be installed in PEP
     *
     * @param man
     * @return
     */
    public Hashtable getInstallPolicy(PCMMPdpReqStateMan man) {
        return installPolicy;
    }

    /**
     * PEP configuration items for sending inside the request
     *
     * @param man
     * @param reqSIs
     */
    public void setClientData(PCMMPdpReqStateMan man, Hashtable reqSIs) {

        logger.info("Request Info");
        /*
                for (Enumeration e = reqSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) reqSIs.get(strprid);

                    // Check PRID-EPD
                    // ....
                    System.out.println(getClass().getName() + ": " + "PRID: " + strprid);
                    System.out.println(getClass().getName() + ": " + "EPD: " + strepd);
                }

                // Create policies to be deleted
                // ....

                // Create policies to be installed
                String prid = new String("<XPath>");
                String epd = new String("<?xml this is an XML policy>");
                installPolicy.put(prid, epd);
        */
    }

    /**
     * Fail report received
     *
     * @param man
     * @param gateMsg
     */
    public void failReport(PCMMPdpReqStateMan man, PCMMGateReq gateMsg) {

        logger.info("Fail Report notified with error - " + gateMsg.getError().toString());
        /*

                System.out.println(getClass().getName() + ": " + "Report Info");
                for (Enumeration e = reportSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) reportSIs.get(strprid);

                    // Check PRID-EPD
                    // ....
                    System.out.println(getClass().getName()+ ": " + "PRID: " + strprid);
                    System.out.println(getClass().getName()+ ": " + "EPD: " + strepd);
                }
        */
    }

    /**
     * Positive report received
     *
     * @param man
     * @param gateMsg
     */
    public void successReport(PCMMPdpReqStateMan man, PCMMGateReq gateMsg) {
        logger.info("Success Report notified.");

        if ( gateMsg.getTransactionID().getGateCommandType() == ITransactionID.GateDeleteAck ) {
            logger.info("GateDeleteAck: GateID = " + gateMsg.getGateID().getGateID());
            if (gateMsg.getGateID().getGateID() == PCMMGlobalConfig.getGateID1())
                PCMMGlobalConfig.setGateID1(0);
            if (gateMsg.getGateID().getGateID() == PCMMGlobalConfig.getGateID2())
                PCMMGlobalConfig.setGateID2(0);

        }
        if ( gateMsg.getTransactionID().getGateCommandType() == ITransactionID.GateSetAck ) {
            logger.info("GateSetAck : GateID = " + gateMsg.getGateID().getGateID());
            if (0 == PCMMGlobalConfig.getGateID1())
                PCMMGlobalConfig.setGateID1(gateMsg.getGateID().getGateID());
            if (0 == PCMMGlobalConfig.getGateID2())
                PCMMGlobalConfig.setGateID2(gateMsg.getGateID().getGateID());
        }

        /*
                System.out.println(getClass().getName()+ ": " + "Report Info");
                for (Enumeration e = reportSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) reportSIs.get(strprid);

                    // Check PRID-EPD
                    // ....
                    System.out.println(getClass().getName()+ ": " + "PRID: " + strprid);
                    System.out.println(getClass().getName()+ ": " + "EPD: " + strepd);
                }
        */

    }

    /**
     * Accounting report received
     *
     * @param man
     * @param gateMsg
     */
    public void acctReport(PCMMPdpReqStateMan man, PCMMGateReq gateMsg) {
        logger.info("Acct Report notified.");

        /*
                System.out.println(getClass().getName()+ ": " + "Report Info");
                for (Enumeration e = reportSIs.keys() ; e.hasMoreElements() ;) {
                    String strprid = (String) e.nextElement();
                    String strepd = (String) reportSIs.get(strprid);

                    // Check PRID-EPD
                    // ....
                    System.out.println(getClass().getName()+ ": " + "PRID: " + strprid);
                    System.out.println(getClass().getName()+ ": " + "EPD: " + strepd);
                }
        */
    }

    /**
     * Notifies that an Accounting report is missing
     *
     * @param man
     */
    public void notifyNoAcctReport(PCMMPdpReqStateMan man) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Notifies that a KeepAlive message is missing
     *
     * @param man
     */
    public void notifyNoKAliveReceived(PCMMPdpReqStateMan man) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * PEP closed the connection
     *
     * @param man
     * @param error
     */
    public void notifyClosedConnection(PCMMPdpReqStateMan man, COPSError error) {
        logger.info("Connection was closed by PEP");
    }

    /**
     * Delete request state received
     *
     * @param man
     */
    public void notifyDeleteRequestState(PCMMPdpReqStateMan man) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Closes request state
     *
     * @param man
     */
    public void closeRequestState(PCMMPdpReqStateMan man) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
