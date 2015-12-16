/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.umu.cops;

import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSSyncStateMsg;

import java.net.Socket;

/**
 * Abstract COPS state manager.
 */
public abstract class COPSStateMan {

    // TODO - place these values into an enumeration
    /**
     * COPS client-type that identifies the policy client
     */
    protected final short _clientType;

    /**
     *  COPS client handle used to uniquely identify a particular
     *  PEP's request for a client-type
     */
    protected final COPSHandle _handle;

    /**
     * The socket connection. Value set when initRequestState is called
     */
    protected final Socket _socket;

    /**
     *  Current state of the request being managed
     */
    protected transient Status _status;

    /**
     * Constructor
     * @param clientType - the client type
     * @param clientHandle - the unique handle to the client
     */
    protected COPSStateMan(final short clientType, final COPSHandle clientHandle, final Socket socket) {
        if (clientHandle == null) throw new IllegalArgumentException("Client handle must not be null");
        if (socket == null) throw new IllegalArgumentException("Socket connection must not be null");
        if (!socket.isConnected()) throw new IllegalArgumentException("Socket connection must be connected");
        this._clientType = clientType;
        this._handle = clientHandle;
        this._socket = socket;
        this._status = Status.ST_CREATE;
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

    /**
     * Gets the status of the request
     * @return      Request state value
     */
    public Status getStatus() {
        return _status;
    }

    /**
     * Called when COPS sync is completed
     * @param    repMsg              COPS sync message
     * @throws COPSPdpException
     */
    public void processSyncComplete(final COPSSyncStateMsg repMsg) throws COPSException {
        _status = Status.ST_SYNCALL;
        // TODO - maybe we should notifySyncComplete ...
    }

    /**
     * The different state manager statuses
     */
    public enum Status {
        NA,
        ST_CREATE, // Request State created
        ST_INIT, // Request received
        ST_DECS, // Decisions sent
        ST_REPORT, // Report received
        ST_FINAL, // Request state finalized
        ST_NEW, // New request state solicited
        ST_DEL, // Delete request state solicited
        ST_SYNC, // SYNC request sent
        ST_SYNCALL, // SYNC completed
        ST_CCONN, // Close connection received
        ST_NOKA, // Keep-alive timeout
        ST_ACCT, // Accounting timeout
    }

}
