/**
 @header@
 */
package org.pcmm.rcd.impl;

import org.pcmm.PCMMConstants;
import org.pcmm.PCMMProperties;
import org.pcmm.concurrent.IWorkerPool;
import org.pcmm.concurrent.impl.WorkerPool;
import org.pcmm.messages.impl.MessageFactory;
import org.pcmm.rcd.IPCMMServer;
import org.pcmm.state.IState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSMsg;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

// import org.junit.Assert;

/*
 * (non-Javadoc)
 *
 * @see pcmm.rcd.IPCMMServer
 */
public abstract class AbstractPCMMServer implements IPCMMServer {
	protected Logger logger;
	/*
	 * A ServerSocket to accept messages ( OPN requests)
	 */
	private ServerSocket serverSocket;

	private Socket stopSocket;

	private volatile boolean keepAlive;
	/*
     *
     */
	private int port;

	IWorkerPool pool;

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
		logger = LoggerFactory.getLogger(getClass().getName());
		int poolSize = PCMMProperties.get(PCMMConstants.PS_POOL_SIZE, Integer.class);
		pool = new WorkerPool(poolSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.rcd.IPCMMServer#startServer()
	 */
	public void startServer() {
		if (serverSocket != null)
			return;
		try {
			serverSocket = new ServerSocket(port);
			port = serverSocket.getLocalPort();
			logger.info("Server started and listening on port :" + port);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		// execute this in a single thread executor
		Executors.newSingleThreadExecutor().execute(new Runnable() {
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
				try {
					if (stopSocket != null && stopSocket.isConnected()) {
						logger.info("Cleaning up");
						stopSocket.close();
					}
					if (serverSocket != null && serverSocket.isBound()) {
						logger.info("Server about to stop");
						serverSocket.close();
						logger.info("Server stopped");
					}
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
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
	protected abstract IPCMMClientHandler getPCMMClientHandler(Socket socket);

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.rcd.IPCMMServer#stopServer()
	 */
	public void stopServer() {
		// set to stop
		keepAlive = false;
		try {
			if (serverSocket != null) {
				stopSocket = new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
				logger.info("STOP socket created and attached");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.state.IStateful#recordState()
	 */
	public void recordState() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.state.IStateful#getRecoredState()
	 */
	public IState getRecoredState() {
		return null;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.rcd.IPCMMServer.IPCMMClientHandler
	 */
	public abstract class AbstractPCMMClientHandler extends AbstractPCMMClient
			implements IPCMMClientHandler {

		protected boolean sendCCMessage = false;

		public AbstractPCMMClientHandler(Socket socket) {
			super();
			setSocket(socket);
		}

		@Override
		public boolean disconnect() {
			// XXX send CC message
			sendCCMessage = true;
			/*
			 * is this really needed ?
			 */
			// if (getSocket() != null)
			// handlersPool.remove(getSocket());
			COPSMsg message = MessageFactory.getInstance().create(OPCode.CC);
			sendRequest(message);
			return super.disconnect();
		}

	}

}
