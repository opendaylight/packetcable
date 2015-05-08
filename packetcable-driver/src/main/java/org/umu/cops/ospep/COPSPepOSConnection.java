package org.umu.cops.ospep;

import org.umu.cops.prpep.COPSPepConnection;
import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;

import java.net.Socket;
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

    @Override
    protected void handleDecisionMsg(final COPSDecisionMsg dMsg) throws COPSException {
        final COPSPepOSReqStateMan manager = (COPSPepOSReqStateMan)_managerMap.get(dMsg.getClientHandle());
        manager.processDecision(dMsg);
    }

    /**
     * Adds a new request state
     * @param handle        Client's handle
     * @param process       Policy data processing object
     * @param clientSIs     Client data from the outsourcing event
     * @return              The newly created request state manager
     * @throws COPSException
     */
    protected COPSPepOSReqStateMan addRequestState(final COPSHandle handle, final COPSPepOSDataProcess process,
                                                   final List<COPSClientSI> clientSIs) throws COPSException {
        final COPSPepOSReqStateMan manager = new COPSPepOSReqStateMan(_clientType, handle, process, clientSIs);
        if (_managerMap.get(handle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + handle.getId().str());
        _managerMap.put(handle, manager);
        manager.initRequestState(_sock);
        return manager;
    }

}
