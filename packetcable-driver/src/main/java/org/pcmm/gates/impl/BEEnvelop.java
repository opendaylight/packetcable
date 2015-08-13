/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import com.google.common.primitives.Bytes;
import org.umu.cops.stack.COPSMsgParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for use as member to the ITrafficProfile#BestEffortService class.
 */
public class BEEnvelop {

    public static final byte DEFAULT_TRAFFIC_PRIORITY = 0;

    public static final int DEFAULT_MAX_TRAFFIC_BURST = 3044;

    /**
     * Traffic Priority is a 1-byte unsigned integer field specifying the relative priority assigned to the Service Flow
     * in comparison with other flows. This field is fully defined in section C.2.2.5.1 of [1]. A default Traffic
     * Priority of SHOULD be used if a specific Traffic Priority value is not required.
     */
    private final byte trafficPriority;

    /**
     * Request/Transmission Policy is a 4-byte bit field as defined in section C.2.2.6.3. A default Request/Transmission
     * policy of 0 SHOULD be used if a specific Request/Transmission Policy value is not required.
     */
    private final int transPolicy;

    /**
     * Maximum Sustained Traffic Rate is a 4-byte unsigned integer field specifying the rate parameter, in bits/sec.
     * A value of 0 indicates that no explicitly-enforced Maximum Sustained Rate is requested. A default Maximum
     * Sustained Traffic Rate of 0 SHOULD be used if a specific Maximum Sustained Traffic Rate is not required.
     */
    private final int maxSusTrafficRate;

    /**
     * Maximum Traffic Burst is a 4-byte unsigned integer field specifying the token bucket size, in bytes, for a
     * tokenbucket-based rate limit for this Service Flow. This field is fully defined in section C.2.2.5.3 of [1].
     * A default Maximum Traffic Burst of 3044 bytes SHOULD be used if a specific Maximum Traffic Burst is not required.
     * The value of this parameter has no effect unless a non-zero value has been provided Rate parameter.
     */
    private final int maxTrafficBurst;

    /**
     * Minimum Reserved Traffic Rate is a 4-byte unsigned integer field specifying the minimum rate, in bits/sec,
     * reserved for this Service Flow. This field is fully defined in section C.2.2.5.4. A default Mini Rate of 0 SHOULD
     * be used if a specific Minimum Reserved Traffic Rate is not required.
     */
    private final int minResTrafficRate;

    /**
     * Assumed Minimum Reserved Traffic Rate Packet Size is a 2-byte unsigned integer field specifying an assumed
     * minimum packet size, in bytes, for which the Minimum Reserved Traffic Rate will be provided for this flow. This
     * field is fully defined in section C.2.2.5.5. A default Assumed Minimum Reserved Traffic Rate Packet Size of
     * 0 SHOULD be used if a specific Assumed Minimum Reserved Traffic Rate Packet size is not required. Upon receip
     * of a value of 0 the CMTS MUST utilize its implementation-specific default size for this parameter, not 0 bytes.
     */
    private final short assumedMinConcatBurst;

    /**
     * Maximum Concatenated Burst is a 2-byte unsigned integer specifying the maximum concatenated burst (in bytes)
     * which a Service Flow is allowed. This field is fully defined in section C.2.2.6.1. A value of 0 means
     * there is no limit. A default Maximum Concatenated Burst of 1522 bytes SHOULD be used if a specific Maximum
     * Concatenated Burst is not required.
     */
    private final short maxConcatBurst;

    /**
     * Upstream Peak Traffic Rate is a 4-byte unsigned integer specifying the Peak traffic rate (in bits per second)
     * which a Service Flow is allowed. This field is fully defined in section C.2.2.5.10.1.
     */
    private final int upPeakTrafficRate;

