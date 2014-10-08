/*
 * Copyright (c) 2004 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.prpep;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.umu.cops.common.COPSDebug;
import org.umu.cops.stack.COPSClientCloseMsg;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSKAMsg;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSSyncStateMsg;
import org.umu.cops.stack.COPSTransceiver;

/**
 * COPSPepConnection represents a PEP-PDP Connection Manager.
 * Responsible for processing messages received from PDP.
 */
public class COPSPepConnection implements Runnable {

    /** Socket connected to PDP */
    protected Socket _sock;

    /** Time to wait responses (milliseconds), default is 10 seconds */
    protected int _responseTime;

    /** COPS Client-type */
    protected short _clientType;

    /**
        Accounting timer value (secs)
     */
    protected short _acctTimer;

    /**
        Keep-alive timer value (secs)
     */
    protected short _kaTimer;

    /**
     *  Time of the latest keep-alive received
     */
    protected Date _lastRecKa;

    /**
        Opcode of the latest message sent
    */
    protected byte _lastmessage;

    /**
        Maps a COPS Client Handle to a Request State Manager
     */
    protected Hashtable _managerMap;
    // map < String(COPSHandle), COPSPepReqStateMan>;

    /**
        COPS error returned by PDP
     */
    protected COPSError _error;

    /**
     * Creates a new PEP connection
     * @param clientType    PEP's client-type
     * @param sock          Socket connected to PDP
     */
    public COPSPepConnection(short clientType, Socket sock) {

        _clientType = clientType;
        _sock = sock;

        // Timers
        _acctTimer = 0;
        _kaTimer = 0;
        _responseTime = 10000;
        _lastmessage = COPSHeader.COPS_OP_CAT;

        _managerMap = new Hashtable(20);
    }

    /**
     * Gets the response time
     * @return  Response time value (msecs)
     */
    public int getResponseTime() {
        return _responseTime;
    }

    /**
     * Gets the socket connected to the PDP
     * @return  Socket connected to PDP
     */
    public Socket getSocket() {
        return _sock;
    }

    /**
     * Gets keep-alive timer
     * @return  Keep-alive timer value (secs)
     */
    public short getKaTimer () {
        return _kaTimer;
    }

    /**
     * Gets accounting timer
     * @return  Accounting timer value (secs)
     */
    public short getAcctTimer () {
        return _acctTimer;
    }

    /**
     * Gets active COPS handles
     * @return  An <tt>Enumeration</tt> holding all active handles
     */
    protected Enumeration getHandles() {
        return _managerMap.keys();
    }

    /**
     * Gets all request state managers
     * @return  A <tt>Hashatable</tt> holding all request state managers
     */
    protected Hashtable getReqStateMans() {
        return _managerMap;
    }

    /**
     * Checks whether the socket to the PDP is closed or not
     * @return  <tt>true</tt> if the socket is closed, <tt>false</tt> otherwise
     */
    public boolean isClosed() {
        return _sock.isClosed();
    }

    /**
     * Closes the socket
     *
     * @throws java.io.IOException
     */
    protected void close()
    throws IOException {
        _sock.close();
    }

    /**
     * Gets the opcode of the lastest message sent
     * @return  Message opcode
     */
    public byte getLastmessage() {
        return _lastmessage;
    }

    /**
     * Sets response time
     * @param respTime  Response time value (msecs)
     */
    public void setResponseTime(int respTime) {
        _responseTime = respTime;
    };

    /**
     * Sets keep-alive timer
     * @param kaTimer   Keep-alive timer value (secs)
     */
    public void setKaTimer (short kaTimer) {
        _kaTimer = kaTimer;
    }

    /**
     * Sets accounting timer
     * @param acctTimer Accounting timer value (secs)
     */
    public void setAcctTimer (short acctTimer) {
        _acctTimer = acctTimer;
    }

