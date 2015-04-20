/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.ByteArrayInputStream;

/**
 * COPS IPv6 Interface
 *
 * @version COPSIpv6Interface.java, v 1.00 2003
 *
 */
public abstract class COPSIpv6Interface extends COPSInterface {

    /**
     * Constructor
     * @param objHdr - the header
     * @param ifindex - the interface value
     * @param addr - the address object
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpv6Interface(final COPSObjHeader objHdr, final COPSIpv6Address addr, final int ifindex) {
        super(objHdr, addr, ifindex);
    }

    @Override
    public boolean isIPv6() { return true; }

    /**
     * Creates a COPSIpv6Address object from a byte array.
     * @param dataPtr - the byte array
     * @return - the address
     * @throws java.lang.IllegalArgumentException
     */
    protected static COPSIpv6Address parseAddress(final byte[] dataPtr) {
        byte[] buf = new byte[16];
        System.arraycopy(dataPtr, 4, buf, 0, 16);
        new ByteArrayInputStream(dataPtr).read(buf, 0, 16);
        return new COPSIpv6Address(buf);
    }

    /**
     * Parses the ifindex value from a byte array.
     * @param dataPtr - the byte array
     * @return - the index value
     */
    protected static int parseIfIndex(final byte[] dataPtr) {
        int ifindex = 0;
        ifindex |= ((int) dataPtr[20]) << 24;
        ifindex |= ((int) dataPtr[21]) << 16;
        ifindex |= ((int) dataPtr[22]) << 8;
        ifindex |= ((int) dataPtr[23]) & 0xFF;
        return ifindex;
    }

}




