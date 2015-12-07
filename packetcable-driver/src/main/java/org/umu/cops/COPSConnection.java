/*
#################################################################################
##                                                                             ##
## Copyright (c) 2014, 2015 Cable Television Laboratories, Inc and others.     ##
## All rights reserved.                                                        ##
##                                                                             ##
## This program and the accompanying materials are made available under the    ##
## terms of the Eclipse Public License v1.0 which accompanies this             ## 
## distribution and is available at http://www.eclipse.org/legal/epl-v10.html  ##
##                                                                             ##
#################################################################################
*/

package org.umu.cops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSClientCloseMsg;
import org.umu.cops.stack.COPSError;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.Socket;

/**
 * Abstract class for all COPS connection implementations.
 */
@ThreadSafe
public abstract class COPSConnection implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(COPSConnection.class);

    /**
     Socket connected to PEP
     */
    protected final Socket _sock;

    /**
     * Accounting timer value (secs)
     * TODO FIXME - Why is this member never being used?
     */
    protected transient short _acctTimer;

    /**
     * Keep-alive timer value (secs)
     */
    protected transient short _kaTimer;

    /**
     * COPS error returned by PEP
     */
    protected transient COPSError _error;

    /**
     * Constructor for children
     * @param sock - Socket connection to PDP or PEP
     * @param kaTimer - the Keep-alive timer value
     * @param acctTimer - the accounting timer value
     */
    protected COPSConnection(final Socket sock, final short kaTimer, final short acctTimer) {
        this._sock = sock;
        this._kaTimer = kaTimer;
        this._acctTimer = acctTimer;
    }

    /**
     * Sets the keep-alive timer value
     * @param kaTimer Keep-alive timer value (secs)
     */
    public void setKaTimer(short kaTimer) {
        _kaTimer = kaTimer;
    }

    /**
     * Sets the accounting timer value
     * @param acctTimer Accounting timer value (secs)
     */
    public void setAcctTimer(short acctTimer) {
        _acctTimer = acctTimer;
    }

    /**
     * Checks whether the socket to the PEP is closed or not
     * @return   <tt>true</tt> if closed, <tt>false</tt> otherwise
     */
    public boolean isClosed() {
        return _sock.isClosed();
    }

    /**
     * Closes the socket to the PEP
     */
    public void close() {
        if (!_sock.isClosed())
            try {
                _sock.close();
            } catch (IOException e) {
                logger.error("Error closing socket", e);
            }
    }

    /**
     * Gets the socket to the PEP
     * @return   Socket connected to the PEP
     */
    public Socket getSocket() {
        return _sock;
    }

    /**
     * Method getError
     * @return   a COPSError
     */
    protected COPSError getError()  {
        return _error;
    }

    /**
     * Handle Client Close Message, close the passed connection
     * @param    conn                a  Socket
     * @param    cMsg                 a  COPSClientCloseMsg
     */
    protected void handleClientCloseMsg(final Socket conn, final COPSClientCloseMsg cMsg) {
        _error = cMsg.getError();
        logger.info("Got close request, closing connection "
                + conn.getInetAddress() + ":" + conn.getPort() + ":[Error " + _error.getDescription() + "]");
        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                logger.warn("Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }
            conn.close();
        } catch (Exception unae) {
            logger.error("Unexpected exception closing connection", unae);
        }
    }

}
