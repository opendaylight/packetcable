/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.net.UnknownHostException;

/**
 * COPS IPv4 Interface
 *
 * @version COPSIpv4Interface.java, v 1.00 2003
 *
 */
public abstract class COPSIpv4Interface extends COPSInterface {

    protected COPSObjHeader _objHdr;
    private COPSIpv4Address _addr;
    private int _ifindex;


    /**
     * Method isIpv4Address
     *
     * @return   a boolean
     *
     */
    public boolean isIpv4Address() {
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
     * Method getIpAddress
     *
     * @return   an int
     *
     */
    public int getIpAddress() {
        return (_addr.getIpAddress());
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

    protected COPSIpv4Interface() {
        _objHdr = new COPSObjHeader();
        _objHdr.setCType((byte) 1);
        _objHdr.setDataLength((short) (_addr.getDataLength() + 4));
    }

    protected COPSIpv4Interface(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        byte[] buf = new byte[4];
        System.arraycopy(dataPtr,4,buf,0,4);

        _addr.parse(buf);

        _ifindex |= ((int) dataPtr[8]) << 24;
        _ifindex |= ((int) dataPtr[9]) << 16;
        _ifindex |= ((int) dataPtr[10]) << 8;
        _ifindex |= ((int) dataPtr[11]) & 0xFF;

        _objHdr.setDataLength((short) (_addr.getDataLength() + 4));
    }

}