    /**
     * Message-processing loop
     */
    public void run () {
        Date _lastSendKa = new Date();
        Date _lastSendAcc = new Date();
        _lastRecKa = new Date();
        try {
            while (!_sock.isClosed()) {
                if (_sock.getInputStream().available() != 0) {
                    _lastmessage = processMessage(_sock);
                    _lastRecKa = new Date();
                }

                // Keep Alive
                if (_kaTimer > 0) {
                    // Timeout at PDP
                    int _startTime = (int) (_lastRecKa.getTime());
                    int cTime = (int) (new Date().getTime());

                    if ((int)(cTime - _startTime) > _kaTimer*1000) {
                        _sock.close();
                        // Notify all Request State Managers
                        notifyNoKAAllReqStateMan();
                    }

                    // Send to PEP
                    _startTime = (int) (_lastSendKa.getTime());
                    cTime = (int) (new Date().getTime());

                    if ((int)(cTime - _startTime) > ((_kaTimer*3/4) * 1000)) {
                        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_KA);
                        COPSKAMsg msg = new COPSKAMsg();

                        msg.add(hdr);

                        COPSTransceiver.sendMsg(msg, _sock);
                        _lastSendKa = new Date();
                    }
                }

                // Accounting
                if (_acctTimer > 0) {
                    int _startTime = (int) (_lastSendAcc.getTime());
                    int cTime = (int) (new Date().getTime());

                    if ((int)(cTime - _startTime) > ((_acctTimer*3/4)*1000)) {
                        // Notify all Request State Managers
                        notifyAcctAllReqStateMan();
                        _lastSendAcc = new Date();
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {};
            }
        } catch (Exception e) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_SOCKET, e);
        }

        // connection closed by server
        // COPSDebug.out(getClass().getName(),"Connection closed by server");
        try {
            _sock.close();
        } catch (IOException e) {};

