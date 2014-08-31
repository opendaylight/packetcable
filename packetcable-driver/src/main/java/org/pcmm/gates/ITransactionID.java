/**
 @header@
 */
package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 */
public interface ITransactionID extends IPCMMBaseObject {

    static final byte SNUM = 1;
    static final byte STYPE = 1;
    static final short LENGTH = 8;

    static final short GateSet = 4;
    static final short GateSetAck = 5;
    static final short GateSetErr = 6;
    static final short GateInfo = 7;
    static final short GateInfoAck = 8;
    static final short GateInfoErr = 9;
    static final short GateDelete = 10;
    static final short GateDeleteAck = 11;
    static final short GateDeleteErr = 12;
    static final short GateReportState = 15;
    static final short GateCmdErr = 16;
    static final short PDPConfig = 17;
    static final short PDPConfigAck = 18;
    static final short PDPConfigErr = 19;
    static final short SynchRequest = 20;
    static final short SynchReport = 21;
    static final short SynchComplete = 22;
    static final short MsgReceipt = 23;

    void setTransactionIdentifier(short id);

    short getTransactionIdentifier();

    void setGateCommandType(short type);

    short getGateCommandType();

}
