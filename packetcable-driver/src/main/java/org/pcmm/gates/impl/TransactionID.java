/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITransactionID;

/**
 *
 */
public class TransactionID extends PCMMBaseObject implements ITransactionID {

    /**
     *
     */
    public TransactionID() {
        this(LENGTH, STYPE, SNUM);
    }

    /**
     * @param data
     */
    public TransactionID(byte[] data) {
        super(data);
    }

    /**
     * @param len
     * @param sType
     * @param sNum
     */
    public TransactionID(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ITransactionID#setTransactionIdentifier(short)
     */
    @Override
    public void setTransactionIdentifier(short id) {
        setShort(id, (short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ITransactionID#getTransactionIdentifier()
     */
    @Override
    public short getTransactionIdentifier() {
        return getShort((short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ITransactionID#setGateCommandType(short)
     */
    @Override
    public void setGateCommandType(short type) {
        setShort(type, (short) 2);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ITransactionID#getGateCommandType()
     */
    @Override
    public short getGateCommandType() {
        return getShort((short) 2);
    }

}
