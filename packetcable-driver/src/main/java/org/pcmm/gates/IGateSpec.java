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
 * GateSpec describes specific authorization parameters defining a Gate (i.e., QoS limits, timers, etc.).
 *
 * From the Packetcable Multimedia specification section 6.1.4
 *
 * The GateSpec describes some high-level attributes of the Gate, and contains information regarding the treatment of
 * other objects specified in the Gate message. Information contained in a GateSpec is outlined below.
 *
 * * SessionClassID
 * * Direction
 * * AUTHORIZED Timer
 * * RESERVED Timer
 * * Committed Timer
 * * Committed Recovery Timer
 * * DSCP/TOS Overwrite
 * * DSCP/TOS Mask
 *
 * SessionClassID provides a way for the Application Manager and the Policy Server to group Gates into different
 * classes with different authorization characteristics. For example, one could use the SessionClassID to represent
 * some prioritization or preemption scheme that would allow either the Policy Server or the CMTS to preempt a preauthorized
 * Gate in favor of allowing a new Gate with a higher priority to be authorized.
 *
 * Direction indicates whether the Gate is for an upstream or downstream flow. Depending on this direction, the CMTS
 * MUST reserve and activate the DOCSIS flows accordingly. For Multicast Gates the CMTS needs to only support
 * flows or gates in the downstream direction.
 *
 * AUTHORIZED Timer limits the amount of time the authorization must remain valid before it is reserved (see
 * Section 6.2).
 *
 * RESERVED Timer limits the amount of time the reservation must remain valid before the resources are committed (see
 * Section 6.2).
 *
 * Committed Timer limits the amount of time a committed service flow may remain idle.
 *
 * Committed Recovery Timer limits the amount of time that a committed service flow can remain without a
 * subsequent refresh message from the PS/AM once the PS/AM has been notified of inactivity (see Section 6.2).
 * DSCP/TOS Overwrite field can be used to overwrite the DSCP/TOS field of packets associated with the DOCSIS
 * Service Flow that corresponds to the Gate. This field may be unspecified in which case the DSCP/TOS field in the
 * packet is not over-written by the CMTS. This field may be used in both the upstream and downstream directions.
 * The CMTS MUST support DSCP/TOS overwrite for upstream gates. The CMTS MAY support DSCP/TOS
 * overwrite for downstream gates. If DSCP/TOS is enabled in a downstream gate and the CMTS does not support that
 * function, then the field is ignored. The manner in which the CMTS interprets the DSCP/TOS Overwrite & Mask
 * fields and transforms it into the corresponding DOCSIS Service Flow Parameters is defined in Section 6.4.2.5.
 */
public interface IGateSpec extends IPCMMBaseObject {

    /**
     * The S-Type for Gate Specifications
     */
    byte STYPE = 1;

    /**
     * <p>
     * Direction indicates whether the Gate is for an upstream or downstream
     * flow. Depending on this direction, the CMTS MUST reserve and activate the
     * DOCSIS flows accordingly. For Multicast Gates the CMTS needs to only
     * support flows or gates in the downstream direction.
     * </p>
     *
     *
     */
    enum Direction {

        UPSTREAM((byte) 1), DOWNSTREAM((byte) 0);

        Direction(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        @Override
        public String toString() {
            switch (value) {
            case 1:
                return "Upstream";
            default:
                return "Downstream";
            }
        }

        private byte value;

        public static Direction valueOf(byte v) {
            switch (v) {
            case 0:
                return Direction.DOWNSTREAM;
            case 1:
                return Direction.UPSTREAM;
            default:
                throw new IllegalArgumentException("not supported value");
            }
        }

    }

    /**
     * <p>
     * provides a way for the Application Manager and the Policy Server to group
     * Gates into different classes with different authorization
     * characteristics. For example, one could use the SessionClassID to
     * represent some prioritization or preemption scheme that would allow
     * either the Policy Server or the CMTS to preempt a pre-authorized Gate in
     * favor of allowing a new Gate with a higher priority to be authorized.
     * </p>
     *
     * @return session class ID;
     */
    ISessionClassID getSessionClassID();

    /**
     *
     * @return direction.
     */
    Direction getDirection();

    /**
     * AUTHORIZED Timer limits the amount of time the authorization must remain
     * valid before it is reserved
     *
     * @return time in ms;
     */
    short getTimerT1();

    /**
     * RESERVED Timer limits the amount of time the reservation must remain
     * valid before the resources are committed
     *
     * @return time in ms;
     */
    short getTimerT2();

    /**
     * Committed Timer limits the amount of time a committed service flow may
     * remain idle.
     *
     * @return time in ms;
     */
    short getTimerT3();

    /**
     * Committed Recovery Timer limits the amount of time that a committed
     * service flow can remain without a subsequent refresh message from the
     * PS/AM once the PS/AM has been notified of inactivity
     *
     * @return time in ms;
     */
    short getTimerT4();

    /**
     *
     * @return DSCP/TOS
     */
    byte getDSCP_TOSOverwrite();

    /**
     *
     * @return - the mask
     */
    byte getDSCP_TOSMask();

}
