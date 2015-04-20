/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpdp;

import org.umu.cops.stack.COPSError;

import java.util.Hashtable;
import java.util.Map;

/**
 * Abstract class for implementing policy data processing classes for provisioning PDPs.
 */
public interface COPSPdpDataProcess {
    /**
     * Gets the policies to be uninstalled
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    public Hashtable getRemovePolicy(COPSPdpReqStateMan man);
    /**
     * Gets the policies to be installed
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    public Hashtable getInstallPolicy(COPSPdpReqStateMan man);
    /**
     * Makes a decision from the supplied request data
     * @param man   The associated request state manager
     * @param reqSIs    Client specific data suppplied in the COPS request
     */
    public void setClientData(COPSPdpReqStateMan man, Map<String, String> reqSIs);
    /**
     * Builds a failure report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    public void failReport(COPSPdpReqStateMan man, Map<String, String> reportSIs);
    /**
     * Builds a success report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    public void successReport(COPSPdpReqStateMan man, Map<String, String> reportSIs);
    /**
     * Builds an accounting report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    public void acctReport(COPSPdpReqStateMan man, Map<String, String> reportSIs);
    /**
     * Notifies that no accounting report has been received
     * @param man   The associated request state manager
     */
    public void notifyNoAcctReport (COPSPdpReqStateMan man);

    /**
     * Notifies a keep-alive timeout
     * @param man   The associated request state manager
     */
    public void notifyNoKAliveReceived (COPSPdpReqStateMan man);

    /**
      * Notifies that the connection has been closed
      * @param man  The associated request state manager
      * @param error Reason
      */
    public void notifyClosedConnection (COPSPdpReqStateMan man, COPSError error);

    /**
     * Notifies that a request state has been deleted
     * @param man   The associated request state manager
     */
    public void notifyDeleteRequestState (COPSPdpReqStateMan man);

    /**
     * Notifies that a request state has been closed
     * @param man   The associated request state manager
     */
    public void closeRequestState(COPSPdpReqStateMan man);
}
