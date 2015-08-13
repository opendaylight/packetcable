/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.base;

import java.io.IOException;
import java.net.Socket;

/**
 * Base interface for all PCMM objects, it define the {@code S-Type},
 * {@code S-Num} and the data length
 * 
 */
public interface IPCMMBaseObject {

    void writeData(Socket id) throws IOException;

//    short getDataLen();

	byte[] getAsBinaryArray();

}
