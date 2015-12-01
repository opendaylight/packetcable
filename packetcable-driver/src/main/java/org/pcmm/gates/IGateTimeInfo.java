/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 *
 * From the Packetcable Multimedia specification section 6.4.2.12
 *
 *The Gate Time Info object contains the total amount of time the Gate has been in the Committed and
 * Committed Recovery states. This counter MUST be stopped upon the Gate transitioning out of the
 * Committed or Committed Recovery states to either the RESERVED state or AUTHORIZED state. If the
 * Gate subsequently transitions back to the Committed state, this counter MUST be restarted where
 * it last stopped, i.e., when transitioning out of the Committed or Committed Recovery states.
 */
public interface IGateTimeInfo extends IPCMMBaseObject {

    /**
     * The S-Type for Gate Time Info
     */
    byte STYPE = 1;


    /**
     * Time Committed total amount of time the Gate has been in the Committed and Committed
     * Recovery states
     *
     * @return time in seconds;
     */
    int getGateTimeInfo();
}
