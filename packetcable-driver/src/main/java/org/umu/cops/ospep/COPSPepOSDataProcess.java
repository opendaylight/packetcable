package org.umu.cops.ospep;

import org.umu.cops.COPSDataProcess;
import org.umu.cops.stack.COPSDecisionMsg;

import java.util.Vector;

/**
 * Abstract class for implementing policy data processing classes for outsourcing PEPs.
 */
public interface COPSPepOSDataProcess extends COPSDataProcess {
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
     * @return A <tt>Vector</tt> holding the report data
     */
    Vector getReportData(COPSPepOSReqStateMan man);

    /**
     * Gets the supplied client data
     * @param man   The request state manager
     * @return A <tt>Vector</tt> holding the client data
     */
    Vector getClientData(COPSPepOSReqStateMan man);

    /**
     * Gets the account data
     * @param man   The request state manager
     * @return A <tt>Vector</tt> holding the account data
     */
    Vector getAcctData(COPSPepOSReqStateMan man);

}
