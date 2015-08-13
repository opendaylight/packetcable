/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.rcd.impl;

import org.pcmm.gates.IGateSpec.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpep.COPSPepConnection;
import org.umu.cops.prpep.COPSPepDataProcess;
import org.umu.cops.prpep.COPSPepException;
import org.umu.cops.prpep.COPSPepReqStateMan;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;

import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The connection object for the PCMM CMTS
 */
class PcmmCmtsConnection extends COPSPepConnection {

    private final static Logger logger = LoggerFactory.getLogger(COPSPepConnection.class);

    /**
     * The configured gates
     */
    private final Map<Direction, Set<String>> gateConfig;

    /**
     * The connected CMTSs and whether or not they are up
     */
    private final Map<String, Boolean> cmStatus;

    /**
     * Constructor
     * @param clientType - the client-type
     * @param sock - the socket connection
     * @param gateConfig - the configured gates
     * @param cmStatus - the configured CMs and whether or each is connected
     */
    public PcmmCmtsConnection(final short clientType, final Socket sock, final Map<Direction, Set<String>> gateConfig,
                              final Map<String, Boolean> cmStatus) {
        super(clientType, sock);
        this.gateConfig = Collections.unmodifiableMap(gateConfig);
        this.cmStatus = Collections.unmodifiableMap(cmStatus);
    }

    @Override
    public COPSPepReqStateMan addRequestState(final COPSHandle clientHandle, final COPSPepDataProcess process)
            throws COPSException {
        final COPSPepReqStateMan manager = new CmtsPepReqStateMan(_clientType, clientHandle, (CmtsDataProcessor)process,
                _sock, gateConfig, cmStatus);
        if (_managerMap.get(clientHandle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);

        _managerMap.put(clientHandle, manager);
        logger.info("Added state manager with key - " + clientHandle);
        manager.initRequestState();
        return manager;
    }
}

