package org.umu.cops.ospdp;

import java.util.Vector;

import org.umu.cops.stack.COPSError;

/**
 * Abstract class for implementing policy data processing classes for outsourcing PDPs.
 */
abstract public class COPSPdpOSDataProcess {
    /**
     * Gets the policies to be uninstalled
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    abstract public Vector getRemovePolicy(COPSPdpOSReqStateMan man);
    /**
     * Gets the policies to be installed
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    abstract public Vector getInstallPolicy(COPSPdpOSReqStateMan man);
    /**
     * Makes a decision from the supplied request data
     * @param man   The associated request state manager
     * @param reqSIs    Client specific data suppplied in the COPS request
     */
    abstract public void setClientData(COPSPdpOSReqStateMan man, Vector reqSIs);
    /**
     * Builds a failure report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    abstract public void failReport (COPSPdpOSReqStateMan man, Vector reportSIs);
    /**
     * Builds a success report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    abstract public void successReport (COPSPdpOSReqStateMan man, Vector reportSIs);
    /**
     * Builds an accounting report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    abstract public void acctReport (COPSPdpOSReqStateMan man, Vector reportSIs);
    /**
     * Notifies that no accounting report has been received
     * @param man   The associated request state manager
     */
    public abstract void notifyNoAcctReport (COPSPdpOSReqStateMan man);

    /**
     * Notifies a keep-alive timeout
     * @param man   The associated request state manager
     */
    public abstract void notifyNoKAliveReceived (COPSPdpOSReqStateMan man);

    /**
      * Notifies that the connection has been closed
      * @param man  The associated request state manager
      * @param error Reason
      */
    public abstract void notifyClosedConnection (COPSPdpOSReqStateMan man, COPSError error);

    /**
     * Notifies that a request state has been deleted
     * @param man   The associated request state manager
     */
    public abstract void notifyDeleteRequestState (COPSPdpOSReqStateMan man);

    /**
     * Notifies that a request state has been closed
     * @param man   The associated request state manager
     */
    public abstract void closeRequestState(COPSPdpOSReqStateMan man);

}
