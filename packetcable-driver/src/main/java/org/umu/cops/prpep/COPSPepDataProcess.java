/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.umu.cops.COPSDataProcess;

import java.util.Map;

/**
 * COPSPepDataProcess process policy data and events.
 *
 * @version COPSPepDataProcess.java, v 2.00 2004
 *
 */
public interface COPSPepDataProcess extends COPSDataProcess {

    /**
     * Establish PDP decisions
     *
     * @param man - the state manager
     * @param removeDecs - the remove decisions
     * @param installDecs - the install decisions
     * @param errorDecs - the error decisions
     */

	void setDecisions(COPSPepReqStateMan man, Map<String, String> removeDecs,
                                      Map<String, String> installDecs, Map<String, String> errorDecs);

    /**
     *  If the report is fail, return true
     * @return - t/f
     */
    boolean isFailReport(COPSPepReqStateMan man);

    /**
     * Return Report Data
     * @return - the report data
     */
    Map<String, String> getReportData(COPSPepReqStateMan man);

    /**
     * Return Client Data
     * @return - the client data
     */
    Map<String, String> getClientData(COPSPepReqStateMan man);

    /**
     * Return Accounting Data
     * @return - the accounting data
     */
    Map<String, String> getAcctData(COPSPepReqStateMan man);

    /**
     * Process a PDP request to open a new Request State
     *
     * @param man       Request State Manager
     */
    void newRequestState(COPSPepReqStateMan man);
}

