package org.umu.cops.ospep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;

/**
 * This is a outsourcing COPS PEP. Responsible for making
 * connection to the PDP and maintaining it
 */
public class COPSPepOSAgent {

    public final static Logger logger = LoggerFactory.getLogger(COPSPepOSAgent.class);

    /**
        PEP's identifier
     */
    private String _pepID;

    /**
        PEP's client-type
     */
    private short _clientType;

    /**
        PDP host name
     */
    private String _psHost;

    /**
        PDP port
     */
    private int _psPort;

    /**
        PEP-PDP connection manager
     */
    private COPSPepOSConnection _conn;

    /**
        COPS error returned by the PDP
     */
    private COPSError _error;

    /**
     * Policy data processor class
     */
    private COPSPepOSDataProcess _process;

    /**
     * Creates a PEP agent
     * @param    pepID              PEP-ID
     * @param    clientType         Client-type
     */
    public COPSPepOSAgent(final String pepID, final short clientType) {
        _pepID = pepID;
        _clientType = clientType;
    }

    /**
     * Creates a PEP agent with a PEP-ID equal to "noname"
     * @param    clientType         Client-type
     */
    public COPSPepOSAgent(final short clientType) {
        // PEPId
        try {
            _pepID = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            _pepID = "noname";
        }

        _clientType = clientType;
    }

    /**
     * Gets the identifier of the PEP
     * @return  PEP-ID
     */
    public String getPepID() {
        return _pepID;
    }

    /**
     * Sets the policy data processor
     * @param aDataProcess  Data processor class
     */
    public void setDataProcess(COPSPepOSDataProcess aDataProcess) {
        this._process = aDataProcess;
    }

    /**
     * Gets the COPS client-type
     * @return  PEP's client-type
     */
    public int getClientType() {
        return _clientType;
    }

    /**
     * Gets PDP host name
     * @return  PDP host name
     */
    public String getPDPName() {
        return _psHost;
    }

    /**
     * Gets the port of the PDP
     * @return  PDP port
     */
    public int getPDPPort() {
        return _psPort;
    }

    /**
     * Connects to a PDP
     * @param    psHost              PDP host name
     * @param    psPort              PDP port
     * @return   <tt>true</tt> if PDP accepts the connection; <tt>false</tt> otherwise
     * @throws   java.net.UnknownHostException
     * @throws   java.io.IOException
     * @throws   COPSException
     * @throws   COPSPepException
     */
    public boolean connect(String psHost, int psPort) throws IOException, COPSException, COPSPepException {
        // COPSDebug.out(getClass().getName(), "Thread ( " + _pepID + ") - Connecting to PDP");
        _psHost = psHost;
        _psPort = psPort;

        // Check whether it already exists
        if (_conn == null)
            _conn = processConnection(psHost,psPort);
        else {
            // Check whether it's closed
            if (_conn.isClosed())
                _conn = processConnection(psHost,psPort);
            else {
                disconnect(null);
                _conn = processConnection(psHost,psPort);
            }
        }

        return (_conn != null);
    }

    /**
     * Gets the connection manager
     * @return  PEP-PDP connection manager object
     */
    public COPSPepOSConnection getConnection() {
        return (_conn);
    }

    /**
     * Gets the COPS error returned by the PDP
     * @return   <tt>COPSError</tt> returned by PDP
     */
    public COPSError getConnectionError() {
        return _error;
    }

    /**
     * Disconnects from the PDP
     * @param error Reason
     * @throws COPSException
     * @throws IOException
     */
    public void disconnect(final COPSError error) throws COPSException, IOException {
        final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg(_clientType, error, null, null);
        closeMsg.writeData(_conn.getSocket());
        _conn.close();
        _conn = null;
    }

    /**
     * Adds a request state to the connection manager.
     * @param clientSIs The client data from the outsourcing event
     * @return  The newly created connection manager
     * @throws COPSPepException
     * @throws COPSException
     */
    public COPSPepOSReqStateMan addRequestState(final COPSHandle handle, List<COPSClientSI> clientSIs) throws COPSPepException, COPSException {
        if (_conn != null)
            return _conn.addRequestState(handle.getId().str(), _process, clientSIs);

        return null;
    }

    /**
     * Queries the connection manager to delete a request state
     * @param man   Request state manager
     * @throws COPSPepException
     * @throws COPSException
     */
    public void deleteRequestState (COPSPepOSReqStateMan man) throws COPSPepException, COPSException {
        if (_conn != null)
            _conn.deleteRequestState(man);
    }

    /**
     * Gets all the request state managers
     * @return  A <tt>Hashtable</tt> holding all active request state managers
     */
    public Hashtable getReqStateMans() {
        if (_conn != null)
            return _conn.getReqStateMans();
        return null;
    }

    /**
     * Establish connection to PDP's IP address
     *
     * <Client-Open> ::= <Common Header>
     *                  <PEPID>
     *                  [<ClientSI>]
     *                  [<LastPDPAddr>]
     *                  [<Integrity>]
     *
     * Not support [<ClientSI>], [<LastPDPAddr>], [<Integrity>]
     *
     * <Client-Accept> ::= <Common Header>
     *                      <KA Timer>
     *                      [<ACCT Timer>]
     *                      [<Integrity>]
     *
     * Not send [<Integrity>]
     *
     * <Client-Close> ::= <Common Header>
     *                      <Error>
     *                      [<PDPRedirAddr>]
     *                      [<Integrity>]
     *
     * Not send [<PDPRedirAddr>], [<Integrity>]
     *
     * @throws   IOException
     * @throws   COPSException
     * @throws   COPSPepException
     *
     */
    private COPSPepOSConnection processConnection(final String psHost, final int psPort)
            throws IOException, COPSException, COPSPepException {
        // Build OPN
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(_clientType, new COPSPepId(new COPSData(_pepID)),
                null, null, null);

        // Create socket and send OPN
        final InetAddress addr = InetAddress.getByName(psHost);
        final Socket socket = new Socket(addr,psPort);
        msg.writeData(socket);

        // Get response
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
            short _acctTimer;
            if (at != null) _acctTimer = at.getTimerVal();
            else _acctTimer = 0;

            // Create connection manager
            final COPSPepOSConnection conn = new COPSPepOSConnection(_clientType, socket);
            conn.setKaTimer(_kaTimeVal);
            conn.setAcctTimer(_acctTimer);
            new Thread(conn).start();

            return conn;
        } else if (recvmsg.getHeader().getOpCode().equals(OPCode.CC)) {
            final COPSClientCloseMsg cMsg = (COPSClientCloseMsg) recvmsg;
            _error = cMsg.getError();
            socket.close();
            return null;
        } else { // other message types are unexpected
            throw new COPSPepException("Message not expected. Closing connection for " + socket.toString());
        }
    }

    /**
     * Creates a new request state when the outsourcing event is detected.
     * @param handle The COPS handle for this request
     * @param clientSIs The client specific data for this request
     */
    public void dispatchEvent(COPSHandle handle, final List<COPSClientSI> clientSIs) {
        try {
            addRequestState(handle, clientSIs);
        } catch (Exception e) {
            logger.error("Error adding request state", e);
        }
    }
}
