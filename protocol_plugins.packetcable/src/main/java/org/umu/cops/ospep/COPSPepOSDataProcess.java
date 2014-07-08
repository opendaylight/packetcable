package org.umu.cops.ospep;

import java.util.Vector;

import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSError;

/**
 * Abstract class for implementing policy data processing classes for outsourcing PEPs.
 */
public abstract class COPSPepOSDataProcess {
    /**
     * Applies the decisions from the PDP
     * @param man   The request state manager
     * @param dMsg  The decisions message
     * @return <tt>true</tt> if failed (reports indicate failure), <tt>false</tt> otherwise
     */
    public abstract boolean setDecisions(COPSPepOSReqStateMan man, COPSDecisionMsg dMsg);

    /**
     * Gets the report data
     * @param man   The request state manager
     * @return A <tt>Vector</tt> holding the report data
     */
    public abstract Vector getReportData(COPSPepOSReqStateMan man);

    /**
     * Gets the supplied client data
     * @param man   The request state manager
     * @return A <tt>Vector</tt> holding the client data
     */
    public abstract Vector getClientData(COPSPepOSReqStateMan man);

    /**
     * Gets the account data
     * @param man   The request state manager
     * @return A <tt>Vector</tt> holding the account data
     */
    public abstract Vector getAcctData(COPSPepOSReqStateMan man);

    /**
     * Called when the connection is closed
     * @param man   The request state manager
     * @param error Reason
     */
    public abstract void notifyClosedConnection (COPSPepOSReqStateMan man, COPSError error);

    /**
     * Called when the keep-alive message is not received
     * @param man   The request state manager
     */
    public abstract void notifyNoKAliveReceived (COPSPepOSReqStateMan man);

    /**
     * Process a PDP request to close a Request State
     * @param man   The request state manager
     */
    public abstract void closeRequestState(COPSPepOSReqStateMan man);
}
