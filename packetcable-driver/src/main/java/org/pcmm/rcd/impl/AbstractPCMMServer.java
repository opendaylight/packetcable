/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import org.pcmm.PCMMConstants;
import org.pcmm.PCMMProperties;
import org.pcmm.concurrent.IWorkerPool;
import org.pcmm.concurrent.impl.WorkerPool;
import org.pcmm.rcd.IPCMMServer;
import org.pcmm.state.IState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// import org.junit.Assert;

/*
 * (non-Javadoc)
 *
 * @see pcmm.rcd.IPCMMServer
 */
public abstract class AbstractPCMMServer implements IPCMMServer {

	private final static Logger logger = LoggerFactory.getLogger(AbstractPCMMServer.class);

	/*
	 * A ServerSocket to accept messages ( OPN requests)
	 */
	private transient ServerSocket serverSocket;

	private volatile boolean keepAlive;

	/**
	 * The port number on which to start the server.
	 */
	private final int port;

	IWorkerPool pool;

	/**
	 * The thread pool executor
	 */
	private final ExecutorService executorService;

	/**
	 * Constructor to use the port number contained within the PCMMProperties static object
	 */
	protected AbstractPCMMServer() {
		this(PCMMProperties.get(PCMMConstants.PCMM_PORT, Integer.class));
	}

	/**
	 * Constructor for starting the server to a pre-defined port number. When 0 is used, the server socket will
	 * assign one for you. To determine which port is being used, call getPort() after startServer() is called.
	 * @param port - the port number on which to start the server
	 */
	protected AbstractPCMMServer(int port) {
		// XXX - Assert.assertTrue(port >= 0 && port <= 65535);
		this.port = port;
		keepAlive = true;
		int poolSize = PCMMProperties.get(PCMMConstants.PS_POOL_SIZE, Integer.class);
		pool = new WorkerPool(poolSize);
		executorService = Executors.newSingleThreadExecutor();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see pcmm.rcd.IPCMMServer#startServer()
	 */
	public void startServer() throws IOException {
		if (serverSocket != null)
			return;

		serverSocket = new ServerSocket(port);
		logger.info("Server started and listening on port :" + port);

		// execute this in a single thread executor
		executorService.execute(new Runnable() {
			public void run() {
				while (keepAlive) {
					try {
						Socket socket = serverSocket.accept();
						logger.info("Accepted a new connection from :" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
						if (keepAlive) {
							pool.schedule(getPCMMClientHandler(socket));
							logger.info("Handler attached tp : " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
						} else {
							logger.info("connection to be closed : " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
							socket.close();
						}
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				}
				stopServer();
			}
		});
	}

	/**
	 * This client is used to handle requests from within the Application
	 * Manager
	 *
	 * @param socket - the connection to the PCMM server
	 * @return client handler
	 */
	protected abstract IPCMMClientHandler getPCMMClientHandler(Socket socket) throws IOException;

	@Override
	public void stopServer() {
		// set to stop
		keepAlive = false;
		executorService.shutdownNow();
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		pool.killAll();
	}

	@Override
	public void recordState() {
		// TODO - implement me
	}

	@Override
	public IState getRecoredState() {
		return null;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		if (serverSocket != null && serverSocket.isBound()) return serverSocket.getLocalPort();
		else return this.port;
	}

}
