/*
 * Copyright (c) 2016 Applied Broadband, Inc. All Rights Reserved
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/* -----------------------------------------------------------------------------
 * This source code is provided under the terms of the Applied Broadband,
 * Inc End User License Agreement (EULA). Any other use of this source 
 * code is strictly prohibited.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *---------------------------------------------------------------------------*/
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.utils.PCMMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.nio.ByteBuffer;

/**
 * The DOCSIS Flow Spec object defines the Flow Spec Traffic Profile of a Gate.
 *
 */
public class DOCSISFlowSpecTrafficProfile extends PCMMBaseObject implements ITrafficProfile {

    private final Logger logger = LoggerFactory.getLogger(DOCSISFlowSpecTrafficProfile.class);
    public static final byte STYPE = 1;
    public static final byte SERVICE_NUMBER = 2;

    /**
     * The envelope
     */
    private final byte envelope;
    private final int tokenBucketRate;
    private final int tokenBucketSize;
    private final int peakDataRate;
    private final int minimumPolicedUnit;
    private final int maximumPacketSize;
    private final int rate;
    private final int slackTerm;
    
    /**
     * Constructor using the default envelope values
     * @param tokenBucketRate - Token Bucket Rate value [r]
     * @param tokenBucketSize - Token Bucket Size value [b]
     * @param peakDataRate - Peak Data Rate value [p]
     * @param minimumPolicedUnit - Minimum Policed Unit value [m]
     * @param maximumPacketSize - Maximum Packet Size value [M]
     * @param rate - Rate value [R]
     * @param slackTerm - Slack Term value [S]
     */
    public DOCSISFlowSpecTrafficProfile(final int tokenBucketRate,
                                        final int tokenBucketSize,
                                        final int peakDataRate,
                                        final int minimumPolicedUnit,
                                        final int maximumPacketSize,
                                        final int rate,
                                        final int slackTerm ) {
        this(DEFAULT_ENVELOP, tokenBucketRate, tokenBucketSize, peakDataRate,
             minimumPolicedUnit, maximumPacketSize, rate, slackTerm);
    }

    /**
     * Constructor to set all values
     * @param envelope - the envelope value
     * @param tokenBucketRate - Token Bucket Rate value [r]
     * @param tokenBucketSize - Token Bucket Size value [b]
     * @param peakDataRate - Peak Data Rate value [p]
     * @param minimumPolicedUnit - Minimum Policed Unit value [m]
     * @param maximumPacketSize - Maximum Packet Size value [M]
     * @param rate - Rate value [R]
     * @param slackTerm - Slack Term value [S]
     */
    protected DOCSISFlowSpecTrafficProfile(final byte envelope,
                                           final int tokenBucketRate,
                                           final int tokenBucketSize,
                                           final int peakDataRate,
                                           final int minimumPolicedUnit,
                                           final int maximumPacketSize,
                                           final int rate,
                                           final int slackTerm) {
        super(SNum.TRAFFIC_PROFILE, STYPE);
        this.tokenBucketRate = tokenBucketRate;
        this.tokenBucketSize = tokenBucketSize;
        this.peakDataRate = peakDataRate;
        this.minimumPolicedUnit = minimumPolicedUnit;
        this.maximumPacketSize = maximumPacketSize;
        this.rate = rate;
        this.slackTerm = slackTerm;
        this.envelope = envelope;
    }

    @Override
    public byte getEnvelop() {
        return envelope;
    }

    /**
     * Returns the token bucket rate value
     * @return - the token bucket rate value
     */
    public int getTokenBucketRate() {
        return tokenBucketRate;
    }

    /**
     * Returns the token bucket size value
     * @return - the token bucket size value
     */
    public int getTokenBucketSize() {
        return tokenBucketSize;
    }

    /**
     * Returns the peak data rate value
     * @return - the peak data rate value
     */
    public int getPeakDataRate() {
        return peakDataRate;
    }

    /**
     * Returns the minimum policed unit value
     * @return - the minimum policed unit value
     */
    public int getMinimumPolicedUnit() {
        return minimumPolicedUnit;
    }

    /**
     * Returns the maximum packet size value
     * @return - the maximum packet size value
     */
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    /**
     * Returns the rate value
     * @return - the rate value
     */
    public int getRate() {
        return rate;
    }

    /**
     * Returns the slack term value
     * @return - the slack term value
     */
    public int getSlackTerm() {
        return slackTerm;
    }

