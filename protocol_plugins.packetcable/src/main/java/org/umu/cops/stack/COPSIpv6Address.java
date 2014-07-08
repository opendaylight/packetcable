/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * COPS IPv6 Address
 *
 * @version COPSIpv6Address.java, v 1.00 2003
 *
 */
public class COPSIpv6Address {

    private byte[] _addr;

    public COPSIpv6Address() {
        _addr = new byte[16];
    }

    public COPSIpv6Address(String hostName) throws UnknownHostException {
        setIpAddress(hostName);
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
        _addr = InetAddress.getByName(hostName).getAddress();
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
        return InetAddress.getByAddress(_addr).getHostName();
    }

    /**
     * Method parse
     *
     * @param    dataPtr             a  byte[]
     *
     */
    public void parse(byte[] dataPtr) {
        new ByteArrayInputStream(dataPtr).read(_addr,0,16);
    }

    /**
     * Method getDataLength
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        return (16);
    }

    /**
     * Write data on a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        COPSUtil.writeData(id, _addr, 16);
    }

}

