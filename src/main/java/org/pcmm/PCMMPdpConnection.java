/*
 @header@
 */

package org.pcmm;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.umu.cops.common.COPSDebug;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.COPSClientCloseMsg;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSDeleteMsg;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSKAMsg;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSPepId;
import org.umu.cops.stack.COPSReportMsg;
import org.umu.cops.stack.COPSReqMsg;
import org.umu.cops.stack.COPSSyncStateMsg;
import org.umu.cops.stack.COPSTransceiver;

/**
 * Class for managing an provisioning connection at the PDP side.
 */
public class PCMMPdpConnection implements Runnable {

    /**
    Socket connected to PEP
     */
    private Socket _sock;

    /**
    PEP identifier
    */
    private COPSPepId _pepId;

    /**
    Time of the latest keep-alive sent
     */
    private Date _lastKa;

    /**
   Opcode of the latest message sent
    */
    private byte _lastmessage;

    /**
     *  Time of the latest keep-alive received
     */
    protected Date _lastRecKa;

    /**
   Maps a Client Handle to a Handler
     */
    protected Hashtable _managerMap;
    // map < String(COPSHandle), COPSPdpHandler> HandlerMap;

    /**
     *  PDP policy data processor class
     */
    protected PCMMPdpDataProcess _process;

    /**
   Accounting timer value (secs)
     */
    protected short _acctTimer;

    /**
   Keep-alive timer value (secs)
     */
    protected short _kaTimer;

    /**
   COPS error returned by PEP
     */
    protected COPSError _error;

    /**
     * Creates a new PDP connection
     *
     * @param pepId PEP-ID of the connected PEP
     * @param sock Socket connected to PEP
     * @param process Object for processing policy data
     */
    public PCMMPdpConnection(COPSPepId pepId, Socket sock, PCMMPdpDataProcess process) {
        _sock = sock;
        _pepId = pepId;

        _lastKa = new Date();
        _lastmessage = COPSHeader.COPS_OP_OPN;
        _managerMap = new Hashtable(20);

        _kaTimer = 120;
        _process = process;
    }

    /**
     * Gets the time of that latest keep-alive sent
     * @return Time of that latest keep-alive sent
     */
    public Date getLastKAlive() {
        return _lastKa;
    }

    /**
     * Sets the keep-alive timer value
     * @param kaTimer Keep-alive timer value (secs)
     */
    public void setKaTimer(short kaTimer) {
        _kaTimer = kaTimer;
    }

    /**
     * Gets the keep-alive timer value
     * @return Keep-alive timer value (secs)
     */
    public short getKaTimer() {
        return _kaTimer;
    }

    /**
     * Sets the accounting timer value
     * @param acctTimer Accounting timer value (secs)
     */
    public void setAccTimer(short acctTimer) {
        _acctTimer = acctTimer;
    }

    /**
     * Gets the accounting timer value
     * @return Accounting timer value (secs)
     */
    public short getAcctTimer() {
        return _acctTimer;
    }

    /**
     * Gets the latest COPS message
     * @return   Code of the latest message sent
     */
    public byte getLastMessage() {
        return _lastmessage;
    }

    /**
     * Gets active handles
     * @return   An <tt>Enumeration</tt> holding all active handles
     */
    public Enumeration getHandles() {
        return _managerMap.keys();
    }

    /**
     * Gets the handle map
     * @return   A <tt>Hashtable</tt> holding the handle map
     */
    public Hashtable getReqStateMans() {
        return _managerMap;
    }

    /**
     * Gets the PEP-ID
     * @return   The ID of the PEP, as a <tt>String</tt>
     */
    public String getPepId() {
        return _pepId.getData().str();
    }

    /**
     * Checks whether the socket to the PEP is closed or not
     * @return   <tt>true</tt> if closed, <tt>false</tt> otherwise
     */
    public boolean isClosed() {
        return _sock.isClosed();
    }

    /**
     * Closes the socket to the PEP
     * @throws IOException
     */
    protected void close()
    throws IOException {
        _sock.close();
    }

    /**
     * Gets the socket to the PEP
     * @return   Socket connected to the PEP
     */
    public Socket getSocket() {
        return _sock;
    }