    @Override
    protected byte[] getBytes() {
        final byte[] data = new byte[4+(4*7*3)];
        
        float fTokenBucketRate = tokenBucketRate;
        float fTokenBucketSize = tokenBucketSize;
        float fPeakDataRate = peakDataRate;
        float fRate = rate;

        //
        // Ok I know this looks crazy but PCMM Flow Spec encodes some values as floats
        // even though they do not contain fractional values, so we 'integerize' them
        // in the constructor and class internals
        //
        Arrays.fill(data, (byte) 0);
        data[0] = envelope;
        data[1] = SERVICE_NUMBER;
        data[2] = 0; // reserved
        data[3] = 0; // reserved
        
        // Authorized Envelope
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fTokenBucketRate).array(), 0, data, 4, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fTokenBucketSize).array(), 0, data, 8, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fPeakDataRate).array(), 0, data, 12, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(minimumPolicedUnit).array(), 0, data, 16, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(maximumPacketSize).array(), 0, data, 20, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fRate).array(), 0, data, 24, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(slackTerm).array(), 0, data, 28, 4);
        
        // Reserved Envelope
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fTokenBucketRate).array(), 0, data, 32, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fTokenBucketSize).array(), 0, data, 36, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fPeakDataRate).array(), 0, data, 40, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(minimumPolicedUnit).array(), 0, data, 44, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(maximumPacketSize).array(), 0, data, 48, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fRate).array(), 0, data, 52, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(slackTerm).array(), 0, data, 56, 4);
        
        // Committed Envelope
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fTokenBucketRate).array(), 0, data, 60, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fTokenBucketSize).array(), 0, data, 64, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fPeakDataRate).array(), 0, data, 68, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(minimumPolicedUnit).array(), 0, data, 72, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(maximumPacketSize).array(), 0, data, 76, 4);
        System.arraycopy(ByteBuffer.allocate(4).putFloat(fRate).array(), 0, data, 80, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(slackTerm).array(), 0, data, 84, 4);
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DOCSISFlowSpecTrafficProfile)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DOCSISFlowSpecTrafficProfile that = (DOCSISFlowSpecTrafficProfile) o;
        return (envelope == that.envelope) &&
            (tokenBucketRate == that.tokenBucketRate) &&
            (tokenBucketSize == that.tokenBucketSize) &&
            (peakDataRate == that.peakDataRate) &&
            (minimumPolicedUnit == that.minimumPolicedUnit) &&
            (maximumPacketSize == that.maximumPacketSize) &&
            (rate == that.rate) &&
            (slackTerm == that.slackTerm);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + tokenBucketRate;
        result = 31 * result + tokenBucketSize;
        result = 31 * result + peakDataRate;
        result = 31 * result + minimumPolicedUnit;
        result = 31 * result + maximumPacketSize;
        result = 31 * result + rate;
        result = 31 * result + slackTerm;        
        result = 31 * result + (int) envelope;
        return result;
    }

    private static int byteToInt(final byte[] data, int start) {
        return (int)(data[start]   << 24 |
                     data[start+1] << 16 |
                     data[start+2] << 8  |
                     data[start+3]);
    }

    private static float byteToFloat(final byte[] data, int start) {
        return (float)(data[start]   << 24 |
                       data[start+1] << 16 |
                       data[start+2] << 8  |
                       data[start+3]);
    }
    
    /**
     * Returns a DOCSISFlowSpecTrafficProfile object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static DOCSISFlowSpecTrafficProfile parse(final byte[] data) {
        byte env = 0;
        int mpu=0,mps=0,st=0;
        float tbr=0,tbs=0,pdr=0,r=0;

        env = data[0];

        tbr = byteToFloat(data, 4);
        tbs = byteToFloat(data, 8);
        pdr = byteToFloat(data, 12);
        mpu = byteToInt(data, 16);
        mps = byteToInt(data, 20);
        r   = byteToFloat(data, 24);
        st  = byteToInt(data, 28);
        //
        // Ok I know this looks crazy but PCMM Flow Spec encodes some values as floats
        // even though they do not contain fractional values, so we 'integerize' them
        // in the constructor and class internals
        //
        int itbr = Math.round(tbr), itbs = Math.round(tbs),
            ipdr = Math.round(pdr), ir = Math.round(r);
        
        return new DOCSISFlowSpecTrafficProfile(env, itbr, itbs, ipdr,
                                                mpu, mps, ir, st);
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
    sb.append("TokenBucketRate: ").append(tokenBucketRate);
    sb.append(variableSeparator);
    sb.append("TokenBucketSize: ").append(tokenBucketSize);
    sb.append(variableSeparator);
    sb.append("PeakDataRate: ").append(peakDataRate);
    sb.append(variableSeparator);
    sb.append("MinimumPolicedUnit: ").append(minimumPolicedUnit);
    sb.append(variableSeparator);
    sb.append("MaximumPacketSize: ").append(maximumPacketSize);
    sb.append(variableSeparator);
    sb.append("Rate: ").append(rate);
    sb.append(variableSeparator);
    sb.append("SlackTerm: ").append(slackTerm);

    return sb.toString();
  }
}
