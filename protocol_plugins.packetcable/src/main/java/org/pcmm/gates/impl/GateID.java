/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IGateID;

/**
 *
 */
public class GateID extends PCMMBaseObject implements IGateID {

    /**
     *
     */
    public GateID() {
        this(LENGTH, STYPE, SNUM);
    }

    /**
     * @param data
     */
    public GateID(byte[] data) {
        super(data);
    }

    /**
     * @param len
     * @param sType
     * @param sNum
     */
    public GateID(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IGateID#setGateID(int)
     */
    @Override
    public void setGateID(int gateID) {
        setInt(gateID, (short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IGateID#getGateID()
     */
    @Override
    public int getGateID() {
        return getInt((short) 0);
    }

}
