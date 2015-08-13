/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import com.google.common.primitives.Bytes;
import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IClassifier;
import org.umu.cops.stack.COPSMsgParser;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the IClassifier interface
 */
public class Classifier extends PCMMBaseObject implements IClassifier {

    /**
     * The classifier's protocol
     */
    protected final Protocol protocol;

    /**
     * When enabled, the CMTS must mark the packets traversing the CMTS DSCP/TOS value
     */
    protected final byte tosOverwrite;

    /**
     * The TOS mask value
     */
    protected final byte tosMask;

    // Instances of this and ExtendedClassifier require Inet4Address objects

    /**
     * The source address
     */
    protected final InetAddress srcAddress;

    /**
     * The destination address
     */
    protected final InetAddress dstAddress;

    /**
     * The source port number
     */
    protected final short srcPort;

    /**
     * The destination port number
     */
    protected final short dstPort;

    /**
     * The priority value
     */
    protected final byte priority;

    /**
     * Constructor for sub-classes
     * @param protocol - the protocol being sent through the gate (can be null for IPv6Classifier instances)
     * @param tosOverwrite - ENABLE/DISABLE
     * @param tosMask - the mask
     * @param srcAddress - the source IP
     * @param dstAddress - the destination IP
     * @param srcPort - the source port
     * @param dstPort - the destination port
     * @param priority - the priority value
     */
    public Classifier(final Protocol protocol, final byte tosOverwrite, final byte tosMask,
                         final Inet4Address srcAddress, final Inet4Address dstAddress, final short srcPort,
                         final short dstPort, final byte priority) {
        this(STYPE, protocol, tosOverwrite, tosMask, srcAddress, dstAddress, srcPort, dstPort, priority);
    }

    /**
     * Constructor for sub-classes
     * @param sType - the type of classifier
     * @param protocol - the protocol being sent through the gate
     * @param tosOverwrite - ENABLE/DISABLE
     * @param tosMask - the mask
     * @param srcAddress - the source IP
     * @param dstAddress - the destination IP
     * @param srcPort - the source port
     * @param dstPort - the destination port
     * @param priority - the priority value
     */
    protected Classifier(final byte sType, final Protocol protocol, final byte tosOverwrite, final byte tosMask,
                      final InetAddress srcAddress, final InetAddress dstAddress, final short srcPort,
                      final short dstPort, final byte priority) {
        super(SNum.CLASSIFIERS, sType);

        if (protocol == null && !(this instanceof IPv6Classifier))
            throw new IllegalArgumentException("Protocol value must not be null");
        if (srcAddress == null) throw new IllegalArgumentException("Source address value must not be null");
        if (dstAddress == null) throw new IllegalArgumentException("Destination address value must not be null");

        this.protocol = protocol;
        this.tosOverwrite = tosOverwrite;
        this.tosMask = tosMask;
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.priority = priority;
    }

    @Override
    public InetAddress getDestinationIPAddress() {
        return dstAddress;
    }

    @Override
    public short getDestinationPort() {
        return dstPort;
    }

    @Override
    public InetAddress getSourceIPAddress() {
        return srcAddress;
    }

    @Override
    public short getSourcePort() {
        return srcPort;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public byte getPriority() {
        return priority;
    }

    @Override
    public byte getDSCPTOS() {
        return tosOverwrite;
    }

    @Override
    public byte getDSCPTOSMask() {
        return tosMask;
    }

    @Override
    protected byte[] getBytes() {
        final List<Byte> byteList = new ArrayList<>(Bytes.asList(COPSMsgParser.shortToBytes(protocol.getValue())));
        byteList.add(tosOverwrite);
        byteList.add(tosMask);
        byteList.addAll(Bytes.asList(srcAddress.getAddress()));
        byteList.addAll(Bytes.asList(dstAddress.getAddress()));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(srcPort)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(dstPort)));
        byteList.add(priority);

        // reserved padding
        byteList.addAll(Bytes.asList((byte) 0, (byte) 0, (byte) 0));

        return Bytes.toArray(byteList);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Classifier)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Classifier that = (Classifier) o;
        return tosMask == that.tosMask && srcPort == that.srcPort && dstPort == that.dstPort &&
                priority == that.priority && protocol == that.protocol && tosOverwrite == that.tosOverwrite &&
                srcAddress.equals(that.srcAddress) && dstAddress.equals(that.dstAddress);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (int) tosOverwrite;
        result = 31 * result + (int) tosMask;
        result = 31 * result + srcAddress.hashCode();
        result = 31 * result + dstAddress.hashCode();
        result = 31 * result + (int) srcPort;
        result = 31 * result + (int) dstPort;
        result = 31 * result + (int) priority;
        return result;
    }

    /**
     * Returns a Classifier object from a byte array
     * @param data - the data to parse
     * @return - the object or null if cannot be parsed
     * TODO - make me more robust as exceptions can be swallowed here.
     */
    public static Classifier parse(final byte[] data) {
        final List<Byte> bytes = Bytes.asList(data);

        try {
            final byte[] srcAddrBytes = Bytes.toArray(bytes.subList(4, 8));
            final byte[] dstAddrBytes = Bytes.toArray(bytes.subList(8, 12));
            return new Classifier(Protocol.valueOf(COPSMsgParser.bytesToShort(data[0], data[1])), data[2], data[3],
                    (Inet4Address)InetAddress.getByAddress(srcAddrBytes),
                    (Inet4Address)InetAddress.getByAddress(dstAddrBytes),
                    COPSMsgParser.bytesToShort(data[12], data[13]), COPSMsgParser.bytesToShort(data[14], data[15]),
                    data[16]);
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
