/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */
package org.umu.cops.common;

import java.io.PrintStream;

/**
 * Class to print debug and error messages.
 *
 * @version COPSDebug.java, v 3.00 2004
 *
 */
public class COPSDebug {

    static public final String ERROR_NOEXPECTEDMSG = "Message not expected";
    static public final String ERROR_EXCEPTION = "Exception not expected";
    static public final String ERROR_SOCKET = "Error socket";
    static public final String ERROR_NOSUPPORTED = "Object not supported";

    static public PrintStream _err = System.err;

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     *
       */
    public static void err (String cname, String str) {
        if (_err != null)
            _err.println(cname + ":" + str);
    }

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     * @param           e                  Exception
     *
       */
    public static void err (String cname, String str, Exception e) {
        if (_err != null) {
            _err.println(cname + ":" + str);
            _err.println(" -Reason: " + e.getMessage());
        }
    }

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     * @param          extra           Information
     * @param           e                  Exception
     *
       */
    public static void err (String cname, String str, String extra, Exception e) {
        if (_err != null) {
            _err.println(cname + ":" + str);
            _err.println(" -Info: " + extra);
            _err.println(" -Reason: " + e.getMessage());
        }
    }

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     * @param          extra           Information
     *
       */
    public static void err (String cname, String str, String extra) {
        if (_err != null) {
            _err.println(cname + ":" + str);
            _err.println(" -Info: " + extra);
        }
    }

}
