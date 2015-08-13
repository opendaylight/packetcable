/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

public interface ITrafficProfile extends IPCMMBaseObject {

    // Authorized
    byte DEFAULT_ENVELOP = 0x7;

    byte getEnvelop();

}
