/**
 @header@
 */
package org.pcmm.rcd.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.pcmm.nio.PCMMChannel;
// import org.junit.Assert;
import org.pcmm.objects.MMVersionInfo;
import org.pcmm.rcd.IPCMMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSMsg;

/**
 * 
 * default implementation for {@link IPCMMClient}
 * 
 * 
 */
public class AbstractPCMMClient implements IPCMMClient {

	protected Logger logger = LoggerFactory.getLogger(AbstractPCMMClient.class);
	/**
	 * socket used to communicated with server.
	 */
	private Socket socket;

	private String clientHanlde;

	private MMVersionInfo versionInfo;

	private PCMMChannel channel;

	public AbstractPCMMClient() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.rcd.IPCMMClient#sendRequest(pcmm.messages.IMessage)
	 */
	public void sendRequest(COPSMsg requestMessage) {
		try {
			channel.sendMsg(requestMessage);
		} catch (Exception e) {
			logger.error(e.getMessage(), getSocket());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.rcd.IPCMMClient#readMessage()
	 */
	public COPSMsg readMessage() {
		try {
			COPSMsg recvdMsg = channel.receiveMessage();
			// logger.debug("received message : " + recvdMsg.getHeader());
			return recvdMsg;
		} catch (Exception e) {
			logger.error(e.getMessage(), getSocket());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.rcd.IPCMMClient#tryConnect(java.lang.String, int)
	 */
	public boolean tryConnect(String address, int port) {
		try {
			InetAddress addr = InetAddress.getByName(address);
			tryConnect(addr, port);
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.rcd.IPCMMClient#tryConnect(java.net.InetAddress, int)
	 */
	public boolean tryConnect(InetAddress address, int port) {
		try {
			setSocket(new Socket(address, port));
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.rcd.IPCMMClient#disconnect()
	 */
	public boolean disconnect() {
		if (isConnected()) {
			try {
				socket.close();
				channel = null;
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return true;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	public PCMMChannel getChannel() {
		return channel;
	}

	/**
	 * @param socket
	 *            the socket to set
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
		if (this.socket != null
				&& (this.channel == null || !this.channel.getSocket().equals(
						this.socket)))
			channel = new PCMMChannel(this.socket);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.rcd.IPCMMClient#isConnected()
	 */
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
	public String getClientHandle() {
		return clientHanlde;
	}

	@Override
	public void setClientHandle(String handle) {
		this.clientHanlde = handle;
	}

}
