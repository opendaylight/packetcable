/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * COPS IPv4 PDP Address
 *
 * @version COPSIpv4PdpAddress.java, v 1.00 2003
 *
 */
abstract public class COPSIpv4PdpAddress extends COPSPdpAddress {

    protected COPSObjHeader _objHdr;
    protected COPSIpv4Address _addr;
    private short _reserved;
    protected short _tcpPort;

    protected COPSIpv4PdpAddress() {
        _addr = new COPSIpv4Address();
        _objHdr = new COPSObjHeader();
        _objHdr.setCType((byte) 1);
        // _objHdr.setDataLength((short) _addr.getDataLength() + sizeof(u_int32_t));
        _objHdr.setDataLength((short) (_addr.getDataLength() + 4));
    }

    protected COPSIpv4PdpAddress(byte[] dataPtr) {
        _addr = new COPSIpv4Address();
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        byte[] buf = new byte[4];
        System.arraycopy(dataPtr,2,buf,0,4);
        _addr.parse(buf);

        _reserved |= ((short) dataPtr[8]) << 8;
        _reserved |= ((short) dataPtr[9]) & 0xFF;
        _tcpPort |= ((short) dataPtr[10]) << 8;
        _tcpPort |= ((short) dataPtr[11]) & 0xFF;

        // _objHdr.setDataLength(_addr.getDataLength() + sizeof(u_int32_t));
        _objHdr.setDataLength((short) (_addr.getDataLength() + 4));
    }

    /**
     * Method setIpAddress
     *
     * @param    hostName            a  String
     *
     * @throws   UnknownHostException
     *
     */
    public void setIpAddress(String hostName) throws UnknownHostException  {
        _addr.setIpAddress(hostName);
    }

    /**
     * Method setTcpPort
     *
     * @param    port                a  short
     *
     */
    public void setTcpPort(short port) {
        _tcpPort = port;
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
     * Method getTcpPort
     *
     * @return   a short
     *
     */
    short getTcpPort() {
        return _tcpPort;
    };

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

    /**
     * Method isIpv6PdpAddress
     *
     * @return   a boolean
     *
     */
    public boolean isIpv6PdpAddress() {
        return true;
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
        //
        _objHdr.writeData(id);
        _addr.writeData(id);

        byte[] buf = new byte[4];
        buf[0] = (byte) (_reserved & 0xFF);
        buf[1] = (byte) (_reserved << 8);
        buf[2] = (byte) (_tcpPort & 0xFF);
        buf[3] = (byte) (_tcpPort << 8);

        COPSUtil.writeData(id, buf, 4);
    }

}

