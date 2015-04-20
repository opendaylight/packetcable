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
 * Abstract COPS Interface for extension of all COPS interface types
 */
abstract class COPSInterface extends COPSObjBase {

    /**
     * The associated address object for a given COPS Interfaace
     */
    protected final COPSIpAddress _addr;

    /**
     * The interface on which the protocol message was received
     */
    protected final int _ifindex;

    /**
     * Constructor
     * @param objHdr - the object's header
     * @param ifindex - the interface value
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSInterface(final COPSObjHeader objHdr, final COPSIpAddress addr, final int ifindex) {
        super(objHdr);
        if (addr == null) throw new IllegalArgumentException("Address object cannot be null");
        _addr = addr;
        _ifindex = ifindex;
    }

    public abstract boolean isInInterface();
    public abstract boolean isIPv6();

    @Override
    protected int getDataLength() {
        return _addr.getDataLength() + 4;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        _addr.writeData(socket);
        final byte[] buf = new byte[4];
        buf[0] = (byte) (_ifindex >> 24);
        buf[1] = (byte) (_ifindex >> 16);
        buf[2] = (byte) (_ifindex >> 8);
        buf[3] = (byte) _ifindex;
        COPSUtil.writeData(socket, buf, 4);
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        os.write(("Address: " + _addr.getIpName() + "\n").getBytes());
        os.write(("ifindex: " + _ifindex + "\n").getBytes());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSInterface)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSInterface that = (COPSInterface) o;

        return _ifindex == that._ifindex && _addr.equals(that._addr);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _addr.hashCode();
        result = 31 * result + _ifindex;
        return result;
    }
}
