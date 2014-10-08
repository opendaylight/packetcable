/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.net.UnknownHostException;

/**
 * COPS IPv6 Interface
 *
 * @version COPSIpv6Interface.java, v 1.00 2003
 *
 */
public abstract class COPSIpv6Interface extends COPSInterface {

    /**
     * Method isIpv6Address
     *
     * @return   a boolean
     *
     */
    public boolean isIpv6Address() {
        return true;
    }

    /**
     * Method setIpAddress
     *
     * @param    hostName            a  String
     *
     * @throws   UnknownHostException
     *
     */
    public void setIpAddress(String hostName) throws UnknownHostException {
        _addr.setIpAddress(hostName);
    }

    /**
     * Method getIpName
     *
     * @return   a String
     *
     * @throws   UnknownHostException
     *
     */
    public String getIpName() throws UnknownHostException {
        return (_addr.getIpName());
    }

    /**
     * Returns size in number of octects, including header
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        //Add the size of the header also
        return (_objHdr.getDataLength());
    }

    protected COPSIpv6Interface() {
        _objHdr = new COPSObjHeader();
        _objHdr.setCType((byte) 2);
        _objHdr.setDataLength((short) (_addr.getDataLength() + 4));
    }

    protected COPSIpv6Interface(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        byte[] buf = new byte[4];
        System.arraycopy(dataPtr,4,buf,0,16);

        _addr.parse(buf);

        _ifindex |= ((int) dataPtr[20]) << 24;
        _ifindex |= ((int) dataPtr[21]) << 16;
        _ifindex |= ((int) dataPtr[22]) << 8;
        _ifindex |= ((int) dataPtr[23]) & 0xFF;

        _objHdr.setDataLength((short) (_addr.getDataLength() + 4));
    }

    protected COPSObjHeader _objHdr;
    private COPSIpv6Address _addr;
    private int _ifindex;
}




