/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

/**
 * COPS IPv4 Interface
 *
 * @version COPSIpv4Interface.java, v 1.00 2003
 *
 */
public abstract class COPSIpv4Interface extends COPSInterface {

    /**
     * Constructor
     * @param objHdr - the header
     * @param ifindex - the interface value
     * @param addr - the address object
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpv4Interface(final COPSObjHeader objHdr, final COPSIpv4Address addr, final int ifindex) {
        super(objHdr, addr, ifindex);
    }

    @Override
    public boolean isIPv6() { return false; }

    /**
     * Creates a COPSIpv4Address object from a byte array.
     * @param dataPtr - the byte array
     * @return - the address
     * @throws java.lang.IllegalArgumentException
     */
    protected static COPSIpv4Address parseAddress(final byte[] dataPtr) {
        byte[] buf = new byte[4];
        buf[0] = dataPtr[4];
        buf[1] = dataPtr[5];
        buf[2] = dataPtr[6];
        buf[3] = dataPtr[7];
        return new COPSIpv4Address(buf);
    }

    /**
     * Parses the ifindex value from a byte array.
     * @param dataPtr - the byte array
     * @return - the index value
     */
    protected static int parseIfIndex(final byte[] dataPtr) {
        int ifindex = 0;
        ifindex |= ((int) dataPtr[8]) << 24;
        ifindex |= ((int) dataPtr[9]) << 16;
        ifindex |= ((int) dataPtr[10]) << 8;
        ifindex |= ((int) dataPtr[11]) & 0xFF;
        return ifindex;
    }

}




