/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

/**
 * Class to hold static utilitarian methods for streaming bytes over a Socket.
 *
 * @version COPSUtil.java, v 2.00 2004
 *
 */
public class COPSUtil {

    private final static Logger logger = LoggerFactory.getLogger(COPSUtil.class);

    /**
     * Streams COPS data
     * @param    socket                  a  Socket
     * @param    data                a  byte[]
     * @param    len                 an int
     * @throws   IOException
     */
    public static void writeData(final Socket socket, final byte[] data, final int len) throws IOException {
        logger.info("Writing COPS data");
        socket.getOutputStream().write(data, 0, len);
    }

    /**
     * Returns true if the data contained within data1 + padding1 is equivalent to data2 + padding2
     * @param data1 - the data from the first
     * @param padding1 - the padding from the first
     * @param data2 - the data from the second
     * @param padding2 - the padding from the second
     * @return - t/f
     */
    public static boolean copsDataPaddingEquals(final COPSData data1, final COPSData padding1,
                                                final COPSData data2, final COPSData padding2) {
        final byte[] data1Bytes = data1.getData();
        final byte[] padding1Bytes = padding1.getData();

        final byte[] data2Bytes = data2.getData();
        final byte[] padding2Bytes = padding2.getData();

        if (data1Bytes.length + padding1Bytes.length != data2Bytes.length + padding2Bytes.length)
            return false;

        final ByteArrayOutputStream thisStream = new ByteArrayOutputStream();
        final ByteArrayOutputStream thatStream = new ByteArrayOutputStream();
        try {
            thisStream.write(data1Bytes);
            thisStream.write(padding1Bytes);
            thatStream.write(data2Bytes);
            thatStream.write(padding2Bytes);
            return Arrays.equals(thisStream.toByteArray(), thatStream.toByteArray());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reads nchar from a given sockets, blocks on read untill nchar are read of conenction has error
     * bRead returns the bytes read
     *
     * @param    connId              a  Socket
     * @param    dataRead            a  byte[]
     * @param    nchar               an int
     *
     * @return   an int
     *
     * @throws   IOException
     *
     */
    @Deprecated
    static int readData(Socket connId, byte[] dataRead, int nchar)  throws IOException {
        InputStream input;
        input = connId.getInputStream();

        int nread = 0;
        int startTime = (int) (new Date().getTime());
        do {
            if (input.available() != 0) {
                nread += input.read(dataRead,nread,nchar-nread);
                startTime = (int) (new Date().getTime());
            } else {
                int nowTime = (int) (new Date().getTime());
                if ((nowTime - startTime) > 2000)
                    break;
            }
        } while (nread != nchar);

        return nread;
    }
}
