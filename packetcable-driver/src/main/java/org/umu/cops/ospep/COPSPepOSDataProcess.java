package org.umu.cops.ospep;

import org.umu.cops.prpep.COPSPepDataProcess;
import org.umu.cops.stack.COPSDecisionMsg;

import java.util.List;

/**
 * Interface for implementing policy data processing classes for outsourcing PEPs.
 */
public interface COPSPepOSDataProcess extends COPSPepDataProcess {

    /**
     * Applies the decisions from the PDP
     * @param man   The request state manager
     * @param dMsg  The decisions message
     * @return <tt>true</tt> if failed (reports indicate failure), <tt>false</tt> otherwise
     */
    boolean setDecisions(COPSPepOSReqStateMan man, COPSDecisionMsg dMsg);

    /**
     * Gets the report data
     * @param man   The request state manager
     * @return A <tt>List</tt> holding the report data (should be List<COPSClientSI>)
     */
    List getReportData(COPSPepOSReqStateMan man);

    /**
     * Gets the supplied client data
     * @param man   The request state manager
     * @return A <tt>List</tt> holding the report data (should be List<COPSClientSI>)
     */
    List getClientData(COPSPepOSReqStateMan man);

    /**
     * Gets the account data
     * @param man   The request state manager
     * @return A <tt>List</tt> holding the report data (should be List<COPSClientSI>)
     */
    List getAcctData(COPSPepOSReqStateMan man);

}
