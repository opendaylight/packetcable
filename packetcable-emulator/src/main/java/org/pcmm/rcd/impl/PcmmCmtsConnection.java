/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import static com.google.common.base.Preconditions.checkNotNull;

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

    private static final Logger logger = LoggerFactory.getLogger(COPSPepConnection.class);

    private final CMTSConfig config;

    /**
     * Constructor
     * @param clientType - the client-type
     * @param sock - the socket connection
     * @param config - emulator configuration
     */
    public PcmmCmtsConnection(final short clientType, final Socket sock, final CMTSConfig config) {
        super(clientType, sock);
        this.config = checkNotNull(config);
    }

    @Override
    public COPSPepReqStateMan addRequestState(final COPSHandle clientHandle, final COPSPepDataProcess process)
            throws COPSException {
        final COPSPepReqStateMan manager = new CmtsPepReqStateMan(_clientType, clientHandle, (CmtsDataProcessor)process,
                _sock, config);
        if (_managerMap.get(clientHandle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);

        _managerMap.put(clientHandle, manager);
        logger.info("Added state manager with key - " + clientHandle);
        manager.initRequestState();
        return manager;
    }
}

