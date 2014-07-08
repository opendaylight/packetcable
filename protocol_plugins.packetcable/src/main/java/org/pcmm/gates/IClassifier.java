/**
 @header@
 */


package org.pcmm.gates;

import java.net.InetAddress;

import org.pcmm.base.IPCMMBaseObject;

/**
 *
 *
 *
 */
public interface IClassifier extends IPCMMBaseObject {

    static final short LENGTH = 24;
    static final byte SNUM = 6;
    static final byte STYPE = 1;

    static enum Protocol {
        /*ICMP((short) 1), IGMP((short) 2), */
        NONE((short)0), TCP((short) 6), UDP((short) 17);

        Protocol(short v) {
            this.value = v;
        }

        public static Protocol valueOf(short v) {
            switch (v) {
            case 0:
                return NONE;
          /*
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

    /**
     * IP Destination Address or IPv6 Destination Address is the termination
     * point for the IP flow
     *
     * @return destination IP address.
     */
    InetAddress getDestinationIPAddress();

    void setDestinationIPAddress(InetAddress address);

    short getDestinationPort();

    void setDestinationPort(short p);

    /**
     * Source IP, IP Source Address, or IPv6 Source Address (in the case of
     * Extended Classifier or IPv6 Classifier) is the IP address (as seen at the
     * CMTS) of the originator of the IP flow.
     *
     * @return source IP address.
     */
    InetAddress getSourceIPAddress();

    void setSourceIPAddress(InetAddress a);

    short getSourcePort();

    void setSourcePort(short p);

    /**
     * Protocol field, in a legacy Classifier or Extended Classifier, identifies
     * the type of protocol (e.g., IP, ICMP, etc.). The Next Header Type field
     * serves a similar function in the IPv6 Classifier.
     *
     * @return the protocol.
     */
    Protocol getProtocol();

    /**
     * @see <a
     *      href="http://www.iana.org/assignments/protocol-numbers/protocol-numbers.txt">protocols</a>
     * @param p
     */
    void setProtocol(Protocol p);

    /**
     * Priority may be used to distinguish between multiple classifiers that
     * match a particular packet. This is typically set to a default value since
     * classifiers are generally intended to be unique.
     *
     * @return priority.
     */
    byte getPriority();

    /**
     * sets the priority;
     *
     * @param p
     *            priority
     */
    void setPriority(byte p);

    byte getDSCPTOS();

    void setDSCPTOS(byte v);

    byte getDSCPTOSMask();

    void setDSCPTOSMask(byte v);

    // DSCP/TOS Field
    // 
    // DSCP/TOS Mask

}
