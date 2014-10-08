/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */


package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

public interface ITrafficProfile extends IPCMMBaseObject {

    static final byte SNUM = 7;

    /**
     * 0x001, 0x011 and 0x111 (Authorized, Reserved, and Committed) are allowed
     *
     * @param en
     */
    void setEnvelop(byte en);

    byte getEnvelop();

}
