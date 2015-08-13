/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import org.junit.Assert;
import org.junit.Test;
import org.pcmm.gates.IClassifier.Protocol;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests the data holder class Classifier to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class ClassifierTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullProtocol() throws UnknownHostException {
        new Classifier(null, (byte)1, (byte)1, (Inet4Address)InetAddress.getByName("localhost"),
                (Inet4Address)InetAddress.getByName("localhost"), (short)1, (short)2, (byte)4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSrcAddr() throws UnknownHostException {
        new Classifier(Protocol.NONE, (byte)1, (byte)1, null, (Inet4Address)InetAddress.getByName("localhost"),
                (short)1, (short)2, (byte)4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDstAddr() throws UnknownHostException {
        new Classifier(Protocol.NONE, (byte)1, (byte)1, (Inet4Address)InetAddress.getByName("localhost"), null,
                (short)1, (short)2, (byte)4);
    }

    @Test
    public void construction() throws UnknownHostException {
        final Classifier classifier = new Classifier(Protocol.NONE, (byte)1, (byte)1,
                (Inet4Address)InetAddress.getByName("localhost"), (Inet4Address)InetAddress.getByName("localhost"),
                (short)1, (short)2, (byte)4);
        Assert.assertEquals(Protocol.NONE, classifier.getProtocol());
        Assert.assertEquals((byte)1, classifier.getDSCPTOS());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getSourceIPAddress());
        Assert.assertEquals(InetAddress.getByName("localhost"), classifier.getDestinationIPAddress());
        Assert.assertEquals((short) 1, classifier.getSourcePort());
        Assert.assertEquals((short) 2, classifier.getDestinationPort());
        Assert.assertEquals((byte) 4, classifier.getPriority());
    }

    @Test
    public void byteParsing() throws UnknownHostException {
        final Classifier classifier = new Classifier(Protocol.NONE, (byte)1, (byte)4,
                (Inet4Address)InetAddress.getByName("localhost"), (Inet4Address)InetAddress.getByName("localhost"),
                (short)5, (short)6, (byte)7);
        final Classifier parsed = Classifier.parse(classifier.getBytes());
        Assert.assertEquals(classifier, parsed);
    }

}