    /*
     * Attribute Masks define a specific set of attributes associated with a DOCSIS 3.0 service flow. The CMTS MUST
     * ignore the bonded bit in the Required and Forbidden Attribute Mask objects if the cable modem associated with the
     * service flow is operating in pre-3.0 DOCSIS mode. The Required Attribute Mask limits the set of channels and
     * bonding groups to which the CMTS assigns the service flow by requiring certain attributes. This field is fully
     * defined in section C.2.2.3.6 of [1]. The Forbidden Attribute Mask limits the set of channels and bonding groups
     * to which the CMTS assigns the service flow by forbidding certain attributes. This field is fully defined in
     * section C.2.2.3.7. The CMTS is free to assign the service flow to any channel that satisfies the traffic profile
     * if no channel is available that satisfies the Required Attribute Mask and Forbidden Attribute Mask for the
     * service flow. The Attribute Aggregation Rule Mask provides guidance to the CMTS as to how it might use the
     * attribute masks of individual channels to construct a dynamic bonding group for this service flow. This field is
     * fully described in section "Service Flow Attribute Aggregation Rule Mask". As described in that section a default
     * Attribute Aggregation Rule Mask of 0 SHOULD be used if specific Attribute Aggregation Rules are not required.
     * The Buffer Control parameters libit the maximum queue depth of a Service Flow. The service flow buffer holds the
     * packets that are enqueued for transmission for the service flow. The size of the service flow buffer sets the
     * maximum queue depth, and upper limit on the amount of data that can be enqueued for transmission at any time by
     * the service flow. By providing the ability to control per-service flow buffers. the below Buffer Control
     * parameters provide a means of balancing throughput and latency in a standardized and configurable manner.
     */

    /**
     * Required Attribute Mask (see explanation above)
     */
    private final int reqAttrMask;

    /**
     * Forbidden Attribute Mask (see explanation above)
     */
    private final int forbidAttrMask;

    /**
     * Attribute Aggregation Rule Mask (see explanation above)
     */
    private final int attrAggRuleMask;

    /**
     * Minimum Buffer is a 4-byte unsigned integer parameter that defines a lower limit for the size of the buffer that
     * is to be provided for a service flow. This field is fully defined in section C.2.2.5.11.3. If this parameter is
     * omitted. The Minimum Buffer defaluts to a value of 0, which indicates that there is no lower limit.
     */
    private final int minBuffer;

    /**
     * Target Buffer is a 4-byte unsigned integer parameter that defines a desired value for the size of the buffer that
     * is to be provided for a service flow. This field is fully defined in section C.2.2.5.11.4. If this parameter is
     * omitted or set to a value of 0, the device selects any buffer size within the range of the Minimum and Maximum
     * Buffers, via a vendor specific algorithm.
     */
    private final int targetBuffer;

    /**
     * Maximum Buffer is a 4-byte unsigned integer parameter that defines an upper limit for the size of the buffer that
     * is to be provided for a service flow. This field is fully defined in section C.2.2.5.11.5. If this parameter is
     * omitted or set to a value of 0, the Maximum Buffer defaults to a value of no limit.
     */
    private final int maxBuffer;

    /**
     * Constructor
     * @param trafficPriority - the Traffic Priority
     * @param transPolicy - the Requested Transmission Policy
     * @param maxSusTrafficRate - the Maximum Sustained Traffic Rate
     * @param maxTrafficBurst - the Maximum Traffic Burst Rate
     * @param minResTrafficRate - the Minimum Reserved Traffic Rate
     * @param assumedMinConcatBurst - the Assumed Minimum Reserved Traffic Rate Packet Size
     * @param maxConcatBurst - the Maximum Concatenated Burst
     * @param upPeakTrafficRate - the Upstream Peak Traffic Rate
     * @param reqAttrMask - the Required Attribute Mask
     * @param forbidAttrMask - the Forbidden Attribute Mask
     * @param attrAggRuleMask - the Attribute Aggregation Rule Mask
     * @param minBuffer - the Minimum Buffer
     * @param targetBuffer - the Target Buffer
     * @param maxBuffer - the Maximum Buffer
     */
    public BEEnvelop(byte trafficPriority, int transPolicy, int maxSusTrafficRate, int maxTrafficBurst,
                     int minResTrafficRate, short assumedMinConcatBurst, short maxConcatBurst, int upPeakTrafficRate,
                     int reqAttrMask, int forbidAttrMask, int attrAggRuleMask, int minBuffer, int targetBuffer,
                     int maxBuffer) {
        this.trafficPriority = trafficPriority;
        this.transPolicy = transPolicy;
        this.maxSusTrafficRate = maxSusTrafficRate;
        this.maxTrafficBurst = maxTrafficBurst;
        this.minResTrafficRate = minResTrafficRate;
        this.assumedMinConcatBurst = assumedMinConcatBurst;
        this.maxConcatBurst = maxConcatBurst;
        this.upPeakTrafficRate = upPeakTrafficRate;
        this.reqAttrMask = reqAttrMask;
        this.forbidAttrMask = forbidAttrMask;
        this.attrAggRuleMask = attrAggRuleMask;
        this.minBuffer = minBuffer;
        this.targetBuffer = targetBuffer;
        this.maxBuffer = maxBuffer;
    }

