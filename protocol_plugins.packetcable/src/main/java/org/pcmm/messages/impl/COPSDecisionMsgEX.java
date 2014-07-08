/**
 @header@
 */
package org.pcmm.messages.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSIntegrity;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSObjHeader;

/**
 * COPS Decision Message
 *
 *
 */

public class COPSDecisionMsgEX extends COPSMsg {

    /* COPSHeader coming from base class */
    private COPSHandle _clientHandle;
    private COPSError _error;
    private Hashtable _decisions;
    private COPSIntegrity _integrity;
    private COPSContext _decContext;
    private COPSClientSI clientSI;

    // /
    public COPSDecisionMsgEX() {
        _clientHandle = null;
        _error = null;
        _decisions = new Hashtable(20);
        _integrity = null;
        _decContext = null;
        clientSI = null;
    }

    /**
     * Checks the sanity of COPS message and throw an COPSBadDataException when
     * data is bad.
     */
    public void checkSanity() throws COPSException {
        if ((_hdr == null) || (_clientHandle == null)
                || ((_error == null) && (_decisions.size() == 0))) {
            throw new COPSException("Bad message format");
        }
    }

    // /
    protected COPSDecisionMsgEX(byte[] data) throws COPSException {
        _decisions = new Hashtable(20);
        _clientHandle = null;
        _error = null;
        _integrity = null;
        _decContext = null;
        clientSI = null;
        parse(data);
    }

