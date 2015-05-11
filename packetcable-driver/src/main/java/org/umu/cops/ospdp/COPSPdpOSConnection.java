package org.umu.cops.ospdp;

import org.umu.cops.prpdp.COPSPdpConnection;
import org.umu.cops.prpdp.COPSPdpReqStateMan;
import org.umu.cops.stack.COPSPepId;
import org.umu.cops.stack.COPSReqMsg;

import javax.annotation.concurrent.ThreadSafe;
import java.net.Socket;

/**
 * Class for managing an outsourcing connection at the PDP side.
 */
@ThreadSafe
public class COPSPdpOSConnection extends COPSPdpConnection {

    /**
     * The PDP OS Data Process object
     */
    private COPSPdpOSDataProcess _thisProcess;

    /**
     * Creates a new PDP connection
     *
     * @param pepId PEP-ID of the connected PEP
     * @param sock  Socket connected to PEP
     * @param process   Object for processing policy data
     */
    public COPSPdpOSConnection(final COPSPepId pepId, final Socket sock, final COPSPdpOSDataProcess process) {
        super(pepId, sock, process);
        this._thisProcess = process;
    }

    @Override
    protected COPSPdpReqStateMan createStateManager(final COPSReqMsg reqMsg) {
        return new COPSPdpOSReqStateMan(reqMsg.getHeader().getClientType(), reqMsg.getClientHandle(), _thisProcess,
                _sock);
    }

}
