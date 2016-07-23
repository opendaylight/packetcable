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
import org.pcmm.gates.IPCMMError.ErrorCode;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Tests the data holder class PCMMError to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class PCMMErrorTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullErrorAndSubCodes() {
        new PCMMError(null, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullErrorCode() {
        new PCMMError(null, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naErrorCode() {
        new PCMMError(ErrorCode.NA);
    }

    @Test
    public void construction() {
        final PCMMError error = new PCMMError(ErrorCode.TRANSPORT_ERROR, (short)0);
        Assert.assertEquals(ErrorCode.TRANSPORT_ERROR, error.getErrorCode());
        Assert.assertEquals(ErrorCode.NA.getCode(), error.getErrorSubcode());

        final byte[] dataBytes = error.getBytes();
        Assert.assertEquals(4, dataBytes.length);
        Assert.assertEquals(ErrorCode.TRANSPORT_ERROR,
                ErrorCode.valueOf(COPSMsgParser.bytesToShort(dataBytes[0], dataBytes[1])));
        final short subCodeVal = COPSMsgParser.bytesToShort(dataBytes[2], dataBytes[3]);
        Assert.assertEquals(ErrorCode.NA,
                ErrorCode.valueOf(COPSMsgParser.bytesToShort(dataBytes[2], dataBytes[3])));
    }

    @Test
    public void byteParsing() {
        final PCMMError error = new PCMMError(ErrorCode.INVALID_FIELD, ErrorCode.INVALID_SUB_ID.getCode());
        final PCMMError parsed = PCMMError.parse(error.getBytes());
        Assert.assertEquals(error, parsed);
    }

}