    /**
     * Returns a List of Bytes which can be parsed back into an equivalent object. This method is generally used when
     * streaming this object over a Socket
     * @return - the byte translation of this object
     */
    public List<Byte> getBytes() {
        final List<Byte> byteList = new ArrayList<>();
        byteList.addAll(Bytes.asList(trafficPriority, (byte) 0, (byte) 0, (byte) 0));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(transPolicy)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(maxSusTrafficRate)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(maxTrafficBurst)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(minResTrafficRate)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(assumedMinConcatBurst)));
        byteList.addAll(Bytes.asList(COPSMsgParser.shortToBytes(maxConcatBurst)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(upPeakTrafficRate)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(reqAttrMask)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(forbidAttrMask)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(attrAggRuleMask)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(minBuffer)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(targetBuffer)));
        byteList.addAll(Bytes.asList(COPSMsgParser.intToBytes(maxBuffer)));
        return byteList;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BEEnvelop)) {
            return false;
        }

        final BEEnvelop beEnvelop = (BEEnvelop) o;

        return trafficPriority == beEnvelop.trafficPriority && transPolicy == beEnvelop.transPolicy &&
                maxSusTrafficRate == beEnvelop.maxSusTrafficRate && maxTrafficBurst == beEnvelop.maxTrafficBurst &&
                minResTrafficRate == beEnvelop.minResTrafficRate &&
                assumedMinConcatBurst == beEnvelop.assumedMinConcatBurst &&
                maxConcatBurst == beEnvelop.maxConcatBurst && upPeakTrafficRate == beEnvelop.upPeakTrafficRate &&
                reqAttrMask == beEnvelop.reqAttrMask && forbidAttrMask == beEnvelop.forbidAttrMask &&
                attrAggRuleMask == beEnvelop.attrAggRuleMask && minBuffer == beEnvelop.minBuffer &&
                targetBuffer == beEnvelop.targetBuffer && maxBuffer == beEnvelop.maxBuffer;

    }

    @Override
    public int hashCode() {
        int result = (int) trafficPriority;
        result = 31 * result + transPolicy;
        result = 31 * result + maxSusTrafficRate;
        result = 31 * result + maxTrafficBurst;
        result = 31 * result + minResTrafficRate;
        result = 31 * result + (int) assumedMinConcatBurst;
        result = 31 * result + (int) maxConcatBurst;
        result = 31 * result + upPeakTrafficRate;
        result = 31 * result + reqAttrMask;
        result = 31 * result + forbidAttrMask;
        result = 31 * result + attrAggRuleMask;
        result = 31 * result + minBuffer;
        result = 31 * result + targetBuffer;
        result = 31 * result + maxBuffer;
        return result;
    }

    /**
     * Returns an BEEnvelop object from a byte array
     * @param data - the data to parse
     * @return - the object or null if cannot be parsed
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static BEEnvelop parse(final byte[] data) {
        if (data.length != 52) return null;
        return new BEEnvelop(data[0],
                COPSMsgParser.bytesToInt(data[4], data[5], data[6], data[7]),
                COPSMsgParser.bytesToInt(data[8], data[9], data[10], data[11]),
                COPSMsgParser.bytesToInt(data[12], data[13], data[14], data[15]),
                COPSMsgParser.bytesToInt(data[16], data[17], data[18], data[19]),
                COPSMsgParser.bytesToShort(data[20], data[21]),
                COPSMsgParser.bytesToShort(data[22], data[23]),
                COPSMsgParser.bytesToInt(data[24], data[25], data[26], data[27]),
                COPSMsgParser.bytesToInt(data[28], data[29], data[30], data[31]),
                COPSMsgParser.bytesToInt(data[32], data[33], data[34], data[35]),
                COPSMsgParser.bytesToInt(data[36], data[37], data[38], data[39]),
                COPSMsgParser.bytesToInt(data[40], data[41], data[42], data[43]),
                COPSMsgParser.bytesToInt(data[44], data[45], data[46], data[47]),
                COPSMsgParser.bytesToInt(data[48], data[49], data[50], data[51]));
    }
}
