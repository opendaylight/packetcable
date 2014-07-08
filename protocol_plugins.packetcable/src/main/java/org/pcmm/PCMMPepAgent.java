/**
 @header@
 */

package org.pcmm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.umu.cops.common.COPSDebug;
import org.umu.cops.prpep.COPSPepAgent;
import org.umu.cops.prpep.COPSPepConnection;
import org.umu.cops.prpep.COPSPepException;
import org.umu.cops.stack.COPSAcctTimer;
import org.umu.cops.stack.COPSClientAcceptMsg;
import org.umu.cops.stack.COPSClientCloseMsg;
import org.umu.cops.stack.COPSClientOpenMsg;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSKATimer;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSPepId;
import org.umu.cops.stack.COPSTransceiver;

/**
 * This is a provisioning COPS PEP. Responsible for making connection to the PDP
 * and maintaining it
 */
public class PCMMPepAgent extends COPSPepAgent implements Runnable {

    /** Well-known port for COPS */
    public static final int WELL_KNOWN_CMTS_PORT = 3918;

    /**
     * PDP host IP
     */
    private ServerSocket serverSocket;

    /**
     * PDP host port
     */
    private int serverPort;

    /**
     * COPS error returned by PDP
     */
    private COPSError error;

    /**
     * Creates a PEP agent
     *
     * @param pepID
     *            PEP-ID
     * @param clientType
     *            Client-type
     */
    public PCMMPepAgent(String pepID, short clientType) {
        super(pepID, clientType);
        serverPort = WELL_KNOWN_CMTS_PORT;
    }

    /**
     * Creates a PEP agent with a PEP-ID equal to "noname"
     *
     * @param clientType
     *            Client-type
     */
    public PCMMPepAgent(short clientType) {
        super(clientType);
        serverPort = WELL_KNOWN_CMTS_PORT;
    }

    /**
     * Runs the PEP process XXX - not sure of the exception throwing
     */
    public void run() {
        try {

            COPSDebug.err(getClass().getName(), "Create Server Socket on Port "
                          + serverPort);

            serverSocket = new ServerSocket(serverPort);
            // Loop through for Incoming messages

            // server infinite loop
            while (true) {

                // Wait for an incoming connection from a PEP
                Socket socket = serverSocket.accept();

                COPSDebug.err(getClass().getName(), "New connection accepted "
                              + socket.getInetAddress() + ":" + socket.getPort());

                processConnection(socket);
                /**
                 * XXX - processConnection handles the open request from PEP And
                 * a thread is created for conn = new
                 * COPSPepConnection(_clientType, socket); the main processing
                 * loop for PEP
                 */

            }
        } catch (IOException e) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_SOCKET, e);
        } catch (COPSException e) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_SOCKET, e);
        } catch (COPSPepException e) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_SOCKET, e);
        }
    }

    /**
     * Establish connection to PDP's IP address
     *
     * <Client-Open> ::= <Common Header> <PEPID> [<ClientSI>] [<LastPDPAddr>]
     * [<Integrity>]
     *
     * Not support [<ClientSI>], [<LastPDPAddr>], [<Integrity>]
     *
     * <Client-Accept> ::= <Common Header> <KA Timer> [<ACCT Timer>]
     * [<Integrity>]
     *
     * Not send [<Integrity>]
     *
     * <Client-Close> ::= <Common Header> <Error> [<PDPRedirAddr>] [<Integrity>]
     *
     * Not send [<PDPRedirAddr>], [<Integrity>]
     *
     * @throws UnknownHostException
     * @throws IOException
     * @throws COPSException
     * @throws COPSPepException
     *
     */
    private COPSPepConnection processConnection(Socket socket)
    throws UnknownHostException, IOException, COPSException,
                COPSPepException {
        // Build OPN
        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_OPN, getClientType());

        COPSPepId pepId = new COPSPepId();
        COPSData d = new COPSData(getPepID());
        pepId.setData(d);

        COPSClientOpenMsg msg = new COPSClientOpenMsg();
        msg.add(hdr);
        msg.add(pepId);

        // Create Socket and send OPN
        /*
         * InetAddress addr = InetAddress.getByName(psHost); Socket socket = new
         * Socket(addr,psPort);
         */
        COPSDebug.err(getClass().getName(), "Send COPSClientOpenMsg to PDP");
        msg.writeData(socket);

        // Receive the response
        COPSDebug.err(getClass().getName(), "Receive the resposne from PDP");
        COPSMsg recvmsg = COPSTransceiver.receiveMsg(socket);

        if (recvmsg.getHeader().isAClientAccept()) {
            COPSDebug.err(getClass().getName(), "isAClientAccept from PDP");
            COPSClientAcceptMsg cMsg = (COPSClientAcceptMsg) recvmsg;

            // Support
            if (cMsg.getIntegrity() != null) {
                throw new COPSPepException("Unsupported object (Integrity)");
            }

            // Mandatory KATimer
            COPSKATimer kt = cMsg.getKATimer();
            if (kt == null)
                throw new COPSPepException(
                    "Mandatory COPS object missing (KA Timer)");
            short _kaTimeVal = kt.getTimerVal();

            // ACTimer
            COPSAcctTimer at = cMsg.getAcctTimer();
            short _acctTimer = 0;
            if (at != null)
                _acctTimer = at.getTimerVal();

            // Create the connection manager
            COPSPepConnection conn = new COPSPepConnection(getClientType(),
                    socket);
            conn.setKaTimer(_kaTimeVal);
            conn.setAcctTimer(_acctTimer);
            COPSDebug.err(getClass().getName(), "Thread(conn).start");
            new Thread(conn).start();

            return conn;
        } else if (recvmsg.getHeader().isAClientClose()) {
            COPSDebug.err(getClass().getName(), "isAClientClose from PDP");
            COPSClientCloseMsg cMsg = (COPSClientCloseMsg) recvmsg;
            error = cMsg.getError();
            socket.close();
            return null;
        } else { // messages of other types are not expected
            throw new COPSPepException(
                "Message not expected. Closing connection for "
                + socket.toString());
        }
    }

    /**
     * Gets the COPS error returned by the PDP
     *
     * @return <tt>COPSError</tt> returned by PDP
     */
    public COPSError getConnectionError() {
        return error;
    }

    public void setConnectionError(COPSError _error) {
        this.error = _error;
    }

    /**
     * @return the serverSocket
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * @param serverSocket
     *            the serverSocket to set
     */
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * @return the serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * @param serverPort
     *            the serverPort to set
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

}
