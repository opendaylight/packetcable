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
 * COPS IPv6 PDP Address
 *
 * @version COPSIpv6PdpAddress.java, v 1.00 2003
 *
 */
abstract public class COPSIpv6PdpAddress extends COPSPdpAddress {

    protected COPSObjHeader _objHdr;
    protected COPSIpv6Address _addr;
    private short _reserved;
    protected short _tcpPort;

    protected COPSIpv6PdpAddress() {
        _addr = new COPSIpv6Address();
        _objHdr = new COPSObjHeader();
        _objHdr.setCType((byte) 2);
        // _objHdr.setDataLength((short) _addr.getDataLength() + sizeof(u_int32_t));
        _objHdr.setDataLength((short) (_addr.getDataLength() + 4));
    }

    protected COPSIpv6PdpAddress(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        byte[] buf = new byte[16];
        System.arraycopy(dataPtr,2,buf,0,16);
        _addr.parse(buf);

        _reserved |= ((short) dataPtr[20]) << 8;
        _reserved |= ((short) dataPtr[21]) & 0xFF;
        _tcpPort |= ((short) dataPtr[22]) << 8;
        _tcpPort |= ((short) dataPtr[23]) & 0xFF;

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
        buf[0] = (byte) (_reserved >> 8);
        buf[1] = (byte) _reserved;
        buf[2] = (byte) (_tcpPort >> 8);
        buf[3] = (byte) _tcpPort ;

        COPSUtil.writeData(id, buf, 4);
    }
}