        // Notify all Request State Managers
        try {
            notifyCloseAllReqStateMan();
        } catch (COPSPepException e) {};
    }

    /**
     * Gets a COPS message from the socket and processes it
     * @param conn  Socket connected to the PDP
     * @return COPS message type
     * @throws COPSPepException
     * @throws COPSException
     * @throws IOException
     */
    protected byte processMessage(Socket conn)
    throws COPSPepException, COPSException, IOException {
        COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        if (msg.getHeader().isAClientClose()) {
            handleClientCloseMsg(conn, msg);
            return COPSHeader.COPS_OP_CC;
        } else if (msg.getHeader().isADecision()) {
            handleDecisionMsg(conn, msg);
            return COPSHeader.COPS_OP_DEC;
        } else if (msg.getHeader().isASyncStateReq()) {
            handleSyncStateReqMsg(conn, msg);
            return COPSHeader.COPS_OP_SSQ;
        } else if (msg.getHeader().isAKeepAlive()) {
            handleKeepAliveMsg(conn, msg);
            return COPSHeader.COPS_OP_KA;
        } else {
            throw new COPSPepException("Message not expected (" + msg.getHeader().getOpCode() + ").");
        }
    }

    /**
     * Handle Client Close Message, close the passed connection
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     *
     * <Client-Close> ::= <Common Header>
     *                      <Error>
     *                      [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     */
    private void handleClientCloseMsg(Socket conn, COPSMsg msg) {
        COPSClientCloseMsg cMsg = (COPSClientCloseMsg) msg;
        _error = cMsg.getError();

        // COPSDebug.out(getClass().getName(),"Got close request, closing connection " +
        //  conn.getInetAddress() + ":" + conn.getPort() + ":[Error " + _error.getDescription() + "]");

        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                              "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }

            conn.close();
        } catch (Exception unae) { };
    }

    /**
     * Method getError
     *
     * @return   a COPSError
     *
     */
    protected COPSError getError()  {
        return _error;
    }

    /**
     * Handle Keep Alive Message
     *
     * <Keep-Alive> ::= <Common Header>
     *                  [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleKeepAliveMsg(Socket conn, COPSMsg msg) {
        COPSKAMsg cMsg = (COPSKAMsg) msg;

        // COPSDebug.out(getClass().getName(),"Get KAlive Msg");

        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                              "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }

            // should we do anything else?? ....

        } catch (Exception unae) { };
    }

    /**
     * Method handleDecisionMsg
     *
     * <Decision Message> ::= <Common Header: Flag SOLICITED>
     *                          <Client Handle>
     *                          *(<Decision>) | <Error>
     *                          [<Integrity>]
     * <Decision> ::= <Context>
     *                  <Decision: Flags>
     *                  [<Named Decision Data: Provisioning>]
     * <Decision: Flags> ::= <Command-Code> NULLFlag
     * <Command-Code> ::= NULLDecision | Install | Remove
     * <Named Decision Data> ::= <<Install Decision> | <Remove Decision>>
     * <Install Decision> ::= *(<PRID> <EPD>)
     * <Remove Decision> ::= *(<PRID> | <PPRID>)
     *
     * Very important, this is actually being treated like this:
     * <Install Decision> ::= <PRID> | <EPD>
     * <Remove Decision> ::= <PRID> | <PPRID>
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleDecisionMsg(Socket conn, COPSMsg msg)
    throws COPSPepException {
        COPSDecisionMsg dMsg = (COPSDecisionMsg) msg;
        COPSHandle handle = dMsg.getClientHandle();
        Hashtable decisions = dMsg.getDecisions();

        for (Enumeration e = decisions.keys() ; e.hasMoreElements() ;) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) decisions.get(context);

            Enumeration ee = v.elements();
            if (ee.hasMoreElements()) {
                COPSDecision decision = (COPSDecision) ee.nextElement();

                // Get the associated manager
                COPSPepReqStateMan manager = (COPSPepReqStateMan) _managerMap.get(handle.getId().str());
                if (manager == null)
                    COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOEXPECTEDMSG);

                // Check message type
                if (decision.getFlags() == COPSDecision.F_REQSTATE) {
                    if (decision.isRemoveDecision())
                        // Delete Request State
                        manager.processDeleteRequestState(dMsg);
                    else
                        // Open new Request State
                        handleOpenNewRequestStateMsg(conn, handle);
                } else
                    // Decision
                    manager.processDecision(dMsg);
            }
        }
    }


    /**
     * Method handleOpenNewRequestStateMsg
     *
     * @param    conn                a  Socket
     * @param    handle              a  COPSHandle
     *
     */
    private void handleOpenNewRequestStateMsg(Socket conn, COPSHandle handle)
    throws COPSPepException {

        COPSPepReqStateMan manager = (COPSPepReqStateMan) _managerMap.get(handle.getId().str());
        if (manager == null)
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOEXPECTEDMSG);

        manager.processOpenNewRequestState();
    }

    /**
     * Method handleSyncStateReqMsg
     *
     *              <Synchronize State> ::= <Common Header>
     *                                      [<Client Handle>]
     *                                      [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleSyncStateReqMsg(Socket conn, COPSMsg msg)
    throws COPSPepException {
        COPSSyncStateMsg cMsg = (COPSSyncStateMsg) msg;
        // COPSHandle handle = cMsg.getClientHandle();
        // COPSHeader header = cMsg.getHeader();

        // Support
        if (cMsg.getIntegrity() != null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                          "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        COPSPepReqStateMan manager = (COPSPepReqStateMan) _managerMap.get(cMsg.getClientHandle().getId().str());
        if (manager == null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOEXPECTEDMSG);
        } else {
            manager.processSyncStateRequest(cMsg);
        }
    }

    /**
     * Method createRequestState
     *
     * @param    clientHandle             a  String
     * @param    process                  a  COPSPepDataProcess
     *
     * @return   a COPSPepmanager
     *
     * @throws   COPSException
     * @throws   COPSPepException
     *
     */
    protected COPSPepReqStateMan addRequestState(String clientHandle, COPSPepDataProcess process)
    throws COPSException, COPSPepException {
        COPSPepReqStateMan manager = new COPSPepReqStateMan(_clientType,clientHandle);
        if (_managerMap.get(clientHandle) != null)
            throw new COPSPepException("Duplicate Handle, rejecting " + clientHandle);

        manager.setDataProcess(process);
        _managerMap.put(clientHandle,manager);
        manager.initRequestState(_sock);
        return manager;
    }

    /**
     * Method deleteRequestState
     *
     * @param    manager             a  COPSPepReqStateMan
     *
     * @throws   COPSException
     * @throws   COPSPepException
     *
     */
    protected void deleteRequestState(COPSPepReqStateMan manager)
    throws COPSException, COPSPepException {
        manager.finalizeRequestState();
    }

    private void notifyCloseAllReqStateMan()
    throws COPSPepException {
        if (_managerMap.size() > 0) {
            for (Enumeration e = _managerMap.keys() ; e.hasMoreElements() ;) {
                String handle = (String) e.nextElement();
                COPSPepReqStateMan man = (COPSPepReqStateMan) _managerMap.get(handle);

                man.processClosedConnection(_error);
            }
        }
    }

    private void notifyNoKAAllReqStateMan()
    throws COPSPepException {
        if (_managerMap.size() > 0) {
            for (Enumeration e = _managerMap.keys() ; e.hasMoreElements() ;) {
                String handle = (String) e.nextElement();
                COPSPepReqStateMan man = (COPSPepReqStateMan) _managerMap.get(handle);

                man.processNoKAConnection();
            }
        }
    }

    private void notifyAcctAllReqStateMan()
    throws COPSPepException {
        if (_managerMap.size() > 0) {
            for (Enumeration e = _managerMap.keys() ; e.hasMoreElements() ;) {
                String handle = (String) e.nextElement();
                COPSPepReqStateMan man = (COPSPepReqStateMan) _managerMap.get(handle);

                man.processAcctReport();
            }
        }
    }

}

