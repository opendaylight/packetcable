/**
 * 
 */
package org.pcmm.nio;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;

import org.pcmm.PCMMProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSMsgParser;

/**
 * this class provides a set of utilities to efficiently read/write data from a
 * stream, it could parameterized with a reading timeout or -1 for blocking
 * until get a message
 * 
 */
public class PCMMChannel {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private ByteBuffer dataBuffer;
	private Socket socket;
	private int timeout;
	public static final int DEFAULT_BYTE_BUFFER_SIZE = 2048;
	public static final int DEFAULT_READ_TIMEOUT = -1;

	public PCMMChannel(Socket socket) {
		this(socket, PCMMProperties.get(PCMMProperties.DEFAULT_TIEMOUT,
				Integer.class, DEFAULT_READ_TIMEOUT));
	}

	public PCMMChannel(Socket socket, int timeout) {
		this.socket = socket;
		dataBuffer = ByteBuffer.allocateDirect(DEFAULT_BYTE_BUFFER_SIZE);
		logger.info("Allocated byte buffer with size = "
				+ DEFAULT_BYTE_BUFFER_SIZE);
		this.timeout = timeout;
		logger.info("Set read/write timeout to : " + timeout);

	}

	public int readData(byte[] dataRead, int nchar) throws IOException {
		InputStream input;
		input = getSocket().getInputStream();
		int nread = 0;
		int startTime = (int) (new Date().getTime());
		do {
			if (timeout == -1 || input.available() != 0) {
				nread += input.read(dataRead, nread, nchar - nread);
				startTime = (int) (new Date().getTime());
			} else {
				int nowTime = (int) (new Date().getTime());
				if ((nowTime - startTime) > timeout)
					break;
			}
		} while (nread != nchar);
		return nread;
	}

	/**
	 * Method sendMsg
	 * 
	 * @param msg
	 *            a COPSMsg
	 * 
	 * @throws IOException
	 * @throws COPSException
	 * 
	 */
	public void sendMsg(COPSMsg msg) throws IOException, COPSException {
		logger.debug("sendMsg({})==>{}", getSocket(), msg);
		msg.checkSanity();
		msg.writeData(getSocket());
	}

	/**
	 * Method receiveMessage
	 * 
	 * @return a COPSMsg
	 * 
	 * @throws IOException
	 * @throws COPSException
	 * 
	 */
	public COPSMsg receiveMessage() throws IOException, COPSException {
		int nread = 0;
		byte[] hBuf = new byte[8];

		logger.debug("receiveMessage({})", getSocket());

		nread = readData(hBuf, 8);

		if (nread == 0) {
			throw new COPSException("Error reading connection");
		}

		if (nread != 8) {
			throw new COPSException("Bad COPS message");
		}

		COPSHeader hdr = new COPSHeader(hBuf);
		int dataLen = hdr.getMsgLength() - hdr.getHdrLength();
		logger.debug("COPS Msg length :[" + dataLen + "]\n");
		byte[] buf = new byte[dataLen + 1];
		nread = 0;

		nread = readData(buf, dataLen);
		buf[dataLen] = (byte) '\0';
		logger.debug("Data read length:[" + nread + "]\n");

		if (nread != dataLen) {
			throw new COPSException("Bad COPS message");
		}
		COPSMsgParser prser = new COPSMsgParser();
		COPSMsg msg = prser.parse(hdr, buf);
		return msg;
	}

	/**
	 * @return the dataBuffer
	 */
	public ByteBuffer getDataBuffer() {
		return dataBuffer;
	}

	/**
	 * @param dataBuffer
	 *            the dataBuffer to set
	 */
	public void setDataBuffer(ByteBuffer dataBuffer) {
		this.dataBuffer = dataBuffer;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * @param socket
	 *            the socket to set
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
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
