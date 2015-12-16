/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import org.pcmm.nio.PCMMChannel;
import org.pcmm.objects.MMVersionInfo;
import org.pcmm.rcd.IPCMMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSMsg;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

// import org.junit.Assert;

/**
 *
 * default implementation for {@link IPCMMClient}
 *
 *
 */
public class AbstractPCMMClient implements IPCMMClient {

    private final static Logger logger = LoggerFactory.getLogger(AbstractPCMMClient.class);

    private final String host;

    private final int port;

    // TODO - consider removing this attribute as it is never being used
    private MMVersionInfo versionInfo;

    // Following two attributes are set when connect() is called
    /**
     * socket used to communicated with server.
     */
    private transient Socket socket;

    private transient PCMMChannel channel;

    // TODO - determine why this class holds a handle as it is not being used.
    private transient COPSHandle clientHandle;

    /**
     * When true, this means the socket object will be generated via the host name and port number vs. being injected.
     * In this case, the socket will be closed on disconnect(), else, the client creating the socked object will be
     * responsible.
     */
    private final boolean ownSocket;

    public AbstractPCMMClient(final String host, final int port) {
        this.host = host;
        this.port = port;
        this.ownSocket = true;
    }

    public AbstractPCMMClient(final Socket socket) {
        this.host = socket.getInetAddress().getHostName();
        this.port = socket.getPort();
        this.socket = socket;
        this.ownSocket = false;
    }

    @Override
    public void connect() throws IOException {
        if (socket == null) {
            socket = new Socket(InetAddress.getByName(host), port);
        }
        channel = new PCMMChannel(this.socket);
    }

    @Override
    public boolean disconnect() {
        if (isConnected()) {
            try {
                if (ownSocket) {
                    socket.close();
                }
                channel = null;
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return true;
    }

    @Override
    public void sendRequest(COPSMsg requestMessage) {
        try {
            channel.sendMsg(requestMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), getSocket());
        }
    }

    @Override
    public COPSMsg readMessage() {
        try {
            COPSMsg recvdMsg = channel.receiveMessage();
            logger.debug("received message : " + recvdMsg.getHeader());
            return recvdMsg;
        } catch (Exception e) {
            logger.error(e.getMessage(), getSocket());
        }
        return null;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * @return the versionInfo
     */
    public MMVersionInfo getVersionInfo() {
        return versionInfo;
    }

    /**
     * @param versionInfo
     *            the versionInfo to set
     */
    public void setVersionInfo(MMVersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    @Override
    public COPSHandle getClientHandle() {
        return clientHandle;
    }

    @Override
    public void setClientHandle(final COPSHandle handle) {
        this.clientHandle = handle;
    }

}
