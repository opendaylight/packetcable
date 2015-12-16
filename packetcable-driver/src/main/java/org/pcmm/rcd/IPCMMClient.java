/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd;

import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSMsg;

import java.io.IOException;

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
	short CLIENT_TYPE = (short) 0x800A;

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
	 * @throws IOException
	 */
	void connect() throws IOException;

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
	COPSHandle getClientHandle();

	void setClientHandle(COPSHandle handle);

}
