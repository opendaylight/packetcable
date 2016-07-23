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
import org.pcmm.gates.IExtendedClassifier;
import org.pcmm.gates.IExtendedClassifier.ActivationState;
import org.pcmm.gates.IIPv6Classifier.FlowLabel;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests the data holder class ExtendedClassifier.
 */
public class IPv6ClassifierTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullSrcAddr() throws UnknownHostException {
        new IPv6Classifier(null, (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (short)1, (short)2, (byte)4, (short)5, (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD,
                FlowLabel.VALID, (byte)11, (byte)12, (byte)13, 14, (short)15, (byte)16, (byte)17);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDstAddr() throws UnknownHostException {
        new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                null, (short)1, (short)2, (byte)4, (short)5, (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD,
                FlowLabel.VALID, (byte)11, (byte)12, (byte)13, 14, (short)15, (byte)16, (byte)17);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullActivationState() throws UnknownHostException {
        new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"), (short)1, (short)2, (byte)4, (short)5,
                (short)6, (short)7, null, IExtendedClassifier.Action.ADD, FlowLabel.VALID, (byte)11, (byte)12, (byte)13, 14, (short)15,
                (byte)16, (byte)17);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAction() throws UnknownHostException {
        new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"), (short)1, (short)2, (byte)4, (short)5,
                (short)6, (short)7, ActivationState.ACTIVE, null, FlowLabel.VALID, (byte)11, (byte)12, (byte)13, 14, (short)15,
                (byte)16, (byte)17);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlowLabel() throws UnknownHostException {
        new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"), (short)1, (short)2, (byte)4, (short)5,
                (short)6, (short)7, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD, null, (byte)11, (byte)12, (byte)13, 14, (short)15,
                (byte)16, (byte)17);
    }

    @Test
    public void constructionActiveValid() throws UnknownHostException {
        final IPv6Classifier classifier = new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (short)11, (short)12, (byte)14, (short)15, (short)16, (short)17, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD,
                FlowLabel.VALID, (byte)21, (byte)22, (byte)23, 24, (short)25, (byte)26, (byte)27);

        Assert.assertNull(classifier.getProtocol());

        Assert.assertEquals((byte) 0, classifier.getDSCPTOS());

        Assert.assertEquals(InetAddress.getByName("00:00:00:00:00:00:00:01"), classifier.getSourceIPAddress());
        Assert.assertEquals(InetAddress.getByName("00:00:00:00:00:00:00:01"), classifier.getDestinationIPAddress());
        Assert.assertEquals((short) 11, classifier.getSourcePort());
        Assert.assertEquals((short) 12, classifier.getDestinationPort());
        Assert.assertEquals((byte) 14, classifier.getPriority());
        Assert.assertEquals((short) 15, classifier.getSourcePortEnd());
        Assert.assertEquals((short) 16, classifier.getDestinationPortEnd());
        Assert.assertEquals((short) 17, classifier.getClassifierID());
        Assert.assertEquals(ActivationState.ACTIVE, classifier.getActivationState());
        Assert.assertEquals(IExtendedClassifier.Action.ADD, classifier.getAction());
        Assert.assertEquals(FlowLabel.VALID, classifier.getFlowLabelEnableFlag());
        Assert.assertEquals((byte) 21, classifier.getTcLow());
        Assert.assertEquals((byte) 22, classifier.getTcHigh());
        Assert.assertEquals((byte) 23, classifier.getTcMask());
        Assert.assertEquals(24, classifier.getFlowLabel());
        Assert.assertEquals((short)25, classifier.getNextHdr());
        Assert.assertEquals((byte)26, classifier.getSourcePrefixLen());
        Assert.assertEquals((byte)27, classifier.getDestinationPrefixLen());
    }

    @Test
    public void constructionInactiveIrrelevant() throws UnknownHostException {
        final IPv6Classifier classifier = new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (short)11, (short)12, (byte)14, (short)15, (short)16, (short)17, ActivationState.INACTIVE, IExtendedClassifier.Action.ADD,
                FlowLabel.IRRELEVANT, (byte)21, (byte)22, (byte)23, 24, (short)25, (byte)26, (byte)27);

        Assert.assertNull(classifier.getProtocol());

        Assert.assertEquals((byte) 0, classifier.getDSCPTOS());

        Assert.assertEquals(InetAddress.getByName("00:00:00:00:00:00:00:01"), classifier.getSourceIPAddress());
        Assert.assertEquals(InetAddress.getByName("00:00:00:00:00:00:00:01"), classifier.getDestinationIPAddress());
        Assert.assertEquals((short) 11, classifier.getSourcePort());
        Assert.assertEquals((short) 12, classifier.getDestinationPort());
        Assert.assertEquals((byte) 14, classifier.getPriority());
        Assert.assertEquals((short) 15, classifier.getSourcePortEnd());
        Assert.assertEquals((short) 16, classifier.getDestinationPortEnd());
        Assert.assertEquals((short) 17, classifier.getClassifierID());
        Assert.assertEquals(ActivationState.INACTIVE, classifier.getActivationState());
        Assert.assertEquals(IExtendedClassifier.Action.ADD, classifier.getAction());
        Assert.assertEquals(FlowLabel.IRRELEVANT, classifier.getFlowLabelEnableFlag());
        Assert.assertEquals((byte) 21, classifier.getTcLow());
        Assert.assertEquals((byte) 22, classifier.getTcHigh());
        Assert.assertEquals((byte) 23, classifier.getTcMask());

        // Per specs, this value must be 0 when IRRELEVANT
        Assert.assertEquals(0, classifier.getFlowLabel());

        Assert.assertEquals((short)25, classifier.getNextHdr());
        Assert.assertEquals((byte)26, classifier.getSourcePrefixLen());
        Assert.assertEquals((byte)27, classifier.getDestinationPrefixLen());
    }

    @Test
    public void byteParsingActiveValid() throws UnknownHostException {
        final IPv6Classifier classifier = new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (short)21, (short)22, (byte)24, (short)25, (short)26, (short)27, ActivationState.ACTIVE, IExtendedClassifier.Action.ADD,
                FlowLabel.VALID, (byte)31, (byte)32, (byte)33, 34, (short)35, (byte)36, (byte)37);
        final IPv6Classifier parsed = IPv6Classifier.parse(classifier.getBytes());
        Assert.assertEquals(classifier, parsed);
    }

    @Test
    public void byteParsingInactiveIrrelevant() throws UnknownHostException {
        final IPv6Classifier classifier = new IPv6Classifier((Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (Inet6Address)InetAddress.getByName("00:00:00:00:00:00:00:01"),
                (short)21, (short)22, (byte)24, (short)25, (short)26, (short)27, ActivationState.INACTIVE, IExtendedClassifier.Action.ADD,
                FlowLabel.IRRELEVANT, (byte)31, (byte)32, (byte)33, 34, (short)35, (byte)36, (byte)37);
        final IPv6Classifier parsed = IPv6Classifier.parse(classifier.getBytes());
        Assert.assertEquals(classifier, parsed);
    }

}
