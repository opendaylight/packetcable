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
 * COPS IPv4 Address
 *
 * @version COPSIpv4Address.java, v 1.00 2003
 *
 */
public class COPSIpv4Address {

    private byte[] _addr;

    public COPSIpv4Address() {
        _addr = new byte[4];
    }

    public COPSIpv4Address(String hostName) throws UnknownHostException {
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
     * Method getIpAddress
     *
     * @return   an int
     *
     */
    public int getIpAddress() {
        int ipaddr = 0;

        ipaddr |= ((int) _addr[0]) << 24;
        ipaddr |= ((int) _addr[1]) << 16;
        ipaddr |= ((int) _addr[2]) << 8;
        ipaddr |= ((int) _addr[3]) & 0xFF;

        return ipaddr;
    }

    /**
     * Method parse
     *
     * @param    dataPtr             a  byte[]
     *
     */
    public void parse(byte[] dataPtr) {
        new ByteArrayInputStream(dataPtr).read(_addr,0,4);
    }

    /**
     * Method getDataLength
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        return (4);
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
        COPSUtil.writeData(id, _addr, 4);
    }

}

