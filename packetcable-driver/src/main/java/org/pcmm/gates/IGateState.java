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
 * From the Packetcable Multimedia specification section 6.4.2.15
 *
 *The information in the Gate State object reflects the current state of the Gate. The CMTS MUST include the Gate
 *State object in any unsolicited messages that it sends to the Policy Server. The Policy Server may use this
 *information to report state to the Application Manager, or for enforcing complex rules that might require state
 *knowledge of the Gate.
 */
public interface IGateState extends IPCMMBaseObject {

    /**
     * The S-Type for Gate State
     */
    byte STYPE = 1;

    /**
     * Returns the gate State value
     * @return - the State (2 bytes unsigned integer)
     */
    //short getGateState();
    GateStateType getGateState();
    GateStateReasonType getGateStateReason();

    /**
     * Gate State Types types
     */
    enum GateStateType {

    	Idle_Closed((short) 1),
    	Authorized((short) 2),
    	Reserved((short) 3),
    	Committed((short) 4),
    	Committed_Recovery((short) 5);

        GateStateType(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }

        public static GateStateType valueOf(short v) {
            switch (v) {
                case 1:
                    return GateStateType.Idle_Closed;
                case 2:
                    return GateStateType.Authorized;
                case 3:
                    return GateStateType.Reserved;
                case 4:
                    return GateStateType.Committed;
                case 5:
                    return GateStateType.Committed_Recovery;
                default:
                    throw new IllegalArgumentException("not supported value");
            }
        }
        private short value;
    }

    enum GateStateReasonType {

    	Idle_Closed((short) 1),
    	Other((short) 65535);

        GateStateReasonType(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }

        public static GateStateReasonType valueOf(short v) {
            switch (v) {
                case 1:
                    return GateStateReasonType.Idle_Closed;
                case (short) 65535:
                    return GateStateReasonType.Other;
                default:
                    throw new IllegalArgumentException("not supported value");
            }
        }
        private short value;
    }

}