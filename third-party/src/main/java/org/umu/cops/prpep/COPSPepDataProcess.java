/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import java.util.Hashtable;

import org.umu.cops.stack.COPSError;

/**
 * COPSPepDataProcess process policy data and events.
 *
 * @version COPSPepDataProcess.java, v 2.00 2004
 *
 */
public abstract class COPSPepDataProcess {

    /**
     * Establish PDP decisions
     *
     * @param removeDecs
     * @param installDecs
     * @param errorDecs
     */

	public abstract void setDecisions(COPSPepReqStateMan man, Hashtable removeDecs, Hashtable installDecs, Hashtable errorDecs);

    /**
     *  If the report is fail, return true
     *
     * @return
     */
    public abstract boolean isFailReport(COPSPepReqStateMan man);

    /**
     * Return Report Data
     *
     * @return
     */
    public abstract Hashtable getReportData(COPSPepReqStateMan man);

    /**
     * Return Client Data
     *
     * @return
     */
    public abstract Hashtable getClientData(COPSPepReqStateMan man);

    /**
     * Return Accouting Data
     *
     * @return
     */
    public abstract Hashtable getAcctData(COPSPepReqStateMan man);

    /**
     * Notify the connection closed
     *
     * @param error
     */
    public abstract void notifyClosedConnection (COPSPepReqStateMan man, COPSError error);

    /**
     * Notify the KAlive timeout
     */
    public abstract void notifyNoKAliveReceived (COPSPepReqStateMan man);

    /**
     * Process a PDP request to close a Request State
     *
     * @param man       Request State Manager
     */
    public abstract void closeRequestState(COPSPepReqStateMan man);

    /**
     * Process a PDP request to open a new Request State
     *
     * @param man
     */
    public abstract void newRequestState(COPSPepReqStateMan man);
}

