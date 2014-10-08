/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */
package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 *
 */
public interface IGateID extends IPCMMBaseObject {
    static final short LENGTH = 8;
    static final byte SNUM = 4;
    static final byte STYPE = 1;

    void setGateID(int gateID);

    int getGateID();
}
