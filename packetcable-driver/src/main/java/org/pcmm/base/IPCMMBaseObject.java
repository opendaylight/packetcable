/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
