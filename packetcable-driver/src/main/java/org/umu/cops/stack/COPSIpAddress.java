package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Abstract superclass for all COPS IP Addresses.
 */
public abstract class COPSIpAddress {

    /**
     * The byte array representation of an IP address
     */
    protected final byte[] _addr;

    /**
     * Creates an address for a given host
     * @param hostName - the host name
     * @throws java.net.UnknownHostException
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpAddress(final String hostName) throws UnknownHostException {
        if (hostName == null) throw new IllegalArgumentException("Hostname must not be null");
        _addr = deriveIpAddress(hostName);
    }

    protected COPSIpAddress(final byte[] addr) {
        if (addr == null) throw new IllegalArgumentException("The addr parameter must not be null");
        _addr = addr;
    }

    protected byte[] getAddressBytes() { return _addr; }

    /**
     * Derives the IP address in a byte array from the host name
     * @param    hostName            a  String
     * @throws   UnknownHostException
     */
    protected abstract byte[] deriveIpAddress(final String hostName) throws UnknownHostException;

    /**
     * Method getIpName
     * @return   a String
     * @throws   UnknownHostException
     */
    public abstract String getIpName() throws UnknownHostException;

    /**
     * Returns the number of bytes that will be written
     * @return   a short
     */
    public int getDataLength() {
        return _addr.length;
    }

    /**
     * Write data on a given network socket
     * @param    socket                  a  Socket
     * @throws   IOException
     */
    public void writeData(Socket socket) throws IOException {
        COPSUtil.writeData(socket, _addr, _addr.length);
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSIpAddress)) {
            return false;
        }

        final COPSIpAddress that = (COPSIpAddress) o;

        return Arrays.equals(_addr, that._addr);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_addr);
    }
}
