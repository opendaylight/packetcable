/**
 * 
 */
package org.pcmm.nio;

import org.pcmm.PCMMProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSMsgParser;

import java.io.IOException;
import java.net.Socket;

/**
 * this class provides a set of utilities to efficiently read/write data from a
 * stream, it could parameterized with a reading timeout or -1 for blocking
 * until get a message
 * 
 */
public class PCMMChannel {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private final Socket socket;
	private int timeout;
	public static final int DEFAULT_BYTE_BUFFER_SIZE = 2048;
	public static final int DEFAULT_READ_TIMEOUT = -1;

	public PCMMChannel(final Socket socket) {
		this(socket, PCMMProperties.get(PCMMProperties.DEFAULT_TIEMOUT,
				Integer.class, DEFAULT_READ_TIMEOUT));
	}

	public PCMMChannel(final Socket socket, int timeout) {
		this.socket = socket;
		logger.info("Allocated byte buffer with size = "
				+ DEFAULT_BYTE_BUFFER_SIZE);
		this.timeout = timeout;
		logger.info("Set read/write timeout to : " + timeout);

	}

	/**
	 * Method sendMsg
	 * @param msg - a COPSMsg
	 * @throws IOException
	 * @throws COPSException
	 */
	public void sendMsg(final COPSMsg msg) throws IOException, COPSException {
		logger.debug("sendMsg({})==>{}", getSocket(), msg);
		msg.writeData(getSocket());
	}

	/**
	 * Method receiveMessage
	 * @return a COPSMsg
	 * @throws IOException
	 * @throws COPSException
	 */
	public COPSMsg receiveMessage() throws IOException, COPSException {
        return COPSMsgParser.parseMessage(socket);
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
