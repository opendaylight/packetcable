/**
 @header@
 */

package org.pcmm.rcd;

import java.net.InetAddress;

import org.umu.cops.stack.COPSMsg;

/**
 * <p>
 * This is a Client Type 1, which represents existing "legacy" endpoints (e.g.,
 * PC applications, gaming consoles) that lack specific QoS awareness or
 * signaling capabilities. This client has no awareness of DOCSIS, CableHome, or
 * PacketCable messaging, and hence no related requirements can be placed upon
 * it. Client Type 1 communicates with an Application Manager to request
 * service, and does not (cannot) request QoS resources directly from the MSO
 * access network.
 * </p>
 * 
 * 
 */
public interface IPCMMClient {

	/**
	 * PCMM client-type
	 */
	static final short CLIENT_TYPE = (short) 0x800A;

	/**
	 * sends a message to the server.
	 * 
	 * @param requestMessage
	 *            request message.
	 */
	void sendRequest(COPSMsg requestMessage);

	/**
	 * Reads message from server
	 * 
	 * @return COPS message
	 */
	COPSMsg readMessage();

	/**
	 * tries to connect to the server.
	 * 
	 * @param address
	 *            server address
	 * @param port
	 *            server port
	 * @return connection state
	 */
	boolean tryConnect(String address, int port);

	/**
	 * tries to connect to the server.
	 * 
	 * @param address
	 *            server address
	 * @param port
	 *            server port
	 * @return connection state
	 */
	boolean tryConnect(InetAddress address, int port);

	/**
	 * disconnects from server.
	 * 
	 * @return disconnection status.
	 */
	boolean disconnect();

	/**
	 * 
	 * @return whether the client is connected to the server of not.
	 */
	boolean isConnected();

	/**
	 * gets the client handle
	 * 
	 * @return client handle
	 */
	String getClientHandle();

	/**
	 * 
	 * sets the client handle
	 * 
	 * @param handle
	 *            cleint hanlde
	 */
	void setClientHandle(String handle);

}
