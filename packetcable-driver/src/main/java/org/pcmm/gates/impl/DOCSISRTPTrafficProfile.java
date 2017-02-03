/*
 * Copyright (c) 2016 Applied Broadband, Inc. All Rights Reserved
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.utils.PCMMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.nio.ByteBuffer;

/**
 * The DOCSIS RTP object defines the RTP Traffic Profile of a Gate.
 *
 */
public class DOCSISRTPTrafficProfile extends PCMMBaseObject implements ITrafficProfile {

    private final Logger logger = LoggerFactory.getLogger(DOCSISRTPTrafficProfile.class);
    public static final byte STYPE = 5;

    /**
     * The envelope
     */
    private final byte envelope;
    private final int requestTransmissionPolicy;
    private final int maximumSustainedTrafficRate;
    private final int maximumTrafficBurst;
    private final int minimumReservedTrafficRate;
    private final int amrtrPacketSize;
    private final int maximumConcatenatedBurst;
    private final int nominalPollingInterval;
    private final int toleratedPollJitter;
    private final int upstreamPeakTrafficRate;
    private final int requiredAttributeMask;
    private final int forbiddenAttributeMask;
    private final int attributeAggregationRuleMask;
    
    /**
     * Constructor using the default envelope values
     * @param requestTransmissionPolicy - Request Transmission Policy
     * @param maximumSustainedTrafficRate - Maximum Sustained Traffic Rate
     * @param maximumTrafficBurst - Maximum Traffic Burst
     * @param minimumReservedTrafficRate - Minimum Reserved Traffic Rate
     * @param amrtrPacketSize - Assumed Minimum Reserved Traffic Rate Packet Size
     * @param maximumConcatenatedBurst - Maximum Concatenated Burst
     * @param nominalPollingInterval - Nominal Polling Interval
     * @param toleratedPollJitter - Tolerated Poll Jitter
     * @param upstreamPeakTrafficRate - Upstream Peak Traffic Rate
     * @param requiredAttributeMask - Required Attribute Mask
     * @param forbiddenAttributeMask - Forbidden Attribute Mask
     * @param attributeAggregationRuleMask - Attribute Aggregation Rule Mask
     */
    public DOCSISRTPTrafficProfile(final Long requestTransmissionPolicy,
                                   final Long maximumSustainedTrafficRate,
                                   final Long maximumTrafficBurst,
                                   final Long minimumReservedTrafficRate,
                                   final Long amrtrPacketSize,
                                   final Long maximumConcatenatedBurst,
                                   final Long nominalPollingInterval,
                                   final Long toleratedPollJitter,
                                   final Long upstreamPeakTrafficRate,
                                   final Long requiredAttributeMask,
                                   final Long forbiddenAttributeMask,
                                   final Long attributeAggregationRuleMask ) {
        
        this(DEFAULT_ENVELOP,
             requestTransmissionPolicy.intValue(),
             maximumSustainedTrafficRate.intValue(), 
             maximumTrafficBurst.intValue(),
             minimumReservedTrafficRate.intValue(),
             amrtrPacketSize.intValue(),
             maximumConcatenatedBurst.intValue(),
             nominalPollingInterval.intValue(),
             toleratedPollJitter.intValue(),
             upstreamPeakTrafficRate.intValue(),
             requiredAttributeMask.intValue(),
             forbiddenAttributeMask.intValue(), 
             attributeAggregationRuleMask.intValue());
    }

