package org.umu.cops.ospdp;

import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSError;

import java.util.Vector;

/**
 * Abstract class for implementing policy data processing classes for outsourcing PDPs.
 */
public interface COPSPdpOSDataProcess {
    /**
     * Gets the policies to be uninstalled
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    public Vector getRemovePolicy(COPSPdpOSReqStateMan man);
    /**
     * Gets the policies to be installed
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    public Vector getInstallPolicy(COPSPdpOSReqStateMan man);
    /**
     * Makes a decision from the supplied request data
     * @param man   The associated request state manager
     * @param reqSIs    Client specific data suppplied in the COPS request
     */
    public void setClientData(COPSPdpOSReqStateMan man, COPSClientSI... reqSIs);
    /**
     * Builds a failure report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    public void failReport (COPSPdpOSReqStateMan man, COPSClientSI... reportSIs);
    /**
     * Builds a success report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    public void successReport (COPSPdpOSReqStateMan man, COPSClientSI... reportSIs);
    /**
     * Builds an accounting report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    public void acctReport (COPSPdpOSReqStateMan man, COPSClientSI... reportSIs);
    /**
     * Notifies that no accounting report has been received
     * @param man   The associated request state manager
     */
    abstract void notifyNoAcctReport (COPSPdpOSReqStateMan man);

    /**
     * Notifies a keep-alive timeout
     * @param man   The associated request state manager
     */
    abstract void notifyNoKAliveReceived (COPSPdpOSReqStateMan man);

    /**
      * Notifies that the connection has been closed
      * @param man  The associated request state manager
      * @param error Reason
      */
    abstract void notifyClosedConnection (COPSPdpOSReqStateMan man, COPSError error);

    /**
     * Notifies that a request state has been deleted
     * @param man   The associated request state manager
     */
    abstract void notifyDeleteRequestState (COPSPdpOSReqStateMan man);

    /**
     * Notifies that a request state has been closed
     * @param man   The associated request state manager
     */
    abstract void closeRequestState(COPSPdpOSReqStateMan man);

}
