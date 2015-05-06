/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This is a provisioning COPS PEP. Responsible for making
 * connection to the PDP and maintaining it
 */
public class COPSPepAgent {

    /** Well-known port for COPS */
    public static final int WELL_KNOWN_CMTS_PORT = 3918;

    private final static Logger logger = LoggerFactory.getLogger(COPSPepAgent.class);

    /**
     PEP's client-type
     */
    protected final short _clientType;

    /**
     * PEP's Identifier
     */
    protected final COPSPepId _pepID;

    /**
     * PDP port
     */
    private final int _psPort;

    // The next two attributes are instantiated after the connect() method has successfully completed.
    /**
     * PEP-PDP connection manager
     */
    protected transient COPSPepConnection _conn;

    /**
     * The thread object to manage the connection thread.
     */
    private transient Thread thread;

    /**
     * Creates a PEP agent
     * @param    clientType         Client-type
     * @param    pepID              PEP-ID
     * @param    port               the server socket port to open on this host
     */
    public COPSPepAgent(final short clientType, final COPSPepId pepID, final int port) {
        _clientType = clientType;
        _pepID = pepID;
        this._psPort = port;
    }

    /**
     * Connects to a PDP and is responsible for setting up the connection
     * @throws   java.io.IOException
     * @throws   COPSException
     * @throws   COPSPepException
     */
    public void connect() throws IOException, COPSException {
        logger.info("Thread ( " + _pepID + ") - Connecting to PDP");

        // Check whether it already exists
        if (_conn == null)
            _conn = processConnection();
        else {
            // Check if it's closed
            if (_conn.isClosed()) {
                _conn = processConnection();
            } else {
                disconnect(new COPSError(ErrorTypes.SHUTTING_DOWN, ErrorTypes.NA));
                _conn = processConnection();
            }
        }

        if (_conn == null) throw new COPSException("Unable to process PEP connection");
    }

    /**
     * Disconnects from the PDP
     * @param error Reason
     * @throws COPSException
     * @throws IOException
     */
    public void disconnect(final COPSError error) throws COPSException, IOException {
        final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(_clientType, error, null, null);
        thread.interrupt();
        thread = null;
        closeMsg.writeData(_conn.getSocket());
        _conn.close();
        _conn = null;
    }

    /**
     * Adds a request state to the connection manager.
     * @return  The newly created connection manager
     * @throws COPSPepException
     * @throws COPSException
     */
    public COPSPepReqStateMan addRequestState(final COPSHandle handle, final COPSPepDataProcess process)
            throws COPSException {
        if (_conn != null) {
            return _conn.addRequestState(handle, process);
        }
        return null;
    }


    /**
     * Queries the connection manager to delete a request state
     * @param man   Request state manager
     * @throws COPSPepException
     * @throws COPSException
     */
    public void deleteRequestState(final COPSPepReqStateMan man) throws COPSException {
        if (_conn != null)
            _conn.deleteRequestState(man);
    }

    /**
     * Establish connection to PDP's IP address
     * @throws   COPSException
     * @throws   COPSPepException
     */
    private COPSPepConnection processConnection() throws IOException, COPSException {
        // Create Socket and send OPN
        final InetAddress addr = InetAddress.getLocalHost();
        return processConnection(new Socket(addr, _psPort));
    }

    /**
     * Establish connection to PDP's IP address
     * @throws   COPSException
     * @throws   COPSPepException
     */
    private COPSPepConnection processConnection(final Socket socket) throws IOException, COPSException {
        // Create Socket and send OPN
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(_clientType, _pepID, null, null, null);
        msg.writeData(socket);

        // Receive the response
        final COPSMsg recvmsg = COPSTransceiver.receiveMsg(socket);

        if (recvmsg.getHeader().getOpCode().equals(OPCode.CAT)) {
            final COPSClientAcceptMsg cMsg = (COPSClientAcceptMsg) recvmsg;

            // Support
            if (cMsg.getIntegrity() != null) {
                throw new COPSPepException("Unsupported object (Integrity)");
            }

            // Mandatory KATimer
            final COPSKATimer kt = cMsg.getKATimer();
            if (kt == null)
                throw new COPSPepException ("Mandatory COPS object missing (KA Timer)");
            short _kaTimeVal = kt.getTimerVal();

            // ACTimer
            final COPSAcctTimer at = cMsg.getAcctTimer();
            short _acctTimer = 0;
            if (at != null)
                _acctTimer = at.getTimerVal();

            // Create the connection manager
            final COPSPepConnection conn = createPepConnection(socket);
            conn.setKaTimer(_kaTimeVal);
            conn.setAcctTimer(_acctTimer);
            thread = new Thread(conn);
            thread.start();

            return conn;
        } else if (recvmsg.getHeader().getOpCode().equals(OPCode.CC)) {
            final COPSClientCloseMsg cMsg = (COPSClientCloseMsg) recvmsg;
            logger.error("Received client-close message with error description [" + cMsg.getError().getDescription()
                    + "]. Closing socket.");
            socket.close();
            return null;
        } else { // messages of other types are not expected
            throw new COPSPepException("Message not expected. Closing connection for " + socket.toString());
        }
    }

    /**
     * Creates a COPSPepConnection object
     * @param socket - the socket on which to create the connection
     * @return - the connection object
     */
    protected COPSPepConnection createPepConnection(final Socket socket) {
        return new COPSPepConnection(_clientType, socket);
    }

}



