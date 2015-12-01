/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import org.pcmm.rcd.ICMTS;

/**
 * Mock CMTS that can be used for testing. startServer() is called to start required threads after instantiation.
 */
public class CMTS extends AbstractPCMMServer implements ICMTS {

    /**
     * Emulator configuration
     */
    private final CMTSConfig config;

	/**
	 * Receives messages from the COPS client
	 */
	private final Map<String, IPCMMClientHandler> handlerMap;

	/**
	 * Constructor for having the server port automatically assigned
	 * Call getPort() after startServer() is called to determine the port number of the server
	 */
	public CMTS(final CMTSConfig config) {
        super(checkNotNull(config, "config must not be null").getPort());
        this.config = config;
        handlerMap = Maps.newConcurrentMap();
    }

	@Override
	public void stopServer() {
		for (final IPCMMClientHandler handler : handlerMap.values()) {
			handler.stop();
		}
		super.stopServer();
	}

	@Override
	protected IPCMMClientHandler getPCMMClientHandler(final Socket socket) throws IOException {
		final String key = socket.getLocalAddress().getHostName() + ':' + socket.getPort();
		if (handlerMap.get(key) == null) {
			final IPCMMClientHandler handler = new CmtsPcmmClientHandler(socket, config);
			handler.connect();
			handlerMap.put(key, handler);
			return handler;
		} else {
            return handlerMap.get(key);
        }
	}

	/**
	 * To start a CMTS
	 * @param args - the arguments which will contain configuration information
	 * @throws IOException - should the server fail to start for reasons such as port contention.
	 */
	public static void main(final String[] args) throws IOException {
		if (args.length != 1) {
            throw new IllegalArgumentException("expected arguments: <cmts_yaml_config_file>");
        }

		final CMTSConfig config = CMTSConfig.loadConfig(args[0]);
		final CMTS cmts = new CMTS(config);
		cmts.startServer();
	}

}
