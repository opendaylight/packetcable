/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This is a provisioning COPS PEP. Responsible for making
 * connection to the PDP and maintaining it
 */
public class COPSPepAgent {

    /**
        PEP's Identifier
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
    private COPSPepConnection _conn;

    /**
        COPS error returned by PDP
     */
    private COPSError _error;

    /**
     * Creates a PEP agent
     * @param    pepID              PEP-ID
     * @param    clientType         Client-type
     */
    public COPSPepAgent(final String pepID, final short clientType) {
        _pepID = pepID;
        _clientType = clientType;
    }

    /**
     * Creates a PEP agent with a PEP-ID equal to "noname"
     * @param    clientType         Client-type
     */
    public COPSPepAgent(final short clientType) {

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
            // Check if it's closed
            if (_conn.isClosed()) {
                _conn = processConnection(psHost,psPort);
            } else {
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
    public COPSPepConnection getConnection () {
        return (_conn);
    }

    /**
     * Gets the COPS error returned by the PDP
     * @return   <tt>COPSError</tt> returned by PDP
     */
    public COPSError getConnectionError()   {
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
    }

    /**
     * Adds a request state to the connection manager.
     * @return  The newly created connection manager
     * @throws COPSPepException
     * @throws COPSException
     */
    public COPSPepReqStateMan addRequestState(final String handle, final COPSPepDataProcess process)
            throws COPSPepException, COPSException {
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
    public void deleteRequestState(final COPSPepReqStateMan man)
    throws COPSPepException, COPSException {
        if (_conn != null)
            _conn.deleteRequestState(man);
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
    private COPSPepConnection processConnection(String psHost, int psPort)
            throws IOException, COPSException, COPSPepException {
        // Build OPN
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(_clientType, new COPSPepId(new COPSData(_pepID)),
                null, null, null);

        // Create Socket and send OPN
        final InetAddress addr = InetAddress.getByName(psHost);
        final Socket socket = new Socket(addr,psPort);
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
            final COPSPepConnection conn = new COPSPepConnection(_clientType, socket);
            conn.setKaTimer(_kaTimeVal);
            conn.setAcctTimer(_acctTimer);
            new Thread(conn).start();

            return conn;
        } else if (recvmsg.getHeader().getOpCode().equals(OPCode.CC)) {
            final COPSClientCloseMsg cMsg = (COPSClientCloseMsg) recvmsg;
            _error = cMsg.getError();
            socket.close();
            return null;
        } else { // messages of other types are not expected
            throw new COPSPepException("Message not expected. Closing connection for " + socket.toString());
        }
    }
}



