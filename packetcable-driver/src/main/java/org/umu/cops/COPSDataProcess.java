/*
###############################################################################
##                                                                           ##
## (c) 2014-2015 Cable Television Laboratories, Inc.  All rights reserved.   ##
##                                                                           ##
###############################################################################
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
