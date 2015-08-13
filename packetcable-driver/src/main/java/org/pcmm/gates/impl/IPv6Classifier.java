/**

 * Copyright (c) 2014 CableLabs.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */
package org.pcmm.gates.impl;

import com.google.common.primitives.Bytes;
import org.pcmm.gates.IIPv6Classifier;
import org.umu.cops.stack.COPSMsgParser;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the IIPv6Classifier interface
 */
public class IPv6Classifier extends ExtendedClassifier implements IIPv6Classifier {

    /**
     * VALID when there is valid data for comparison with the IPv6 Flow label else IRRELEVANT
     */
    private final FlowLabel flowEnabled;

    /**
     * Allows for matching on the IPv6 Traffic Class value (with tcHigh & tcMask)
     */
    private final byte tcLow;

    /**
     * Allows for matching on the IPv6 Traffic Class value (with tcLow & tcMask)
     */
    private final byte tcHigh;

    /**
     * Allows for matching on the IPv6 Traffic Class value (with tcLow & tcHigh)
     */
    private final byte tcMask;

    /**
     * Contains data for comparison When flowEnabled == VALID else this value must be 0
     */
    private final int flowLabel;

    /**
     * Next Header Type field specifies the desired next header type value for any header or extension header associated
     * with the packet. Typically this value will specify the next layer protocol type. There are two special IPv6 next
     * header type field values: "256" matches traffic with any IPv6 next header type value, and "257" matches both TCP
     * and UDP traffic. Values greater than 257 are invalid for comparisons (i.e., no traffic can match this entry).
     * For further discussion of the IPv6 Next Header Type field, refer to C.2.1.10.3
     */
    private final short nextHdr;

    /**
     * Source Prefix Length field specifies the fixed, most significant bits of an IPv6 address that are used to
     * determine address range and subnet ID for the IPv6 Source Address.
     */
    private final byte srcPrefixLen;

    /**
     * Destination Prefix Length field specifies the fixed, most significant bits of an IPv6 address that are used to
     * determine address range and subnet ID for the IPv6 Destination Address.
     */
    private final byte dstPrefixLen;

    /**
     * Constructor
     * @param srcAddress - the source IP (not null)
     * @param dstAddress - the destination IP (not null)
     * @param srcPortBegin - the source begin port
     * @param dstPortBegin - the destination begin port
     * @param priority - the priority value
     * @param srcPortEnd - the source start port
     * @param dstPortEnd - the destination end port
     * @param classifierId - the classifier identifier
     * @param activationState - denotes the activation state (not null)
     * @param action - the action
     * @param flowEnabled - eumeration of FlowLabel (VALID|IRRELEVANT)
     * @param tcLow - low matching on the IPv6 Traffic Class
     * @param tcHigh - high matching on the IPv6 Traffic Class
     * @param tcMask - mask matching on the IPv6 Traffic Class
     * @param flowLabel - data for comparison
     * @param nextHdr - the next header type value
     * @param srcPrefixLen - source prefix length
     * @param dstPrefixLen - destination prefix length
     */
    public IPv6Classifier(final Inet6Address srcAddress, final Inet6Address dstAddress, final short srcPortBegin,
                          final short dstPortBegin, final byte priority, final short srcPortEnd, final short dstPortEnd,
                          final short classifierId, final ActivationState activationState, final byte action,
                          final FlowLabel flowEnabled, final byte tcLow, final byte tcHigh, final byte tcMask,
                          final int flowLabel, final short nextHdr, final byte srcPrefixLen, final byte dstPrefixLen) {
        super(IIPv6Classifier.STYPE, srcAddress, dstAddress, srcPortBegin, dstPortBegin, priority, srcPortEnd,
                dstPortEnd, classifierId, activationState, action);
        if (flowEnabled == null) throw new IllegalArgumentException("Flow enabled enumeration must not be null");
        if (flowEnabled.equals(FlowLabel.IRRELEVANT)) this.flowLabel = 0;
        else this.flowLabel = flowLabel;
        this.flowEnabled = flowEnabled;
        this.tcLow = tcLow;
        this.tcHigh = tcHigh;
        this.tcMask = tcMask;
        this.nextHdr = nextHdr;
        this.srcPrefixLen = srcPrefixLen;
        this.dstPrefixLen = dstPrefixLen;
    }

    // 00:01 Flags: 0000.0001 Flow Label enable match
    @Override
    public FlowLabel getFlowLabelEnableFlag() {
        return flowEnabled;
    }

