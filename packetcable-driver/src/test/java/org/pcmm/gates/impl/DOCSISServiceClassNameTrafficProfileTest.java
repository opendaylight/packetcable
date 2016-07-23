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

/**
 * Tests the data holder class DOCSISServiceClassNameTrafficProfile to ensure both construction and byte parsing result
 * in correct object creation.
 */
public class DOCSISServiceClassNameTrafficProfileTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructor1NullSCN() {
        new DOCSISServiceClassNameTrafficProfile(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1ShortSCN() {
        new DOCSISServiceClassNameTrafficProfile("1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1LongSCN() {
        new DOCSISServiceClassNameTrafficProfile("01234567891234567");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2NullSCN() {
        new DOCSISServiceClassNameTrafficProfile((byte)1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2ShortSCN() {
        new DOCSISServiceClassNameTrafficProfile((byte)1, "1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2LongSCN() {
        new DOCSISServiceClassNameTrafficProfile((byte)1, "01234567891234567");
    }

    @Test
    public void construction() {
        final DOCSISServiceClassNameTrafficProfile profile =
                new DOCSISServiceClassNameTrafficProfile((byte)5, "0123456789123456");
        Assert.assertEquals((byte)5, profile.getEnvelop());
        Assert.assertEquals("0123456789123456", profile.getScnName());
    }

    @Test
    public void byteParsingWithoutPadding() {
        // Name length % 4 == 0
        final DOCSISServiceClassNameTrafficProfile profile =
                new DOCSISServiceClassNameTrafficProfile((byte)25, "1234");
        final DOCSISServiceClassNameTrafficProfile parsed =
                DOCSISServiceClassNameTrafficProfile.parse(profile.getBytes());
        Assert.assertEquals(profile, parsed);
    }

    @Test
    public void byteParsingWithOnePaddedChar() {
        // Name length % 4 == 1
        final DOCSISServiceClassNameTrafficProfile profile =
                new DOCSISServiceClassNameTrafficProfile((byte)25, "123");
        final DOCSISServiceClassNameTrafficProfile parsed =
                DOCSISServiceClassNameTrafficProfile.parse(profile.getBytes());
        Assert.assertEquals(profile, parsed);
    }

    @Test
    public void byteParsingWithThreePaddedChar() {
        // Name length % 4 == 3
        final DOCSISServiceClassNameTrafficProfile profile =
                new DOCSISServiceClassNameTrafficProfile((byte)25, "12345");
        final DOCSISServiceClassNameTrafficProfile parsed =
                DOCSISServiceClassNameTrafficProfile.parse(profile.getBytes());
        Assert.assertEquals(profile, parsed);
    }

}