    /**
     * Constructor to set all values
     * @param envelope - the envelope value
     * @param requestTransmissionPolicy - Request Transmission Policy
     * @param maximumSustainedTrafficRate - Maximum Sustained Traffic Rate
     * @param maximumTrafficBurst - Maximum Traffic Burst
     * @param minimumReservedTrafficRate - Minimum Reserved Traffic Rate
     * @param amrtrPacketSize - Assumed Minimum Reserved Traffic Rate Packet Size
     * @param maximumConcatenatedBurst - Maximum Concatenated Burst
     * @param nominalPollingInterval - Nominal Polling Interval
     * @param toleratedPollJitter - Tolerated Poll Jitter
     * @param upstreamPeakTrafficRate - Upstream Peak Traffic Rate
     * @param requiredAttributeMask - Required Attribute Mask
     * @param forbiddenAttributeMask - Forbidden Attribute Mask
     * @param attributeAggregationRuleMask - Attribute Aggregation Rule Mask
     */
    protected DOCSISRTPTrafficProfile(final byte envelope,
                               final int requestTransmissionPolicy,   
                               final int maximumSustainedTrafficRate,
                               final int maximumTrafficBurst,
                               final int minimumReservedTrafficRate,
                               final int amrtrPacketSize,
                               final int maximumConcatenatedBurst,
                               final int nominalPollingInterval,
                               final int toleratedPollJitter,
                               final int upstreamPeakTrafficRate,
                               final int requiredAttributeMask,
                               final int forbiddenAttributeMask,
                               final int attributeAggregationRuleMask ) {
        super(SNum.TRAFFIC_PROFILE, STYPE);
        this.requestTransmissionPolicy = requestTransmissionPolicy;
        this.maximumSustainedTrafficRate = maximumSustainedTrafficRate;
        this.maximumTrafficBurst = maximumTrafficBurst;
        this.minimumReservedTrafficRate = minimumReservedTrafficRate;
        this.amrtrPacketSize = amrtrPacketSize;
        this.maximumConcatenatedBurst = maximumConcatenatedBurst;
        this.nominalPollingInterval = nominalPollingInterval;
        this.toleratedPollJitter = toleratedPollJitter;
        this.upstreamPeakTrafficRate = upstreamPeakTrafficRate;
        this.requiredAttributeMask = requiredAttributeMask;
        this.forbiddenAttributeMask = forbiddenAttributeMask;
        this.attributeAggregationRuleMask = attributeAggregationRuleMask;
        this.envelope = envelope;
    }

    @Override
    public byte getEnvelop() {
        return envelope;
    }

    /**
     * Returns the Request Transmission Policy value
     * @return - the Request Transmission Policy value
     */
    public int getRequestTransmissionPolicy() {
        return requestTransmissionPolicy;
    }

    /**
     * Returns the Maximum Sustained Traffic Rate value
     * @return - the Maximum Sustained Traffic Rate value
     */
    public int getMaximumSustainedTrafficRate() {
        return maximumSustainedTrafficRate;
    }

    /**
     * Returns the Maximum Traffic Burst value
     * @return - the Maximum Traffic Burst value
     */
    public int getMaximumTrafficBurst() {
        return maximumTrafficBurst;
    }

    /**
     * Returns the Minimum Reserved Traffic Rate value
     * @return - the Minimum Reserved Traffic Rate value
     */
    public int getMinimumReservedTrafficRate() {
        return minimumReservedTrafficRate;
    }

    /**
     * Returns the Assumed Minimum Reserved Traffic Rate Packet Size value
     * @return - the Assumed Minimum Reserved Traffic Rate Packet Size value
     */
    public int getAmrtrPacketSize() {
        return amrtrPacketSize;
    }

    /**
     * Returns the Maximum Concatenated Burst value
     * @return - the Maximum Concatenated Burst value
     */
    public int getMaximumConcatenatedBurst() {
        return maximumConcatenatedBurst;
    }

    /**
     * Returns the Nominal Polling Interval value
     * @return - the Nominal Polling Interval value
     */
    public int getNominalPollingInterval() {
        return nominalPollingInterval;
    }

    /**
     * Returns the Tolerated Poll Jitter value
     * @return - the Tolerated Poll Jitter value
     */
    public int getToleratedPollJitter() {
        return toleratedPollJitter;
    }

    /**
     * Returns the Upstream Peak Traffic Rate value
     * @return - the maximum packet size value
     */
    public int getUpstreamPeakTrafficRate() {
        return upstreamPeakTrafficRate;
    }

    /**
     * Returns the Required Attribute Mask value
     * @return - the Required Attribute Mask value
     */
    public int getRequiredAttributeMask() {
        return requiredAttributeMask;
    }

    /**
     * Returns the Forbidden Attribute Mask value
     * @return - the Forbidden Attribute Mask value
     */
    public int getForbiddenAttributeMask() {
        return forbiddenAttributeMask;
    }

