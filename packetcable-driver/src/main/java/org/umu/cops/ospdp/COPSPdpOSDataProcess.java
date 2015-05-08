package org.umu.cops.ospdp;

import org.umu.cops.COPSDataProcess;
import org.umu.cops.stack.COPSClientSI;

import java.util.Vector;

/**
 * Abstract class for implementing policy data processing classes for outsourcing PDPs.
 */
public interface COPSPdpOSDataProcess extends COPSDataProcess {

    /**
     * Gets the policies to be uninstalled
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    Vector getRemovePolicy(COPSPdpOSReqStateMan man);

    /**
     * Gets the policies to be installed
     * @param man   The associated request state manager
     * @return A <tt>Vector</tt> holding the policies to be uninstalled
     */
    Vector getInstallPolicy(COPSPdpOSReqStateMan man);

    /**
     * Makes a decision from the supplied request data
     * @param man   The associated request state manager
     * @param reqSIs    Client specific data suppplied in the COPS request
     */
    void setClientData(COPSPdpOSReqStateMan man, COPSClientSI... reqSIs);

    /**
     * Builds a failure report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    void failReport(COPSPdpOSReqStateMan man, COPSClientSI... reportSIs);

    /**
     * Builds a success report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    void successReport(COPSPdpOSReqStateMan man, COPSClientSI... reportSIs);

    /**
     * Builds an accounting report
     * @param man   The associated request state manager
     * @param reportSIs Report data
     */
    void acctReport(COPSPdpOSReqStateMan man, COPSClientSI... reportSIs);

    /**
     * Notifies that no accounting report has been received
     * @param man   The associated request state manager
     */
    void notifyNoAcctReport(COPSPdpOSReqStateMan man);

    /**
     * Notifies that a request state has been deleted
     * @param man   The associated request state manager
     */
    void notifyDeleteRequestState(COPSPdpOSReqStateMan man);

}
