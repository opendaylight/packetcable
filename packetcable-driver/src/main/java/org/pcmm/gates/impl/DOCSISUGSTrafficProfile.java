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
 * The DOCSIS UGS object defines the UGS Traffic Profile of a Gate.
 *
 */
public class DOCSISUGSTrafficProfile extends PCMMBaseObject implements ITrafficProfile {

    private final Logger logger = LoggerFactory.getLogger(DOCSISUGSTrafficProfile.class);
    public static final byte STYPE = 6;

    /**
     * The envelope
     */
    private final byte envelope;
    private final int requestedTransmissionPolicy;
    private final int unsolicitedGrantSize;
    private final int grantsPerInterval;
    private final int nominalGrantInterval;
    private final int toleratedGrantJitter;
    private final int upstreamPeakTrafficRate;
    private final int requiredAttributeMask;
    private final int forbiddenAttributeMask;
    private final int attributeAggregationRuleMask;
    
    /**
     * Constructor using the default envelope values
     * @param requestedTransmissionPolicy - Request Transmission Policy
     * @param unsolicitedGrantSize - Unsolicited Grant Size
     * @param grantsPerInterval - Grants per Interval
     * @param nominalGrantInterval - Nominal Grant Interval
     * @param toleratedGrantJitter - Tolerated Grant Jitter
     * @param upstreamPeakTrafficRate - Upstream Peak Traffic Rate
     * @param requiredAttributeMask - Required Attribute Mask
     * @param forbiddenAttributeMask - Forbidden Attribute Mask
     * @param attributeAggregationRuleMask - Attribute Aggregation Rule Mask
     */
    public DOCSISUGSTrafficProfile(final Long requestedTransmissionPolicy,
                                   final Long unsolicitedGrantSize,
                                   final Short grantsPerInterval,
                                   final Long nominalGrantInterval,
                                   final Long toleratedGrantJitter,
                                   final Long upstreamPeakTrafficRate,
                                   final Long requiredAttributeMask,
                                   final Long forbiddenAttributeMask,
                                   final Long attributeAggregationRuleMask ) {
        
        this(DEFAULT_ENVELOP,
             requestedTransmissionPolicy.intValue(),
             unsolicitedGrantSize.intValue(), 
             grantsPerInterval.intValue(),
             nominalGrantInterval.intValue(),
             toleratedGrantJitter.intValue(),
             upstreamPeakTrafficRate.intValue(),
             requiredAttributeMask.intValue(),
             forbiddenAttributeMask.intValue(), 
             attributeAggregationRuleMask.intValue());
    }