    // 01:01 Tc-low
    @Override
    public byte getTcLow() {
        return tcLow;
    }

    // 02:01 Tc-high
    @Override
    public byte getTcHigh() {
        return tcHigh;
    }

    // 03:01 Tc-mask
    @Override
    public byte getTcMask() {
        return tcMask;
    }

    // 04:04 Flow Label: low order 20 bits; high order 12 bits ignored
    @Override
    public int getFlowLabel() {
        return flowLabel;
    }

    // 08:02 Next Header Type
    @Override
    public short getNextHdr() {
        return nextHdr;
    }

    // 10:01 Source Prefix Length
    @Override
    public byte getSourcePrefixLen() {
        return srcPrefixLen;
    }

    // 11:01 Destination Prefix Length
    @Override
    public byte getDestinationPrefixLen() {
        return dstPrefixLen;
    }

    @Override
    protected byte[] getBytes() {
        final List<Byte> byteList = new ArrayList<>();
        byteList.add(flowEnabled.getValue());
        byteList.add(tcLow);
        byteList.add(tcHigh);
        byteList.add(tcMask);
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(flowLabel)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(nextHdr)));
        byteList.add(srcPrefixLen);
        byteList.add(dstPrefixLen);
        byteList.addAll(Bytes.asList(srcAddress.getAddress()));
        byteList.addAll(Bytes.asList(dstAddress.getAddress()));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(srcPort)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(srcPortEnd)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(dstPort)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(dstPortEnd)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(classifierId)));
        byteList.add(priority);
        byteList.add(activationState.getValue());
        byteList.add(action);
        return Bytes.toArray(byteList);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IPv6Classifier)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final IPv6Classifier that = (IPv6Classifier) o;
        return flowEnabled == that.flowEnabled && tcLow == that.tcLow && tcHigh == that.tcHigh &&
                tcMask == that.tcMask && flowLabel == that.flowLabel && nextHdr == that.nextHdr &&
                srcPrefixLen == that.srcPrefixLen && dstPrefixLen == that.dstPrefixLen;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) flowEnabled.getValue();
        result = 31 * result + (int) tcLow;
        result = 31 * result + (int) tcHigh;
        result = 31 * result + (int) tcMask;
        result = 31 * result + flowLabel;
        result = 31 * result + (int) nextHdr;
        result = 31 * result + (int) srcPrefixLen;
        result = 31 * result + (int) dstPrefixLen;
        return result;
    }

    /**
     * Returns a ExtendedClassifier object from a byte array
     * @param data - the data to parse
     * @return - the object or null if cannot be parsed
     * TODO - make me more robust as exceptions can be swallowed here.
     */
    public static IPv6Classifier parse(final byte[] data) {
        final List<Byte> bytes = new ArrayList<>(Bytes.asList(data));

        try {
            final Inet6Address srcAddress = (Inet6Address)InetAddress.getByAddress(Bytes.toArray(bytes.subList(12, 28)));
            final Inet6Address dstAddress = (Inet6Address)InetAddress.getByAddress(Bytes.toArray(bytes.subList(28, 44)));
            final short srcPortBegin = COPSMsgParser.bytesToShort(data[44], data[45]);
            final short srcPortEnd = COPSMsgParser.bytesToShort(data[46], data[47]);
            final short dstPortBegin = COPSMsgParser.bytesToShort(data[48], data[49]);
            final short dstPortEnd = COPSMsgParser.bytesToShort(data[50], data[51]);
            final short classifierId = COPSMsgParser.bytesToShort(data[52], data[53]);
            final byte priority = data[54];
            final ActivationState activationState = ActivationState.valueOf(data[55]);
            final byte action = data[56];
            final FlowLabel flowEnabled = FlowLabel.valueOf(data[0]);
            final byte tcLow = data[1];
            final byte tcHigh = data[2];
            final byte tcMask = data[3];
            final int flowLabel = COPSMsgParser.bytesToInt(data[4], data[5], data[6], data[7]);
            final short nextHdr = COPSMsgParser.bytesToShort(data[8], data[9]);
            final byte srcPrefixLen = data[10];
            final byte dstPrefixLen = data[11];

            return new IPv6Classifier(srcAddress, dstAddress, srcPortBegin, dstPortBegin, priority, srcPortEnd,
                    dstPortEnd, classifierId, activationState, action, flowEnabled, tcLow, tcHigh, tcMask, flowLabel,
                    nextHdr, srcPrefixLen, dstPrefixLen);
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
