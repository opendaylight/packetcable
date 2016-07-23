/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.pcmm.gates.impl;

import org.junit.Assert;
import org.junit.Test;
import org.pcmm.gates.IGateSpec.Direction;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Tests the data holder class GateSpec to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class GateSpecTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullDirection() {
        new GateSpec(null, (byte)1, (byte)1, new SessionClassID((byte)1),
                (short)1, (short)2, (short)3, (short)4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSessionClassID() {
        new GateSpec(Direction.UPSTREAM, (byte)1, (byte)1, null,
                (short)1, (short)2, (short)3, (short)4);
    }

    @Test
    public void construction() {
        final GateSpec spec = new GateSpec(Direction.UPSTREAM, (byte)1, (byte)1, new SessionClassID((byte)1),
                (short)1, (short)2, (short)3, (short)4);
        Assert.assertEquals(Direction.UPSTREAM, spec.getDirection());
        Assert.assertEquals((byte)1, spec.getDSCP_TOSOverwrite());
        Assert.assertEquals((byte)1, spec.getDSCP_TOSMask());
        Assert.assertEquals(new SessionClassID((byte) 1), spec.getSessionClassID());
        Assert.assertEquals((short)1, spec.getTimerT1());
        Assert.assertEquals((short)2, spec.getTimerT2());
        Assert.assertEquals((short)3, spec.getTimerT3());
        Assert.assertEquals((short)4, spec.getTimerT4());

        final byte[] data = spec.getBytes();

        Assert.assertEquals(Direction.UPSTREAM, Direction.valueOf(data[0]));
        Assert.assertEquals((byte)1, data[1]);
        Assert.assertEquals((byte)1, data[2]);
        Assert.assertEquals(new SessionClassID((byte)1), new SessionClassID(data[3]));
        Assert.assertEquals((short)1, COPSMsgParser.bytesToShort(data[4], data[5]));
        Assert.assertEquals((short)2, COPSMsgParser.bytesToShort(data[6], data[7]));
        Assert.assertEquals((short)3, COPSMsgParser.bytesToShort(data[8], data[9]));
        Assert.assertEquals((short)4, COPSMsgParser.bytesToShort(data[10], data[11]));
    }

    @Test
    public void byteParsing() {
        final GateSpec spec = new GateSpec(Direction.DOWNSTREAM, (byte)1, (byte)2, new SessionClassID((byte)3),
                (short)4, (short)5, (short)6, (short)7);
        final GateSpec parsed = GateSpec.parse(spec.getBytes());
        Assert.assertEquals(spec, parsed);
    }

}
