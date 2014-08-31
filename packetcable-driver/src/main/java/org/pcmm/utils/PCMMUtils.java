/**
 @header@
 */
package org.pcmm.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PCMMUtils {

    public static void WriteBinaryDump(String rootFileName, byte[] buffer) {
        // Make this Unique
        String fileName = "/tmp/" + rootFileName + "-" + java.util.UUID.randomUUID() + ".bin";
        try {

            System.out.println("Open fileName " + fileName);
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

            System.out.println("Wrote " + buffer.length + " bytes");
        } catch (IOException ex) {
            System.out.println("Error writing file '" + fileName + "'");
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

            System.out.println("Read " + total + " bytes");
            return buffer;
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return null;
    }
}
