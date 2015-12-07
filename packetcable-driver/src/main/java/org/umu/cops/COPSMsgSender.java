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

import org.umu.cops.stack.COPSHandle;

import java.net.Socket;

/**
 * Abstract COPS Message sender
 */
public abstract class COPSMsgSender {

    /**
     * COPS client-type that identifies the policy client
     */
    protected final short _clientType;

    /**
     * COPS client handle used to uniquely identify a particular
     * PEP's request for a client-type
     */
    protected final COPSHandle _handle;

    /**
     * Socket connected to PEP
     */
    protected final Socket _sock;

    public COPSMsgSender(final short clientType, final COPSHandle handle, final Socket sock) {
        if (handle == null) throw new IllegalArgumentException("Client handle must not be null");
        if (sock == null) throw new IllegalArgumentException("Socket must not be null");
        this._clientType = clientType;
        this._handle = handle;
        this._sock = sock;
    }

    /**
     * Gets the client handle
     * @return   Client's <tt>COPSHandle</tt>
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Gets the client-type
     * @return   Client-type value
     */
    public short getClientType() {
        return _clientType;
    }

}
