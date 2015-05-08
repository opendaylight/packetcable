package org.umu.cops;

import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSSyncStateMsg;

import java.net.Socket;

/**
 * Abstract COPS state manager.
 */
public abstract class COPSStateMan {

    // TODO - place these values into an enumeration
    /**
     * COPS client-type that identifies the policy client
     */
    protected final short _clientType;

    /**
     *  COPS client handle used to uniquely identify a particular
     *  PEP's request for a client-type
     */
    protected final COPSHandle _handle;

    /**
     *  Current state of the request being managed
     */
    protected transient Status _status;

    /**
     * Constructor
     * @param clientType - the client type
     * @param clientHandle - the unique handle to the client
     */
    public COPSStateMan(final short clientType, final COPSHandle clientHandle) {
        this._clientType = clientType;
        this._handle = clientHandle;
        this._status = Status.ST_CREATE;
    }

    /**
     * Gets the client handle
     * @return   Client's <tt>COPSHandle</tt>
     */
    public COPSHandle getClientHandle() {
        return _handle;
    }

    /**
     * Gets the client-type
     * @return   Client-type value
     */
    public short getClientType() {
        return _clientType;
    }

    /**
     * Gets the status of the request
     * @return      Request state value
     */
    public Status getStatus() {
        return _status;
    }

    /**
     * Called when COPS sync is completed
     * @param    repMsg              COPS sync message
     * @throws COPSPdpException
     */
    public void processSyncComplete(final COPSSyncStateMsg repMsg) throws COPSException {
        _status = Status.ST_SYNCALL;
        // TODO - maybe we should notifySyncComplete ...
    }

    /**
     * Initializes a new request state over a socket
     * @param sock  Socket to the PEP
     * @throws COPSPdpException
     */
    protected abstract void initRequestState(final Socket sock) throws COPSException;

    /**
     * The different state manager statuses
     */
    public enum Status {
        NA,
        ST_CREATE, // Request State created
        ST_INIT, // Request received
        ST_DECS, // Decisions sent
        ST_REPORT, // Report received
        ST_FINAL, // Request state finalized
        ST_NEW, // New request state solicited
        ST_DEL, // Delete request state solicited
        ST_SYNC, // SYNC request sent
        ST_SYNCALL, // SYNC completed
        ST_CCONN, // Close connection received
        ST_NOKA, // Keep-alive timeout
        ST_ACCT, // Accounting timeout
    }

}
