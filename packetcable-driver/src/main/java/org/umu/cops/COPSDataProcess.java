/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.umu.cops;

import org.umu.cops.stack.COPSError;

/**
 * Defines the standard methods for implementors for processing COPS data.
 */
public interface COPSDataProcess {

    /**
     * Notifies a keep-alive timeout
     * @param man   The associated request state manager
     */
    void notifyNoKAliveReceived(COPSStateMan man);

    /**
     * Notifies that the connection has been closed
     * @param man  The associated request state manager
     * @param error Reason
     */
    void notifyClosedConnection(COPSStateMan man, COPSError error);

    /**
     * Notifies that a request state has been closed
     * @param man   The associated request state manager
     */
    void closeRequestState(COPSStateMan man);

}
