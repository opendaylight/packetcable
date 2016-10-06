/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IGateSpec;
import org.pcmm.gates.ISessionClassID;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the IGateSpec interface
 */
public class GateSpec extends PCMMBaseObject implements IGateSpec {

    /**
     * The gate's direction
     */
    private Direction direction;

    /**
     * The DSCP/TOS Overwrite is a 1-byte bit field [8] defined by the following alternative structures, depending upon
     network management strategy.
     *
     * When enabled, the CMTS must mark the packets traversing the CMTS DSCP/TOS value
     */
    private final byte tosOverwrite;

    /**
     * tosOverwrite field with the TOS mask is used to identify particular bits within the IPv4 DSCP/TOS byte
     */
    private final byte tosMask;

    /**
     * Session Class ID identifies the proper admission control policy or parameters to be applied for this gate
     */
    private final SessionClassID sessionClassID;

    /**
     * Timer value in seconds. Value of 0 indicates that the CMTS provisioned value for the timer MUST be used.
     */
    private final short timer1;

    /**
     * DOCSIS Admitted timer in seconds
     */
    private final short timer2;

    /**
     * DOCSIS Active timer in seconds
     */
    private final short timer3;

    /**
     * The fourth timer value in seconds
     */
    private final short timer4;

    /**
     * General constructor
     * @param direction - the gate direction
     * @param tosOverwrite - ENABLE/DISABLE
     * @param tosMask - the mask
     */
    public GateSpec(final Direction direction, final byte tosOverwrite, final byte tosMask) {
        this(direction, tosOverwrite, tosMask, new SessionClassID((byte) 0), (short) 0, (short) 0, (short) 0, (short) 0);
    }

    /**
     * Constructor generally for use when parsing a byte array to create an instance of this object.
     * @param direction - the gate direction
     * @param tosOverwrite - ENABLE/DISABLE
     * @param tosMask - the mask
     * @param sessionClassID - the session class ID
     * @param timer1 - timer1 in seconds
     * @param timer2 - timer2 in seconds
     * @param timer3 - timer3 in seconds
     * @param timer4 - timer4 in seconds
     */
    protected GateSpec(final Direction direction, final byte tosOverwrite, final byte tosMask,
                    final SessionClassID sessionClassID, final short timer1, final short timer2, final short timer3,
                    final short timer4) {

        super(SNum.GATE_SPEC, STYPE);

        if (direction == null) throw new IllegalArgumentException("Direction is required");
//        if (tosOverwrite == null) throw new IllegalArgumentException("TOS Overwrite is required");
        if (sessionClassID == null) throw new IllegalArgumentException("Session class ID is required");

        this.direction = direction;
        this.tosOverwrite = tosOverwrite;
        this.tosMask = tosMask;
        this.sessionClassID = sessionClassID;
        this.timer1 = timer1;
        this.timer2 = timer2;
        this.timer3 = timer3;
        this.timer4 = timer4;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public byte getDSCP_TOSOverwrite() {
        return tosOverwrite;
    }

    @Override
    public byte getDSCP_TOSMask() {
        return tosMask;
    }

    @Override
    public ISessionClassID getSessionClassID() {
        return sessionClassID;
    }

    @Override
    public short getTimerT1() {
        return timer1;
    }

    @Override
    public short getTimerT2() {
        return timer2;
    }

    @Override
    public short getTimerT3() {
        return timer3;
    }

    @Override
    public short getTimerT4() {
        return timer4;
    }

    @Override
    protected byte[] getBytes() {
        final byte[] data = new byte[12];
        data[0] = direction.getValue();
        data[1] = tosOverwrite;
        data[2] = tosMask;
        data[3] = sessionClassID.toSingleByte();

        final byte[] timer1Bytes = COPSMsgParser.shortToBytes(timer1);
        data[4] = timer1Bytes[0];
        data[5] = timer1Bytes[1];

        final byte[] timer2Bytes = COPSMsgParser.shortToBytes(timer2);
        data[6] = timer2Bytes[0];
        data[7] = timer2Bytes[1];

        final byte[] timer3Bytes = COPSMsgParser.shortToBytes(timer3);
        data[8] = timer3Bytes[0];
        data[9] = timer3Bytes[1];

        final byte[] timer4Bytes = COPSMsgParser.shortToBytes(timer4);
        data[10] = timer4Bytes[0];
        data[11] = timer4Bytes[1];

        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GateSpec)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final GateSpec gateSpec = (GateSpec) o;
        return tosMask == gateSpec.tosMask && timer1 == gateSpec.timer1 && timer2 == gateSpec.timer2 &&
                timer3 == gateSpec.timer3 && timer4 == gateSpec.timer4 && direction == gateSpec.direction &&
                tosOverwrite == gateSpec.tosOverwrite && sessionClassID.equals(gateSpec.sessionClassID);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + direction.hashCode();
        result = 31 * result + (int) tosOverwrite;
        result = 31 * result + (int) tosMask;
        result = 31 * result + sessionClassID.hashCode();
        result = 31 * result + (int) timer1;
        result = 31 * result + (int) timer2;
        result = 31 * result + (int) timer3;
        result = 31 * result + (int) timer4;
        return result;
    }

    /**
     * Returns a GateSpec object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static GateSpec parse(final byte[] data) {
        return new GateSpec(Direction.valueOf(data[0]), data[1], data[2],
                new SessionClassID(data[3]), COPSMsgParser.bytesToShort(data[4], data[5]),
                COPSMsgParser.bytesToShort(data[6], data[7]), COPSMsgParser.bytesToShort(data[8], data[9]),
                COPSMsgParser.bytesToShort(data[10], data[11]));
    }
}
