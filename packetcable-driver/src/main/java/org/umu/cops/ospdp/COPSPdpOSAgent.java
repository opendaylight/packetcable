package org.umu.cops.ospdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpAgent;

/**
 * Core PDP agent for outsourcing.
 * TODO - Implement me
 */
public class COPSPdpOSAgent extends COPSPdpAgent {

    public final static Logger logger = LoggerFactory.getLogger(COPSPdpOSAgent.class);

    /**
     *  Policy data processing object
     */
    private COPSPdpOSDataProcess _thisProcess;

    /**
     * Creates a PDP Agent
     *
     * @param port  Port to listen to
     * @param clientType    COPS Client-type
     * @param process   Object to perform policy data processing
     */
    public COPSPdpOSAgent(final String host, final int port, final short clientType,
                          final COPSPdpOSDataProcess process) {
        super(host, port, clientType, process);
        this._thisProcess = process;
    }

}
