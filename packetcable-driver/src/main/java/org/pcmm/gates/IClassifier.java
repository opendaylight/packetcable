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
 * The Classifier object specifies the packet matching rules associated with a Gate. As defined in Sections 6.4.3.1 and
 * 6.4.3.2, for Unicast Gates multiple Classifier objects may be included in the Gate-Set to allow for complex
 * classifier rules. When an AM is using Classifier objects, at least one Classifier MUST be provided by the PDP in all
 * Gate-Set messages. More than one Classifier is allowed for Unicast Gates. Only one classifier is required to be
 * supported for Multicast Gates. Unlike DOCSIS DSx signaling, Classifiers have no identifier and have no explicit add,
 * change, or remove operations. The entire set of Classifiers present in a Gate-Set message replaces the entire set of
 * classifiers for the existing Gate. Equivalence of Classifiers is determined by comparing all fields within the
 * Classifier. Classifiers may be provided in any order.
 */
public interface IClassifier extends IPCMMBaseObject {

    byte STYPE = 1;

    /**
     * IP Destination Address or IPv6 Destination Address is the termination
     * point for the IP flow
     *
     * @return destination IP address.
     */
    InetAddress getDestinationIPAddress();

    /**
     * Returns the destination port value
     * @return - the port number
     */
    short getDestinationPort();

    /**
     * Source IP, IP Source Address, or IPv6 Source Address (in the case of
     * Extended Classifier or IPv6 Classifier) is the IP address (as seen at the
     * CMTS) of the originator of the IP flow.
     *
     * @return source IP address.
     */
    InetAddress getSourceIPAddress();

    /**
     * Returns the source port value
     * @return - the port number
     */
    short getSourcePort();

    /**
     * Protocol field, in a legacy Classifier or Extended Classifier, identifies
     * the type of protocol (e.g., IP, ICMP, etc.). The Next Header Type field
     * serves a similar function in the IPv6 Classifier.
     *
     * @return the protocol.
     */
    Protocol getProtocol();

    /**
     * Priority may be used to distinguish between multiple classifiers that
     * match a particular packet. This is typically set to a default value since
     * classifiers are generally intended to be unique.
     *
     * @return priority.
     */
    byte getPriority();

    /**
     * Returns the DSCPTOS enumeration value (ENABLE|DISABLE)
     * @return the enumeration
     */
    byte getDSCPTOS();

    /**
     * Returns the DSCPTOS mask value
     * @return the mask
     */
    byte getDSCPTOSMask();

    /**
     * Enumeration of supported protocols
     */
    enum Protocol {
        /*ICMP((short) 1), IGMP((short) 2), */
        NONE((short)0), TCP((short) 6), UDP((short) 17);

        Protocol(short v) {
            this.value = v;
        }

        public static Protocol valueOf(short v) {
            switch (v) {
                case 0:
                    return NONE;
          /* TODO - Determine why these two values are not being supported???
            case 1:
                return ICMP;
            case 2:
                return IGMP;
          */
                case 6:
                    return TCP;
                default:
                    return UDP;
            }
        }
        private short value;

        public short getValue() {
            return value;
        }
    }

}
