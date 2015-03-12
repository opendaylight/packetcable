/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */
package org.umu.cops.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(COPSDebug.class);

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     *
       */
    @Deprecated
    public static void err (String cname, String str) {
        logger.info(cname + ":" + str);
    }

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     * @param           e                  Exception
     *
       */
    @Deprecated
    public static void err (String cname, String str, Exception e) {
        logger.info(cname + ":" + str, e);
    }

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     * @param          extra           Information
     * @param           e                  Exception
     *
       */
    @Deprecated
    public static void err (String cname, String str, String extra, Exception e) {
        logger.info(cname + ":" + str + " -Info: " + extra, e);
    }

    /** Prints an error message.
        *
        * @param          cname           Name of class that generated the error
     * @param          str                Error message
     * @param          extra           Information
     *
       */
    @Deprecated
    public static void err (String cname, String str, String extra) {
        logger.info(cname + ":" + str + " -Info: " + extra);
    }

}
