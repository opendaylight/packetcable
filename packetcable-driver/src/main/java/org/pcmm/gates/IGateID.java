/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 * GateID is the handle for the Gate. The GateID is assigned by the CMTS and is used by the Application Manager,
 * Policy Server, and client to reference the Gate.
 *
 * From the Packetcable Multimedia specification section 6.1.1
 *
 * A GateID is an identifier that is locally allocated by the CMTS where the Gate resides. A GateID MUST be
 * associated with only one Gate. Whereas the PacketCable 1.x DQoS Gate Control model generally assumed a pair of
 * unidirectional Gates (one upstream and one downstream) per GateID in support of a typical two-way voice session,
 * here the Gate/GateID relationship is explicitly one-to-one, so that it is easier to support a wide range of Multimedia
 * services.
 *
 * When the Application Manager issues a Gate-Set request, this triggers the Policy Server to issue a Gate-Set message
 * to the CMTS. When the CMTS responds with an acknowledgment containing the GateID, the Policy Server
 * forwards this response including the GateID back to the Application Manager. Note that since there can be a manyto-
 * many relationship between a PS and CMTS, the GateID assigned by one CMTS cannot be guaranteed to be
 * unique across the network, so the PSs may use the AMID of the AM along with the SubscriberID and GateID in
 * order to uniquely identify the Gate.
 *
 * An algorithm that may be used to assign values of GateIDs is as follows. Partition the 32-bit word into two parts, an
 * index part, and a random part. The index part identifies the Gate by indexing into a small table, while the random
 * part provides some level of obscurity to the value. Regardless of the algorithm chosen, the CMTS SHOULD attempt
 * to minimize the possibility of GateID ambiguities by ensuring that no GateID gets reused within three minutes of its
 * prior closure or deletion. For the algorithm suggested this could be accomplished by simply incrementing the index
 * part for each consecutively assigned GateID, wrapping around to zero when the maximum integer value of the index
 * part is reached.
 */
public interface IGateID extends IPCMMBaseObject {

    /**
     * The S-Type for Gate IDs
     */
    byte STYPE = 1;

    /**
     * Returns the gate ID value
     * @return - the ID
     */
    int getGateID();

}
