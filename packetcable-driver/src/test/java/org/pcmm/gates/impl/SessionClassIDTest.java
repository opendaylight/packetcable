package org.pcmm.gates.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the data holder class SessionClassID to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class SessionClassIDTest {

    @Test
    public void constructor1() {
        final SessionClassID sessionClassID = new SessionClassID((byte)1);
        Assert.assertEquals((byte)1, sessionClassID.toSingleByte());
        Assert.assertEquals((byte)1 >> 2, sessionClassID.getPriority());
        Assert.assertEquals((byte)1 >> 3, sessionClassID.getPreemption());
    }

    @Test
    public void constructor2() {
        final SessionClassID sessionClassID = new SessionClassID((byte)1, (byte)3, (byte)4);
        Assert.assertEquals((byte)1, sessionClassID.toSingleByte());
        Assert.assertEquals((byte)3, sessionClassID.getPriority());
        Assert.assertEquals((byte)4, sessionClassID.getPreemption());
    }
}
