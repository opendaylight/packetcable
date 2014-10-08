/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

/**
 * COPS Message Parser
 *
 * @version COPSMsgParser.java, v 1.00 2003
 *
 */

// import org.umu.cops.common.COPSDebug;

public class COPSMsgParser {
    ///
    public COPSMsgParser() {

    }

    /** Parses the given COPS data and returns a COPSMsg object
     *     with COPS object filed in.The COPSMsg object is allocated in the
     *     call and it is the responsibility of the caller to free the memory
     *
     * @param    data                a  byte[]
     *
     * @return   a COPSMsg
     *
     * @throws   COPSException
     *
     */
    public COPSMsg parse(byte[] data) throws COPSException {
        COPSHeader hdr = new COPSHeader(data);

        byte[] buf = new byte[data.length - 8];
        System.arraycopy(data,8,buf,0,data.length - 8);

        return (parse(hdr, buf));
    }

    /**
     * Parse the message with given header , the data is pointing
     * to the data following the header
     *
     * @param    hdr                 a  COPSHeader
     * @param    data                a  byte[]
     *
     * @return   a COPSMsg
     *
     * @throws   COPSException
     *
     */
    public COPSMsg parse(COPSHeader hdr, byte[] data) throws COPSException {
        COPSMsg copsMsg = null;
        short cCode = hdr.getOpCode();
        switch (cCode) {
        case COPSHeader.COPS_OP_REQ: {
            // COPSDebug.out(getClass().getName(), "Creating REQ msg");
            copsMsg = new COPSReqMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_DEC: {
            // COPSDebug.out(getClass().getName(), "Creating DEC msg");
            copsMsg = new COPSDecisionMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_RPT: {
            // COPSDebug.out(getClass().getName(), "Creating RPT msg");
            copsMsg = new COPSReportMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_DRQ: {
            // COPSDebug.out(getClass().getName(), "Creating DRQ msg");
            copsMsg = new COPSDeleteMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_OPN: {
            // COPSDebug.out(getClass().getName(), "Creating Client-Open msg");
            copsMsg = new COPSClientOpenMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_CAT: {
            // COPSDebug.out(getClass().getName(), "Creating Client-Accept msg");
            copsMsg = new COPSClientAcceptMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_CC: {
            // COPSDebug.out(getClass().getName(), "Creating Client-Close msg");
            copsMsg = new COPSClientCloseMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_KA: {
            // COPSDebug.out(getClass().getName(), "Creating KA msg");
            copsMsg = new COPSKAMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_SSQ: {
            // COPSDebug.out(getClass().getName(), "Creating Sync-State Request msg");
            copsMsg = new COPSSyncStateMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        case COPSHeader.COPS_OP_SSC: {
            // COPSDebug.out(getClass().getName(), "Creating Sync-State Complete msg");
            copsMsg = new COPSSyncStateMsg();
            copsMsg.parse(hdr, data);
        }
        break;
        default:
            // COPSDebug.out(getClass().getName(), "Unknown message type");
            break;
        }


        // if(copsMsg != null)
        //     try { copsMsg.dump(COPSDebug.out); } catch (Exception e) {};

        return copsMsg;
    }
}


