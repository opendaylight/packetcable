/*
 * Copyright (c) 2014 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.objects;

/**
 * this class holds and maps flow ID to PCMM gate ID and Transaction ID
 *
 */
public class PCMMIDHolder extends PCMMResource {

    /**
     * flow id.
     */
    private int flowID;
    /**
     * gate id.
     */
    private int gateID;
    /**
     * transaction id.
     */
    private short transactionID;

    public PCMMIDHolder(int flowID, int gateID, short transactionID) {
        this.flowID = flowID;
        this.gateID = gateID;
        this.transactionID = transactionID;

    }

    public PCMMIDHolder() {

    }

    public int getFlowID() {
        return flowID;
    }

    public void setFlowID(int flowID) {
        this.flowID = flowID;
    }

    public int getGateID() {
        return gateID;
    }

    public void setGateID(int gateID) {
        this.gateID = gateID;
    }

    public short getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(short transactionID) {
        this.transactionID = transactionID;
    }

}
