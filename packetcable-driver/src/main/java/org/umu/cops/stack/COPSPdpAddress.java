/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Superclass for  all PDP Address classes for which we appear to only have 2 concrete implementations and 4
 * subclasses meaning this class hierarchy is ripe for further refactoring.
 */
abstract public class COPSPdpAddress extends COPSObjBase {

    /**
     * The address
     */
    private COPSIpAddress _addr;

    /**
     * The port number
     */
    private final int _tcpPort;

    /**
     * Currently the space holding this value is not in use.
     */
    private final short _reserved;

    /**
     * Constructor
     * @param objHdr - the object's header info
     * @param tcpPort - the port associated with an IP device
     * @param reserved - not in use
     */
    protected COPSPdpAddress(final COPSObjHeader objHdr, final COPSIpAddress addr, final int tcpPort,
                             final short reserved) {
        super(objHdr);
        if (addr == null) throw new IllegalArgumentException("Address must not be null");
        if (tcpPort < 1) throw new IllegalArgumentException("Invalid port number - " + tcpPort);
        this._addr = addr;
        this._tcpPort = tcpPort;
        this._reserved = reserved;
    }

    public int getTcpPort() {
        return _tcpPort;
    }

    public short getReserved() {
        return _reserved;
    }

    /**
     * Creates the correct object of this type from a byte array.
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSPdpAddress parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        short reserved = 0;
        int tcpPort = 0;

        switch (objHdrData.header.getCType()) {
            case DEF:
                final byte[] addr4 = new byte[4];
                System.arraycopy(dataPtr, 4, addr4, 0, 4);
                reserved |= ((short) dataPtr[8]) << 8;
                reserved |= ((short) dataPtr[9]) & 0xFF;
                tcpPort |= ((short) dataPtr[10]) << 8;
                tcpPort |= ((short) dataPtr[11]) & 0xFF;

                switch (objHdrData.header.getCNum()) {
                    case LAST_PDP_ADDR:
                        return new COPSIpv4LastPdpAddr(objHdrData.header, addr4, tcpPort, reserved);
                    case PDP_REDIR:
                        return new COPSIpv4PdpRedirectAddress(objHdrData.header, addr4, tcpPort, reserved);
                    default:
                        throw new IllegalArgumentException("Unsupported CNum - " + objHdrData.header.getCNum());
                }
            case STATELESS:
                final byte[] addr6 = new byte[16];
                System.arraycopy(dataPtr, 4, addr6, 0, 16);
                reserved |= ((short) dataPtr[20]) << 8;
                reserved |= ((short) dataPtr[21]) & 0xFF;
                tcpPort |= ((short) dataPtr[22]) << 8;
                tcpPort |= ((short) dataPtr[23]) & 0xFF;
                switch (objHdrData.header.getCNum()) {
                    case LAST_PDP_ADDR:
                        return new COPSIpv6LastPdpAddr(objHdrData.header, addr6, tcpPort, reserved);
                    case PDP_REDIR:
                        return new COPSIpv6PdpRedirectAddress(objHdrData.header, addr6, tcpPort, reserved);
                    default:
                        throw new IllegalArgumentException("Unsupported CNum - " + objHdrData.header.getCNum());
                }
            default:
                throw new IllegalArgumentException("CType was not DEF(1) or STATELESS (2)");
        }
    }

    @Override
    public int getDataLength() {
        return _addr.getDataLength() + 4;
    }

    @Override
    public void dumpBody(OutputStream os) throws IOException {
        os.write(("Address: " + _addr.getIpName() + "\n").getBytes());
        os.write(("Port: " + _tcpPort + "\n").getBytes());
    }

    @Override
    public void writeBody(final Socket socket) throws IOException {
        _addr.writeData(socket);

        byte[] buf = new byte[4];
        buf[0] = (byte) (_reserved >> 8);
        buf[1] = (byte) _reserved;
        buf[2] = (byte) (_tcpPort >> 8);
        buf[3] = (byte) _tcpPort ;

        COPSUtil.writeData(socket, buf, 4);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSPdpAddress)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSPdpAddress that = (COPSPdpAddress) o;

        return _reserved == that._reserved && _tcpPort == that._tcpPort && _addr.equals(that._addr);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _addr.hashCode();
        result = 31 * result + _tcpPort;
        result = 31 * result + (int) _reserved;
        return result;
    }
}

