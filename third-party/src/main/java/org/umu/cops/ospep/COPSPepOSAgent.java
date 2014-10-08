package org.umu.cops.ospep;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;

import org.umu.cops.stack.COPSAcctTimer;
import org.umu.cops.stack.COPSClientAcceptMsg;
import org.umu.cops.stack.COPSClientCloseMsg;
import org.umu.cops.stack.COPSClientOpenMsg;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSKATimer;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSPepId;
import org.umu.cops.stack.COPSTransceiver;

/**
 * This is a outsourcing COPS PEP. Responsible for making
 * connection to the PDP and maintaining it
 */
public class COPSPepOSAgent {
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
    public COPSPepOSAgent(String pepID, short clientType) {
        _pepID = pepID;
        _clientType = clientType;
    }

    /**
     * Creates a PEP agent with a PEP-ID equal to "noname"
     * @param    clientType         Client-type
     */
    public COPSPepOSAgent(short clientType) {
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
    public short getClientType() {
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
    public boolean connect(String psHost, int psPort) throws UnknownHostException, IOException, COPSException, COPSPepException {
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
    public void disconnect(COPSError error) throws COPSException, IOException {
        COPSHeader cHdr = new COPSHeader(COPSHeader.COPS_OP_CC, _clientType);
        COPSClientCloseMsg closeMsg = new COPSClientCloseMsg();
        closeMsg.add(cHdr);
        if (error != null)
            closeMsg.add(error);

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
    public COPSPepOSReqStateMan addRequestState(COPSHandle handle, Vector clientSIs) throws COPSPepException, COPSException {
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
     * @throws   UnknownHostException
     * @throws   IOException
     * @throws   COPSException
     * @throws   COPSPepException
     *
     */
    private COPSPepOSConnection processConnection(String psHost, int psPort) throws UnknownHostException, IOException, COPSException, COPSPepException {
        // Build OPN
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_OPN, _clientType);

        COPSPepId pepId = new COPSPepId();
        COPSData d = new COPSData(_pepID);
        pepId.setData(d);

        COPSClientOpenMsg msg = new COPSClientOpenMsg();
        msg.add(hdr);
        msg.add(pepId);

        // Create socket and send OPN
        InetAddress addr = InetAddress.getByName(psHost);
        Socket socket = new Socket(addr,psPort);
        msg.writeData(socket);

        // Get response
        COPSMsg recvmsg = COPSTransceiver.receiveMsg(socket);

        if (recvmsg.getHeader().isAClientAccept()) {
            COPSClientAcceptMsg cMsg = (COPSClientAcceptMsg) recvmsg;

            // Support
            if (cMsg.getIntegrity() != null) {
                throw new COPSPepException("Unsupported object (Integrity)");
            }

            // Mandatory KATimer
            COPSKATimer kt = cMsg.getKATimer();
            if (kt == null)
                throw new COPSPepException ("Mandatory COPS object missing (KA Timer)");
            short _kaTimeVal = kt.getTimerVal();

            // ACTimer
            COPSAcctTimer at = cMsg.getAcctTimer();
            short _acctTimer = 0;
            if (at != null)
                _acctTimer = at.getTimerVal();

            // Create connection manager
            COPSPepOSConnection conn = new COPSPepOSConnection(_clientType, socket);
            conn.setKaTimer(_kaTimeVal);
            conn.setAcctTimer(_acctTimer);
            new Thread(conn).start();

            return conn;
        } else if (recvmsg.getHeader().isAClientClose()) {
            COPSClientCloseMsg cMsg = (COPSClientCloseMsg) recvmsg;
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
    public void dispatchEvent(COPSHandle handle, Vector clientSIs) {
        try {
            addRequestState(handle, clientSIs);
        } catch (Exception e) {
            System.err.println("COPSPepOSAgent: " + e.toString());
        }
    }
}
