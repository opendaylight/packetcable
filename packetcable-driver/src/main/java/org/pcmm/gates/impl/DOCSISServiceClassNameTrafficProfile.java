/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITrafficProfile;

import java.util.Arrays;

/**
 * The DOCSIS Service Class Name object defines the preconfigured Service Class Name associated with a Gate.
 *
 */
public class DOCSISServiceClassNameTrafficProfile extends PCMMBaseObject implements ITrafficProfile {

    public static final byte STYPE = 2;
    public static final int SCN_MAX_LEN = 16;

    /**
     * The Service Class Name. REQUIRED and length must be >= 2 and <= 16 characters
     */
    private final String scnName;

    /**
     * The envelope
     */
    private final byte envelope;

    /**
     * Constructor using the default envelope value
     * @param scnName - the service class name (required characters >=2 && <=16
     */
    public DOCSISServiceClassNameTrafficProfile(final String scnName) {
        this(DEFAULT_ENVELOP, scnName);
    }

    /**
     * Constructor to set all values
     * @param envelope - the envelope value
     * @param scnName - the service class name (required number of characters >=2 && <=16)
     */
    protected DOCSISServiceClassNameTrafficProfile(final byte envelope, final String scnName) {
        super(SNum.TRAFFIC_PROFILE, STYPE);
        if (scnName == null || scnName.length() < 2 || scnName.length() > SCN_MAX_LEN)
            throw new IllegalArgumentException("Service class name must be between 2-16 characters");
        this.scnName = scnName;
        this.envelope = envelope;
    }

    @Override
    public byte getEnvelop() {
        return envelope;
    }

    /**
     * Returns the service class name value
     * @return - the SCN value
     */
    public String getScnName() {
        return scnName;
    }

    @Override
    protected byte[] getBytes() {
        final int padLen;
        if ((scnName.length() % 4) != 0) padLen = 4 - (scnName.length() % 4);
        else padLen = 0;

        final byte[] data = new byte[4 + scnName.getBytes().length + padLen];
        Arrays.fill(data, (byte) 0);
        data[0] = envelope;

        System.arraycopy(scnName.getBytes(), 0, data, 4, scnName.getBytes().length);
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DOCSISServiceClassNameTrafficProfile)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DOCSISServiceClassNameTrafficProfile that = (DOCSISServiceClassNameTrafficProfile) o;
        return envelope == that.envelope && scnName.equals(that.scnName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + scnName.hashCode();
        result = 31 * result + (int) envelope;
        return result;
    }

    /**
     * Returns a DOCSISServiceClassNameTrafficProfile object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static DOCSISServiceClassNameTrafficProfile parse(final byte[] data) {
        // variable i will denote the index where the data padding starts (if any) or the end of the array
        int i = 4;
        for (; i < data.length; i++) {
            if (data[i] == 0) {
                break;
            }
        }
        final int nameLength = i - 4;

        final byte[] scnNameBytes = new byte[nameLength];
        System.arraycopy(data, 4, scnNameBytes, 0, nameLength);
        final String scnName = new String(scnNameBytes);
        return new DOCSISServiceClassNameTrafficProfile(data[0], scnName);
    }

}