    /**
     * Main loop
     */
    public void run () {
        Date _lastSendKa = new Date();
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

                    if ((int)(cTime - _startTime) > ((_kaTimer*3/4)*1000)) {
                        COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_KA);
                        COPSKAMsg msg = new COPSKAMsg();

                        msg.add(hdr);

                        COPSTransceiver.sendMsg(msg, _sock);
                        _lastSendKa = new Date();
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
        // COPSDebug.out(getClass().getName(),"Connection closed by client");
        try {
            _sock.close();
        } catch (IOException e) {};

        // Notify all Request State Managers
        try {
            notifyCloseAllReqStateMan();
        } catch (COPSPdpException e) {};
    }

    /**
     * Gets a COPS message from the socket and processes it
     * @param    conn Socket connected to the PEP
     * @return Type of COPS message
     */
    private byte processMessage(Socket conn)
    throws COPSPdpException, COPSException, IOException {
        COPSMsg msg = COPSTransceiver.receiveMsg(conn);

        if (msg.getHeader().isAClientClose()) {
            handleClientCloseMsg(conn, msg);
            return COPSHeader.COPS_OP_CC;
        } else if (msg.getHeader().isAKeepAlive()) {
            handleKeepAliveMsg(conn, msg);
            return COPSHeader.COPS_OP_KA;
        } else if (msg.getHeader().isARequest()) {
            handleRequestMsg(conn, msg);
            return COPSHeader.COPS_OP_REQ;
        } else if (msg.getHeader().isAReport()) {
            handleReportMsg(conn, msg);
            return COPSHeader.COPS_OP_RPT;
        } else if (msg.getHeader().isADeleteReq()) {
            handleDeleteRequestMsg(conn, msg);
            return COPSHeader.COPS_OP_DRQ;
        } else if (msg.getHeader().isASyncComplete()) {
            handleSyncComplete(conn, msg);
            return COPSHeader.COPS_OP_SSC;
        } else {
            throw new COPSPdpException("Message not expected (" + msg.getHeader().getOpCode() + ").");
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
     *  <Error>
     *  [<Integrity>]
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
     * Gets the occurred COPS Error
     * @return   <tt>COPSError</tt> object
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

        COPSKAMsg kaMsg = (COPSKAMsg) msg;
        try {
            // Support
            if (cMsg.getIntegrity() != null) {
                COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                              "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
            }

            kaMsg.writeData(conn);
        } catch (Exception unae) { };
    }

    /**
     * Handle Delete Request Message
     *
     * <Delete Request> ::= <Common Header>
     *                      <Client Handle>
     *                      <Reason>
     *                      [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleDeleteRequestMsg(Socket conn, COPSMsg msg)
    throws COPSPdpException {
        COPSDeleteMsg cMsg = (COPSDeleteMsg) msg;
        // COPSDebug.out(getClass().getName(),"Removing ClientHandle for " +
        //  conn.getInetAddress() + ":" + conn.getPort() + ":[Reason " + cMsg.getReason().getDescription() + "]");

        // Support
        if (cMsg.getIntegrity() != null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                          "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        // Delete clientHandler
        if (_managerMap.remove(cMsg.getClientHandle().getId().str()) == null) {
            // COPSDebug.out(getClass().getName(),"Missing for ClientHandle " +
            //  cMsg.getClientHandle().getId().getData());
        }

        PCMMPdpReqStateMan man = (PCMMPdpReqStateMan) _managerMap.get(cMsg.getClientHandle().getId().str());
        if (man == null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOEXPECTEDMSG);
        } else {
            man.processDeleteRequestState(cMsg);
        }

    }

    /**
     * Handle Request Message
     *
     * <Request> ::= <Common Header>
     *  <Client Handle>
     *  <Context>
     *  *(<Named ClientSI>)
     *  [<Integrity>]
     * <Named ClientSI> ::= <*(<PRID> <EPD>)>
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleRequestMsg(Socket conn, COPSMsg msg)
    throws COPSPdpException {

        COPSReqMsg reqMsg = (COPSReqMsg) msg;
        COPSContext cntxt = reqMsg.getContext();
        COPSHeader header = reqMsg.getHeader();
        //short reqType = cntxt.getRequestType();
        short cType   = header.getClientType();

        // Support
        if (reqMsg.getIntegrity() != null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                          "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        PCMMPdpReqStateMan man;
        man = (PCMMPdpReqStateMan) _managerMap.get(reqMsg.getClientHandle().getId().str());
        if (man == null) {

            man = new PCMMPdpReqStateMan(cType, reqMsg.getClientHandle().getId().str());
            _managerMap.put(reqMsg.getClientHandle().getId().str(),man);
            man.setDataProcess(_process);
            man.initRequestState(_sock);

            // COPSDebug.out(getClass().getName(),"createHandler called, clientType=" +
            //    header.getClientType() + " msgType=" +
            //    cntxt.getMessageType() + ", connId=" + conn.toString());
        }

        man.processRequest(reqMsg);
    }

    /**
     * Handle Report Message
     *
     * <Report State> ::= <Common Header>
     *  <Client Handle>
     *  <Report Type>
     *  *(<Named ClientSI>)
     *  [<Integrity>]
     *
     * Not support [<Integrity>]
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleReportMsg(Socket conn, COPSMsg msg)
    throws COPSPdpException {
        COPSReportMsg repMsg = (COPSReportMsg) msg;
        // COPSHandle handle = repMsg.getClientHandle();
        // COPSHeader header = repMsg.getHeader();

        // Support
        if (repMsg.getIntegrity() != null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                          "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        PCMMPdpReqStateMan man = (PCMMPdpReqStateMan) _managerMap.get(repMsg.getClientHandle().getId().str());
        if (man == null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOEXPECTEDMSG);
        } else {
            man.processReport(repMsg);
        }
    }

    /**
     * Method handleSyncComplete
     *
     * @param    conn                a  Socket
     * @param    msg                 a  COPSMsg
     *
     */
    private void handleSyncComplete(Socket conn, COPSMsg msg)
    throws COPSPdpException {
        COPSSyncStateMsg cMsg = (COPSSyncStateMsg) msg;
        // COPSHandle handle = cMsg.getClientHandle();
        // COPSHeader header = cMsg.getHeader();

        // Support
        if (cMsg.getIntegrity() != null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOSUPPORTED,
                          "Unsupported objects (Integrity) to connection " + conn.getInetAddress());
        }

        PCMMPdpReqStateMan man = (PCMMPdpReqStateMan) _managerMap.get(cMsg.getClientHandle().getId().str());
        if (man == null) {
            COPSDebug.err(getClass().getName(), COPSDebug.ERROR_NOEXPECTEDMSG);
        } else {
            man.processSyncComplete(cMsg);
        }
    }

