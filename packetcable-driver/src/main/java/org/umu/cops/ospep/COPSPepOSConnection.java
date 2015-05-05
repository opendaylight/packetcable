package org.umu.cops.ospep;

import org.umu.cops.prpep.COPSPepConnection;
import org.umu.cops.stack.*;

import java.net.Socket;
import java.util.Hashtable;
import java.util.List;

/**
 * COPSPepConnection represents a PEP-PDP Connection Manager.
 * Responsible for processing messages received from PDP.
 */
public class COPSPepOSConnection extends COPSPepConnection {

    /**
     * Creates a new PEP connection
     * @param clientType    PEP's client-type
     * @param sock          Socket connected to PDP
     */
    public COPSPepOSConnection(final short clientType, final Socket sock) {
        super(clientType, sock);
    }

    /**
     * Gets all request state managers
     * @return  A <tt>Hashatable</tt> holding all request state managers
     * TODO - change the return to Map
     */
    protected Hashtable getReqStateMans() {
        return new Hashtable(_managerMap);
    }

    @Override
    protected void handleDecisionMsg(final COPSDecisionMsg dMsg) throws COPSException {
        final COPSPepOSReqStateMan manager = (COPSPepOSReqStateMan)_managerMap.get(dMsg.getClientHandle());
        manager.processDecision(dMsg);
    }

    /**
     * Adds a new request state
     * @param clientHandle  Client's handle
     * @param process       Policy data processing object
     * @param clientSIs     Client data from the outsourcing event
     * @return              The newly created request state manager
     * @throws COPSException
     */
    protected COPSPepOSReqStateMan addRequestState(final String clientHandle, final COPSPepOSDataProcess process,
                                                   final List<COPSClientSI> clientSIs) throws COPSException {
        final COPSHandle handle = new COPSHandle(new COPSData(clientHandle));
        final COPSPepOSReqStateMan manager = new COPSPepOSReqStateMan(_clientType, handle, process, clientSIs);
        if (_managerMap.get(handle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);
        _managerMap.put(handle, manager);
        manager.initRequestState(_sock);
        return manager;
    }

}
