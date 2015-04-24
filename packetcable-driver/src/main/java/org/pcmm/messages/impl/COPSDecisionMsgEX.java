/**
 @header@
 */
package org.pcmm.messages.impl;

import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COPS Decision Message
 *
 *
 */

public class COPSDecisionMsgEX extends COPSMsg {

    /* COPSHeader coming from base class */
    private final COPSHandle _clientHandle;
    private final COPSError _error;
    private final COPSIntegrity _integrity;
//    private COPSContext _decContext;
    private final COPSClientSI clientSI;
    private final Map<COPSContext, List<COPSDecision>> _decisions;

    public COPSDecisionMsgEX(final short clientType, final COPSHandle _clientHandle, final COPSError _error,
                             final COPSIntegrity _integrity, final COPSClientSI clientSI,
                             final Map<COPSContext, List<COPSDecision>> _decisions) {
        super(new COPSHeader(OPCode.DEC, clientType));
        this._clientHandle = _clientHandle;
        this._error = _error;
        this._integrity = _integrity;
        this.clientSI = clientSI;
        this._decisions = new ConcurrentHashMap<>(_decisions);
    }

    @Override
    protected int getDataLength() {
        int out = 0;
        if (_clientHandle != null) out += _clientHandle.getDataLength();
        if (_error != null) out += _error.getDataLength();

        // Display decisions
        // Display any local decisions
        for (final Map.Entry<COPSContext, List<COPSDecision>> entry : _decisions.entrySet()) {
            out += entry.getKey().getDataLength();
            for (final COPSDecision decision : entry.getValue()) {
                out += decision.getDataLength();
            }
        }
        if (clientSI != null) out += clientSI.getDataLength();
        if (_integrity != null) out += _integrity.getDataLength();
        return out;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        if (_clientHandle != null)
            _clientHandle.writeData(socket);
        if (_error != null)
            _error.writeData(socket);

        // Display decisions
        // Display any local decisions
        for (final Map.Entry<COPSContext, List<COPSDecision>> entry : _decisions.entrySet()) {

            final COPSContext context = entry.getKey();
            final List<COPSDecision> decisions = entry.getValue();
            context.writeData(socket);

            for (final COPSDecision decision : decisions) {
                decision.writeData(socket);
            }
        }
        if (clientSI != null)
            clientSI.writeData(socket);
        if (_integrity != null)
            _integrity.writeData(socket);
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

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        if (_clientHandle != null)
            _clientHandle.dump(os);
        if (_error != null)
            _error.dump(os);

        // Display any local decisions
        for (final Map.Entry<COPSContext, List<COPSDecision>> entry : _decisions.entrySet()) {
            final COPSContext context = entry.getKey();
            final List<COPSDecision> v = entry.getValue();
            context.dump(os);

            for (final COPSDecision decision : v) {
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
