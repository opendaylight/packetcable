/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.rcd.impl;

import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.impl.PCMMGateReq;
import org.umu.cops.COPSStateMan;
import org.umu.cops.prpep.COPSPepDataProcess;
import org.umu.cops.prpep.COPSPepReqStateMan;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSError;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data processor for the CMTS emulator.
 */
class CmtsDataProcessor implements COPSPepDataProcess {

    private transient Map<String, String> installDecs = new ConcurrentHashMap<>();
    private transient Map<String, String> errorDecs = new ConcurrentHashMap<>();

    @Override
    public void setDecisions(final COPSPepReqStateMan man, final Map<String, String> removeDecs,
                             final Map<String, String> installDecs, final Map<String, String> errorDecs) {

        // TODO - parameters man & removeDecs not used. They were members when encapsulated in CMTS but were never used.
        this.installDecs = new ConcurrentHashMap<>(installDecs);
        this.errorDecs = new ConcurrentHashMap<>(errorDecs);
    }

    @Override
    public boolean isFailReport(final COPSPepReqStateMan man) {
        return (errorDecs != null && errorDecs.size() > 0);
    }

    @Override
    public Map<String, String> getReportData(final COPSPepReqStateMan man) {
        if (isFailReport(man)) {
            return errorDecs;
        } else {
            final Map<String, String> siDataHashTable = new HashMap<>();
            if (installDecs.size() > 0) {
                String data = "";
                for (String k : installDecs.keySet()) {
                    data = installDecs.get(k);
                    break;
                }
                final ITransactionID transactionID = new PCMMGateReq(new COPSData(data).getData()).getTransactionID();
                final IPCMMGate responseGate = new PCMMGateReq();
                responseGate.setTransactionID(transactionID);

                // TODO FIXME - Why is the key always null??? What value should be used here???
                final String key = null;
                siDataHashTable.put(key, new String(responseGate.getData()));
            }
            return siDataHashTable;
        }
    }

    @Override
    public Map<String, String> getClientData(COPSPepReqStateMan man) {
        // TODO Auto-generated method stub
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getAcctData(COPSPepReqStateMan man) {
        // TODO Auto-generated method stub
        return new HashMap<>();
    }

    @Override
    public void notifyClosedConnection(final COPSStateMan man, final COPSError error) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyNoKAliveReceived(final COPSStateMan man) {
        // TODO Auto-generated method stub
    }

    @Override
    public void closeRequestState(final COPSStateMan man) {
        // TODO Auto-generated method stub
    }

    @Override
    public void newRequestState(final COPSPepReqStateMan man) {
        // TODO Auto-generated method stub
    }

}
