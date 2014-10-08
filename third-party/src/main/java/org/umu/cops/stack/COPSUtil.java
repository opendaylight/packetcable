/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * COPS Utils
 *
 * @version COPSUtil.java, v 2.00 2004
 *
 */
public class COPSUtil {

    /**
     * Method writeData
     *
     * @param    id                  a  Socket
     * @param    data                a  byte[]
     * @param    len                 an int
     *
     * @throws   IOException
     *
     */
    static void writeData(Socket id, byte[] data, int len) throws IOException {
        OutputStream output;
        output = id.getOutputStream();

        output.write(data,0,len);
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
                if ((int)(nowTime - startTime) > 2000)
                    break;
            }
        } while (nread != nchar);

        return nread;
    }
}

