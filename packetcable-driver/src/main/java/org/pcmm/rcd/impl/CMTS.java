/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.rcd.impl;

import org.pcmm.PCMMConstants;
import org.pcmm.PCMMProperties;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.rcd.ICMTS;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class starts a mock CMTS that can be used for testing.
 */
public class CMTS extends AbstractPCMMServer implements ICMTS {

	/**
	 * Receives messages from the COPS client
	 */
	private final Map<String, IPCMMClientHandler> handlerMap;

	/**
	 * The configured gates
	 */
	private final Map<Direction, Set<String>> gateConfig;

	/**
	 * The connected CMTSs and whether or not they are up
	 */
	private final Map<String, Boolean> cmStatus;

	/**
	 * Constructor for having the server port automatically assigned
	 * Call getPort() after startServer() is called to determine the port number of the server
	 */
	public CMTS(final Map<Direction, Set<String>> gateConfig, final Map<String, Boolean> cmStatus) {
		this(0, gateConfig, cmStatus);
	}

	/**
	 * Constructor for starting the server to a pre-defined port number
	 * @param port - the port number on which to start the server.
	 */
	public CMTS(final int port, final Map<Direction, Set<String>> gateConfig, final Map<String, Boolean> cmStatus) {
		super(port);
		if (gateConfig == null || cmStatus == null) throw new IllegalArgumentException("Config must not be null");
		this.gateConfig = Collections.unmodifiableMap(gateConfig);
		this.cmStatus = Collections.unmodifiableMap(cmStatus);
		handlerMap = new ConcurrentHashMap<>();
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
			final IPCMMClientHandler handler = new CmtsPcmmClientHandler(socket, gateConfig, cmStatus);
			handler.connect();
			handlerMap.put(key, handler);
			return handler;
		} else return handlerMap.get(key);
	}

	/**
	 * To start a CMTS
	 * @param args - the arguments which will contain configuration information
	 * @throws IOException - should the server fail to start for reasons such as port contention.
	 */
	public static void main(final String[] args) throws IOException {
		final CMTS cmts = new CMTS(PCMMProperties.get(PCMMConstants.PCMM_PORT, Integer.class),
				new HashMap<Direction, Set<String>>(), new HashMap<String, Boolean>());
		cmts.startServer();
	}

}
