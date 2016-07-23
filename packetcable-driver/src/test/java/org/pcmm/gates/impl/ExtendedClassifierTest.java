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
import org.pcmm.gates.IClassifier.Protocol;
import org.pcmm.gates.IExtendedClassifier;
import org.pcmm.gates.IExtendedClassifier.ActivationState;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests the data holder class ExtendedClassifier to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class ExtendedClassifierTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullProtocol() throws UnknownHostException {
        new ExtendedClassifier(null, (byte)1, (byte)1, (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)1, (short)2, (byte)4,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)5, (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSrcAddr() throws UnknownHostException {
        new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)1, null,
                (Inet4Address)InetAddress.getByName("localhost"), (short)1, (short)2, (byte)4,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)5, (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDstAddr() throws UnknownHostException {
        new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)1, (Inet4Address) InetAddress.getByName("localhost"),
                null, (short)1, (short)2, (byte)4,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)5, (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSrcMask() throws UnknownHostException {
        new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)1, (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)1, (short)2, (byte)4,
                null, (Inet4Address) InetAddress.getByName("localhost"),
                (short)5, (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDstMask() throws UnknownHostException {
        new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)1, (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)1, (short)2, (byte)4,
                (Inet4Address) InetAddress.getByName("localhost"), null,
                (short)5, (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
      public void nullActivationState() throws UnknownHostException {
        new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)1, (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)1, (short)2, (byte)4,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)5, (short)6, (short)7, null, IExtendedClassifier.Action.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAction() throws UnknownHostException {
        new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)1, (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)1, (short)2, (byte)4,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)5, (short)6, (short)7, ActivationState.ACTIVE, null);
    }

    @Test
    public void constructionActive() throws UnknownHostException {
        final ExtendedClassifier classifier = new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)10,
                (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)11, (short)12, (byte)14,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)15, (short)16, (short)17, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD);
        Assert.assertEquals(Protocol.NONE, classifier.getProtocol());
        Assert.assertEquals((byte)1, classifier.getDSCPTOS());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getSourceIPAddress());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getDestinationIPAddress());
        Assert.assertEquals((short) 11, classifier.getSourcePort());
        Assert.assertEquals((short) 12, classifier.getDestinationPort());
        Assert.assertEquals((byte) 14, classifier.getPriority());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getIPSourceMask());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getIPDestinationMask());
        Assert.assertEquals((short) 15, classifier.getSourcePortEnd());
        Assert.assertEquals((short) 16, classifier.getDestinationPortEnd());
        Assert.assertEquals((short) 17, classifier.getClassifierID());
        Assert.assertEquals(ActivationState.ACTIVE, classifier.getActivationState());
        Assert.assertEquals(IExtendedClassifier.Action.ADD, classifier.getAction());
    }

    @Test
    public void constructionInactive() throws UnknownHostException {
        final ExtendedClassifier classifier = new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)10,
                (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)11, (short)12, (byte)14,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)15, (short)16, (short)17, ActivationState.INACTIVE, IExtendedClassifier.Action.ADD);
        Assert.assertEquals(Protocol.NONE, classifier.getProtocol());
        Assert.assertEquals((byte)1, classifier.getDSCPTOS());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getSourceIPAddress());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getDestinationIPAddress());
        Assert.assertEquals((short) 11, classifier.getSourcePort());
        Assert.assertEquals((short) 12, classifier.getDestinationPort());
        Assert.assertEquals((byte) 14, classifier.getPriority());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getIPSourceMask());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getIPDestinationMask());
        Assert.assertEquals((short) 15, classifier.getSourcePortEnd());
        Assert.assertEquals((short) 16, classifier.getDestinationPortEnd());
        Assert.assertEquals((short) 17, classifier.getClassifierID());
        Assert.assertEquals(ActivationState.INACTIVE, classifier.getActivationState());
        Assert.assertEquals(IExtendedClassifier.Action.ADD, classifier.getAction());
    }

    @Test
    public void byteParsingActive() throws UnknownHostException {
        final ExtendedClassifier classifier = new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)20,
                (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)21, (short)22, (byte)24,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)25, (short)26, (short)27, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD);
        final ExtendedClassifier parsed = ExtendedClassifier.parse(classifier.getBytes());
        Assert.assertEquals(classifier, parsed);
    }

    @Test
    public void byteParsingInactive() throws UnknownHostException {
        final ExtendedClassifier classifier = new ExtendedClassifier(Protocol.NONE, (byte)1, (byte)20,
                (Inet4Address) InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)21, (short)22, (byte)24,
                (Inet4Address) InetAddress.getByName("localhost"), (Inet4Address) InetAddress.getByName("localhost"),
                (short)25, (short)26, (short)27, ActivationState.INACTIVE, IExtendedClassifier.Action.ADD);
        final ExtendedClassifier parsed = ExtendedClassifier.parse(classifier.getBytes());
        Assert.assertEquals(classifier, parsed);
    }

}