    /**
     * Returns the Attribute Aggregation Rule Mask value
     * @return - the Attribute Aggregation Rule Mask value
     */
    public int getAttributeAggregationRuleMask() {
        return attributeAggregationRuleMask;
    }

    @Override
    protected byte[] getBytes() {
        final byte[] data = new byte[4+(4*10*3)];
        ByteBuffer buffer = ByteBuffer.wrap(data);

        buffer.put(envelope);
        buffer.put((byte)0); // reserved
        buffer.put((byte)0); // reserved
        buffer.put((byte)0); // reserved

        final int value = ((int)amrtrPacketSize<<16) | (int)maximumConcatenatedBurst;

        // Authorized Envelope
        buffer.putInt(requestTransmissionPolicy);
        buffer.putInt(maximumSustainedTrafficRate);
        buffer.putInt(maximumTrafficBurst);
        buffer.putInt(minimumReservedTrafficRate);
        buffer.putInt(value);
        buffer.putInt(nominalPollingInterval);
        buffer.putInt(toleratedPollJitter);
        buffer.putInt(requiredAttributeMask);
        buffer.putInt(forbiddenAttributeMask);
        buffer.putInt(attributeAggregationRuleMask);

        // Reserved Envelope
        buffer.putInt(requestTransmissionPolicy);
        buffer.putInt(maximumSustainedTrafficRate);
        buffer.putInt(maximumTrafficBurst);
        buffer.putInt(minimumReservedTrafficRate);
        buffer.putInt(value);
        buffer.putInt(nominalPollingInterval);
        buffer.putInt(toleratedPollJitter);
        buffer.putInt(requiredAttributeMask);
        buffer.putInt(forbiddenAttributeMask);
        buffer.putInt(attributeAggregationRuleMask);

        // Committed Envelope
        buffer.putInt(requestTransmissionPolicy);
        buffer.putInt(maximumSustainedTrafficRate);
        buffer.putInt(maximumTrafficBurst);
        buffer.putInt(minimumReservedTrafficRate);
        buffer.putInt(value);
        buffer.putInt(nominalPollingInterval);
        buffer.putInt(toleratedPollJitter);
        buffer.putInt(requiredAttributeMask);
        buffer.putInt(forbiddenAttributeMask);
        buffer.putInt(attributeAggregationRuleMask);

        if (buffer.hasRemaining()) {
            logger.error("Original buffer too large");
        }

        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DOCSISRTPTrafficProfile)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DOCSISRTPTrafficProfile that = (DOCSISRTPTrafficProfile) o;
        return (envelope == that.envelope) &&
            (requestTransmissionPolicy == that.requestTransmissionPolicy) &&
            (maximumSustainedTrafficRate == that.maximumSustainedTrafficRate) &&
            (maximumTrafficBurst == that.maximumTrafficBurst) &&
            (minimumReservedTrafficRate == that.minimumReservedTrafficRate) &&
            (amrtrPacketSize == that.amrtrPacketSize) &&
            (maximumConcatenatedBurst == that.maximumConcatenatedBurst) &&
            (nominalPollingInterval == that.nominalPollingInterval) &&
            (toleratedPollJitter == that.toleratedPollJitter) &&
            (upstreamPeakTrafficRate == that.upstreamPeakTrafficRate) &&
            (requiredAttributeMask == that.requiredAttributeMask) &&
            (forbiddenAttributeMask == that.forbiddenAttributeMask) &&
            (attributeAggregationRuleMask == that.attributeAggregationRuleMask);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + requestTransmissionPolicy;
        result = 31 * result + maximumSustainedTrafficRate;
        result = 31 * result + maximumTrafficBurst;
        result = 31 * result + minimumReservedTrafficRate;
        result = 31 * result + amrtrPacketSize;
        result = 31 * result + maximumConcatenatedBurst;
        result = 31 * result + nominalPollingInterval;
        result = 31 * result + toleratedPollJitter;
        result = 31 * result + upstreamPeakTrafficRate;
        result = 31 * result + requiredAttributeMask;        
        result = 31 * result + forbiddenAttributeMask;        
        result = 31 * result + attributeAggregationRuleMask;        
        result = 31 * result + (int) envelope;
        return result;
    }

    private static int byteToInt(final byte[] data, int start) {
        return (int)(data[start]   << 24 |
                     data[start+1] << 16 |
                     data[start+2] << 8  |
                     data[start+3]);
    }

    private static int shortToInt(final byte[] data, int start) {
        return (int)(data[start] << 8  |
                     data[start+1]);
    }

    private static float byteToFloat(final byte[] data, int start) {
        return (float)(data[start]   << 24 |
                       data[start+1] << 16 |
                       data[start+2] << 8  |
                       data[start+3]);
    }
    
    /**
     * Returns a DOCSISRTPProfile object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static DOCSISRTPTrafficProfile parse(final byte[] data) {
        byte env = 0;
        int requestTransmissionPolicy,
            maximumSustainedTrafficRate,
            maximumTrafficBurst,
            minimumReservedTrafficRate,
            amrtrPacketSize,
            maximumConcatenatedBurst,
            nominalPollingInterval,
            toleratedPollJitter,
            upstreamPeakTrafficRate,
            requiredAttributeMask,
            forbiddenAttributeMask,
            attributeAggregationRuleMask;

        env = data[0];

        requestTransmissionPolicy = byteToInt(data, 4);
        maximumSustainedTrafficRate = byteToInt(data, 8);
        maximumTrafficBurst = byteToInt(data, 12);
        minimumReservedTrafficRate = byteToInt(data, 16);
        amrtrPacketSize = shortToInt(data, 20);
        maximumConcatenatedBurst = shortToInt(data, 22);
        nominalPollingInterval = byteToInt(data, 24);
        toleratedPollJitter = byteToInt(data, 28);
        upstreamPeakTrafficRate = byteToInt(data,32);
        requiredAttributeMask = byteToInt(data,36);
        forbiddenAttributeMask = byteToInt(data, 40);
        attributeAggregationRuleMask = byteToInt(data, 44);
        
        return new DOCSISRTPTrafficProfile(env,
                                           requestTransmissionPolicy,
                                           maximumSustainedTrafficRate,
                                           maximumTrafficBurst,
                                           minimumReservedTrafficRate,
                                           amrtrPacketSize,
                                           maximumConcatenatedBurst,
                                           nominalPollingInterval,
                                           toleratedPollJitter,
                                           upstreamPeakTrafficRate,
                                           requiredAttributeMask,
                                           forbiddenAttributeMask,
                                           attributeAggregationRuleMask);
    }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    final int sbSize = 1000;
    final String variableSeparator = "\n";
    final StringBuffer sb = new StringBuffer(sbSize);

    sb.append("Envelope: ").append(envelope);
    sb.append(variableSeparator);
    sb.append("RequestTransmissionPolicy: ").append(requestTransmissionPolicy);
    sb.append(variableSeparator);
    sb.append("MaximumSustainedTrafficRate: ").append(maximumSustainedTrafficRate);
    sb.append(variableSeparator);
    sb.append("MaximumTrafficBurst: ").append(maximumTrafficBurst);
    sb.append(variableSeparator);
    sb.append("MinimumReservedTrafficRate: ").append(minimumReservedTrafficRate);
    sb.append(variableSeparator);
    sb.append("AmrtrPacketSize: ").append(amrtrPacketSize);
    sb.append(variableSeparator);
    sb.append("MaximumConcatenatedBurst: ").append(maximumConcatenatedBurst);
    sb.append(variableSeparator);
    sb.append("NominalPollingInterval: ").append(nominalPollingInterval);
    sb.append(variableSeparator);
    sb.append("ToleratedPollJitter: ").append(toleratedPollJitter);
    sb.append(variableSeparator);
    sb.append("UpstreamPeakTrafficRate: ").append(upstreamPeakTrafficRate);
    sb.append(variableSeparator);
    sb.append("RequiredAttributeMask: ").append(requiredAttributeMask);
    sb.append(variableSeparator);
    sb.append("ForbiddenAttributeMask: ").append(forbiddenAttributeMask);
    sb.append(variableSeparator);
    sb.append("AttributeAggregationRuleMask: ").append(attributeAggregationRuleMask);

    return sb.toString();
  }
}
