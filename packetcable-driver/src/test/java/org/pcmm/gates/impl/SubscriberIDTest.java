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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests the data holder class SubscriberId to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class SubscriberIDTest {

    @Test(expected = NullPointerException.class)
    public void nullConstArg() {
        new SubscriberID(null);
    }

    @Test
    public void constructorIpv4() {
        final InetAddress addr;
        try {
            addr = InetAddress.getByName("127.0.0.1");
            if (addr instanceof Inet6Address) Assert.fail("Address should be IPv4");
        } catch (UnknownHostException e) {
            throw new RuntimeException("host not found");
        }
        final SubscriberID subId = new SubscriberID(addr);
        Assert.assertEquals(addr, subId.getSourceIPAddress());
    }

    @Test
    public void byteParsingIpv4() {
        final InetAddress addr;
        try {
            addr = InetAddress.getByName("127.0.0.1");
            if (addr instanceof Inet6Address) Assert.fail("Address should be IPv4");
        } catch (UnknownHostException e) {
            throw new RuntimeException("host not found");
        }
        final SubscriberID subId = new SubscriberID(addr);
        final SubscriberID parsed = SubscriberID.parse(subId.getBytes());
        Assert.assertEquals(subId, parsed);
    }

    @Test
    public void constructorIpv6() {
        final InetAddress addr;
        try {
            addr = InetAddress.getByName("00:00:00:00:00:00:00:01");
            if (addr instanceof Inet4Address) Assert.fail("Address should be IPv6");
        } catch (UnknownHostException e) {
            throw new RuntimeException("host not found");
        }
        final SubscriberID subId = new SubscriberID(addr);
        Assert.assertEquals(addr, subId.getSourceIPAddress());
    }

    @Test
    public void byteParsingIpv6() {
        final InetAddress addr;
        try {
            addr = InetAddress.getByName("00:00:00:00:00:00:00:01");
            if (addr instanceof Inet4Address) Assert.fail("Address should be IPv6");
        } catch (UnknownHostException e) {
            throw new RuntimeException("host not found");
        }
        final SubscriberID subId = new SubscriberID(addr);
        final SubscriberID parsed = SubscriberID.parse(subId.getBytes());
        Assert.assertEquals(subId, parsed);
    }

}
