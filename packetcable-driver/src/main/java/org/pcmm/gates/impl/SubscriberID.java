/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.Inet4Address;
import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ISubscriberID;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Implementation of the ISubscriberID interface
 */
public class SubscriberID extends PCMMBaseObject implements ISubscriberID {

    /**
     * The source IPv4 or IPv6 address
     */
    private final InetAddress srcIp;

    /**
     * Constructor
     * @param srcIp - the source host address
     */
    public SubscriberID(final InetAddress srcIp) {
        super(SNum.SUBSCRIBER_ID, chooseSType(srcIp));
        if (srcIp == null) throw new IllegalArgumentException("srcIp must not be null");
        this.srcIp = srcIp;
    }

    @Override
    public InetAddress getSourceIPAddress() {
        return srcIp;
    }

    @Override
    protected byte[] getBytes() {
        return srcIp.getAddress();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscriberID)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final SubscriberID that = (SubscriberID) o;
        return srcIp.equals(that.srcIp);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + srcIp.hashCode();
        return result;
    }

    private static byte chooseSType(final InetAddress srcIp) {
        checkNotNull(srcIp);
        if (srcIp instanceof Inet4Address) {
            return STYPE_IPV4;
        }
        else {
            return STYPE_IPV6;
        }
    }

    /**
     * Returns a SubscriberID object from a byte array
     * @param data - the data to parse
     * @return - the object or null if cannot be parsed
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static SubscriberID parse(final byte[] data) {
        try {
            return new SubscriberID(InetAddress.getByAddress(data));
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
