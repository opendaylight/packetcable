/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * COPS Object
 *
 * @version COPSObjBase.java, v 1.00 2003
 *
 */
public abstract class COPSObjBase {
    /**
     * Add padding in the data, if the data does not fall on 32-bit boundary
     *
     * @param    len                 an int
     *
     * @return   a COPSData
     *
     */
    static COPSData getPadding(int len) {
        byte[] padBuf = new byte[len];
        Arrays.fill(padBuf, (byte) 0);
        COPSData d = new COPSData(padBuf, 0, len);
        return d;
    }

    /**
     * Writes data to a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public abstract void writeData(Socket id) throws IOException;

    /**
     * Method getDataLength
     *
     * @return   a short
     *
     */
    short getDataLength() {
        return 0;
    }

    /**
     * Method isCOPSHeader
     *
     * @return   a boolean
     *
     */
    boolean isCOPSHeader() {
        return false;
    }

    /**
     * Method isClientHandle
     *
     * @return   a boolean
     *
     */
    boolean isClientHandle() {
        return false;
    }

    /**
     * Method isContext
     *
     * @return   a boolean
     *
     */
    boolean isContext() {
        return false;
    }

    /**
     * Method isInterface
     *
     * @return   a boolean
     *
     */
    boolean isInterface() {
        return false;
    }

    /**
     * Method isDecision
     *
     * @return   a boolean
     *
     */
    boolean isDecision() {
        return false;
    }

    /**
     * Method isLocalDecision
     *
     * @return   a boolean
     *
     */
    boolean isLocalDecision() {
        return false;
    }

    /**
     * Method isReport
     *
     * @return   a boolean
     *
     */
    boolean isReport() {
        return false;
    }

    /**
     * Method isError
     *
     * @return   a boolean
     *
     */
    boolean isError() {
        return false;
    }

    /**
     * Method isTimer
     *
     * @return   a boolean
     *
     */
    boolean isTimer() {
        return false;
    }

    /**
     * Method isPepId
     *
     * @return   a boolean
     *
     */
    boolean isPepId() {
        return false;
    }

    /**
     * Method isReason
     *
     * @return   a boolean
     *
     */
    boolean isReason() {
        return false;
    }

    /**
     * Method isPdpAddress
     *
     * @return   a boolean
     *
     */
    boolean isPdpAddress() {
        return false;
    }

    /**
     * Method isClientSI
     *
     * @return   a boolean
     *
     */
    boolean isClientSI() {
        return false;
    }

    /**
     * Method isMessageIntegrity
     *
     * @return   a boolean
     *
     */
    boolean isMessageIntegrity() {
        return false;
    }

};

