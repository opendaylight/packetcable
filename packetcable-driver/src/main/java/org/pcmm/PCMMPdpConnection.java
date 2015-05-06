/*
 @header@
 */

package org.pcmm;

import org.umu.cops.prpdp.COPSPdpConnection;
import org.umu.cops.prpdp.COPSPdpReqStateMan;
import org.umu.cops.stack.COPSPepId;
import org.umu.cops.stack.COPSReqMsg;

import javax.annotation.concurrent.ThreadSafe;
import java.net.Socket;

/**
 * Class for managing an provisioning connection at the PDP side for receiving and brokering out COPS messages.
 */
@ThreadSafe
public class PCMMPdpConnection extends COPSPdpConnection {

    /**
     *  PDP policy data processor class
     */
    protected final PCMMPdpDataProcess _thisProcess;

    /**
     * Creates a new PDP connection
     *
     * @param pepId PEP-ID of the connected PEP
     * @param sock Socket connected to PEP
     * @param process Object for processing policy data
     */
    public PCMMPdpConnection(final COPSPepId pepId, final Socket sock, final PCMMPdpDataProcess process,
                             final short kaTimer, final short acctTimer) {
        super(pepId, sock, process, kaTimer, acctTimer);
        _thisProcess = process;
    }

    /**
     * Returns an instance of a COPSPdpReqStateMan
     * @param reqMsg - the request on which to create the state manager
     * @return - the state manager
     */
    protected COPSPdpReqStateMan createStateManager(final COPSReqMsg reqMsg) {
        return new PCMMPdpReqStateMan(reqMsg.getHeader().getClientType(), reqMsg.getClientHandle(), _thisProcess);
    }

}