    /**
     * Requests a COPS sync from the PEP
     * @throws COPSException
     * @throws COPSPdpException
     */
    protected void syncAllRequestState()
    throws COPSException, COPSPdpException {
        if (_managerMap.size() > 0) {
            for (Enumeration e = _managerMap.keys() ; e.hasMoreElements() ;) {
                String handle = (String) e.nextElement();
                PCMMPdpReqStateMan man = (PCMMPdpReqStateMan) _managerMap.get(handle);

                man.syncRequestState();
            }
        }
    }

    private void notifyCloseAllReqStateMan()
    throws COPSPdpException {
        if (_managerMap.size() > 0) {
            for (Enumeration e = _managerMap.keys() ; e.hasMoreElements() ;) {
                String handle = (String) e.nextElement();
                PCMMPdpReqStateMan man = (PCMMPdpReqStateMan) _managerMap.get(handle);

                man.processClosedConnection(_error);
            }
        }
    }

    private void notifyNoKAAllReqStateMan()
    throws COPSPdpException {
        if (_managerMap.size() > 0) {
            for (Enumeration e = _managerMap.keys() ; e.hasMoreElements() ;) {
                String handle = (String) e.nextElement();
                PCMMPdpReqStateMan man = (PCMMPdpReqStateMan) _managerMap.get(handle);

                man.processNoKAConnection();
            }
        }
    }

}
