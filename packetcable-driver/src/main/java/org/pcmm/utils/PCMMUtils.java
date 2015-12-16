/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PCMMUtils {

    public final static Logger logger = LoggerFactory.getLogger(PCMMUtils.class);

    public static void WriteBinaryDump(String rootFileName, byte[] buffer) {
        // Make this Unique
        String fileName = "/tmp/" + rootFileName + "-" + java.util.UUID.randomUUID() + ".bin";
        try {

            logger.info("Open fileName " + fileName);
            FileOutputStream outputStream = new FileOutputStream(fileName);

            // write() writes as many bytes from the buffer
            // as the length of the buffer. You can also
            // use
            // write(buffer, offset, length)
            // if you want to write a specific number of
            // bytes, or only part of the buffer.
            outputStream.write(buffer);

            // Always close files.
            outputStream.close();

            logger.info("Wrote " + buffer.length + " bytes");
        } catch (IOException ex) {
            logger.error("Error writing file '" + fileName + "'", ex);
            // Or we could just do this:
            // ex.printStackTrace();
        }
    }

    public static byte[] ReadBinaryDump(String fileName) {
        // The name of the file to open.
        // String fileName = "COPSReportMessage.txt";
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            // Use this for reading the data.
            byte[] buffer = new byte[inputStream.available()];
            // read fills buffer with data and returns
            // the number of bytes read (which of course
            // may be less than the buffer size, but
            // it will never be more).
            int total = inputStream.read(buffer);

            // Always close files.
            inputStream.close();

            logger.info("Read " + total + " bytes");
            return buffer;
        } catch (FileNotFoundException ex) {
            logger.error("Unable to open file '" + fileName + "'", ex);
        } catch (IOException ex) {
            logger.error("Error reading file '" + fileName + "'", ex);
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return null;
    }
}
