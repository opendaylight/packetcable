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
 * COPS Message
 *
 * @version COPSMsg.java, v 1.00 2003
 *
 */
abstract public class COPSMsg {

    protected COPSHeader _hdr;
    protected int _dataLength;
    protected int _dataStart;

    /**
     * Method getHeader
     *
     * @return   a COPSHeader
     *
     */
    public COPSHeader getHeader() {
        return _hdr;
    }

    /**
     * Method writeData
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public abstract void writeData(Socket id) throws IOException;

    /**
     * Method getMsgLength
     *
     * @return   an int
     *
     */
    public int getMsgLength() {
        return _hdr.getMsgLength();
    }

    /**
     * Method parse
     *
     * @param    hdr                 a  COPSHeader
     * @param    data                a  byte[]
     *
     * @throws   COPSException
     *
     */
    protected abstract void parse(COPSHeader hdr, byte[] data) throws COPSException;

    /**
     * Method parse
     *
     * @param    data                a  byte[]
     *
     * @throws   COPSException
     *
     */
    protected abstract void parse(byte[] data) throws COPSException;

    /**
     * Method parseHeader
     *
     * @param    data                a  byte[]
     *
     * @throws   COPSException
     *
     */
    protected void parseHeader(byte[] data) throws COPSException {
        _dataLength = 0;
        _dataStart = 0;
        if (_hdr == null) {
            // _hdr = new COPSHeader(COPSHeader.COPS_OP_CAT);
            _hdr = new COPSHeader(data);
            _dataStart += 8;
            _dataLength = _hdr.getMsgLength();
        } else {
            //header is already read
            _dataLength = _hdr.getMsgLength() - 8;
        }

        //validate the message length
        //Should fill on the 32bit boundary
        if ((_hdr.getMsgLength() % 4 != 0)) {
            throw new COPSException("Bad message format: COPS message is not on 32 bit bounday");
        }
    }

    /** Checks the sanity of COPS message and throw an
         COPSBadDataException when data is bad.
    */
    public abstract void checkSanity()throws COPSException;

    /**
     * Write an object textual description in the output stream
     *
     * @param    os                  an OutputStream
     *
     * @throws   IOException
     *
     */
    public void dump(OutputStream os) throws IOException {
        os.write(new String("COPS Message").getBytes());
    }

}
