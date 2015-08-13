/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITransactionID;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Implementation of the ITransactionID interface
 */
public class TransactionID extends PCMMBaseObject implements ITransactionID {

    /**
     * The transaction identifier
     */
    private final short transId;

    /**
     * The gate command type
     */
    private final GateCommandType gateCommandType;

    /**
     * Constructor
     * @param transId - the transaction identifier
     * @param gateCommandType - the gate command type
     */
    public TransactionID(final short transId, final GateCommandType gateCommandType) {
        super(SNum.TRANSACTION_ID, STYPE);
        if (gateCommandType == null)
            throw new IllegalArgumentException("Invalid gate command type");
        this.transId = transId;
        this.gateCommandType = gateCommandType;
    }

    @Override
    public short getTransactionIdentifier() {
        return transId;
    }

    @Override
    public GateCommandType getGateCommandType() {
        return gateCommandType;
    }

    @Override
    protected byte[] getBytes() {
        final byte[] transIdBytes = COPSMsgParser.shortToBytes(transId);
        final byte[] data = new byte[transIdBytes.length + transIdBytes.length];
        System.arraycopy(transIdBytes, 0, data, 0, transIdBytes.length);

        final byte[] gateCmdBytes = COPSMsgParser.shortToBytes(gateCommandType.getValue());
        System.arraycopy(gateCmdBytes, 0, data, transIdBytes.length, gateCmdBytes.length);
        return data;
    }

    /**
     * Returns a TransactionID object from a byte array
     * @param data - the data to parse
     * @return - the object
     * TODO - make me more robust as RuntimeExceptions can be thrown here.
     */
    public static TransactionID parse(final byte[] data) {
        return new TransactionID(COPSMsgParser.bytesToShort(data[0], data[1]),
                GateCommandType.valueOf(COPSMsgParser.bytesToShort(data[2], data[3])));
    }
}