    /**
     * Parses the data and fills COPSDecisionMsg with its constituents
     *
     * @param data
     *            a byte[]
     *
     * @throws COPSException
     *
     */
    protected void parse(byte[] data) throws COPSException {
        super.parseHeader(data);

        while (_dataStart < _dataLength) {
            byte[] buf = new byte[data.length - _dataStart];
            System.arraycopy(data, _dataStart, buf, 0, data.length - _dataStart);

            COPSObjHeader objHdr = new COPSObjHeader(buf) {
            };
            switch (objHdr.getCNum()) {
            case COPSObjHeader.COPS_HANDLE: {
                _clientHandle = new COPSHandle(buf) {
                };
                _dataStart += _clientHandle.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_CONTEXT: {
                // dec context
                _decContext = new COPSContext(buf) {
                };
                _dataStart += _decContext.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_ERROR: {
                _error = new COPSError(buf) {
                };
                _dataStart += _error.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_DEC: {
                COPSDecision decs = new COPSDecision(buf) {
                };
                _dataStart += decs.getDataLength();
                addDecision(decs, _decContext);
            }
            break;
            case COPSObjHeader.COPS_MSG_INTEGRITY: {
                _integrity = new COPSIntegrity(buf);
                _dataStart += _integrity.getDataLength();
            }
            break;
            case COPSObjHeader.COPS_CSI: {
                clientSI = new COPSClientSI(buf) {
                };
                _dataStart += clientSI.getDataLength();
            }
            break;
            default: {
                throw new COPSException(
                    "Bad Message format, unknown object type");
            }
            }
        }
        checkSanity();
    }

    /**
     * Parses the data and fills that follows the header hdr and fills
     * COPSDecisionMsg
     *
     * @param hdr
     *            a COPSHeader
     * @param data
     *            a byte[]
     *
     * @throws COPSException
     *
     */
    protected void parse(COPSHeader hdr, byte[] data) throws COPSException {
        _hdr = hdr;
        parse(data);
        setMsgLength();
    }

    /**
     * Add message header
     *
     * @param hdr
     *            a COPSHeader
     *
     * @throws COPSException
     *
     */
    public void add(COPSHeader hdr) throws COPSException {
        if (hdr == null)
            throw new COPSException("Null Header");
        if (hdr.getOpCode() != COPSHeader.COPS_OP_DEC)
            throw new COPSException("Error Header (no COPS_OP_DEC)");
        _hdr = hdr;
        setMsgLength();
    }

    /**
     * Add client handle to the message
     *
     * @param handle
     *            a COPSHandle
     *
     * @throws COPSException
     *
     */
    public void add(COPSHandle handle) throws COPSException {
        if (handle == null)
            throw new COPSException("Null Handle");
        _clientHandle = handle;
        setMsgLength();
    }

    /**
     * Add an Error object
     *
     * @param error
     *            a COPSError
     *
     * @throws COPSException
     *
     */
    public void add(COPSError error) throws COPSException {
        if (_decisions.size() != 0)
            throw new COPSException("No null decisions");
        if (_error != null)
            throw new COPSException("No null error");
        // Message integrity object should be the very last one
        // If it is already added
        if (_integrity != null)
            throw new COPSException("No null integrity");
        _error = error;
        setMsgLength();
    }

    /**
     * Add one or more local decision object for a given decision context the
     * context is optional, if null all decision object are tided to message
     * context
     *
     * @param decision
     *            a COPSDecision
     * @param context
     *            a COPSContext
     *
     * @throws COPSException
     *
     */
    public void addDecision(COPSDecision decision, COPSContext context)
    throws COPSException {
        // Either error or decision can be added
        // If error is aleady there assert
        if (_error != null)
            throw new COPSException("No null error");

        if (decision.isLocalDecision())
            throw new COPSException("Is local decision");

        Vector v = (Vector) _decisions.get(context);
        if (v == null)
            v = new Vector();

        if (decision.isFlagSet()) {// Commented out as advised by Felix
            // if (v.size() != 0)
            // {
            // Only one set of decision flags is allowed
            // for each context
            // throw new COPSException
            // ("Bad Message format, only one set of decision flags is allowed.");
            // }
        } else {
            if (v.size() == 0) {
                // The flags decision must precede any other
                // decision message, since the decision is not
                // flags throw exception
                throw new COPSException(
                    "Bad Message format, flags decision must precede any other decision object.");
            }
        }
        v.add(decision);
        _decisions.put(context, v);

        setMsgLength();
    }

    /**
     * Add integrity object
     *
     * @param integrity
     *            a COPSIntegrity
     *
     * @throws COPSException
     *
     */
    public void add(COPSIntegrity integrity) throws COPSException {
        if (integrity == null)
            throw new COPSException("Null Integrity");
        if (!integrity.isMessageIntegrity())
            throw new COPSException("Error Integrity");
        _integrity = integrity;
        setMsgLength();
    }

    /**
     * Add a client specific informations
     *
     * @param clientSI
     *            a COPSClientSI
     *
     * @throws COPSException
     *
     */
    public void add(COPSClientSI clientSI) throws COPSException {
        if (clientSI == null)
            throw new COPSException("Null ClientSI");
        this.clientSI = clientSI;
        setMsgLength();
    }

    /**
     * Writes data to given socket
     *
     * @param id
     *            a Socket
     *
     * @throws IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        // checkSanity();
        if (_hdr != null)
            _hdr.writeData(id);
        if (_clientHandle != null)
            _clientHandle.writeData(id);
        if (_error != null)
            _error.writeData(id);

        // Display decisions
        // Display any local decisions
        for (Enumeration e = _decisions.keys(); e.hasMoreElements();) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) _decisions.get(context);
            context.writeData(id);

            for (Enumeration ee = v.elements(); ee.hasMoreElements();) {
                COPSDecision decision = (COPSDecision) ee.nextElement();
                decision.writeData(id);
            }
        }
        if (clientSI != null)
            clientSI.writeData(id);
        if (_integrity != null)
            _integrity.writeData(id);
    }

    /**
     * Method getHeader
     *
     * @return a COPSHeader
     *
     */
    public COPSHeader getHeader() {
        return _hdr;
    }

    /**
     * Method getClientHandle
     *
     * @return a COPSHandle
     *
     */
    public COPSHandle getClientHandle() {
        return _clientHandle;
    }

    public COPSClientSI getClientSI() {
        return clientSI;
    }

    /**
     * Returns true if it has error object
     *
     * @return a boolean
     *
     */
    public boolean hasError() {
        return (_error != null);
    };

    /**
     * Should check hasError() before calling
     *
     * @return a COPSError
     *
     */
    public COPSError getError() {
        return _error;
    };

    /**
     * Returns a map of decision for which is an arry of context and vector of
     * associated decision object.
     *
     * @return a Hashtable
     *
     */
    public Hashtable getDecisions() {
        return _decisions;
    };

    /**
     * Returns true if it has integrity object
     *
     * @return a boolean
     *
     */
    public boolean hasIntegrity() {
        return (_integrity != null);
    };

    /**
     * Should check hasIntegrity() before calling
     *
     * @return a COPSIntegrity
     *
     */
    public COPSIntegrity getIntegrity() {
        return _integrity;
    };

    /**
     * Method setMsgLength
     *
     * @throws COPSException
     *
     */
    protected void setMsgLength() throws COPSException {
        short len = 0;
        if (_clientHandle != null)
            len += _clientHandle.getDataLength();
        if (_error != null)
            len += _error.getDataLength();

        // Display any local decisions
        for (Enumeration e = _decisions.keys(); e.hasMoreElements();) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) _decisions.get(context);
            len += context.getDataLength();

            for (Enumeration ee = v.elements(); ee.hasMoreElements();) {
                COPSDecision decision = (COPSDecision) ee.nextElement();
                len += decision.getDataLength();
            }
        }
        if (clientSI != null)
            len += clientSI.getDataLength();
        if (_integrity != null) {
            len += _integrity.getDataLength();
        }

        _hdr.setMsgLength((int) len);
    }

    /**
     * Write an object textual description in the output stream
     *
     * @param os
     *            an OutputStream
     *
     * @throws IOException
     *
     */
    public void dump(OutputStream os) throws IOException {
        _hdr.dump(os);

        if (_clientHandle != null)
            _clientHandle.dump(os);
        if (_error != null)
            _error.dump(os);

        // Display any local decisions
        for (Enumeration e = _decisions.keys(); e.hasMoreElements();) {

            COPSContext context = (COPSContext) e.nextElement();
            Vector v = (Vector) _decisions.get(context);
            context.dump(os);

            for (Enumeration ee = v.elements(); ee.hasMoreElements();) {
                COPSDecision decision = (COPSDecision) ee.nextElement();
                decision.dump(os);
            }
        }
        if (clientSI != null)
            clientSI.dump(os);
        if (_integrity != null) {
            _integrity.dump(os);
        }
    }
}
