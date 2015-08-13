/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 * TransactionID is a 2-byte unsigned integer quantity, which contains a token that is used by the Application Manager
 * to match responses from the Policy Server and by the Policy Server to match responses from the CMTS to the
 * previous requests. The TransactionID MUST also contain the command type that identifies the action to be taken or
 * response.
 */
public interface ITransactionID extends IPCMMBaseObject {

    byte STYPE = 1;

    /**
     * Returns the transaction identifier value
     * @return - the ID
     */
    short getTransactionIdentifier();

    /**
     * Returns the command type
     * @return - the command type
     */
    GateCommandType getGateCommandType();

    /**
     * The supported Synchronization types
     */
    enum GateCommandType {

        GATE_SET((short) 4),
        GATE_SET_ACK((short) 5),
        GATE_SET_ERR((short) 6),
        GATE_INFO((short) 7),
        GATE_INFO_ACK((short) 8),
        GATE_INFO_ERR((short) 9),
        GATE_DELETE((short) 10),
        GATE_DELETE_ACK((short) 11),
        GATE_DELETE_ERR((short) 12),
        GATE_RPT_STATE((short) 15),
        GATE_CMD_ERR((short) 16),
        PDP_CONFIG((short) 17),
        PDP_CONFIG_ACK((short) 18),
        PDP_CONFIG_ERR((short) 19),
        SYNC_REQUEST((short) 20),
        SYNC_RPT((short) 21),
        SYNC_COMPLETE((short) 22),
        MSG_RECEIPT((short) 23);

        GateCommandType(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }

        public static GateCommandType valueOf(short v) {
            switch (v) {
                case 4:
                    return GateCommandType.GATE_SET;
                case 5:
                    return GateCommandType.GATE_SET_ACK;
                case 6:
                    return GateCommandType.GATE_SET_ERR;
                case 7:
                    return GateCommandType.GATE_INFO;
                case 8:
                    return GateCommandType.GATE_INFO_ACK;
                case 9:
                    return GateCommandType.GATE_INFO_ERR;
                case 10:
                    return GateCommandType.GATE_DELETE;
                case 11:
                    return GateCommandType.GATE_DELETE_ACK;
                case 12:
                    return GateCommandType.GATE_DELETE_ERR;
                case 15:
                    return GateCommandType.GATE_RPT_STATE;
                case 16:
                    return GateCommandType.GATE_CMD_ERR;
                case 17:
                    return GateCommandType.PDP_CONFIG;
                case 18:
                    return GateCommandType.PDP_CONFIG_ACK;
                case 19:
                    return GateCommandType.PDP_CONFIG_ERR;
                case 20:
                    return GateCommandType.SYNC_REQUEST;
                case 21:
                    return GateCommandType.SYNC_RPT;
                case 22:
                    return GateCommandType.SYNC_COMPLETE;
                case 23:
                    return GateCommandType.MSG_RECEIPT;
                default:
                    throw new IllegalArgumentException("not supported value");
            }
        }

        private short value;

    }

}
