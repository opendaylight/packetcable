/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import org.pcmm.base.IPCMMBaseObject;

/**
 * From the Packetcable Multimedia specification section 6.4.2.15
 * <p>
 * The information in the Gate State object reflects the current state of the Gate. The CMTS MUST include the Gate
 * State object in any unsolicited messages that it sends to the Policy Server. The Policy Server may use this
 * information to report state to the Application Manager, or for enforcing complex rules that might require state
 * knowledge of the Gate.
 */
public interface IGateState extends IPCMMBaseObject {

    static final Converter<String, String> converter = CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL);
    /**
     * The S-Type for Gate State
     */
    byte STYPE = 1;

    /**
     * Returns the gate State value
     *
     * @return - the State (2 bytes unsigned integer)
     */
    //short getGateState();
    GateStateType getGateState();

    GateStateReasonType getGateStateReason();

    /**
     * Gate State Types types
     */
    enum GateStateType {

        IDLE_CLOSED((short) 1),
        AUTHORIZED((short) 2),
        RESERVED((short) 3),
        COMMITTED((short) 4),
        COMMITTED_RECOVERY((short) 5);

        GateStateType(short value) {
            this.value = value;
            this.displayName = converter.convert(name());
        }

        public short getValue() {
            return value;
        }

        public static GateStateType valueOf(short v) {
            switch (v) {
                case 1:
                    return GateStateType.IDLE_CLOSED;
                case 2:
                    return GateStateType.AUTHORIZED;
                case 3:
                    return GateStateType.RESERVED;
                case 4:
                    return GateStateType.COMMITTED;
                case 5:
                    return GateStateType.COMMITTED_RECOVERY;
                default:
                    throw new IllegalArgumentException("not supported value");
            }
        }

        private final short value;
        private final String displayName;

        public String toString() {
            return displayName + "(" + getValue() + ")";
        }
    }


    /**
     * From PCMM Spec section 6.4.2.15
     * Reason is a 2-byte unsigned integer field which MUST indicate one of the following reasons for this update:<pre>
     * 1 = Close initiated by CMTS due to reservation reassignment
     * 2 = Close initiated by CMTS due to lack of DOCSIS MAC-layer responses
     * 3 = Close initiated by CMTS due to timer T1 expiration
     * 4 = Close initiated by CMTS due to timer T2 expiration
     * 5 = Inactivity timer expired due to Service Flow inactivity (timer T3 expiration)
     * 6 = Close initiated by CMTS due to lack of Reservation Maintenance
     * 7 = Gate state unchanged, but volume limit reached
     * 8 = Close initiated by CMTS due to timer T4 expiration
     * 9 = Gate state unchanged, but timer T2 expiration caused reservation reduction
     * 10 = Gate state unchanged, but time limit reached
     * 11 = Close initiated by Policy Server or CMTS, volume limit reached
     * 12 = Close initiated by Policy Server or CMTS, time limit reached
     * 13 = Close initiated by CMTS, other
     * 14 = Gate state unchanged, but SharedResourceID updated
     * 15 = Close initiated by CMTS due to loss of shared resource
     * 65535 = Other
     * </pre>
     */
    enum GateStateReasonType {
        ZERO((short)0),
        RESERVATION_REASSIGNMENT((short) 1),
        LACK_OF_DOCSIS_MAC_LAYER_RESPONSES((short) 2),
        T1_EXPIRATION((short)3),
        T2_EXPIRATION((short)4),
        T3_EXPIRATION((short)5),
        LACK_OF_RESERVATION_MAINTENANCE((short)6),
        UNCHANGED_BUT_VOLUME_LIMIT_REACHED((short)7),
        T4_EXPIRATION((short)8),
        UNCHANGED_BUT_T2_EXPIRATION_CAUSED_RESERVATION_REDUCTION((short)9),
        UNCHANGED_BUT_TIME_LIMIT_REACHED((short)10),
        VOLUME_LIMIT_REACHED((short)11),
        TIME_LIMIT_REACHED((short)12),
        CMTS_OTHER((short)13),
        UNCHANGED_BUT_SHARED_RESOURCE_ID_CHANGED((short)14),
        LOSS_OF_SHARED_RESOURCE((short)15),
        OTHER((short) 65535);

        GateStateReasonType(short value) {
            this.value = value;
            this.displayName = converter.convert(name());
        }

        public short getValue() {
            return value;
        }

        public static GateStateReasonType valueOf(short v) {
            for (GateStateReasonType type : GateStateReasonType.values()) {
                if (type.getValue() == v) {
                    return type;
                }
            }
            throw new IllegalArgumentException("not supported value: " + v);
        }

        private final short value;
        private final String displayName;

        public String toString() {
            return displayName + "(" + getValue() + ")";
        }
    }

}
