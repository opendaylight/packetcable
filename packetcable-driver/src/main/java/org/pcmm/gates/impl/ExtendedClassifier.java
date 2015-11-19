/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import com.google.common.primitives.Bytes;
import org.pcmm.gates.IExtendedClassifier;
import org.umu.cops.stack.COPSMsgParser;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the IExtendedClassifier interface
 */
public class ExtendedClassifier extends Classifier implements IExtendedClassifier {

    /**
     * IPv4 (for this class) and IPv6 (for extended IPv6Classifier) mask. When the address is zero, the mask is
     * irrelevant else only packets with source IP address 'pkt.ip-src'
     * will match if (pkt.ip-src AND classifier.ipmask-src) == classifier.ip-src.
     */
    protected final InetAddress srcMask;

    /**
     * IPv4 (for this class) and IPv6 (for extended IPv6Classifier) mask. When the address is zero, the mask is
     * irrelevant else only packets with source IP address 'pkt.ip-dst'
     * will match if (pkt.ip-dst AND classifier.ipmask-dst) == classifier.ip-dst.
     */
    protected final InetAddress dstMask;

    /**
     * Source Port End specifies the high-end TCP/UDP source port value. See super srcPort for low-end value.
     */
    protected final short srcPortEnd;

    /**
     * Destination Port End specifies the high-end TCP/UDP source port value. See super dstPort for low-end value.
     */
    protected final short dstPortEnd;

    /**
     * The classifiers identifier
     */
    protected final short classifierId;

    /**
     * Enumeration of the activation state
     */
    protected final ActivationState activationState;

    /**
     * The action value
     */
    protected final Action action;

    /**
     * Constructor
     * @param protocol - the protocol being sent through the gate
     * @param tosOverwrite - ENABLE/DISABLE
     * @param tosMask - the mask
     * @param srcAddress - the source IP
     * @param dstAddress - the destination IP
     * @param srcPortBegin - the source begin port
     * @param dstPortBegin - the destination begin port
     * @param priority - the priority value
     * @param srcMask - the source IP mask
     * @param dstMask - the destination IP mask
     * @param srcPortEnd - the source start port
     * @param dstPortEnd - the destination end port
     * @param classifierId - the classifier identifier
     * @param activationState - denotes the activation state
     * @param action - the action
     */
    public ExtendedClassifier(final Protocol protocol, final byte tosOverwrite, final byte tosMask,
                              final Inet4Address srcAddress, final Inet4Address dstAddress, final short srcPortBegin,
                              final short dstPortBegin, final byte priority, final Inet4Address srcMask,
                              final Inet4Address dstMask, final short srcPortEnd, final short dstPortEnd,
                              final short classifierId, final ActivationState activationState, final Action action) {
        super(IExtendedClassifier.STYPE, protocol, tosOverwrite, tosMask, srcAddress, dstAddress, srcPortBegin,
                dstPortBegin, priority);
        if (srcMask == null) throw new IllegalArgumentException("Source IP Mask cannot be null");
        if (dstMask == null) throw new IllegalArgumentException("Destination IP Mask cannot be null");
        if (activationState == null) throw new IllegalArgumentException("Activation state must not be null");
        if (action == null) throw new IllegalArgumentException("Action must not be null");
        this.srcMask = srcMask;
        this.dstMask = dstMask;
        this.srcPortEnd = srcPortEnd;
        this.dstPortEnd = dstPortEnd;
        this.classifierId = classifierId;
        this.activationState = activationState;
        this.action = action;
    }

    /**
     * Constructor for IPv6Classifier subclass
     * @param sType - the type of classifier
     * @param srcAddress - the source IP
     * @param dstAddress - the destination IP
     * @param srcPortBegin - the source begin port
     * @param dstPortBegin - the destination begin port
     * @param priority - the priority value
     * @param srcPortEnd - the source start port
     * @param dstPortEnd - the destination end port
     * @param classifierId - the classifier identifier
     * @param activationState - denotes the activation state
     * @param action - the action
     */
    protected ExtendedClassifier(final byte sType, final InetAddress srcAddress, final InetAddress dstAddress,
                                 final short srcPortBegin, final short dstPortBegin, final byte priority,
                                 final short srcPortEnd, final short dstPortEnd, final short classifierId,
                                 final ActivationState activationState, final Action action) {
        super(sType, null, (byte)0, (byte)0, srcAddress, dstAddress, srcPortBegin, dstPortBegin, priority);
        if (activationState == null) throw new IllegalArgumentException("Activation state must not be null");
        if (action == null) throw new IllegalArgumentException("Action must not be null");
        this.srcMask = null;
        this.dstMask = null;
        this.srcPortEnd = srcPortEnd;
        this.dstPortEnd = dstPortEnd;
        this.classifierId = classifierId;
        this.activationState = activationState;
        this.action = action;
    }

