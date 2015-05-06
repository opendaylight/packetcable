package org.umu.cops.ospep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpep.COPSPepAgent;
import org.umu.cops.prpep.COPSPepConnection;
import org.umu.cops.prpep.COPSPepReqStateMan;
import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSPepId;

import java.net.Socket;
import java.util.List;

/**
 * This is a outsourcing COPS PEP. Responsible for making
 * connection to the PDP and maintaining it
 */
public class COPSPepOSAgent extends COPSPepAgent {

    public final static Logger logger = LoggerFactory.getLogger(COPSPepOSAgent.class);

    /**
     * Policy data processor class
     */
    private transient COPSPepOSDataProcess _process;

    /**
     * Creates a PEP agent
     * @param    clientType         Client-type
     * @param    pepID              PEP-ID
     * @param    port               The port to begin listening
     */
    public COPSPepOSAgent(final short clientType, final COPSPepId pepID, final int port,
                          final COPSPepOSDataProcess process) {
        super(clientType, pepID, port);
        this._process = process;
    }

    /**
     * Adds a request state to the connection manager.
     * @param clientSIs The client data from the outsourcing event
     * @return  The newly created connection manager
     * @throws COPSPepException
     * @throws COPSException
     */
    public COPSPepReqStateMan addRequestState(final COPSHandle handle, final List<COPSClientSI> clientSIs)
            throws COPSException {
        if (_conn != null)
            return ((COPSPepOSConnection)_conn).addRequestState(handle, _process, clientSIs);

        return null;
    }

    @Override
    protected COPSPepConnection createPepConnection(final Socket socket) {
        return new COPSPepOSConnection(_clientType, socket);
    }

    /**
     * Creates a new request state when the outsourcing event is detected.
     * @param handle The COPS handle for this request
     * @param clientSIs The client specific data for this request
     */
    public void dispatchEvent(final COPSHandle handle, final List<COPSClientSI> clientSIs) {
        try {
            addRequestState(handle, clientSIs);
        } catch (Exception e) {
            logger.error("Error adding request state", e);
        }
    }
}
