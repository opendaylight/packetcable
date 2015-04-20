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
 * Represents objects that can be contained within COPS messages.
 */
public abstract class COPSObjBase {

    /**
     * Generally used to determine the type of message
     */
    private final COPSObjHeader _objHdr;

    /**
     * The base object constructor
     * @param objHdr - the header (required)
     */
    public COPSObjBase(final COPSObjHeader objHdr) {
        if (objHdr == null) throw new IllegalArgumentException("Object header must not be null");
        this._objHdr = objHdr;
    }

    /**
     * Returns the header
     * @return - the header
     */
    public COPSObjHeader getHeader() { return _objHdr; }

    /**
     * Writes data to a given network _socket
     * @param    socket                  a  Socket
     * @throws   IOException
     */
    final public void writeData(final Socket socket) throws IOException {
        _objHdr.writeData(socket, getDataLength());
        writeBody(socket);
    }

    protected abstract void writeBody(Socket socket) throws IOException;

    /**
     * Returns the length of the body data to be output (not including header)
     * @return   a short
     */
    protected abstract int getDataLength();

    /**
     * Write an object textual description in the output stream
     * @param    os                  an OutputStream
     * @throws   IOException
     */
    final public void dump(final OutputStream os) throws IOException {
        _objHdr.dump(os);
        dumpBody(os);
    }

    protected abstract void dumpBody(OutputStream os) throws IOException;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSObjBase)) {
            return false;
        }

        final COPSObjBase that = (COPSObjBase) o;

        return !(!_objHdr.equals(that._objHdr));

    }

    @Override
    public int hashCode() {
        return _objHdr.hashCode();
    }
}
