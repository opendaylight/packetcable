/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IAMID;

/**
 *
 */
public class AMID extends PCMMBaseObject implements IAMID {

    /**
     *
     */
    public AMID() {
        this(LENGTH, STYPE, SNUM);
    }

    /**
     * @param data
     */
    public AMID(byte[] data) {
        super(data);
    }

    /**
     * @param len
     * @param sType
     * @param sNum
     */
    public AMID(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IAMID#setApplicationType(short)
     */
    @Override
    public void setApplicationType(short type) {
        setShort(type, (short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IAMID#getApplicationType()
     */
    @Override
    public short getApplicationType() {
        return getShort((short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IAMID#setApplicationMgrTag(short)
     */
    @Override
    public void setApplicationMgrTag(short type) {
        setShort(type, (short) 2);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IAMID#getApplicationMgrTag()
     */
    @Override
    public short getApplicationMgrTag() {
        return getShort((short) 2);
    }

}