    @Override
    public InetAddress getIPSourceMask() {
        return srcMask;
    }

    @Override
    public InetAddress getIPDestinationMask() {
        return dstMask;
    }

    @Override
    public short getSourcePortStart() {
        return super.getSourcePort();
    }

    @Override
    public short getSourcePortEnd() {
        return srcPortEnd;
    }

    @Override
    public short getDestinationPortStart() {
        return super.getDestinationPort();
    }

    @Override
    public short getDestinationPortEnd() {
        return dstPortEnd;
    }

    @Override
    public short getClassifierID() {
        return classifierId;
    }

    @Override
    public ActivationState getActivationState() {
        return activationState;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    protected byte[] getBytes() {
        final List<Byte> byteList = new ArrayList<>(Bytes.asList(COPSMsgParser.shortToBytes(protocol.getValue())));
        byteList.add(tosOverwrite);
        byteList.add(tosMask);
        byteList.addAll(Bytes.asList(srcAddress.getAddress()));
        byteList.addAll(Bytes.asList(srcMask.getAddress()));
        byteList.addAll(Bytes.asList(dstAddress.getAddress()));
        byteList.addAll(Bytes.asList(dstMask.getAddress()));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(srcPort)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(srcPortEnd)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(dstPort)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(dstPortEnd)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(classifierId)));
        byteList.add(priority);
        byteList.add(activationState.getValue());
        byteList.add(action.getByte());

        // reserved padding
        byteList.addAll(Bytes.asList((byte) 0, (byte) 0, (byte) 0));

        return Bytes.toArray(byteList);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExtendedClassifier)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ExtendedClassifier that = (ExtendedClassifier) o;
        return srcPortEnd == that.srcPortEnd && dstPortEnd == that.dstPortEnd && classifierId == that.classifierId &&
                activationState == that.activationState && action == that.action &&
                !(srcMask != null ? !srcMask.equals(that.srcMask) : that.srcMask != null) &&
                !(dstMask != null ? !dstMask.equals(that.dstMask) : that.dstMask != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (srcMask != null ? srcMask.hashCode() : 0);
        result = 31 * result + (dstMask != null ? dstMask.hashCode() : 0);
        result = 31 * result + (int) srcPortEnd;
        result = 31 * result + (int) dstPortEnd;
        result = 31 * result + (int) classifierId;
        result = 31 * result + (int) activationState.getValue();
        result = 31 * result + (int) action.getByte();
        return result;
    }

    /**
     * Returns a ExtendedClassifier object from a byte array
     * @param data - the data to parse
     * @return - the object or null if cannot be parsed
     * TODO - make me more robust as exceptions can be swallowed here.
     */
    public static ExtendedClassifier parse(final byte[] data) {
        final List<Byte> bytes = new ArrayList<>(Bytes.asList(data));

        try {
            return new ExtendedClassifier(Protocol.valueOf(COPSMsgParser.bytesToShort(data[0], data[1])),
                    data[2], data[3],
                    (Inet4Address)InetAddress.getByAddress(Bytes.toArray(bytes.subList(4, 8))),
                    (Inet4Address)InetAddress.getByAddress(Bytes.toArray(bytes.subList(8, 12))),
                    COPSMsgParser.bytesToShort(data[20], data[21]), COPSMsgParser.bytesToShort(data[24], data[25]),
                    data[30], (Inet4Address)InetAddress.getByAddress(Bytes.toArray(bytes.subList(12, 16))),
                    (Inet4Address)InetAddress.getByAddress(Bytes.toArray(bytes.subList(16, 20))),
                    COPSMsgParser.bytesToShort(data[22], data[23]), COPSMsgParser.bytesToShort(data[26], data[27]),
                    COPSMsgParser.bytesToShort(data[28], data[29]), ActivationState.valueOf(data[31]),
                    Action.getFromByte(data[32]));
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
