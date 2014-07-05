/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import java.util.Hashtable;

import org.umu.cops.stack.COPSError;

/**
 * Abstract class for implementing policy data processing classes for provisioning PDPs.
 */
abstract public class COPSPdpDataProcess {
    /**
     * Gets the policies to be uninstalled
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    abstract public Hashtable getRemovePolicy(COPSPdpReqStateMan man);
    /**
     * Gets the policies to be installed
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    abstract public Hashtable getInstallPolicy(COPSPdpReqStateMan man);
    /**
     * Makes a decision from the supplied request data
     * @param man   The associated request state manager
     * @param reqSIs    Client specific data suppplied in the COPS request
     */
    abstract public void setClientData(COPSPdpReqStateMan man, Hashtable reqSIs);
    /**
     * Builds a failure report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    abstract public void failReport (COPSPdpReqStateMan man, Hashtable reportSIs);
    /**
     * Builds a success report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    abstract public void successReport (COPSPdpReqStateMan man, Hashtable reportSIs);
    /**
     * Builds an accounting report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    abstract public void acctReport (COPSPdpReqStateMan man, Hashtable reportSIs);
    /**
     * Notifies that no accounting report has been received
     * @param man   The associated request state manager
     */
    public abstract void notifyNoAcctReport (COPSPdpReqStateMan man);

    /**
     * Notifies a keep-alive timeout
     * @param man   The associated request state manager
     */
    public abstract void notifyNoKAliveReceived (COPSPdpReqStateMan man);

    /**
      * Notifies that the connection has been closed
      * @param man  The associated request state manager
      * @param error Reason
      */
    public abstract void notifyClosedConnection (COPSPdpReqStateMan man, COPSError error);

    /**
     * Notifies that a request state has been deleted
     * @param man   The associated request state manager
     */
    public abstract void notifyDeleteRequestState (COPSPdpReqStateMan man);

    /**
     * Notifies that a request state has been closed
     * @param man   The associated request state manager
     */
    public abstract void closeRequestState(COPSPdpReqStateMan man);
}
