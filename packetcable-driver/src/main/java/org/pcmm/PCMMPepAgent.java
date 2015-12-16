/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm;

import org.umu.cops.prpep.COPSPepAgent;
import org.umu.cops.stack.COPSPepId;

/**
 * This is a provisioning COPS PEP. Responsible for making connection to the PDP
 * and maintaining it.
 *
 * TODO - implement me
 */
public class PCMMPepAgent extends COPSPepAgent {

    /**
     * Creates a PEP agent
     * @param    clientType         Client-type
     * @param    pepID              PEP-ID
     * @param    port               the server socket port to open on this host
     */
    public PCMMPepAgent(final short clientType, final COPSPepId pepID, final int port) {
        super(clientType, pepID, port);
    }

}
