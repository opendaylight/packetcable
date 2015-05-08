/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Represents messages coming from and going to a COPS device such as a CMTS
 */
abstract public class COPSMsg {

    private static final Logger logger = LoggerFactory.getLogger(COPSMsg.class);

    /**
     * The COPS header that is associated with all COPS messages
     */
    private final COPSHeader _hdr;

    /**
     * Base constructor
     * @param hdr - the header
     */
    public COPSMsg(final COPSHeader hdr) {
        if (hdr == null) throw new IllegalArgumentException("Header must not be null");
        this._hdr = hdr;
    }
    /**
     * Returns the message header object
     * @return   a COPSHeader
     */
    public COPSHeader getHeader() {
        return _hdr;
    }

    /**
     * Method writeData. Implementers should be calling super.writeData() for the header prior to writing out the rest.
     * @param    socket                  a  Socket
     * @throws   IOException
     */
    public final void writeData(final Socket socket) throws IOException {
        logger.info("Writing data for OPCode - " + _hdr.getOpCode());
        _hdr.writeData(socket, _hdr.getHdrLength() + getDataLength());
        writeBody(socket);
    }

    /**
     * Returns the number of bytes to be contained within the payload excluding the header
     * @return - a positive value including the header size
     */
    protected abstract int getDataLength();

    /**
     * Writes out the body data over a socket
     * @param socket - the socket to which to write
     */
    protected abstract void writeBody(Socket socket) throws IOException;

    /**
     * Write an object textual description in the output stream
     * @param    os                  an OutputStream
     * @throws   IOException
     */
    final public void dump(final OutputStream os) throws IOException {
        _hdr.dump(os);
        dumpBody(os);
    }

    /**
     * Creates a string representation of this object and sends it to an output stream
     * @param os - the output stream
     * @throws IOException
     */
    protected abstract void dumpBody(final OutputStream os) throws IOException;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSMsg)) {
            return false;
        }

        final COPSMsg copsMsg = (COPSMsg) o;

        return _hdr.equals(copsMsg._hdr);

    }

    @Override
    public int hashCode() {
        return _hdr.hashCode();
    }
}
