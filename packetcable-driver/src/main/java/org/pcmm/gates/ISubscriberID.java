/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

import java.net.InetAddress;

/**
 * <p>
 * The SubscriberID, consisting of the IPv4 or IPv6 address of either the CM or
 * client CPE device (either directly connected to the CM or on a routable
 * network behind the CM), identifies the user requesting the service. In
 * complex network environments this address may be used to route Gate Control
 * messages between a number of Policy Servers and to determine which CMTS is
 * providing service to a particular endpoint. In addition to the IPv4 or IPv6
 * address, a subscriber may also be identified via a FQDN or some opaque data
 * (object defined below) relevant to the service in question.
 * </p>
 * <p>
 * For a Multicast Gates the CMTS uses the SubscriberID to decide where the
 * Multicast replication needs to be created. The CMTS treats the SubscriberID
 * as the source IP address of a JoinMulticastSession [1]. If the SubscriberID
 * is on an IP subnet that is not directly connected to the CMTS, the CMTS MAY
 * reject the Gate as having an invalid SubscriberID see 6.4.2.14 PacketCable
 * Error.
 * </p>
 *
 *
 */

public interface ISubscriberID extends IPCMMBaseObject {

    byte STYPE_IPV4 = 1;
    byte STYPE_IPV6 = 2;

    /**
     * source IP address for the PCMM gate.
     *
     * @return IP v4 or v6 ip address.
     */
    InetAddress getSourceIPAddress();

}
