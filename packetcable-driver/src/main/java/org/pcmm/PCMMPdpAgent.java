/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpAgent;
import org.umu.cops.prpdp.COPSPdpConnection;
import org.umu.cops.stack.COPSHandle;

import java.net.Socket;

/**
 * Core PDP agent for provisioning
 */
public class PCMMPdpAgent extends COPSPdpAgent {

    private static final Logger logger = LoggerFactory.getLogger(PCMMPdpAgent.class);

    /** Well-known port for PCMM */
    public static final int WELL_KNOWN_PDP_PORT = 3918;

    /**
     * Policy data processing object
     */
    private final PCMMPdpDataProcess _thisProcess;

    /**
     * Creates a PDP Agent
     *
     * @param clientType - COPS Client-type
     * @param psHost - Host to connect to
     * @param psPort - Port to connect to
     * @param process - Object to perform policy data processing
     */
    public PCMMPdpAgent(final String psHost, final int psPort, final short clientType,
                        final PCMMPdpDataProcess process) {
        super(psHost, psPort, clientType, process);
        _thisProcess = process;
    }

    @Override
    protected COPSPdpConnection setputPdpConnection(final Socket conn, final COPSHandle handle) {
        logger.debug("PDPCOPSConnection");
        final PCMMPdpConnection pdpConn = new PCMMPdpConnection(_pepId, conn, _thisProcess, _kaTimer, _acctTimer);
        final PCMMPdpReqStateMan man = new PCMMPdpReqStateMan(_clientType, handle, _thisProcess, conn);
        pdpConn.addStateMan(handle, man);
        // XXX - End handleRequestMsg

        logger.info("Starting PDP connection thread to - " + _host);
        return pdpConn;
    }

}