    /**
     * Constructor to set all values
     * @param envelope - the envelope value
     * @param requestedTransmissionPolicy - Request Transmission Policy
     * @param unsolicitedGrantSize - Unsolicited Grant Size
     * @param grantsPerInterval - Grants per Interval
     * @param nominalGrantInterval - Nominal Grant Interval
     * @param toleratedGrantJitter - Tolerated Grant Jitter
     * @param upstreamPeakTrafficRate - Upstream Peak Traffic Interval
     * @param requiredAttributeMask - Required Attribute Mask
     * @param forbiddenAttributeMask - Forbidden Attribute Mask
     * @param attributeAggregationRuleMask - Attribute Aggregation Rule Mask
     */
    protected DOCSISUGSTrafficProfile(final byte envelope,
                               final int requestedTransmissionPolicy,   
                               final int unsolicitedGrantSize,
                               final int grantsPerInterval,
                               final int nominalGrantInterval,
                               final int toleratedGrantJitter,
                               final int upstreamPeakTrafficRate,
                               final int requiredAttributeMask,
                               final int forbiddenAttributeMask,
                               final int attributeAggregationRuleMask ) {
        super(SNum.TRAFFIC_PROFILE, STYPE);
        this.requestedTransmissionPolicy = requestedTransmissionPolicy;
        this.unsolicitedGrantSize = unsolicitedGrantSize;
        this.grantsPerInterval = grantsPerInterval;
        this.nominalGrantInterval = nominalGrantInterval;
        this.toleratedGrantJitter = toleratedGrantJitter;
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
    public int getRequestedTransmissionPolicy() {
        return requestedTransmissionPolicy;
    }

    /**
     * Returns the Unsolicited Grant Size value
     * @return - the Unsolicited Grant Size value
     */
    public int getUnsolicitedGrantSize() {
        return unsolicitedGrantSize;
    }

    /**
     * Returns the Grants per Interval value
     * @return - the Grants per Interval value
     */
    public int getGrantsPerInterval() {
        return grantsPerInterval;
    }

    /**
     * Returns the Nominal Grant Interval value
     * @return - the Nominal Grant Interval value
     */
    public int getNominalGrantInterval() {
        return nominalGrantInterval;
    }

    /**
     * Returns the Tolerated Grant Jitter value
     * @return - the Tolerated Grant Jitter value
     */
    public int getToleratedGrantJitter() {
        return toleratedGrantJitter;
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
        final byte[] data = new byte[88];
        
        Arrays.fill(data, (byte) 0);
        data[0] = envelope;
        data[1] = 0; // reserved
        data[2] = 0; // reserved
        data[3] = 0; // reserved
        
        int value = ((int)unsolicitedGrantSize<<16) | ((int)grantsPerInterval<<8);
        int zero = 0;
  
        // Authorized Envelope
        System.arraycopy(ByteBuffer.allocate(4).putInt(requestedTransmissionPolicy).array(), 0, data, 4, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(value).array(), 0, data, 8, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(nominalGrantInterval).array(), 0, data, 12, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(toleratedGrantJitter).array(), 0, data, 16, 4);
        //System.arraycopy(ByteBuffer.allocate(4).putInt(upstreamPeakTrafficRate).array(), 0, data, 20, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(requiredAttributeMask).array(), 0, data, 20, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(forbiddenAttributeMask).array(), 0, data, 24, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(attributeAggregationRuleMask).array(), 0, data, 28, 4);

        // Reserved Envelope
        System.arraycopy(ByteBuffer.allocate(4).putInt(requestedTransmissionPolicy).array(), 0, data, 32, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(value).array(), 0, data, 36, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(nominalGrantInterval).array(), 0, data, 40, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(toleratedGrantJitter).array(), 0, data, 44, 4);
        //System.arraycopy(ByteBuffer.allocate(4).putInt(upstreamPeakTrafficRate).array(), 0, data, 52, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(requiredAttributeMask).array(), 0, data, 48, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(forbiddenAttributeMask).array(), 0, data, 52, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(attributeAggregationRuleMask).array(), 0, data, 56, 4);

        // Committed Envelope
        System.arraycopy(ByteBuffer.allocate(4).putInt(requestedTransmissionPolicy).array(), 0, data, 60, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(value).array(), 0, data, 64, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(nominalGrantInterval).array(), 0, data, 68, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(toleratedGrantJitter).array(), 0, data, 72, 4);
        //System.arraycopy(ByteBuffer.allocate(4).putInt(upstreamPeakTrafficRate).array(), 0, data, 84, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(requiredAttributeMask).array(), 0, data, 76, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(forbiddenAttributeMask).array(), 0, data, 80, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(attributeAggregationRuleMask).array(), 0, data, 84, 4);

        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DOCSISUGSTrafficProfile)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DOCSISUGSTrafficProfile that = (DOCSISUGSTrafficProfile) o;
        return (envelope == that.envelope) &&
            (requestedTransmissionPolicy == that.requestedTransmissionPolicy) &&
            (unsolicitedGrantSize == that.unsolicitedGrantSize) &&
            (grantsPerInterval == that.grantsPerInterval) &&
            (nominalGrantInterval == that.nominalGrantInterval) &&
            (toleratedGrantJitter == that.toleratedGrantJitter) &&
            (upstreamPeakTrafficRate == that.upstreamPeakTrafficRate) &&
            (requiredAttributeMask == that.requiredAttributeMask) &&
            (forbiddenAttributeMask == that.forbiddenAttributeMask) &&
            (attributeAggregationRuleMask == that.attributeAggregationRuleMask);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + requestedTransmissionPolicy;
        result = 31 * result + unsolicitedGrantSize;
        result = 31 * result + grantsPerInterval;
        result = 31 * result + nominalGrantInterval;
        result = 31 * result + toleratedGrantJitter;
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
     * Returns a DOCSISUGSProfile object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static DOCSISUGSTrafficProfile parse(final byte[] data) {
        byte env = 0, gpi = 0;
        int rtp, ugs,ngi,tgj,uptr = 0,ram,fam,aarm;

        env = data[0];

        rtp = byteToInt(data, 4);
        ugs = shortToInt(data, 8);
        gpi = data[10];
        ngi = byteToInt(data, 12);
        tgj = byteToInt(data, 16);
        //uptr= byteToInt(data, 20);
        ram = byteToInt(data, 20);
        fam = byteToInt(data, 24);
        aarm= byteToInt(data, 28);
        
        return new DOCSISUGSTrafficProfile(env,rtp,ugs,gpi,ngi,tgj,uptr,ram,fam,aarm);
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
    sb.append("RequestedTransmissionPolicy: ").append(requestedTransmissionPolicy);
    sb.append(variableSeparator);
    sb.append("UnsolicitedGrantSize: ").append(unsolicitedGrantSize);
    sb.append(variableSeparator);
    sb.append("GrantsPerInterval: ").append(grantsPerInterval);
    sb.append(variableSeparator);
    sb.append("NominalGrantInterval: ").append(nominalGrantInterval);
    sb.append(variableSeparator);
    sb.append("ToleratedGrantJitter: ").append(toleratedGrantJitter);
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
